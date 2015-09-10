package nars.concept;

import infinispan.com.google.common.collect.Iterators;
import javolution.util.function.Equality;
import nars.Global;
import nars.Memory;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.link.*;
import nars.premise.Premise;
import nars.premise.PremiseGenerator;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Term;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static nars.budget.BudgetFunctions.divide;

/**
 * Created by me on 9/2/15.
 */
public class AtomConcept extends AbstractConcept {

    final AtomicBoolean locked = new AtomicBoolean(false);

    protected final Bag<Sentence, TaskLink> taskLinks;
    protected final Bag<TermLinkKey, TermLink> termLinks;

    /**
     * Link templates of TermLink, only in concepts with CompoundTerm Templates
     * are used to improve the efficiency of TermLink building
     */
    protected final TermLinkBuilder termLinkBuilder;
    protected transient final TaskLinkBuilder taskLinkBuilder;
    protected final PremiseGenerator premiseGenerator;

    public AtomConcept(Term atom, Budget budget, final Bag<TermLinkKey, TermLink> termLinks, final Bag<Sentence, TaskLink> taskLinks, PremiseGenerator ps, Memory memory) {
        super(atom, budget, memory);
        this.termLinks = termLinks;
        this.taskLinks = taskLinks;
        this.taskLinkBuilder = new TaskLinkBuilder(memory);
        this.premiseGenerator = ps;
        this.termLinkBuilder = new TermLinkBuilder(this);
    }

    static boolean aboveThreshold(Budget b) {
        return b.getPriority() >= Global.BUDGET_EPSILON;
    }

    @Override
    public PremiseGenerator getPremiseGenerator() {
        return premiseGenerator;
    }

    /**
     * Task links for indirect processing
     */
    public Bag<Sentence, TaskLink> getTaskLinks() {
        return taskLinks;
    }

    /**
     * Term links between the term and its components and compounds; beliefs
     */
    public Bag<TermLinkKey, TermLink> getTermLinks() {
        return termLinks;
    }

    /**
     * called by concept before it fires to update any pending changes
     */
    public void updateLinks() {


        if (Global.TERMLINK_FORGETTING_EXTRA_DEPTH > 0)
            getTermLinks().forgetNext(
                    getMemory().param.termLinkForgetDurations,
                    Global.TERMLINK_FORGETTING_EXTRA_DEPTH,
                    getMemory());



        if (Global.TASKLINK_FORGETTING_EXTRA_DEPTH > 0)
            getTaskLinks().forgetNext(
                    getMemory().param.taskLinkForgetDurations,
                    Global.TASKLINK_FORGETTING_EXTRA_DEPTH,
                    getMemory());


        linkTerms(null, true);

    }

    public boolean link(Task t) {
        if (linkTask(t))
            return linkTerms(t.getBudget(), true);  // recursively insert TermLink
        return false;
    }

    /**
     * Link to a new task from all relevant concepts for continued processing in
     * the near future for unspecified time.
     * <p>
     * The only method that calls the TaskLink constructor.
     *
     * @param task The task to be linked
     */
    protected boolean linkTask(final Task task) {

        final List<TermLinkTemplate> templates = termLinkBuilder.templates();
        final int numTemplates = termLinkBuilder.getNonTransforms();
        if (templates == null || numTemplates == 0) {
            //distribute budget to incoming termlinks?
            return false;
        }

        //TODO parameter to use linear division, conserving total budget
        //float linkSubBudgetDivisor = (float)Math.sqrt(termLinkTemplates.size());
        //float linkSubBudgetDivisor = (float) Math.sqrt(numTemplates);
        float linkSubBudgetDivisor = numTemplates;

        final Budget taskBudget = task.getBudget();

        float subPri = taskBudget.getPriority()/ linkSubBudgetDivisor;
        if ((subPri < Global.BUDGET_EPSILON) ||
                (subPri < memory.param.taskLinkThreshold.floatValue() ))
            return false;

        taskLinkBuilder.setTemplate(null);
        taskLinkBuilder.setTask(task);


        final Budget subBudget = divide(taskBudget, linkSubBudgetDivisor);

        taskLinkBuilder.setBudget(subBudget);

        //give self transform task subBudget (previously it got the entire budget)
        this.activateTaskLink(taskLinkBuilder);


        for (int i = 0; i < numTemplates; i++) {
            TermLinkTemplate linkTemplate = templates.get(i);

            //if (!(task.isStructural() && (linkTemplate.getType() == TermLink.TRANSFORM))) { // avoid circular transform

            final Term componentTerm = linkTemplate.getTarget();
            if (componentTerm.equals(getTerm())) // avoid circular transform
                continue;

            Concept componentConcept = getMemory().conceptualize(linkTemplate, subBudget);

            if (componentConcept != null) {

                //share merge term instances
                linkTemplate.setTargetInstance(componentConcept.getTerm());

                taskLinkBuilder.setTemplate(linkTemplate);

                /** activate the task tlink */
                ((AtomConcept)componentConcept).activateTaskLink(taskLinkBuilder);

            } else {
                //taskBudgetBalance += subBudget.getPriority();
            }

        }

        return true;
    }

    /**
     * Insert a TaskLink into the TaskLink bag
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskLink The termLink to be inserted
     * @return the tasklink which was selected or updated
     */
    protected final TaskLink activateTaskLink(final TaskLinkBuilder taskLink) {
        return getTaskLinks().update(taskLink);
    }

    /**
     * Recursively build TermLinks between a compound and its components
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param b   The BudgetValue of the task
     * @param updateTLinks true: causes update of actual termlink bag, false: just queues the activation for future application.  should be true if this concept calls it for itself, not for another concept
     * @return whether any activity happened as a result of this invocation
     */
    public boolean linkTerms(final Budget b, boolean updateTLinks) {

        //activate the concept with the taskbudget


        int recipients = termLinkBuilder.getNonTransforms();
        if (recipients == 0) {
            //termBudgetBalance += subBudget;
            //subBudget = 0;
            //return false;
        }

        List<TermLinkTemplate> tl = getTermLinkTemplates();

        //accumulate incoming task budget to the tasklinks
        boolean activity = false;
        float subPriority;
        if (tl!=null && (b != null) && (recipients > 0)) {

            float dur, qua;
            //TODO make this parameterizable

            //float linkSubBudgetDivisor = (float)Math.sqrt(recipients);

            //half of each subBudget is spent on this concept and the other concept's termlink
            //subBudget = b.getPriority() * (1f / (2 * recipients));

            //subPriority = b.getPriority() / (float) Math.sqrt(recipients);
            subPriority = b.getPriority() / recipients;
            dur = b.getDurability();
            qua = b.getQuality();

            if (subPriority >= Global.BUDGET_EPSILON) {

                int numTemplates = tl.size();
                final float termLinkThresh = memory.param.termLinkThreshold.floatValue();

                for (int i = 0; i < numTemplates; i++) {

                    final TermLinkTemplate t = tl.get(i);
                    if (t.type == TermLink.TRANSFORM)
                        continue;

                    //only apply this loop to non-transform termlink templates
                    t.accumulate(subPriority, dur, qua);

                    if (updateTLinks) {
                        if (t.getPriority() >= termLinkThresh) {
                            if (t.link(this))
                                activity = true;
                        }
                    }

                }
            }
        }


        return activity;
    }


    @Override
    public TermLinkBuilder getTermLinkBuilder() {
        return termLinkBuilder;
    }


    static final BeliefTable emptyTable = new BeliefTable() {

        @Override
        final public Iterator<Task> iterator() {
            return Iterators.emptyIterator();
        }

        @Override
        public void setCapacity(int newCapacity) {

        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void clear() {

        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Task add(Task q, Equality<Task> e, Concept c) {
            return null;
        }

        @Override
        public Task add(Task input, Ranker r, Concept c, Premise nal) {
            return null;
        }

        @Override
        public Ranker getRank() {
            return null;
        }

        @Override
        public Task top(boolean eternal, boolean temporal) {
            return null;
        }
    };

    @Override
    public BeliefTable getBeliefs() {
        return emptyTable;
    }

    @Override
    public BeliefTable getGoals() {
        return emptyTable;
    }

    @Override
    public TaskTable getQuestions() {
        return emptyTable;
    }

    @Override
    public TaskTable getQuests() {
        return emptyTable;
    }

    @Override
    public boolean processBelief(Premise nal, Task task) {
        return false;
    }

    @Override
    public boolean processGoal(Premise nal, Task task) {
        return false;
    }

    @Override
    public Task processQuestion(Premise nal, Task task) {
        return null;
    }

    /**
     * Insert a new or activate an existing TermLink in the TermLink bag
     * via a caching TermLinkSelector which has been configured for the
     * target Concept and the current budget
     * <p>
     * called from buildTermLinks only
     * <p>
     * If the tlink already exists, the budgets will be merged
     *
     * @param termLink The termLink to be inserted
     * @return the termlink which was selected or updated
     */
    public TermLink activateTermLink(final TermLinkBuilder termLink) {
        return getTermLinks().update(termLink);
    }

    public List<TermLinkTemplate> getTermLinkTemplates() {
        return termLinkBuilder.templates();
    }

    /**
     * called when concept is activated; empty and subclassable
     */
    protected void onActive() {

    }
}
