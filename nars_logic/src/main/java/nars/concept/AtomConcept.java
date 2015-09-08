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
import nars.util.data.list.FastConcurrentDirectDeque;

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
    private FastConcurrentDirectDeque<Runnable> pending;

    public AtomConcept(Term atom, Budget budget, final Bag<TermLinkKey, TermLink> termLinks, final Bag<Sentence, TaskLink> taskLinks, PremiseGenerator ps, Memory memory) {
        super(atom, budget, memory);
        this.termLinks = termLinks;
        this.taskLinks = taskLinks;
        this.taskLinkBuilder = new TaskLinkBuilder(memory);
        this.premiseGenerator = ps;
        this.termLinkBuilder = new TermLinkBuilder(this);
    }

    static boolean aboveThreshold(Budget b) {
        return b.summary() >= Global.BUDGET_EPSILON;
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

        if (!taskBudget.summaryGreaterOrEqual(
                memory.param.taskLinkThreshold.floatValue() / linkSubBudgetDivisor
        ))
            return false;


        taskLinkBuilder.setTemplate(null);
        taskLinkBuilder.setTask(task);

        activateTaskLink(taskLinkBuilder);  // tlink type: SELF

        final Budget subBudget = divide(taskBudget, linkSubBudgetDivisor);

        if (!aboveThreshold(subBudget)) {
            return false;
        }

        taskLinkBuilder.setBudget(subBudget);

        for (int i = 0; i < numTemplates; i++) {
            TermLinkTemplate termLink = templates.get(i);

            //if (!(task.isStructural() && (termLink.getType() == TermLink.TRANSFORM))) { // avoid circular transform

            Term componentTerm = termLink.getTarget();
            if (componentTerm.equals(getTerm())) // avoid circular transform
                continue;

            Concept componentConcept = getMemory().conceptualize(componentTerm, subBudget);

            if (componentConcept != null) {

                termLink.setTargetInstance(componentConcept.getTerm());

                taskLinkBuilder.setTemplate(termLink);

                /** activate the task tlink */
                activatePeer(componentConcept, taskLinkBuilder);

            } else {
                //taskBudgetBalance += subBudget.getPriority();
            }

        }

        return true;
    }

    /* called by a concept when it activates another concept's tasklink */
    final void activatePeer(final Concept componentConcept, final TaskLinkBuilder taskLinkBuilder) {
        componentConcept.activateTaskLink(taskLinkBuilder);
    }

    /**
     * Insert a TaskLink into the TaskLink bag
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskLink The termLink to be inserted
     * @return the tasklink which was selected or updated
     */
    public TaskLink activateTaskLink(final TaskLinkBuilder taskLink) {

        TaskLink t = getTaskLinks().update(taskLink);
        return t;
    }

    /**
     * Recursively build TermLinks between a compound and its components
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskBudget   The BudgetValue of the task
     * @param updateTLinks true: causes update of actual termlink bag, false: just queues the activation for future application.  should be true if this concept calls it for itself, not for another concept
     * @return whether any activity happened as a result of this invocation
     */
    public boolean linkTerms(final Budget taskBudget, boolean updateTLinks) {

        final float subPriority;
        int recipients = termLinkBuilder.getNonTransforms();
        if (recipients == 0) {
            //termBudgetBalance += subBudget;
            //subBudget = 0;
            //return false;
        }

        boolean activity = false;
        if ((taskBudget != null) && (recipients > 0)) {
            float dur, qua;
            //TODO make this parameterizable

            //float linkSubBudgetDivisor = (float)Math.sqrt(recipients);

            //half of each subBudget is spent on this concept and the other concept's termlink
            //subBudget = taskBudget.getPriority() * (1f / (2 * recipients));

            //subPriority = taskBudget.getPriority() / (float) Math.sqrt(recipients);
            subPriority = taskBudget.getPriority() / recipients;
            dur = taskBudget.getDurability();
            qua = taskBudget.getQuality();

            final List<TermLinkTemplate> templates = termLinkBuilder.templates();

            int numTemplates = templates.size();
            for (int i = 0; i < numTemplates; i++) {

                final TermLinkTemplate template = templates.get(i);

                //only apply this loop to non-transform termlink templates
                if (template.type != TermLink.TRANSFORM) {

                    template.accumulate(this);

                    if (updateTLinks) {
                        if (template.link(this))
                            activity = true;
                    }
                }

            }
        } else {
            subPriority = 0;
        }


        //TODO merge with above loop, or avoid altogether under certain conditions

        List<TermLinkTemplate> tl = getTermLinkTemplates();
        if (tl != null && updateTLinks) {
            int n = tl.size();
            for (int i = 0; i < n; i++) {

                TermLinkTemplate t = tl.get(i);

                if (t.summaryGreaterOrEqual(memory.param.termLinkThreshold)) {

                    if (t.link(this))
                        activity = true;

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
