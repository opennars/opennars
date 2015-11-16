package nars.concept;

import com.gs.collections.api.block.procedure.Procedure2;
import javolution.util.function.Equality;
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
import nars.task.Task;
import nars.term.Term;


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


//    final static public Equality<Task> taskEquivalence = new Equality<Task>() {
//
//        @Override
//        public boolean areEqual(Task a, Task b) {
//            return (a.equals(b));
//        }
//
//        //N/A
//        @Override public int compare(Task task, Task t1) {  return 0;        }
//        @Override public int hashCodeOf(Task task) { return task.hashCode(); }
//    };




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

    public DefaultConcept(final Term term, Param p) {
        this(term, new NullBag(), new NullBag(), p);
    }

    /**
     * Constructor, called in Memory.getConcept only
     * @param term      A term corresponding to the concept
     * @param taskLinks
     * @param termLinks
     */
    public DefaultConcept(final Term term, final Bag<Task, TaskLink> taskLinks, final Bag<TermLinkKey, TermLink> termLinks, Param p) {
        super(term, termLinks, taskLinks);

        //TODO lazy instantiate?
        this.beliefs = new ArrayListBeliefTable(p.conceptBeliefsMax.intValue());
        this.goals = new ArrayListBeliefTable(p.conceptGoalsMax.intValue());

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




//    /** updates the concept-has-questions index if the concept transitions from having no questions to having, or from having to not having */
//    public void onTableUpdated(char punctuation, int originalSize) {
//
//        switch (punctuation) {
//            /*case Symbols.GOAL:
//                break;*/
//            case Symbols.QUEST:
//            case Symbols.QUESTION:
//                if (getQuestions().isEmpty() && getQuests().isEmpty()) {
//                    //if (originalSize > 0) //became empty
//                        //getMemory().updateConceptQuestions(this);
//                } else {
//                    //if (originalSize == 0) //became non-empty
//                        //getMemory().updateConceptQuestions(this);
//
//                }
//                break;
//        }
//    }

    /* ---------- direct processing of tasks ---------- */


    //
//    /**
//     * for batch processing a collection of tasks; more efficient than processing them individually
//     */
//    //TODO untested
//    public void link(Collection<Task> tasks) {
//
//        final int s = tasks.size();
//        if (s == 0) return;
//
//        if (s == 1) {
//            link(tasks.iterator().next());
//            return;
//        }
//
//
//        //aggregate a merged budget, allowing a maximum of (1,1,1)
//        Budget aggregateBudget = null;
//        for (Task t : tasks) {
//            if (linkTask(t)) {
//                if (aggregateBudget == null) aggregateBudget = new Budget(t, false);
//                else {
//                    //aggregateBudget.merge(t);
//                    aggregateBudget.accumulate(t);
//                }
//            }
//        }
//
//        //linkToTerms the aggregate budget, rather than each task's budget separately
//        if (aggregateBudget != null) {
//            linkTerms(aggregateBudget, true);
//        }
//    }
//



    /** attempt to insert a task.
     *
     * @param c the concept in which this occurrs
     * @param nal
     * @return:
     *      the input value that was inserted, if it was added to the table
     *      a previous stored task if this was a duplicate (table unchanged)
     *      a new belief created from older ones which serves as a revision of what was input, if it was added to the table
     *      null if it was discarded
     *
     */




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

        float successBefore = getSuccess();

        final Task strongest = getBeliefs().add( belief, BeliefTable.BeliefConfidenceOrOriginality, this, nal);

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
        if (delta!=0)
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

        final Task strongest = getGoals().add( goal, BeliefTable.BeliefConfidenceOrOriginality, this, nal);

        if (strongest==null) {
            return false;
        }
        else {
            float successAfter = getSuccess();
            float delta = successAfter - successBefore;
            if (delta!=0)
                memory.emotion.happy(delta);

            nal.memory().execute(goal);

            return true;

        }

            //long then = goal.getOccurrenceTime();
            //int dur = nal.duration();

//        //this task is not up to date (now is ahead of then) we have to project it first
//        if(TemporalRules.after(then, now, dur)) {
//
//            nal.singlePremiseTask(task.projection(nal.memory, now, then) );
//
//            return true;
//
//        }

//         if (goal.getBudget().summaryGreaterOrEqual(memory.questionFromGoalThreshold)) {
//
//                // check if the Goal is already satisfied
//                //Task beliefSatisfied = getBeliefs().topRanked();
//
//            /*float AntiSatisfaction = 0.5f; //we dont know anything about that goal yet, so we pursue it to remember it because its maximally unsatisfied
//            if (beliefSatisfied != null) {
//
//                Truth projectedTruth = beliefSatisfied.projection(goal.getOccurrenceTime(), dur);
//                //Sentence projectedBelief = belief.projectionSentence(goal.getOccurrenceTime(), dur);
//
//                boolean solved = null!=trySolution(beliefSatisfied, projectedTruth, goal, nal); // check if the Goal is already satisfied (manipulate budget)
//                if (solved) {
//                    AntiSatisfaction = goal.getTruth().getExpDifAbs(beliefSatisfied.getTruth());
//                }
//            }
//
//            float Satisfaction = 1.0f - AntiSatisfaction;
//            Truth T = BasicTruth.clone(goal.getTruth());
//
//            T.setFrequency((float) (T.getFrequency() - Satisfaction)); //decrease frequency according to satisfaction value
//
//            if (AntiSatisfaction >= Global.SATISFACTION_TRESHOLD && goal.sentence.truth.getExpectation() > nal.memory.param.executionThreshold.get()) {
//*/
//
//                questionFromGoal(goal, nal);
//
//                //TODO
//                //InternalExperience.experienceFromTask(nal, task, false);
//
//
//                //}
//            }


    }


//    public static void questionFromGoal(final Task task, final Premise p) {
//        if (Global.QUESTION_GENERATION_ON_DECISION_MAKING || Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
//            //ok, how can we achieve it? add a question of whether it is fullfilled
//
//            List<Compound> qu = Global.newArrayList(3);
//
//            final Compound term = task.getTerm();
//
//            if (Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
//                if (!(term instanceof Equivalence) && !(term instanceof Implication)) {
//
//                    Implication i1 = Implication.make(how, term, TemporalRules.ORDER_CONCURRENT);
//                    if (i1 != null)
//                        qu.add(i1);
//
//                    Implication i2 = Implication.make(how, term, TemporalRules.ORDER_FORWARD);
//                    if (i2 != null)
//                        qu.add(i2);
//
//                }
//            }
//
//            if (Global.QUESTION_GENERATION_ON_DECISION_MAKING) {
//                qu.add(term);
//            }
//
//            if (qu.isEmpty()) return;
//
//            p.input(
//                qu.stream().map(q -> p.newTask(q)
//                    .question()
//                    .parent(task)
//                    .occurr(task.getOccurrenceTime()) //set tense of question to goal tense)
//                    .budget(task.getPriority() * Global.CURIOSITY_DESIRE_PRIORITY_MUL, task.getDurability() * Global.CURIOSITY_DESIRE_DURABILITY_MUL, 1)
//            ));
//
//        }
//    }


    /**
     * To answer a quest or q by existing beliefs
     *
     * @param q The task to be processed
     * @return true if the quest/question table changed
     */
    public boolean processQuestion(final Premise nal) {

        Task q = nal.getTask();
        TaskTable table = q.isQuestion() ? getQuestions() : getQuests();

//        //if (Global.DEBUG) {
//            if (q.getTruth() != null) {
//                System.err.println(q + " has non-null truth");
//                System.err.println(q.getExplanation());
//                throw new RuntimeException(q + " has non-null truth");
//            }
        //}


        /** execute the question, for any attached operators that will handle it */

        //getMemory().execute(q);


        //boolean tableAffected = false;

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




    /* ---------- insert Links for indirect processing ---------- */


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


//    @Override public synchronized boolean delete() {
//
//        if (!super.delete())
//            return false;
//
//        //dont delete the tasks themselves because they may be referenced from othe concepts.
//        beliefs.clear();
//        goals.clear();
//        questions.clear();
//        quests.clear();
//
//
//        getTermLinks().delete();
//        getTaskLinks().delete();
//
//        if (getTermLinkBuilder() != null)
//            getTermLinkBuilder().delete();
//
//        return true;
//    }

}
