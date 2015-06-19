package nars.nal.concept;

import nars.Events;
import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.nal.*;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.process.TaskProcess;
import nars.nal.task.TaskSeed;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.term.Variable;
import nars.nal.tlink.*;
import nars.util.data.id.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static nars.budget.BudgetFunctions.divide;
import static nars.nal.UtilityFunctions.or;
import static nars.nal.nal1.LocalRules.*;
import static nars.nal.nal7.TemporalRules.solutionQuality;


public class DefaultConcept extends Item<Term> implements Concept {

    State state;
    final long creationTime;
    long deletionTime;

    private final Term term;

    private final Bag<Sentence, TaskLink> taskLinks;

    private final Bag<Identifier, TermLink> termLinks;

    private Map<Object, Meta> meta = null;


    private List<Task> questions;
    private List<Task> quests;
    private List<Task> beliefs;
    private List<Task> goals;

    transient private final Memory memory;

    /**
     * Link templates of TermLink, only in concepts with CompoundTerm Templates
     * are used to improve the efficiency of TermLink building
     */
    private final TermLinkBuilder termLinkBuilder;
    transient private final TaskLinkBuilder taskLinkBuilder;

    private boolean constant = false;


    /**
     * Constructor, called in Memory.getConcept only
     *
     * @param term      A term corresponding to the concept
     * @param b
     * @param taskLinks
     * @param termLinks
     * @param memory    A reference to the memory
     */
    public DefaultConcept(final Term term, final Budget b, final Bag<Sentence, TaskLink> taskLinks, final Bag<Identifier, TermLink> termLinks, final Memory memory) {
        super(b);


        this.term = term;
        this.memory = memory;

        this.creationTime = memory.time();
        this.deletionTime = creationTime - 1; //set to one cycle before created meaning it was potentially reborn

        this.questions = Global.newArrayList(1);
        this.beliefs = Global.newArrayList(1);
        this.quests = Global.newArrayList(1);
        this.goals = Global.newArrayList(1);

        this.taskLinks = taskLinks;
        this.termLinks = termLinks;

        this.termLinkBuilder = new TermLinkBuilder(this);
        this.taskLinkBuilder = new TaskLinkBuilder(memory);

        this.state = State.New;
        memory.emit(Events.ConceptNew.class, this);
        if (memory.logic != null)
            memory.logic.CONCEPT_NEW.hit();


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
    public Bag<Identifier, TermLink> getTermLinks() {
        return termLinks;
    }

    /**
     * metadata table where processes can store and retrieve concept-specific data by a key. lazily allocated
     */
    public Map<Object, Meta> getMeta() {
        return meta;
    }

    public void setMeta(Map<Object, Meta> meta) {
        this.meta = meta;
    }

    /**
     * Pending Quests to be answered by new desire values
     */
    public List<Task> getQuests() {
        return quests;
    }

    /**
     * Judgments directly made about the term Use ArrayList because of access
     * and insertion in the middle
     */
    public List<Task> getBeliefs() {
        return beliefs;
    }

    /**
     * Desire values on the term, similar to the above one
     */
    public List<Task> getGoals() {
        return goals;
    }

    /**
     * Reference to the memory to which the Concept belongs
     */
    public Memory getMemory() {
        return memory;
    }


    /**
     * The term is the unique ID of the concept
     */
    public Term getTerm() {
        return term;
    }

    public boolean hasGoals() {
        final List<Task> s = getGoals();
        if (s == null) return false;
        return !s.isEmpty();
    }

    public boolean hasBeliefs() {
        final List<Task> s = getBeliefs();
        if (s == null) return false;
        return !s.isEmpty();
    }

    public boolean hasQuestions() {
        final List<Task> s = getQuestions();
        if (s == null) return false;
        return !s.isEmpty();
    }

    public boolean hasQuests() {
        final List<Task> s = getQuests();
        if (s == null) return false;
        return !s.isEmpty();
    }

    public State getState() {
        return state;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getDeletionTime() {
        return deletionTime;
    }

    /**
     * returns the same instance, used for fluency
     */
    public Concept setState(State nextState) {

        State lastState = this.state;

        if (lastState == nextState) {
            throw new RuntimeException(toInstanceString() + " already in state " + nextState);
        }

        if (nextState == State.New)
            throw new RuntimeException(toInstanceString() + " can not return to New state ");


        if (lastState == State.Deleted)
            throw new RuntimeException(toInstanceString() + " can not exit from Deleted state");


        //ok set the state ------
        switch (this.state = nextState) {


            case Forgotten:
                getMemory().emit(Events.ConceptForget.class, this);
                break;

            case Deleted:

                if (lastState == State.Active) //emit forget event if it came directly to delete
                    getMemory().emit(Events.ConceptForget.class, this);

                deletionTime = getMemory().time();
                getMemory().emit(Events.ConceptDelete.class, this);
                break;

            case Active:
                onActive();
                getMemory().emit(Events.ConceptActive.class, this);
                break;
        }

        getMemory().updateConceptState(this);

        if (getMeta() != null) {
            for (Meta m : getMeta().values()) {
                m.onState(this, getState());
            }
        }

        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Concept)) return false;
        return ((Concept) obj).name().equals(name());
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    /* ---------- direct processing of tasks ---------- */

    /**
     * Directly process a new task. Called exactly once on each task. Using
     * local information and finishing in a constant time. Provide feedback in
     * the taskBudget value of the task.
     * <p>
     * called in Memory.immediateProcess only
     *
     * @param nal  reasoning context it is being processed in
     * @param task The task to be processed
     * @return whether it was processed
     */
    public boolean process(final TaskProcess nal) {


        if (!ensureActiveFor("DirectProcess")) return false;

        final Task task = nal.getCurrentTask();

        if (!valid(task)) {
            getMemory().removed(task, "Filtered by Concept");
            return false;
        }

        char type = task.sentence.punctuation;
        switch (type) {
            case Symbols.JUDGMENT:
                if (!processJudgment(nal, task))
                    return false;

                getMemory().logic.JUDGMENT_PROCESS.hit();
                break;
            case Symbols.GOAL:
                if (!processGoal(nal, task))
                    return false;
                getMemory().logic.GOAL_PROCESS.hit();
                break;
            case Symbols.QUESTION:
            case Symbols.QUEST:
                processQuestion(nal, task);
                getMemory().logic.QUESTION_PROCESS.hit();
                break;
            default:
                throw new RuntimeException("Invalid sentence type: " + task);
        }

        return true;
    }

    /**
     * called by concept before it fires to update any pending changes
     */
    public void updateTermLinks() {

        /*
        getTermLinks().forgetNext(
                getMemory().param.termLinkForgetDurations,
                getMemory().random.nextFloat() * Global.TERMLINK_FORGETTING_ACCURACY,
                getMemory());
                */

        getTaskLinks().forgetNext(
                getMemory().param.taskLinkForgetDurations,
                getMemory().random.nextFloat() * Global.TASKLINK_FORGETTING_ACCURACY,
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

        if (!ensureActiveFor("link(Collection<Task>)")) return;

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
                if (aggregateBudget == null) aggregateBudget = new Budget(t);
                else {
                    aggregateBudget.merge(t);
                }
            }
        }

        //linkToTerms the aggregate budget, rather than each task's budget separately
        if (aggregateBudget != null) {
            linkTerms(aggregateBudget, true);
        }
    }


    /**
     * by default, any Task is valid
     */
    public boolean valid(Task t) {
        return true;
    }


    /**
     * To accept a new judgment as belief, and check for revisions and solutions
     *
     * @param judg      The judgment to be accepted
     * @param newBelief The task to be processed
     * @return Whether to continue the processing of the task
     */
    public boolean processJudgment(final TaskProcess nal, Task newBelief) {

        if (hasBeliefs() && isConstant())
            return false;


        final Task oldBelief = getTask(newBelief.sentence, getBeliefs());   // only revise with the strongest -- how about projection?

        if (oldBelief != null) {

            if (oldBelief.sentence == newBelief.sentence) {
                return false;
            }

            if (newBelief.sentence.equalStamp(oldBelief.sentence, true, false, true)) {
//                if (task.getParentTask() != null && task.getParentTask().sentence.isJudgment()) {
//                    //task.budget.decPriority(0);    // duplicated task
//                }   // else: activated belief

                getMemory().removed(newBelief, "Duplicated");
                return false;
            } else if (revisible(newBelief.sentence, oldBelief.sentence)) {
                //final long now = getMemory().time();

//                if (nal.setTheNewStamp( //temporarily removed
//                /*
//                if (equalBases(first.getBase(), second.getBase())) {
//                return null;  // do not merge identical bases
//                }
//                 */
//                //        if (first.baseLength() > second.baseLength()) {
//                new Stamp(newStamp, oldStamp, memory.time()) // keep the order for projection
//                //        } else {
//                //            return new Stamp(second, first, time);
//                //        }
//                ) != null) {

                //TaskSeed projectedBelief = oldBelief.projection(nal.memory, now, task.getOccurrenceTime());


                //Task r = oldBelief.projection(nal.memory, now, newBelief.getOccurrenceTime());

                //Truth r = oldBelief.projection(now, newBelief.getOccurrenceTime());
                /*
                if (projectedBelief.getOccurrenceTime()!=oldBelief.getOccurrenceTime()) {
                }
                */



                Task revised = tryRevision(newBelief, oldBelief, false, nal);
                if (revised != null) {
                    newBelief = revised;
                    nal.setCurrentBelief(revised);
                }

            }
        }


        /*if (task.aboveThreshold())*/
        {

            if (nal != null) {
                int nnq = getQuestions().size();
                for (int i = 0; i < nnq; i++) {
                    trySolution(newBelief, getQuestions().get(i), nal);
                }
            }


            if (!addToTable(newBelief, getBeliefs(), getMemory().param.conceptBeliefsMax.get(), Events.ConceptBeliefAdd.class, Events.ConceptBeliefRemove.class)) {
                //wasnt added to table
                getMemory().removed(newBelief, "Insufficient Rank"); //irrelevant
                return false;
            }
        }
        return true;
    }


    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it
     *
     * @param judg    The judgment to be accepted
     * @param newGoal The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected boolean processGoal(final NAL nal, Task newGoal) {
        

        final Task oldGoalT = getTask(newGoal.sentence, goals); // revise with the existing desire values
        Sentence oldGoal;



        if (oldGoalT != null) {
            oldGoal = oldGoalT.sentence;

            if (newGoal.sentence.equalStamp(oldGoal, true, true, false)) {
                return false; // duplicate
            }
            if (revisible(newGoal.sentence, oldGoal)) {

                //nal.setTheNewStamp(newStamp, oldStamp, memory.time());


                //Truth projectedTruth = oldGoal.projection(now, task.getOccurrenceTime());
                /*if (projectedGoal!=null)*/
                {
                    // if (goal.after(oldGoal, nal.memory.param.duration.get())) { //no need to project the old goal, it will be projected if selected anyway now
                    // nal.singlePremiseTask(projectedGoal, task.budget);
                    //return;
                    // }
                    //nal.setCurrentBelief(projectedGoal);

                    Task revisedTask = tryRevision(newGoal, oldGoalT, false, nal);
                    if (revisedTask != null) { // it is revised, so there is a new task for which this function will be called
                        newGoal = revisedTask;
                        //return true; // with higher/lower desire
                    } //it is not allowed to go on directly due to decision making https://groups.google.com/forum/#!topic/open-nars/lQD0no2ovx4

                    //nal.setCurrentBelief(revisedTask);
                }
            }
        }

        //long then = goal.getOccurrenceTime();
        int dur = nal.memory.duration();

//        //this task is not up to date (now is ahead of then) we have to project it first
//        if(TemporalRules.after(then, now, dur)) {
//
//            nal.singlePremiseTask(task.projection(nal.memory, now, then) );
//
//            return true;
//
//        }

        if (newGoal.summaryGreaterOrEqual(memory.param.goalThreshold)) {

            // check if the Goal is already satisfied
            Task beliefSatisfied = getTask(newGoal.sentence, beliefs);

            double AntiSatisfaction = 0.5f; //we dont know anything about that goal yet, so we pursue it to remember it because its maximally unsatisfied
            if (beliefSatisfied != null) {



                Truth projectedTruth = beliefSatisfied.projection(newGoal.getOccurrenceTime(), dur);
                //Sentence projectedBelief = belief.projectionSentence(newGoal.getOccurrenceTime(), dur);

                beliefSatisfied = trySolution(beliefSatisfied, projectedTruth, newGoal, nal); // check if the Goal is already satisfied (manipulate budget)
                if (beliefSatisfied!=null) {
                    AntiSatisfaction = newGoal.getTruth().getExpDifAbs(beliefSatisfied.truth);
                }
            }

            double Satisfaction = 1.0 - AntiSatisfaction;
            Truth T = new DefaultTruth(newGoal.getTruth());

            T.setFrequency((float) (T.getFrequency() - Satisfaction)); //decrease frequency according to satisfaction value

            if (AntiSatisfaction >= Global.SATISFACTION_TRESHOLD && newGoal.sentence.truth.getExpectation() > nal.memory.param.executionThreshold.get()) {

                questionFromGoal(newGoal, nal);

                if (!addToTable(newGoal, getGoals(), getMemory().param.conceptGoalsMax.get(), Events.ConceptGoalAdd.class, Events.ConceptGoalRemove.class)) {
                    //wasnt added to table
                    getMemory().removed(newGoal, "Insufficient Rank"); //irrelevant
                    return false;
                }

                //TODO
                //InternalExperience.experienceFromTask(nal, task, false);

                getMemory().execute(this, newGoal);
            }
        }

        return true;
    }

    final static Variable how = new Variable("?how");

    public static void questionFromGoal(final Task task, final NAL nal) {
        if (Global.QUESTION_GENERATION_ON_DECISION_MAKING || Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
            //ok, how can we achieve it? add a question of whether it is fullfilled

            ArrayList<Compound> qu = new ArrayList(3);

            if (Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
                if (!(task.sentence.term instanceof Equivalence) && !(task.sentence.term instanceof Implication)) {

                    Implication i1 = Implication.make(how, task.sentence.term, TemporalRules.ORDER_CONCURRENT);
                    if (i1 != null)
                        qu.add(i1);

                    Implication i2 = Implication.make(how, task.sentence.term, TemporalRules.ORDER_FORWARD);
                    if (i2 != null)
                        qu.add(i2);

                }
            }

            if (Global.QUESTION_GENERATION_ON_DECISION_MAKING) {
                qu.add(task.sentence.term);
            }

            if (qu.isEmpty()) return;

            TaskSeed<Compound> t = nal.newTask()
                    .question()
                    .parent(task)
                    .occurr(task.sentence.getOccurrenceTime()) //set tense of question to goal tense)
                    .budget(task.getPriority() * Global.CURIOSITY_DESIRE_PRIORITY_MUL, task.getDurability() * Global.CURIOSITY_DESIRE_DURABILITY_MUL, 1);

            for (Compound q : qu)
                nal.deriveSingle(t.term(q));
        }
    }


    /**
     * To answer a quest or question by existing beliefs
     *
     * @param n The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected void processQuestion(final TaskProcess nal, final Task n) {

        Sentence ques = n.sentence;

        List<Task> table = ques.isQuestion() ? getQuestions() : getQuests();

        if (Global.DEBUG) {
            if (n.sentence.truth != null) {
                System.err.println(n.sentence + " has non-null truth");
                System.err.println(n.getExplanation());
                throw new RuntimeException(n.sentence + " has non-null truth");
            }
        }


        boolean newQuestion = table.isEmpty();
        final int presize = table.size();

        for (final Task t : table) {

            //equality test only needs to considers parent
            // (truth==null in all cases, and term will be equal)

            if (Global.DEBUG) {
                if (!t.equalPunctuations(n))
                    throw new RuntimeException("Sentence punctuation mismatch: " + t.sentence.punctuation + " != " + n.sentence.punctuation);
            }

            if (t.equalParents(n)) {
                ques = t.sentence;
                newQuestion = false;
                break;
            }
        }

        if (!isConstant()) {

            if (newQuestion) {

                if (getMemory().answer(this, n)) {

                }

                if (table.size() + 1 > getMemory().param.conceptQuestionsMax.get()) {
                    Task removed = table.remove(0);    // FIFO
                    getMemory().event.emit(Events.ConceptQuestionRemove.class, this, removed, n);
                }

                table.add(n);
                getMemory().event.emit(Events.ConceptQuestionAdd.class, this, n);

            }

            onTableUpdated(n.getPunctuation(), presize);
        }


        if (ques.isQuest()) {
            trySolution(getTask(ques, getGoals()), n, nal);
        } else {
            trySolution(getTask(ques, getBeliefs()), n, nal);
        }
    }

    /**
     * Add a new belief (or goal) into the table Sort the beliefs/goals by
     * rank, and remove redundant or low rank one
     *
     * @param newSentence The judgment to be processed
     * @param table       The table to be revised
     * @param capacity    The capacity of the table
     * @return whether table was modified
     */
    public Task addToTable(final Memory memory, final Task newSentence, final List<Task> table, final int capacity) {

        if (!ensureActiveFor("addToTable")) return null;

        long now = memory.time();

        float rank1 = rankBelief(newSentence, now);    // for the new isBelief

        float rank2;
        int i;

        //int originalSize = table.size();


        //TODO decide if it's better to iterate from bottom up, to find the most accurate replacement index rather than top
        for (i = 0; i < table.size(); i++) {
            Sentence existingSentence = table.get(i).sentence;

            rank2 = rankBelief(existingSentence, now);

            if (rank1 >= rank2) {
                if (newSentence.sentence.equivalentTo(existingSentence, false, false, true, true, false)) {
                    //System.out.println(" ---------- Equivalent Belief: " + newSentence + " == " + judgment2);
                    return newSentence;
                }
                table.add(i, newSentence);
                break;
            }
        }

        if (table.size() == capacity) {
            // no change
            return null;
        }

        Task removed = null;

        final int ts = table.size();
        if (ts > capacity) {
            removed = table.remove(ts - 1);
        } else if (i == table.size()) { // branch implies implicit table.size() < capacity
            table.add(newSentence);
            //removed = nothing
        }

        return removed;
    }

    protected void onTableUpdated(char punctuation, int originalSize) {
        if (!isActive()) return;

        switch (punctuation) {
            /*case Symbols.GOAL:
                break;*/
            case Symbols.QUESTION:
                if (getQuestions().isEmpty()) {
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

    protected boolean addToTable(final Task goalOrJudgment, final List<Task> table, final int max, final Class eventAdd, final Class eventRemove) {
        int preSize = table.size();

        Task removed = addToTable(getMemory(), goalOrJudgment, table, max);

        onTableUpdated(goalOrJudgment.getPunctuation(), preSize);

        if (removed != null) {
            if (removed == goalOrJudgment) return false;

            getMemory().event.emit(eventRemove, this, removed.sentence, goalOrJudgment.sentence);

            if (preSize != table.size()) {
                getMemory().event.emit(eventAdd, this, goalOrJudgment.sentence);
            }
        }

        return true;
    }

    /**
     * Select a belief value or desire value for a given query
     *
     * @param query The query to be processed
     * @param list  The list of beliefs or goals to be used
     * @return The best candidate selected
     */
    @Override
    public Task getTask(final Sentence query, final List<Task>... lists) {
        float currentBest = 0;
        float beliefQuality;
        Task candidate = null;

        final long now = getMemory().time();
        for (List<Task> list : lists) {
            if (list.isEmpty()) continue;

            int lsv = list.size();
            for (int i = 0; i < lsv; i++) {
                Task judg = list.get(i);
                beliefQuality = solutionQuality(query, judg.sentence, now);
                if (beliefQuality > currentBest) {
                    currentBest = beliefQuality;
                    candidate = judg;
                }
            }
        }

        return candidate;
    }
    @Override
    public Task getTask(boolean hasQueryVar, long occTime, Truth truth, final List<Task>... lists) {
        float currentBest = 0;
        float beliefQuality;
        Task candidate = null;

        final long now = getMemory().time();
        for (List<Task> list : lists) {
            if (list.isEmpty()) continue;

            int lsv = list.size();
            for (int i = 0; i < lsv; i++) {
                Task judg = list.get(i);
                beliefQuality = solutionQuality(hasQueryVar, occTime, judg.sentence, truth, now);
                if (beliefQuality > currentBest) {
                    currentBest = beliefQuality;
                    candidate = judg;
                }
            }
        }

        return candidate;
    }

    public Sentence getSentence(final Sentence query, final List<Task>... lists) {
        Task t = getTask(query, lists);
        if (t == null) return null;
        return t.sentence;
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

        taskLinkBuilder.setBudget(subBudget);

        for (int i = 0; i < numTemplates; i++) {
            TermLinkTemplate termLink = templates.get(i);

            //if (!(task.isStructural() && (termLink.getType() == TermLink.TRANSFORM))) { // avoid circular transform

            Term componentTerm = termLink.target;
            if (componentTerm.equals(getTerm())) // avoid circular transform
                continue;

            Concept componentConcept = getMemory().conceptualize(subBudget, componentTerm);

            if (componentConcept != null) {

                taskLinkBuilder.setTemplate(termLink);

                /** activate the task tlink */
                componentConcept.activateTaskLink(taskLinkBuilder);
            } else {
                //taskBudgetBalance += subBudget.getPriority();
            }

        }

        return true;
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

    @Override
    public Term name() {
        return getTerm();
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

                TermLinkTemplate template = templates.get(i);

                //only apply this loop to non-transform termlink templates
                if (template.type != TermLink.TRANSFORM) {
                    if (linkTerm(template, subPriority, dur, qua, updateTLinks))
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
        return linkTerm(template, 0, 0, 0, updateTLinks);
    }

    boolean linkTerm(TermLinkTemplate template, float priInc, float durInc, float quaInc, boolean updateTLinks) {

        final Budget b = template;
        b.accumulate(priInc, durInc, quaInc);
        if (!updateTLinks) {
            return false;
        }

        Term otherTerm = termLinkBuilder.budget(template).getOther();

        Concept otherConcept = getMemory().conceptualize(b, otherTerm);
        if (otherConcept == null) {
            return false;
        }


        activateTermLink(termLinkBuilder.setIncoming(false));  // this concept termLink to that concept
        otherConcept.activateTermLink(termLinkBuilder.setIncoming(true)); // that concept termLink to this concept

        if (otherTerm instanceof Compound) {
            otherConcept.linkTerms(termLinkBuilder.getBudgetRef(), false);
        }

        //spent
        b.zero();

        return true;
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


    /**
     * Determine the rank of a judgment by its quality and originality (stamp
     * baseLength), called from Concept
     *
     * @param s The judgment to be ranked
     * @return The rank of the judgment, according to truth value only
     */
    public float rankBelief(final Sentence s, final long now) {
        return rankBeliefOriginal(s);
    }


    public static float rankBeliefOriginal(final Sentence judg) {
        final float confidence = judg.truth.getConfidence();
        final float originality = judg.getOriginality();
        return or(confidence, originality);
    }


    /**
     * Return a string representation of the concept, called in ConceptBag only
     *
     * @return The concept name, with taskBudget in the full version
     */
    @Override
    public String toString() {  // called from concept bag
        //return (super.toStringBrief() + " " + key);
        //return super.toStringExternal();
        return getTerm().toString();
    }

    /**
     * called from {@link NARRun}
     */
    @Override
    public String toStringLong() {
        String res =
                toStringWithBudget() + " " + getTerm().name()
                        + toStringIfNotNull(getTermLinks().size(), "termLinks")
                        + toStringIfNotNull(getTaskLinks().size(), "taskLinks")
                        + toStringIfNotNull(getBeliefs().size(), "beliefs")
                        + toStringIfNotNull(getGoals().size(), "goals")
                        + toStringIfNotNull(getQuestions().size(), "questions")
                        + toStringIfNotNull(getQuests().size(), "quests");

        //+ toStringIfNotNull(null, "questions");
        /*for (Task t : questions) {
            res += t.toString();
        }*/
        // TODO other details?
        return res;
    }

    private String toStringIfNotNull(final Object item, final String title) {
        if (item == null) {
            return "";
        }

        final String itemString = item.toString();

        return new StringBuilder(2 + title.length() + itemString.length() + 1).
                append(' ').append(title).append(':').append(itemString).toString();
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


    @Override public void delete() {

        if (getMemory().inCycle())
            throw new RuntimeException("concept " + this + " attempt to delete() during an active cycle; must be done between cycles");

        if (isDeleted()) return;

        //called first to allow listeners to have a final attempt to interact with this concept before it dies
        setState(State.Deleted);

        {
            //completely remove from active bags and concept indexes

            memory.concepts.remove(getTerm());
            Concept removed = memory.cycle.remove(this);
            if (removed!=null && removed != this) {
                throw new RuntimeException("Duplicate instances of Concepts with same term: " + this + " (deleting) " + removed);
            }
        }

        zero();

        super.delete();

        //dont delete the tasks themselves because they may be referenced from othe concepts.
        beliefs.clear();
        goals.clear();
        questions.clear();
        quests.clear();


        if (getMeta() != null) {
            getMeta().clear();
            setMeta(null);
        }

        getTermLinks().delete();
        getTaskLinks().delete();

        if (termLinkBuilder != null)
            termLinkBuilder.delete();
    }
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
    public List<Task> getQuestions() {
        return questions;
    }


    /**
     * get a random belief, weighted by their sentences confidences
     */
    public Sentence getBeliefRandomByConfidence(boolean eternal) {

        if (getBeliefs().isEmpty()) return null;

        float totalConfidence = getConfidenceSum(getBeliefs());
        float r = getMemory().random.nextFloat() * totalConfidence;

        Sentence s = null;
        for (int i = 0; i < getBeliefs().size(); i++) {
            s = getBeliefs().get(i).sentence;
            r -= s.truth.getConfidence();
            if (r < 0)
                return s;
        }

        return s;
    }


    public static float getConfidenceSum(Iterable<? extends Truthed> beliefs) {
        float t = 0;
        for (final Truthed s : beliefs)
            t += s.getTruth().getConfidence();
        return t;
    }

    public static float getMeanFrequency(Collection<? extends Truthed> beliefs) {
        if (beliefs.isEmpty()) return 0.5f;

        float t = 0;
        for (final Truthed s : beliefs)
            t += s.getTruth().getFrequency();
        return t / beliefs.size();
    }

    @Override
    public boolean isConstant() {
        return constant;
    }

    @Override
    public boolean setConstant(boolean b) {
        this.constant = b;
        return constant;
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

}
