package nars.concept;

import javolution.util.function.Equality;
import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
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

import static nars.nal.nal1.LocalRules.trySolution;


public class DefaultConcept extends AtomConcept {

    private final TaskTable questions;
    private final TaskTable quests;
    private final BeliefTable beliefs;
    private final BeliefTable goals;


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


    /**
     * Constructor, called in Memory.getConcept only
     *  @param term      A term corresponding to the concept
     * @param b
     * @param taskLinks
     * @param termLinks
     * @param memory    A reference to the memory
     */
    public DefaultConcept(final Term term, final Budget b, final Bag<Sentence, TaskLink> taskLinks, final Bag<TermLinkKey, TermLink> termLinks, @Deprecated PremiseGenerator ps, BeliefTable.RankBuilder rb, final Memory memory) {
        super(term, b, termLinks, taskLinks, memory);

        //TODO move PremiseGenerator into ConceptProcess , and only in one subclass of them
        if (ps!=null)
            ps.setConcept(this);

        this.beliefs = new ArrayListBeliefTable(memory.conceptBeliefsMax.intValue(), rb.get(this, true));
        this.goals = new ArrayListBeliefTable(memory.conceptGoalsMax.intValue(), rb.get(this, false));

        final int maxQuestions = memory.conceptQuestionsMax.intValue();
        this.questions = new ArrayListTaskTable(maxQuestions);
        this.quests = new ArrayListTaskTable(maxQuestions);


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




    /** updates the concept-has-questions index if the concept transitions from having no questions to having, or from having to not having */
    public void onTableUpdated(char punctuation, int originalSize) {

        switch (punctuation) {
            /*case Symbols.GOAL:
                break;*/
            case Symbols.QUEST:
            case Symbols.QUESTION:
                if (getQuestions().isEmpty() && getQuests().isEmpty()) {
                    //if (originalSize > 0) //became empty
                        //getMemory().updateConceptQuestions(this);
                } else {
                    //if (originalSize == 0) //became non-empty
                        //getMemory().updateConceptQuestions(this);

                }
                break;
        }
    }

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



    /**
     * To accept a new judgment as belief, and check for revisions and solutions
     *
     * @param judg      The judgment to be accepted
     * @param belief The task to be processed
     * @return Whether to continue the processing of the task
     */
    public boolean processBelief(final Premise nal, Task belief) {

        float successBefore = getSuccess();

        final Task newSolution = getBeliefs().add(belief, this, nal);

        if (newSolution != null) {

//            String reason = "Unbelievable or Duplicate";
//            //String reason = input.equals(belief) ? "Duplicate" : "Unbelievable";
//                // + "compared to: " + belief
//            getMemory().removed(input, reason);
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

            return true;
        } else {
            getMemory().remove(belief, "Ineffective Belief");
            return false;
        }

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

        final Task newSolution = getGoals().add(goal, this, nal);

        if (newSolution==null) {
            getMemory().remove(goal, "Ineffective Goal");
            return false;
        }
        else {
            float successAfter = getSuccess();
            float delta = successAfter - successBefore;
            if (delta!=0)
                memory.emotion.happy(delta);

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

            if (goal.getBudget().summaryGreaterOrEqual(memory.questionFromGoalThreshold)) {

                // check if the Goal is already satisfied
                //Task beliefSatisfied = getBeliefs().topRanked();

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

                questionFromGoal(goal, nal);

                //TODO
                //InternalExperience.experienceFromTask(nal, task, false);

                nal.memory().execute(goal);

                //}
            }

            return true;

        }

    }


    public static void questionFromGoal(final Task task, final Premise p) {
        if (Global.QUESTION_GENERATION_ON_DECISION_MAKING || Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
            //ok, how can we achieve it? add a question of whether it is fullfilled

            ArrayList<Compound> qu = new ArrayList(3);

            final Compound term = task.getTerm();

            if (Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
                if (!(term instanceof Equivalence) && !(term instanceof Implication)) {

                    Implication i1 = Implication.make(how, term, TemporalRules.ORDER_CONCURRENT);
                    if (i1 != null)
                        qu.add(i1);

                    Implication i2 = Implication.make(how, term, TemporalRules.ORDER_FORWARD);
                    if (i2 != null)
                        qu.add(i2);

                }
            }

            if (Global.QUESTION_GENERATION_ON_DECISION_MAKING) {
                qu.add(term);
            }

            if (qu.isEmpty()) return;

            for (Compound q : qu) {
                TaskSeed t = p.newTask(q)
                        .question()
                        .parent(task)
                        .occurr(task.getOccurrenceTime()) //set tense of question to goal tense)
                        .budget(task.getPriority() * Global.CURIOSITY_DESIRE_PRIORITY_MUL, task.getDurability() * Global.CURIOSITY_DESIRE_DURABILITY_MUL, 1);


                p.derive(t.term(q));
            }
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
            if (q.getTruth() != null) {
                System.err.println(q + " has non-null truth");
                System.err.println(q.getExplanation());
                throw new RuntimeException(q + " has non-null truth");
            }
        }


        /** execute the question, for any attached operators that will handle it */
        //getMemory().execute(q);

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

        if (getTermLinkBuilder() != null)
            getTermLinkBuilder().delete();
    }

}
