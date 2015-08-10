package nars.concept;

import javolution.util.function.Equality;
import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.link.*;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.premise.Premise;
import nars.premise.PremiseGenerator;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Compound;
import nars.term.Term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static nars.budget.BudgetFunctions.divide;
import static nars.nal.nal1.LocalRules.trySolution;


public class DefaultConcept extends AbstractConcept {


    long deletionTime;

    private final TaskTable questions;
    private final TaskTable quests;
    private final BeliefTable beliefs;
    private final BeliefTable goals;

    private final Bag<Sentence, TaskLink> taskLinks;
    private final Bag<TermLinkKey, TermLink> termLinks;

    /**
     * Link templates of TermLink, only in concepts with CompoundTerm Templates
     * are used to improve the efficiency of TermLink building
     */
    private final TermLinkBuilder termLinkBuilder;
    transient private final TaskLinkBuilder taskLinkBuilder;


    final static public Equality<Task> taskEquivalence = new Equality<Task>() {

        @Override
        public boolean areEqual(Task a, Task b) {
            return (a.equals(b));
        }

        //N/A
        @Override public int compare(Task task, Task t1) {  return 0;        }
        @Override public int hashCodeOf(Task task) { return task.hashCode(); }
    };


    final static public Equality<Task> questionEquivalence = new Equality<Task>() {

        @Override
        public boolean areEqual(Task a, Task b) {
            return (a.equalParents(b));
        }

        //N/A
        @Override public int compare(Task task, Task t1) {  return 0;        }
        @Override public int hashCodeOf(Task task) { return task.hashCode(); }
    };

    private final PremiseGenerator premiseGenerator;


    /**
     * Constructor, called in Memory.getConcept only
     *
     * @param term      A term corresponding to the concept
     * @param b
     * @param taskLinks
     * @param termLinks
     * @param memory    A reference to the memory
     */
    public DefaultConcept(final Term term, final Budget b, final Bag<Sentence, TaskLink> taskLinks, final Bag<TermLinkKey, TermLink> termLinks, BeliefTable.RankBuilder rb, PremiseGenerator ps, final Memory memory) {
        super(term, b, memory);

        this.premiseGenerator = ps;
        ps.setConcept(this);

        this.deletionTime = creationTime - 1; //set to one cycle before created meaning it was potentially reborn

        this.beliefs = new ArrayListBeliefTable(memory.param.conceptBeliefsMax.intValue(), rb.get(this, true));
        this.goals = new ArrayListBeliefTable(memory.param.conceptGoalsMax.intValue(), rb.get(this, false));

        final int maxQuestions = memory.param.conceptQuestionsMax.intValue();
        this.questions = new ArrayListTaskTable(maxQuestions);
        this.quests = new ArrayListTaskTable(maxQuestions);

        this.taskLinks = taskLinks;
        this.termLinks = termLinks;

        this.termLinkBuilder = new TermLinkBuilder(this);
        this.taskLinkBuilder = new TaskLinkBuilder(memory);

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
     * Pending Quests to be answered by new desire values
     */
    public TaskTable getQuests() {
        return quests;
    }

    /**
     * Judgments directly made about the term Use ArrayList because of access
     * and insertion in the middle
     */
    public BeliefTable getBeliefs() {
        return beliefs;
    }

    /**
     * Desire values on the term, similar to the above one
     */
    public BeliefTable getGoals() {
        return goals;
    }


    public long getDeletionTime() {
        return deletionTime;
    }



    /** updates the concept-has-questions index if the concept transitions from having no questions to having, or from having to not having */
    public void onTableUpdated(char punctuation, int originalSize) {

        switch (punctuation) {
            /*case Symbols.GOAL:
                break;*/
            case Symbols.QUEST:
            case Symbols.QUESTION:
                if (getQuestions().isEmpty() && getQuests().isEmpty()) {
                    if (originalSize > 0) //became empty
                        getMemory().updateConceptQuestions(this);
                } else {
                    if (originalSize == 0) { //became non-empty
                        getMemory().updateConceptQuestions(this);
                    }
                }
                break;
        }
    }

    /* ---------- direct processing of tasks ---------- */



    /**
     * called by concept before it fires to update any pending changes
     */
    public void updateLinks() {


        getTermLinks().forgetNext(
                getMemory().param.termLinkForgetDurations,
                Global.TERMLINK_FORGETTING_EXTRA_DEPTH,
                getMemory());



        getTaskLinks().forgetNext(
                getMemory().param.taskLinkForgetDurations,
                Global.TASKLINK_FORGETTING_EXTRA_DEPTH,
                getMemory());


        linkTerms(null, true);

    }

    public boolean link(Task t) {
        if (linkTask(t))
            return linkTerms(t, true);  // recursively insert TermLink
        return false;
    }

    /**
     * for batch processing a collection of tasks; more efficient than processing them individually
     */
    //TODO untested
    public void link(Collection<Task> tasks) {

        final int s = tasks.size();
        if (s == 0) return;

        if (s == 1) {
            link(tasks.iterator().next());
            return;
        }


        //aggregate a merged budget, allowing a maximum of (1,1,1)
        Budget aggregateBudget = null;
        for (Task t : tasks) {
            if (linkTask(t)) {
                if (aggregateBudget == null) aggregateBudget = new Budget(t, false);
                else {
                    //aggregateBudget.merge(t);
                    aggregateBudget.accumulate(t);
                }
            }
        }

        //linkToTerms the aggregate budget, rather than each task's budget separately
        if (aggregateBudget != null) {
            linkTerms(aggregateBudget, true);
        }
    }




    /**
     * To accept a new judgment as belief, and check for revisions and solutions
     *
     * @param judg      The judgment to be accepted
     * @param belief The task to be processed
     * @return Whether to continue the processing of the task
     */
    public boolean processBelief(final Premise nal, Task belief) {

        float successBefore = getSuccess();

        final Task input = belief;

        belief = getBeliefs().add(input, this, nal);

        boolean added;


        if (belief!=input) {
//            String reason = "Unbelievable or Duplicate";
//            //String reason = input.equals(belief) ? "Duplicate" : "Unbelievable";
//                // + "compared to: " + belief
//            getMemory().removed(input, reason);
            added = false;
        }
        else {
            added = (belief!=null);
        }

        if (added) {
            /*if (task.aboveThreshold())*/
                //if (nal != null) {
            if (hasQuestions()) {
                final Task b = belief;
                //TODO move this to a subclass of TaskTable which is customized for questions. then an arraylist impl of TaskTable can iterate by integer index and not this iterator/lambda
                getQuestions().forEach( t -> trySolution(b, t, nal) );
            }
            //}

            float successAfter = getSuccess();
            float delta = successAfter - successBefore;
            if (delta!=0)
                memory.emotion.happy(delta);
        }

        return added;
    }


    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it
     *
     * @param judg    The judgment to be accepted
     * @param goal The task to be processed
     * @return Whether to continue the processing of the task
     */
    public boolean processGoal(final Premise nal, Task goal) {

        float successBefore = getSuccess();

        final Task input = goal;

        goal = getGoals().add(input, this, nal);

        boolean added;

        if (goal!=input) {
            //String reason = "Undesirable or Duplicate";
            //String reason = input.equals(goal) ? "Duplicate" : "Undesirable";
            // + "compared to: " + belief
            //getMemory().removed(input, reason);
            added = false;
        }
        else {
            added = (goal!=null);
        }

        if (!added) return false;


        float successAfter = getSuccess();
        float delta = successAfter - successBefore;
        if (delta!=0)
            memory.emotion.happy(delta);

        //long then = goal.getOccurrenceTime();
        int dur = nal.duration();

//        //this task is not up to date (now is ahead of then) we have to project it first
//        if(TemporalRules.after(then, now, dur)) {
//
//            nal.singlePremiseTask(task.projection(nal.memory, now, then) );
//
//            return true;
//
//        }

        if (goal.summaryGreaterOrEqual(memory.param.goalThreshold)) {

            // check if the Goal is already satisfied
            Task beliefSatisfied = getBeliefs().topRanked();

            /*float AntiSatisfaction = 0.5f; //we dont know anything about that goal yet, so we pursue it to remember it because its maximally unsatisfied
            if (beliefSatisfied != null) {

                Truth projectedTruth = beliefSatisfied.projection(goal.getOccurrenceTime(), dur);
                //Sentence projectedBelief = belief.projectionSentence(goal.getOccurrenceTime(), dur);

                boolean solved = null!=trySolution(beliefSatisfied, projectedTruth, goal, nal); // check if the Goal is already satisfied (manipulate budget)
                if (solved) {
                    AntiSatisfaction = goal.getTruth().getExpDifAbs(beliefSatisfied.getTruth());
                }
            }

            float Satisfaction = 1.0f - AntiSatisfaction;
            Truth T = BasicTruth.clone(goal.getTruth());

            T.setFrequency((float) (T.getFrequency() - Satisfaction)); //decrease frequency according to satisfaction value

            if (AntiSatisfaction >= Global.SATISFACTION_TRESHOLD && goal.sentence.truth.getExpectation() > nal.memory.param.executionThreshold.get()) {
*/
                executeGoal(goal, this, nal);
            //}
        }

        return true;
    }

    protected static void executeGoal(Task goal, Concept c, Premise nal) {

        questionFromGoal(goal, nal);

        //TODO
        //InternalExperience.experienceFromTask(nal, task, false);

        nal.getMemory().execute(c, goal);

    }


    public static void questionFromGoal(final Task task, final Premise p) {
        if (Global.QUESTION_GENERATION_ON_DECISION_MAKING || Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
            //ok, how can we achieve it? add a question of whether it is fullfilled

            ArrayList<Compound> qu = new ArrayList(3);

            if (Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
                if (!(task.sentence.getTerm() instanceof Equivalence) && !(task.sentence.getTerm() instanceof Implication)) {

                    Implication i1 = Implication.make(how, task.sentence.getTerm(), TemporalRules.ORDER_CONCURRENT);
                    if (i1 != null)
                        qu.add(i1);

                    Implication i2 = Implication.make(how, task.sentence.getTerm(), TemporalRules.ORDER_FORWARD);
                    if (i2 != null)
                        qu.add(i2);

                }
            }

            if (Global.QUESTION_GENERATION_ON_DECISION_MAKING) {
                qu.add(task.sentence.getTerm());
            }

            if (qu.isEmpty()) return;

            TaskSeed<Compound> t = p.newTask()
                    .question()
                    .parent(task)
                    .occurr(task.sentence.getOccurrenceTime()) //set tense of question to goal tense)
                    .budget(task.getPriority() * Global.CURIOSITY_DESIRE_PRIORITY_MUL, task.getDurability() * Global.CURIOSITY_DESIRE_DURABILITY_MUL, 1);

            for (Compound q : qu)
                p.deriveSingle(t.term(q));
        }
    }


    /**
     * To answer a quest or q by existing beliefs
     *
     * @param q The task to be processed
     * @return the matching task if it was not a new task to be added
     */
    public Task processQuestion(final Premise nal, Task q) {


        TaskTable table = q.isQuestion() ? getQuestions() : getQuests();

        if (Global.DEBUG) {
            if (q.sentence.truth != null) {
                System.err.println(q.sentence + " has non-null truth");
                System.err.println(q.getExplanation());
                throw new RuntimeException(q.sentence + " has non-null truth");
            }
        }


        if (getMemory().answer(this, q)) {

        }

        if (!isConstant()) {
            //boolean newQuestion = table.isEmpty();

            Task match = table.add(q, questionEquivalence, this);
            if (match == q) {
                final int presize = getQuestions().size() + getQuests().size();
                onTableUpdated(q.getPunctuation(), presize);
            }
            q = match; //try solution with the original question
        }

        final long now = getMemory().time();
        if (q.isQuest()) {
            trySolution(getGoals().top(q, now), q, nal);
        } else {
            trySolution(getBeliefs().top(q, now), q, nal);
        }

        return q;
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

        if (!task.summaryGreaterOrEqual(memory.param.taskLinkThreshold))
            return false;

        Budget taskBudget = task;
        taskLinkBuilder.setTemplate(null);
        taskLinkBuilder.setTask(task);

        activateTaskLink(taskLinkBuilder);  // tlink type: SELF

        List<TermLinkTemplate> templates = termLinkBuilder.templates();

        if (templates == null || templates.isEmpty()) {
            //distribute budget to incoming termlinks?
            return false;
        }


        //TODO parameter to use linear division, conserving total budget
        //float linkSubBudgetDivisor = (float)Math.sqrt(termLinkTemplates.size());
        final int numTemplates = templates.size();


        float linkSubBudgetDivisor = termLinkBuilder.getNonTransforms();
        //float linkSubBudgetDivisor = (float) Math.sqrt(numTemplates);


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
                componentConcept.activateTaskLink(taskLinkBuilder);
            } else {
                //taskBudgetBalance += subBudget.getPriority();
            }

        }

        return true;
    }

    static boolean aboveThreshold(Budget b) {
        return b.summary() >= Global.BUDGET_EPSILON;
    }


    /* ---------- insert Links for indirect processing ---------- */

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

        float dur = 0, qua = 0;
        if (taskBudget != null) {
            //TODO make this parameterizable

            //float linkSubBudgetDivisor = (float)Math.sqrt(recipients);

            //half of each subBudget is spent on this concept and the other concept's termlink
            //subBudget = taskBudget.getPriority() * (1f / (2 * recipients));

            //subPriority = taskBudget.getPriority() / (float) Math.sqrt(recipients);
            subPriority = taskBudget.getPriority() / recipients;
            dur = taskBudget.getDurability();
            qua = taskBudget.getQuality();
        } else {
            subPriority = 0;
        }


        boolean activity = false;

        if (recipients > 0) {
            final List<TermLinkTemplate> templates = termLinkBuilder.templates();

            int numTemplates = templates.size();
            for (int i = 0; i < numTemplates; i++) {

                final TermLinkTemplate template = templates.get(i);

                //only apply this loop to non-transform termlink templates
                if (template.type != TermLink.TRANSFORM) {
                    if (template.link(this, subPriority, dur, qua, updateTLinks))
                        activity = true;
                }

            }

        }

        //TODO merge with above loop, or avoid altogether under certain conditions

        List<TermLinkTemplate> tl = getTermLinkTemplates();
        if (tl != null && updateTLinks) {
            int n = tl.size();
            for (int i = 0; i < n; i++) {
                TermLinkTemplate t = tl.get(i);
                if (t.summaryGreaterOrEqual(memory.param.termLinkThreshold)) {
                    if (linkTerm(t, updateTLinks))
                        activity = true;

                }
            }
        }


        return activity;
    }

    boolean linkTerm(TermLinkTemplate template, boolean updateTLinks) {
        return template.link(this, 0,0, 0, updateTLinks);
        //return linkTerm(template, 0, 0, 0, updateTLinks);
    }

    @Override
    public TermLinkBuilder getTermLinkBuilder() {
        return termLinkBuilder;
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


    //    /**
//     * Recalculate the quality of the concept [to be refined to show
//     * extension/intension balance]
//     *
//     * @return The quality value
//     */
//    public float getAggregateQuality() {
//        float linkPriority = termLinks.getPriorityMean();
//        float termComplexityFactor = 1.0f / term.getComplexity();
//        float result = or(linkPriority, termComplexityFactor);
//        if (result < 0) {
//            throw new RuntimeException("Concept.getQuality < 0:  result=" + result + ", linkPriority=" + linkPriority + " ,termComplexityFactor=" + termComplexityFactor + ", termLinks.size=" + termLinks.size());
//        }
//        return result;
//    }


    //
//
//    /**
//     * Collect direct isBelief, questions, and goals for display
//     *
//     * @return String representation of direct content
//     */
//    public String displayContent() {
//        final StringBuilder buffer = new StringBuilder(18);
//        buffer.append("\n  Beliefs:\n");
//        if (!beliefsEternal.isEmpty()) {
//            for (Sentence s : beliefsEternal) {
//                buffer.append(s).append('\n');
//            }
//        }
//        if (!beliefsTemporal.isEmpty()) {
//            for (Sentence s : beliefsTemporal) {
//                buffer.append(s).append('\n');
//            }
//        }
//        if (!questions.isEmpty()) {
//            buffer.append("\n  Question:\n");
//            for (Task t : questions) {
//                buffer.append(t).append('\n');
//            }
//        }
//        return buffer.toString();
//    }


    public List<TermLinkTemplate> getTermLinkTemplates() {
        return termLinkBuilder.templates();
    }


    /**
     * Pending Question directly asked about the term
     *
     * Note: since this is iterated frequently, an array should be used. To
     * avoid iterator allocation, use .get(n) in a for-loop
     */
    /**
     * Return the questions, called in ComposionalRules in
     * dedConjunctionByQuestion only
     */
    public TaskTable getQuestions() {
        return questions;
    }


    //
//    public Collection<Sentence> getSentences(char punc) {
//        switch(punc) {
//            case Symbols.JUDGMENT: return beliefs;
//            case Symbols.GOAL: return goals;
//            case Symbols.QUESTION: return Task.getSentences(questions);
//            case Symbols.QUEST: return Task.getSentences(quests);
//        }
//        throw new RuntimeException("Invalid punctuation: " + punc);
//    }
//    public CharSequence getBeliefsSummary() {
//        if (beliefs.isEmpty())
//            return "0 beliefs";
//        StringBuilder sb = new StringBuilder();
//        for (Sentence s : beliefs)
//            sb.append(s.toString()).append('\n');
//        return sb;
//    }
//    public CharSequence getGoalSummary() {
//        if (goals.isEmpty())
//            return "0 goals";
//        StringBuilder sb = new StringBuilder();
//        for (Sentence s : goals)
//            sb.append(s.toString()).append('\n');
//        return sb;
//    }


    /**
     * called when concept is activated; empty and subclassable
     */
    protected void onActive() {

    }

    @Override
    public TermLink nextTermLink(TaskLink taskLink) {
        return premiseGenerator.nextTermLink(this, taskLink);
    }

    /** called by memory, dont call directly */
    @Override public void delete() {

        super.delete();

        //dont delete the tasks themselves because they may be referenced from othe concepts.
        beliefs.clear();
        goals.clear();
        questions.clear();
        quests.clear();


        getTermLinks().delete();
        getTaskLinks().delete();

        if (termLinkBuilder != null)
            termLinkBuilder.delete();
    }

}
