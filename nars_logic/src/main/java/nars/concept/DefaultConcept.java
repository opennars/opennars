package nars.concept;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.bag.Bag;
import nars.bag.NullBag;
import nars.budget.Budget;
import nars.budget.BudgetMerge;
import nars.concept.util.ArrayListBeliefTable;
import nars.concept.util.ArrayListTaskTable;
import nars.concept.util.BeliefTable;
import nars.concept.util.TaskTable;
import nars.nal.LocalRules;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;

import java.util.function.BiPredicate;


public class DefaultConcept extends AtomConcept {

    protected final TaskTable questions;
    protected final TaskTable quests;
    protected final BeliefTable beliefs;
    protected final BeliefTable goals;



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




    public static final BiPredicate<Task,Task> questionEquivalence = new BiPredicate<Task,Task> () {


        @Override
        public boolean test(Task a, Task b) {
            return (a.equals(b));
        }

//        //N/A
//        @Override public int compare(Task task, Task t1) {  return 0;        }
//        @Override public int hashCodeOf(Task task) { return task.hashCode(); }
    };

    /** how incoming budget is merged into its existing duplicate quest/question */
    static final BudgetMerge duplicateQuestionMerge = Budget.plus;
    private final Termed[] termLinkTemplates;

    public DefaultConcept(Term term, Memory p) {
        this(term, new NullBag(), new NullBag(), p);
    }

    /**
     * Constructor, called in Memory.getConcept only
     * @param term      A term corresponding to the concept
     * @param taskLinks
     * @param termLinks
     */
    public DefaultConcept(Term term, Bag<Task> taskLinks, Bag<Termed> termLinks, Memory p) {
        super(term, termLinks, taskLinks);

        //TODO lazy instantiate?
        beliefs = new ArrayListBeliefTable(p.conceptBeliefsMax.intValue());
        goals = new ArrayListBeliefTable(p.conceptGoalsMax.intValue());

        int maxQuestions = p.conceptQuestionsMax.intValue();
        questions = new ArrayListTaskTable(maxQuestions);
        quests = new ArrayListTaskTable(maxQuestions);

        this.termLinkTemplates = TermLinkBuilder.build(term, p);

    }



    /**
     * Pending Quests to be answered by new desire values
     */
    @Override
    public final TaskTable getQuests() {
        return quests;
    }

    /**
     * Judgments directly made about the term Use ArrayList because of access
     * and insertion in the middle
     */
    @Override
    public final BeliefTable getBeliefs() {
        return beliefs;
    }

    /**
     * Desire values on the term, similar to the above one
     */
    @Override
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




    Task add(TaskTable table, Task input, BiPredicate<Task,Task>  eq, BudgetMerge duplicateMerge, Memory memory) {
        return table.add(input, eq, duplicateMerge, memory);
    }


    /**
     * To accept a new judgment as belief, and check for revisions and solutions
     *
     * @param belief The task to be processed
     * @param nar
     * @return Whether to continue the processing of the task
     */
    @Override
    public boolean processBelief(Task belief, NAR nar) {

        float successBefore = getSuccess();

        Task strongest = getBeliefs().add( belief,
                new BeliefTable.SolutionQualityMatchingOrderRanker(belief, nar.memory.time()),
                this, nar.memory);

        if (strongest == null || strongest.isDeleted()) {
            return false;
        }


        if (hasQuestions()) {
            //TODO move this to a subclass of TaskTable which is customized for questions. then an arraylist impl of TaskTable can iterate by integer index and not this iterator/lambda
            getQuestions().forEach( question ->
                LocalRules.trySolution(question, strongest, nar, (s) -> {
                    //..
                })
            );
        }
        //}


        /** update happiness meter on solution  TODO revise */
        float successAfter = getSuccess();
        float delta = successAfter - successBefore;
        if (delta!=0) //more satisfaction of a goal due to belief, more happiness
            nar.memory.emotion.happy(delta);

        return true;
    }


    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it
     *
     * @param goal The task to be processed
     * @param task
     * @return Whether to continue the processing of the task
     */
    @Override
    public boolean processGoal(Task goal, NAR nar) {

        float successBefore = getSuccess();

        Memory memory = nar.memory;

        Task strongest = getGoals().add( goal,
                new BeliefTable.SolutionQualityMatchingOrderRanker(goal, memory.time()),
                this, memory);

        if (strongest==null) {
            return false;
        }
        else {
            float successAfter = getSuccess();
            float delta = successAfter - successBefore;
            if (delta!=0) //less desire of a goal, more happiness
               memory.emotion.happy(delta);


            if(Math.abs(delta)>= memory.getExecutionSatisfactionThreshold()) {
                if (strongest.getTruth().getExpectation() > Global.EXECUTION_DESIRE_EXPECTATION_THRESHOLD) {
                    nar.execute(goal);
                }
            }


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
     * @param task
     * @param nar
     * @return true if the quest/question table changed
     */
    @Override
    public boolean processQuestion(Task q, NAR nar) {

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

            Task match = add(table, q, questionEquivalence, duplicateQuestionMerge, nar.memory);
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

        Task sol = q.isQuest() ? getGoals().top(q, nar.time()) : getBeliefs().top(q, nar.time());

        if (sol!=null) {
            /*Task solUpdated = */LocalRules.trySolution(q, sol, nar, (s) -> {
                //...
            });
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
    @Override
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



//    /**
//     * Recursively build TermLinks between a compound and its components
//     * <p>
//     * called only from Memory.continuedProcess
//     * activates termlinked concepts with fractions of the taskbudget
//
//     *
//     * @param b            The BudgetValue of the task
//     * @param updateTLinks true: causes update of actual termlink bag, false: just queues the activation for future application.  should be true if this concept calls it for itself, not for another concept
//     * @return whether any activity happened as a result of this invocation
//     */
//    public boolean linkTemplates(Budget b, float scale, boolean updateTLinks, NAR nar) {
//
//        if ((b == null) || (b.isDeleted())) return false;
//
//        Term[] tl = getTermLinkTemplates();
//        if (tl == null || tl.length == 0)
//            return false;
//
//        //subPriority = b.getPriority() / (float) Math.sqrt(recipients);
//        float factor = scale / (tl.length);
//
//        final Memory memory = nar.memory;
//
//        if (factor < memory.termLinkThreshold.floatValue())
//            return false;
//
//        boolean activity = false;
//
//        for (Term t : tl) {
//
//            /*if ((t.getTarget().equals(getTerm()))) {
//                //self
//                continue;
//            }*/
//
//
//            //only apply this loop to non-transform termlink templates
//            //PENDING_TERMLINK_BUDGET_MERGE.value(t, subBudget);
//
//            if (updateTLinks) {
//                //if (t.summaryGreaterOrEqual(termLinkThresh)) {
//
//                    if (link(t, b, factor, nar))
//                        activity = true;
//                //}
//            }
//
//        }
//
//        return activity;
//
//    }


//    /**
//     * Recursively build TermLinks between a compound and its components
//     * Process is started by one Task, and recurses only to templates
//     * creating bidirectional links between compound to components
//     */
//    @Override public void linkTemplates(Budget budget, float scale, NAR nar) {
//
//        Termed[] tl = getTermLinkTemplates();
//        int numTemplates;
//        if (tl == null || (numTemplates = tl.length) == 0)
//            return;
//
//        final Memory memory = nar.memory;
//
//        float subScale = scale / numTemplates;
//        if (subScale < memory.termLinkThreshold.floatValue())
//            return;
//
//        for (int i = 0; i < tl.length; i++) {
//            Termed t = tl[i];
//
//            final Concept target;
//            if (t instanceof Concept) {
//                target = (Concept) t;
//            } else {
//                target = nar.conceptualize(t);
//                if (target == null) continue;
//                tl[i] = target;
//            }
//
//            linkTemplate(target, budget, subScale, nar);
//            target.linkTemplates(budget, subScale, nar);
//        }
//
//    }


//    @Override public boolean link(Term t, Budget b, float scale, NAR nar) {
//
//        if (t.equals(term()))
//            throw new RuntimeException("looped activation");
//
//        Concept otherConcept = activateConcept(t, b, scale, nar);
//
//        //termLinkBuilder.set(t, false, nar.memory);
//
//        //activate this termlink to peer
//        // this concept termLink to that concept
//        getTermLinks().put(t, b, scale);
//
//        //activate peer termlink to this
//        //otherConcept.activateTermLink(termLinkBuilder.setIncoming(true)); // that concept termLink to this concept
//        otherConcept.getTermLinks().put(term(), b, scale);
//
//        //if (otherConcept.getTermLinkTemplates()) {
//        //UnitBudget termlinkBudget = termLinkBuilder.getBudget();
//        linkTemplates(b, scale, immediateTermLinkPropagation, nar);
//
//        return true;
//    }

    @Override public Termed[] getTermLinkTemplates() {
        return termLinkTemplates;
    }

    /**
     * Directly process a new task. Called exactly once on each task. Using
     * local information and finishing in a constant time. Provide feedback in
     * the taskBudget value of the task.
     * <p>
     * called in Memory.immediateProcess only
     *
     * @return whether it was processed
     */
    public boolean process(final Task task, NAR nar) {

        task.onConcept(this);

        //LogicMeter logicMeter = nar.memory.logic;

        switch (task.getPunctuation()) {
            case Symbols.JUDGMENT:
                return processBelief(task, nar );
            case Symbols.GOAL:
                return processGoal(task, nar);
            case Symbols.QUESTION:
                return processQuestion(task, nar );
            case Symbols.QUEST:
                return processQuest(task,nar );
            default:
                throw new RuntimeException("Invalid sentence type: " + task);
        }

        //logicMeter.process(task).hit();
    }




}
