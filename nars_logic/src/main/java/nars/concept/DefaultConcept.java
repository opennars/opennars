package nars.concept;

import com.gs.collections.api.block.procedure.Procedure2;
import javolution.util.function.Equality;
import nars.Memory;
import nars.Param;
import nars.Premise;
import nars.bag.Bag;
import nars.bag.NullBag;
import nars.budget.Budget;
import nars.concept.util.ArrayListBeliefTable;
import nars.concept.util.ArrayListTaskTable;
import nars.concept.util.BeliefTable;
import nars.concept.util.TaskTable;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkBuilder;
import nars.link.TermLinkKey;
import nars.nal.nal1.LocalRules;
import nars.op.mental.Anticipate;
import nars.task.DefaultTask;
import nars.task.Task;
import nars.term.Term;
import nars.Global;
import nars.truth.DefaultTruth;


public class DefaultConcept extends AtomConcept {

    protected final TaskTable questions;
    protected final TaskTable quests;
    protected final BeliefTable beliefs;
    protected final BeliefTable goals;

    /**
     * Link templates of TermLink, only in concepts with CompoundTerm Templates
     * are used to improve the efficiency of TermLink building
     */
    protected TermLinkBuilder termLinkBuilder = null;

    final static public Equality<Task> questionEquivalence = new Equality<Task>() {

        @Override
        public boolean areEqual(Task a, Task b) {
            return (a.equals(b));
        }

        //N/A
        @Override public int compare(Task task, Task t1) {  return 0;        }
        @Override public int hashCodeOf(Task task) { return task.hashCode(); }
    };

    /** how incoming budget is merged into its existing duplicate quest/question */
    final static Procedure2<Budget, Budget> duplicateQuestionMerge = Budget.plus;

    public DefaultConcept(Memory memory, final Term term, Param p) {
        this(memory, term, new NullBag(), new NullBag(), p);
    }

    /**
     * Constructor, called in Memory.getConcept only
     * @param term      A term corresponding to the concept
     * @param taskLinks
     * @param termLinks
     */
    public DefaultConcept(Memory memory, final Term term, final Bag<Task, TaskLink> taskLinks, final Bag<TermLinkKey, TermLink> termLinks, Param p) {
        super(term, termLinks, taskLinks);

        this.memory = memory;
        //TODO lazy instantiate?
        this.beliefs = new ArrayListBeliefTable(memory, p.conceptBeliefsMax.intValue());
        this.goals = new ArrayListBeliefTable(memory, p.conceptGoalsMax.intValue());

        final int maxQuestions = p.conceptQuestionsMax.intValue();
        this.questions = new ArrayListTaskTable(maxQuestions);
        this.quests = new ArrayListTaskTable(maxQuestions);


    }

    @Override
    public TermLinkBuilder getTermLinkBuilder() {
        final TermLinkBuilder termLinkBuilder = this.termLinkBuilder;
        if (termLinkBuilder == null) {
            return this.termLinkBuilder = new TermLinkBuilder(this);
        }
        return termLinkBuilder;
    }

    /**
     * Pending Quests to be answered by new desire values
     */
    public final TaskTable getQuests() {
        return quests;
    }

    /**
     * Judgments directly made about the term Use ArrayList because of access
     * and insertion in the middle
     */
    public final BeliefTable getBeliefs() {
        return beliefs;
    }

    /**
     * Desire values on the term, similar to the above one
     */
    public final BeliefTable getGoals() {
        return goals;
    }

    static Task add(TaskTable table, Task input, Equality<Task> eq, Procedure2<Budget,Budget> duplicateMerge, Premise nal) {
        return table.add(input, eq, duplicateMerge, nal.memory());
    }


    /**
     * To accept a new judgment as belief, and check for revisions and solutions
     *
     * @param belief The task to be processed
     * @return Whether to continue the processing of the task
     */
    public boolean processBelief(final Premise nal) {

        final Task belief = nal.getTask();

        if(belief.isInput() && belief.isJudgment() && !belief.isEternal()) {
            this.put(Anticipate.class, true);
        }

        float successBefore = getSuccess();

        final Task strongest = getBeliefs().add( belief, new BeliefTable.SolutionQualityMatchingOrderRanker(belief, nal.time()), this, nal);

        if (strongest == null || strongest.isDeleted()) {
            return false;
        }


        if (hasQuestions()) {
            //TODO move this to a subclass of TaskTable which is customized for questions. then an arraylist impl of TaskTable can iterate by integer index and not this iterator/lambda
            getQuestions().forEach( question -> LocalRules.trySolution(question, strongest, nal) );
        }
        //}


        /** update happiness meter on solution  TODO revise */
        float successAfter = getSuccess();
        float delta = successAfter - successBefore;
        if (delta!=0) //more satisfaction of a goal due to belief, more happiness
            memory.emotion.happy(delta);

        return true;
    }


    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it
     *
     * @param goal The task to be processed
     * @return Whether to continue the processing of the task
     */
    public boolean processGoal(final Premise nal) {

        final Task goal = nal.getTask();
        final float successBefore = getSuccess();

        if(goal.getBudget().summary() <= memory.taskProcessThreshold.floatValue()) {
            return false;
        }

        final Task strongest = getGoals().add( goal, new BeliefTable.SolutionQualityMatchingOrderRanker(goal, nal.time()), this, nal);

        if (strongest==null) {
            return false;
        }
        else {
            float successAfter = getSuccess();
            float delta = successAfter - successBefore;
            if (delta!=0) //less desire of a goal, more happiness
               memory.emotion.happy(delta);

            float expectation_diff = (1-successAfter) / successAfter;
            if(Math.abs(expectation_diff) >= Global.EXECUTION_SATISFACTION_TRESHOLD) {
                DefaultTruth projected = strongest.projection(memory.time(), memory.time());
                if (projected.getExpectation() > Global.EXECUTION_DESIRE_EXPECTATION_THRESHOLD) {
                    nal.nar().execute((DefaultTask) goal);
                }
            }


            return true;

        }
    }


    /**
     * To answer a quest or q by existing beliefs
     *
     * @param q The task to be processed
     * @return true if the quest/question table changed
     */
    public boolean processQuestion(final Premise nal) {

        Task q = nal.getTask();
        TaskTable table = q.isQuestion() ? getQuestions() : getQuests();

        if (!isConstant()) {
            //boolean newQuestion = table.isEmpty();

            Task match = add(table, q, questionEquivalence, duplicateQuestionMerge, nal);
            if (match == q) {
                //final int presize = getQuestions().size() + getQuests().size();
                //onTableUpdated(q.getPunctuation(), presize);
                //tableAffected = true;
            }
            else {
                q = match; //try solution with the original question
            }
        }

        //TODO if the table was not affected, does the following still need to happen:

        final long now = getMemory().time();

        Task sol;
        if (q.isQuest()) {
            sol = getGoals().top(q, now);
        } else {
            sol = getBeliefs().top(q, now);
        }

        if (sol!=null) {
            /*Task solUpdated = */LocalRules.trySolution(q, sol, nal);
        }

        return true;
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


}
