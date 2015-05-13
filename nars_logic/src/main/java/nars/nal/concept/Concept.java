/*
 * Concept.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.nal.concept;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import nars.Events;
import nars.Events.*;
import nars.Global;
import nars.Memory;
import nars.Memory.MemoryAware;
import nars.Symbols;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.nal.*;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.stamp.Stamp;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.term.Termed;
import nars.nal.term.Variable;
import nars.nal.tlink.*;
import nars.op.mental.InternalExperience;

import java.io.PrintStream;
import java.util.*;

import static com.google.common.collect.Iterators.*;
import static nars.budget.BudgetFunctions.divide;
import static nars.nal.UtilityFunctions.or;
import static nars.nal.nal1.LocalRules.*;
import static nars.nal.nal7.TemporalRules.solutionQuality;

abstract public class Concept extends Item<Term> implements Termed {






    public enum State {

        /** created but not added to memory */
        New,

        /** in memory */
        Active,

        /** in sub-concepts */
        Forgotten,

        /** unrecoverable, will be garbage collected eventually */
        Deleted
    }

    State state;
    final long creationTime;
    long deletionTime;

    /**
     * The term is the unique ID of the concept
     */
    public final Term term;

    /**
     * Task links for indirect processing
     */
    public final Bag<Sentence, TaskLink> taskLinks;

    /**
     * Term links between the term and its components and compounds; beliefs
     */
    public final Bag<TermLinkKey, TermLink> termLinks;

    /** metadata table where processes can store and retrieve concept-specific data by a key. lazily allocated */
    protected Map<Object,Meta> meta = null;


    /**
     * Pending Question directly asked about the term
     *
     * Note: since this is iterated frequently, an array should be used. To
     * avoid iterator allocation, use .get(n) in a for-loop
     */
    public final List<Task> questions;

    
    /**
     * Pending Quests to be answered by new desire values
     */
    public final List<Task> quests;

    /**
     * Judgments directly made about the term Use ArrayList because of access
     * and insertion in the middle
     */
    public final List<Task> beliefs;



    /**
     * Desire values on the term, similar to the above one
     */
    public final List<Task> goals;

    /**
     * Reference to the memory to which the Concept belongs
     */
    public final Memory memory;

    /**
     * Link templates of TermLink, only in concepts with CompoundTerm Templates
     * are used to improve the efficiency of TermLink building
     */
    private final TermLinkBuilder termLinkBuilder;
    private final TaskLinkBuilder taskLinkBuilder;


    /** parameter to experiment with */
    private static boolean linkPendingEveryCycle = false;



    ///** remaining unspent budget from previous cycle can be accumulated */
    /*float taskBudgetBalance = 0;
    float termBudgetBalance = 0;*/


    /* ---------- constructor and initialization ---------- */
    /**
     * Constructor, called in Memory.getConcept only
     * @param term A term corresponding to the concept
     * @param memory A reference to the memory
     */
    public Concept(final Term term, final Budget b, final Bag<Sentence, TaskLink> taskLinks, final Bag<TermLinkKey, TermLink> termLinks, final Memory memory) {
        super(b);        

        this.state = State.New;

        this.term = term;
        this.memory = memory;

        this.creationTime = memory.time();
        this.deletionTime = creationTime - 1; //set to one cycle before created meaning it was potentially reborn

        this.questions = Global.newArrayList();
        this.beliefs = Global.newArrayList();
        this.quests = Global.newArrayList();
        this.goals = Global.newArrayList();

        this.taskLinks = taskLinks;
        this.termLinks = termLinks;

        if (taskLinks instanceof MemoryAware)  ((MemoryAware)taskLinks).setMemory(memory);
        if (termLinks instanceof MemoryAware)  ((MemoryAware)termLinks).setMemory(memory);
                
        this.termLinkBuilder = new TermLinkBuilder(this);
        this.taskLinkBuilder = new TaskLinkBuilder(memory);

        memory.emit(Events.ConceptNew.class, this);
        if (memory.logic!=null)
            memory.logic.CONCEPT_NEW.hit();


    }

    public String toInstanceString() {
        String id = Integer.toString(System.identityHashCode(this), 16);
        return this + "::" + id + "." + getState().toString().toLowerCase();
    }


    /** like Map.put for storing data in meta map
     *  @param value if null will perform a removal
     * */
    public Meta put(Object key, Meta value) {
        if (meta == null) meta = Global.newHashMap();

        if (value != null) {
            Meta removed = meta.put(key, value);
            if (removed!=value) {
                value.onState(this, getState());
            }
            return removed;
        }
        else
            return meta.remove(key);
    }

    /** like Map.gett for getting data stored in meta map */
    public <C extends Meta> C get(Object key) {
        if (meta == null) return null;
        return (C) meta.get(key);
    }

    public boolean hasGoals() {
        return !goals.isEmpty();
    }
    public boolean hasBeliefs() {
        return !beliefs.isEmpty();
    }
    public boolean hasQuestions() {
        return !questions.isEmpty();
    }
    public boolean hasQuests() {
        return !quests.isEmpty();
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

    public boolean isDeleted() {
        return getState() == Concept.State.Deleted;
    }

    public boolean isActive() {
        return getState() == State.Active;
    }
    public boolean isForgotten() {
        return getState() == State.Forgotten;
    }

    /** returns the same instance, used for fluency */
    public Concept setState(State nextState) {

        if (nextState == State.New)
            throw new RuntimeException(toInstanceString() + " can not return to New state ");

        State lastState = this.state;

        if (lastState == State.Deleted)
            throw new RuntimeException(toInstanceString() + " can not exit from Deleted state");

        if (lastState == nextState)
            throw new RuntimeException(this + " already in state " + nextState);


        this.state = nextState;

        //ok set the state ------
        switch (this.state) {


            case Forgotten:
                memory.emit(Events.ConceptForget.class, this);
                break;

            case Deleted:

                if (lastState==State.Active) //emit forget event if it came directly to delete
                    memory.emit(Events.ConceptForget.class, this);

                deletionTime = memory.time();
                memory.emit(Events.ConceptDelete.class, this);
                break;

            case Active:
                onActive();
                memory.emit(ConceptActive.class, this);
                break;
        }

        memory.updateConceptState(this);

        if (meta!=null) {
            for (Meta m : meta.values()) {
                m.onState(this, getState());
            }
        }

        return this;
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Concept)) return false;
        return ((Concept)obj).name().equals(name());
    }

    @Override public int hashCode() { return name().hashCode();     }

    @Override
    public Term name() {
        return term;
    }

    public boolean ensureActiveTo(String activity) {
        if (!this.isActive()) {
            System.err.println(activity + " fail: " + this + " (state=" + getState() + ')');
            new Exception().printStackTrace();
            return false;
        }
        return true;
    }

    /* ---------- direct processing of tasks ---------- */
    /**
     * Directly process a new task. Called exactly once on each task. Using
     * local information and finishing in a constant time. Provide feedback in
     * the taskBudget value of the task.
     * <p>
     * called in Memory.immediateProcess only
     *
     * @param nal reasoning context it is being processed in
     * @param task The task to be processed
     * @return whether it was processed
     */
    public boolean process(final DirectProcess nal) {

        if (!ensureActiveTo("DirectProcess")) return false;

        final Task task = nal.getCurrentTask();

        if (!valid(task)) {
            memory.removed(task, "Filtered by Concept");
            return false;
        }

        char type = task.sentence.punctuation;
        switch (type) {
            case Symbols.JUDGMENT:
                if (!processJudgment(nal, task))
                    return false;

                memory.logic.JUDGMENT_PROCESS.hit();
                break;
            case Symbols.GOAL:
                if (!processGoal(nal, task))
                    return false;
                memory.logic.GOAL_PROCESS.hit();
                break;
            case Symbols.QUESTION:
            case Symbols.QUEST:
                processQuestion(nal, task);
                memory.logic.QUESTION_PROCESS.hit();
                break;
            default:
                throw new RuntimeException("Invalid sentence type");
        }

        return true;
    }

    /** called by concept before it fires to update any pending changes */
    public void updateTermLinks() {

        termLinks.forgetNext(
                memory.param.termLinkForgetDurations,
                Global.TERMLINK_FORGETTING_ACCURACY,
                memory);

        taskLinks.forgetNext(
                memory.param.taskLinkForgetDurations,
                Global.TASKLINK_FORGETTING_ACCURACY,
                memory);

        linkTerms(null, true);

    }

    public boolean link(Task t) {
        if (linkTask(t))
            return linkTerms(t, true);  // recursively insert TermLink
        return false;
    }

    /** for batch processing a collection of tasks; more efficient than processing them individually */
    //TODO untested
    public void link(Collection<Task> tasks) {

        if (!ensureActiveTo("link(Collection<Task>)")) return;

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
        if (aggregateBudget!=null) {
            linkTerms(aggregateBudget, true);
        }
    }


    /**
     * whether a concept's desire exceeds decision threshold
     */
    public boolean isDesired() {
        return isDesired(memory.param.decisionThreshold.floatValue());
    }

    public boolean isDesired(float threshold) {
        Truth desire=this.getDesire();
        if(desire==null) {
            return false;
        }
        return desire.getExpectation() > threshold;
    }


    /** by default, any Task is valid */
    public boolean valid(Task t) {
        return true;
    }


    /**
     * To accept a new judgment as belief, and check for revisions and solutions
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected boolean processJudgment(final DirectProcess nal, final Task task) {
        final Sentence judg = task.sentence;
        final Sentence oldBelief;

        oldBelief = getSentence(judg, beliefs);   // only revise with the strongest -- how about projection?

        if ((oldBelief != null) && (oldBelief!=judg)) {
            final Stamp newStamp = judg.stamp;
            final Stamp oldStamp = oldBelief.stamp;
            if (newStamp.equals(oldStamp, true, true, false, true)) {
//                if (task.getParentTask() != null && task.getParentTask().sentence.isJudgment()) {
//                    //task.budget.decPriority(0);    // duplicated task
//                }   // else: activated belief

                memory.removed(task, "Duplicated");
                return false;
            } else if (revisible(judg, oldBelief)) {
                final long now = memory.time();

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

                Sentence projectedBelief = oldBelief.projection(newStamp.getOccurrenceTime(), now);
                if (projectedBelief!=null) {

                    /*
                    if (projectedBelief.getOccurrenceTime()!=oldBelief.getOccurrenceTime()) {
                        nal.singlePremiseTask(projectedBelief, task.budget);
                    }
                    */

                    nal.setCurrentBelief(null);

                    if (revision(judg, projectedBelief, false, nal))
                        return false;
                }

            }
        }

        /*if (task.aboveThreshold())*/ {
            int nnq = questions.size();
            for (int i = 0; i < nnq; i++) {
                trySolution(judg, questions.get(i), nal);
            }


            if (!addToTable(task, beliefs, memory.param.conceptBeliefsMax.get(), ConceptBeliefAdd.class, ConceptBeliefRemove.class)) {
                //wasnt added to table
                memory.removed(task, "Insufficient Rank"); //irrelevant
                return false;
            }
        }
        return true;
    }




    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected boolean processGoal(final DirectProcess nal, final Task task) {


        final Sentence goal = task.sentence, oldGoal;

        oldGoal = getSentence(goal, goals); // revise with the existing desire values

        if (oldGoal != null) {
            final Stamp newStamp = goal.stamp;
            final Stamp oldStamp = oldGoal.stamp;



            if (newStamp.equals(oldStamp, true, true, false, true)) {
                memory.removed(task, "Duplicated");
                return false; // duplicate
            } else if (revisible(goal, oldGoal)) {

                Sentence projectedGoal = oldGoal.projection(newStamp.getOccurrenceTime(), memory.time());
                if (projectedGoal!=null) {

                    /*
                    if (projectedGoal.getOccurrenceTime()!=oldGoal.getOccurrenceTime()) {
                        nal.singlePremiseTask(projectedGoal, task.budget);
                    }
                    */

                    nal.setCurrentBelief(null);

                    boolean revisionSucceeded = revision(goal, projectedGoal, false, nal);
                    if(revisionSucceeded) {
                        // it is revised, so there is a new task for which this function will be called
                        memory.removed(task, "Revised");
                        return false; // with higher/lower desire
                    }
                }
            } 
        }


        double AntiSatisfaction = 0.5f; //we dont know anything about that goal yet, so we pursue it to remember it because its maximally unsatisfied
        Sentence projectedGoal = task.sentence.projection(task.sentence.getOccurrenceTime(), memory.time());


        Sentence sol = getSentence(goal, beliefs);
        if (sol!=null) {
            // check if the Goal is already satisfied

            trySolution(sol, task, nal);
            Sentence projectedBelief = sol.projection(sol.getOccurrenceTime(), memory.time());
            AntiSatisfaction = task.sentence.truth.getExpDifAbs(projectedBelief.truth);
        }

        double Satisfaction=1.0-AntiSatisfaction;
        Truth T=projectedGoal.truth.clone();
        T.setFrequency((float) (T.getFrequency()-Satisfaction)); //decrease frequency according to satisfaction value


        // still worth pursuing?
        if (!task.aboveThreshold()) {
            return false;
        }


        questionFromGoal(task, nal);



        if (!addToTable(task, goals, memory.param.conceptGoalsMax.get(), ConceptGoalAdd.class, ConceptGoalRemove.class)) {
            //wasnt added to table
            memory.removed(task, "Insufficient Rank"); //irrelevant
            return false;
        }

        //TODO
        //InternalExperience.InternalExperienceFromTask(memory, task, false);

        memory.decide(this, task);

        return true;
    }

    private void questionFromGoal(final Task task, final NAL nal) {
        if(Global.QUESTION_GENERATION_ON_DECISION_MAKING || Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
            //ok, how can we achieve it? add a question of whether it is fullfilled
            ArrayList<Term> qu=new ArrayList<Term>();
            if(Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
                if(!(task.sentence.term instanceof Equivalence) && !(task.sentence.term instanceof Implication)) {
                    Variable how=new Variable("?how");
                    Implication imp=Implication.make(how, task.sentence.term, TemporalRules.ORDER_CONCURRENT);
                    Implication imp2=Implication.make(how, task.sentence.term, TemporalRules.ORDER_FORWARD);
                    qu.add(imp);
                    qu.add(imp2);
                }
            }
            if(Global.QUESTION_GENERATION_ON_DECISION_MAKING) {
                qu.add(task.sentence.term);
            }
            for(Term q : qu) {
                if(q!=null) {
                    NAL.StampBuilder st = nal.newStamp(task.sentence, nal.time());
                    st.setOccurrenceTime(task.sentence.getOccurrenceTime()); //set tense of question to goal tense
                    Sentence s=new Sentence(q,Symbols.QUESTION,null,st);
                    if(s!=null) {
                        Budget budget=new Budget(task.getPriority()*Global.CURIOSITY_DESIRE_PRIORITY_MUL,task.getDurability()*Global.CURIOSITY_DESIRE_DURABILITY_MUL,1);
                        nal.singlePremiseTask(s, budget);
                    }
                }
            }
        }
    }

    /**
     * To answer a quest or question by existing beliefs
     *
     * @param newTask The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected void processQuestion(final DirectProcess nal, final Task newTask) {

        Sentence ques = newTask.sentence;

        List<Task> table = ques.isQuestion() ? questions : quests;

        if (Global.DEBUG) {
            if (newTask.sentence.truth!=null) {
                System.err.println(newTask.sentence + " has non-null truth");
                System.err.println(newTask.getExplanation());
                throw new RuntimeException(newTask.sentence + " has non-null truth");
            }
        }


        boolean newQuestion = table.isEmpty();
        final int presize = table.size();

        for (final Task t : table) {

            //equality test only needs to considers parent
            // (truth==null in all cases, and term will be equal)

            if (Global.DEBUG) {
                if (!t.equalPunctuations(newTask))
                    throw new RuntimeException("Sentence punctuation mismatch: " + t.sentence.punctuation + " != " + newTask.sentence.punctuation);
            }

            if (t.equalParents(newTask)) {
                ques = t.sentence;
                newQuestion = false;
                break;
            }
        }

        if (newQuestion) {
            if (table.size() + 1 > memory.param.conceptQuestionsMax.get()) {
                Task removed = table.remove(0);    // FIFO
                memory.event.emit(ConceptQuestionRemove.class, this, removed, newTask);
            }

            table.add(newTask);
            memory.event.emit(ConceptQuestionAdd.class, this, newTask);
        }

        onTableUpdated(newTask.getPunctuation(), presize);


        if (ques.isQuest()) {
            trySolution(getSentence(ques, goals), newTask, nal);
        }
        else {
            trySolution(getSentence(ques, beliefs), newTask, nal);
        }
    }

    /**
     * Add a new belief (or goal) into the table Sort the beliefs/desires by
     * rank, and remove redundant or low rank one
     *
     * @param newSentence The judgment to be processed
     * @param table The table to be revised
     * @param capacity The capacity of the table
     * @return whether table was modified
     */
    public Task addToTable(final Memory memory, final Task newSentence, final List<Task> table, final int capacity) {

        if (!ensureActiveTo("addToTable")) return null;

        long now = memory.time();

        float rank1 = rankBelief(newSentence, now);    // for the new isBelief

        float rank2;
        int i;

        int originalSize = table.size();



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
        }
        else if (i == table.size()) { // branch implies implicit table.size() < capacity
            table.add(newSentence);
            //removed = nothing
        }

        return removed;
    }

    protected void onTableUpdated(char punctuation, int originalSize) {
        switch (punctuation) {
            /*case Symbols.GOAL:
                break;*/
            case Symbols.QUESTION:
                if (questions.isEmpty()) {
                    if (originalSize > 0) //became empty
                        memory.updateConceptQuestions(this);
                } else {
                    if (originalSize == 0) { //became non-empty
                        memory.updateConceptQuestions(this);
                    }
                }
                break;
        }
    }

    protected boolean addToTable(final Task goalOrJudgment, final List<Task> table, final int max, final Class eventAdd, final Class eventRemove) {
        int preSize = table.size();

        Task removed = addToTable(memory, goalOrJudgment, table, max);

        onTableUpdated(goalOrJudgment.getPunctuation(), preSize);

        if (removed != null) {
            if (removed == goalOrJudgment) return false;

            memory.event.emit(eventRemove, this, removed.sentence, goalOrJudgment.sentence);

            if (preSize != table.size()) {
                memory.event.emit(eventAdd, this, goalOrJudgment.sentence);
            }
        }

        return true;
    }

    /**
     * Select a belief value or desire value for a given query
     *
     * @param query The query to be processed
     * @param list The list of beliefs or desires to be used
     * @return The best candidate selected
     */
    public Sentence getSentence(final Sentence query, final List<Task>... lists) {
        float currentBest = 0;
        float beliefQuality;
        Sentence candidate = null;

        final long now = memory.time();
        for (List<Task> list : lists) {
            int lsv = list.size();
            for (int i = 0; i < lsv; i++) {
                Sentence judg = list.get(i).sentence;
                beliefQuality = solutionQuality(query, judg, now);
                if (beliefQuality > currentBest) {
                    currentBest = beliefQuality;
                    candidate = judg;
                }
            }
        }

        return candidate;
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

        if (!isActive()) return false;

        Budget taskBudget = task;
        taskLinkBuilder.setTemplate(null);
        taskLinkBuilder.setTask(task);

        taskLinkBuilder.setBudget(taskBudget);
        activateTaskLink(taskLinkBuilder);  // tlink type: SELF


        List<TermLinkTemplate> templates = termLinkBuilder.templates();

        if (templates == null || templates.isEmpty()) {
            //distribute budget to incoming termlinks?
            return false;
        }


        //TODO parameter to use linear division, conserving total budget
        //float linkSubBudgetDivisor = (float)Math.sqrt(termLinkTemplates.size());
        final int numTemplates = templates.size();


        //float linkSubBudgetDivisor = (float)numTemplates;
        float linkSubBudgetDivisor = (float)Math.sqrt(numTemplates);



        final Budget subBudget = divide(taskBudget, linkSubBudgetDivisor);
        if (!subBudget.aboveThreshold()) {
            //unused
            //taskBudgetBalance += taskBudget.getPriority();
            return false;
        }

        taskLinkBuilder.setBudget(subBudget);

        for (int i = 0; i < numTemplates; i++) {
            TermLinkTemplate termLink = templates.get(i);

            //if (!(task.isStructural() && (termLink.getType() == TermLink.TRANSFORM))) { // avoid circular transform

            Term componentTerm = termLink.target;
            if (componentTerm.equals(term)) // avoid circular transform
                continue;

            Concept componentConcept = memory.conceptualize(subBudget, componentTerm);

            if (componentConcept != null) {

                taskLinkBuilder.setTemplate(termLink);

                /** activate the task tlink */
                componentConcept.activateTaskLink(taskLinkBuilder);
            }

            else {
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
    protected TaskLink activateTaskLink(final TaskLinkBuilder taskLink) {
        TaskLink t = taskLinks.update(taskLink);
        return t;
    }



    /**
     * Recursively build TermLinks between a compound and its components
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskBudget The BudgetValue of the task
     * @param updateTLinks true: causes update of actual termlink bag, false: just queues the activation for future application.  should be true if this concept calls it for itself, not for another concept
     * @return whether any activity happened as a result of this invocation
     */
    protected boolean linkTerms(final Budget taskBudget, boolean updateTLinks) {

        //if (!ensureActiveTo("linkTerms")) return false;

        final float subPriority;
        int recipients = termLinkBuilder.getNonTransforms();
        if (recipients == 0) {
            //termBudgetBalance += subBudget;
            //subBudget = 0;
            //return false;
        }

        float dur = 0, qua = 0;
        if (taskBudget !=null) {
            //TODO make this parameterizable

            //float linkSubBudgetDivisor = (float)Math.sqrt(recipients);

            //half of each subBudget is spent on this concept and the other concept's termlink
            //subBudget = taskBudget.getPriority() * (1f / (2 * recipients));

            subPriority = taskBudget.getPriority() * (1f / (float) Math.sqrt(recipients));
            dur = taskBudget.getDurability();
            qua = taskBudget.getQuality();
        }
        else {
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


        List<TermLinkTemplate> tl = getTermLinkTemplates();
        if (tl!=null && (updateTLinks || linkPendingEveryCycle )) {
            int n = tl.size();
            for (int i = 0; i < n; i++) {
                TermLinkTemplate t = getTermLinkTemplates().get(i);
                if (t.pending.aboveThreshold()) {
                    if (linkTerm(t, t.pending, updateTLinks))
                        activity = true;

                    t.pending.set(0,0,0); //reset having spent it
                }
            }
        }


        return activity;
    }

    boolean linkTerm(TermLinkTemplate template, Budget b, boolean updateTLinks) {
        return linkTerm(template, b.getPriority(), b.getDurability(), b.getQuality(), updateTLinks);
    }

    boolean linkTerm(TermLinkTemplate template, float priority, float durability, float quality, boolean updateTLinks) {


        Term otherTerm = termLinkBuilder.set(template).getOther();


        Budget b = template.pending;
        if (b!=null) {
            b.setPriority(b.getPriority() + priority);
            b.setDurability(Math.max(b.getDurability(), durability));
            b.setQuality(Math.max(b.getQuality(), quality));
            if (!b.aboveThreshold()) {
                accumulate(template, b);
                return false;
            }
        }
        else {
            if (priority > 0)
                b = new Budget(priority, durability, quality);
        }

        if (b == null) return false;

        Concept otherConcept = memory.conceptualize(b, otherTerm);
        if (otherConcept == null) {
            accumulate(template, b);
            return false;
        }

        termLinkBuilder.set(b);

        if (updateTLinks) {
            activateTermLink(termLinkBuilder.setIncoming(false));  // this concept termLink to that concept
            otherConcept.activateTermLink(termLinkBuilder.setIncoming(true)); // that concept termLink to this concept
        }
        else {
            accumulate(template, b);
        }

        if (otherTerm instanceof Compound) {
            otherConcept.linkTerms(termLinkBuilder.getBudgetRef(), false);
        }

        return true;
    }

    protected void accumulate(TermLinkTemplate template, Budget added) {
        template.pending.accumulate(added);
    }


    /**
     * Insert a new or activate an existing TermLink in the TermLink bag
     * via a caching TermLinkSelector which has been configured for the
     * target Concept and the current budget
     *
     * called from buildTermLinks only
     *
     * If the tlink already exists, the budgets will be merged
     *
     * @param termLink The termLink to be inserted
     * @return the termlink which was selected or updated
     * */
    public TermLink activateTermLink(final TermLinkBuilder termLink) {

        return termLinks.update(termLink);


    }


    /**
     * Determine the rank of a judgment by its quality and originality (stamp
     baseLength), called from Concept
     *
     * @param s The judgment to be ranked
     * @return The rank of the judgment, according to truth value only
     */
    public float rankBelief(final Sentence s, final long now) {
        return rankBeliefOriginal(s);
    }
    public float rankBelief(final Task s, final long now) {
        return rankBelief(s.sentence, now);
    }



    public static float rankBeliefOriginal(final Sentence judg) {
        final float confidence = judg.truth.getConfidence();
        final float originality = judg.stamp.getOriginality();
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
        return term.toString();
    }

    /**
     * called from {@link NARRun}
     */
    @Override
    public String toStringLong() {
        String res = 
                toStringWithBudget() + " " + term.name()
                + toStringIfNotNull(termLinks.size(), "termLinks")
                + toStringIfNotNull(taskLinks.size(), "taskLinks")
                + toStringIfNotNull(beliefs.size(), "beliefs")
                + toStringIfNotNull(goals.size(), "goals")
                + toStringIfNotNull(questions.size(), "questions")
                + toStringIfNotNull(quests.size(), "quests");
        
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



    /**
     * Select a belief to interact with the given task in logic
     * <p>
     * get the first qualified one
     * <p>
     * only called in RuleTables.rule
     *
     * @param task The selected task
     * @return The selected isBelief
     */
    public Sentence getBelief(final NAL nal, final Task task) {
        final Stamp taskStamp = task.sentence.stamp;
        final long currentTime = memory.time();
        long occurrenceTime = taskStamp.getOccurrenceTime();

        final int b = beliefs.size();
        for (int i = 0; i < b; i++) {
            Sentence belief = beliefs.get(i).sentence;

            //if (task.sentence.isEternal() && belief.isEternal()) return belief;

            Sentence projectedBelief = belief.projection(occurrenceTime, currentTime);
            if (projectedBelief.getOccurrenceTime()!=belief.getOccurrenceTime()) {
                nal.singlePremiseTask(projectedBelief, task);
            }
            
            return projectedBelief;     // return the first satisfying belief
        }
        return null;
    }

    /**
     * Get the current overall desire value. TODO to be refined
     */
    public Truth getDesire() {
        if (goals.isEmpty()) {
            return null;
        }
        Truth topValue = goals.get(0).getTruth();
        return topValue;
    }


    @Override public void delete() {

        if (isDeleted()) return;

        zero();

        //called first to allow listeners to have a final attempt to interact with this concept before it dies
        setState(State.Deleted);

        super.delete();

        //dont delete the tasks themselves because they may be referenced from othe concepts.
        questions.clear();
        quests.clear();

        goals.clear();
        beliefs.clear();

        if (meta!=null) {
            meta.clear();
            meta = null;
        }

        termLinks.delete();
        taskLinks.delete();

        if (termLinkBuilder != null)
            termLinkBuilder.delete();
    }
//
//
//    /**
//     * Collect direct isBelief, questions, and desires for display
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

    /** returns the best belief of the specified types */
    public Sentence getStrongestBelief(boolean eternal, boolean nonEternal) {
        return getStrongestSentence(beliefs, eternal, nonEternal);
    }


    public Sentence getStrongestGoal(boolean eternal, boolean nonEternal) {
        return getStrongestSentence(goals, eternal, nonEternal);
    }

    /** temporary until goal is separated into goalEternal, goalTemporal */
    @Deprecated public Sentence getStrongestSentence(List<Task> table, boolean eternal, boolean temporal) {
        for (Task t : table) {
            Sentence s = t.sentence;
            boolean e = s.isEternal();
            if (e && eternal) return s;
            if (!e && temporal) return s;
        }
        return null;
    }
    protected static Sentence getStrongestSentence(List<Task> table) {
        if (table.isEmpty()) return null;
        return table.get(0).sentence;
    }

    public Sentence getStrongestBelief() {
        return getStrongestBelief(true, true);
    }
    public Sentence getStrongGoal() {
        return getStrongestGoal(true, true);
    }

    public List<TermLinkTemplate> getTermLinkTemplates() {
        return termLinkBuilder.templates();
    }


    public Iterator<? extends Termed> adjacentTermables(boolean termLinks, boolean taskLinks) {
        if (termLinks && taskLinks) {
            return concat(
                    this.termLinks.iterator(), this.taskLinks.iterator()
            );
        }
        else if (termLinks) {
            return this.termLinks.iterator();
        }
        else if (taskLinks) {
            return this.taskLinks.iterator();
        }

        return null;
    }
    public Iterator<Term> adjacentTerms(boolean termLinks, boolean taskLinks) {
        return transform(adjacentTermables(termLinks, taskLinks), new Function<Termed, Term>() {
            @Override
            public Term apply(final Termed term) {
                return term.getTerm();
            }
        });
    }

    public Iterator<Concept> adjacentConcepts(boolean termLinks, boolean taskLinks) {
        final Iterator<Concept> termToConcept = transform(adjacentTerms(termLinks, taskLinks), new Function<Termed, Concept>() {
            @Override
            public Concept apply(final Termed term) {
                return memory.concept(term.getTerm());
            }
        });
        return filter(termToConcept, Concept.class); //should remove null's (unless they never get included anyway), TODO Check that)
    }


    public static final class TermLinkNoveltyFilter implements Predicate<TermLink>    {

        TaskLink taskLink;
        private long now;
        private int noveltyHorizon;
        private int recordLength;

        public void set(TaskLink t, long now, int noveltyHorizon, int recordLength) {
            this.taskLink = t;
            this.now = now;
            this.noveltyHorizon = noveltyHorizon;
            this.recordLength = recordLength;
        }

        @Override
        public boolean apply(TermLink termLink) {
            return taskLink.novel(termLink, now, noveltyHorizon, recordLength);
        }
    }



    /**
     * Return the questions, called in ComposionalRules in
     * dedConjunctionByQuestion only
     */
    public List<Task> getQuestions() {
        return questions;
    }

    public void discountConfidence(final boolean onBeliefs) {
        if (onBeliefs) {
            for (final Task s : beliefs)
                s.getTruth().discountConfidence();
        } else {
            for (final Task s : goals) {
                s.getTruth().discountConfidence();
            }
        }
    }

    /** get a random belief, weighted by their sentences confidences */
    public Sentence getBeliefRandomByConfidence(boolean eternal) {

        if (beliefs.isEmpty()) return null;
        
        float totalConfidence = getConfidenceSum(beliefs);
        float r = Memory.randomNumber.nextFloat() * totalConfidence;
                
        Sentence s = null;
        for (int i = 0; i < beliefs.size(); i++) {
            s = beliefs.get(i).sentence;
            r -= s.truth.getConfidence();
            if (r < 0)
                return s;
        }
        
        return s;
    }


    public static float getConfidenceSum(Iterable<? extends Truth.Truthable> beliefs) {
        float t = 0;
        for (final Truth.Truthable s : beliefs)
            t += s.getTruth().getConfidence();
        return t;
    }

    public static float getMeanFrequency(Collection<? extends Truth.Truthable> beliefs) {
        if (beliefs.isEmpty()) return 0.5f;
        
        float t = 0;
        for (final Truth.Truthable s : beliefs)
            t += s.getTruth().getFrequency();
        return t / beliefs.size();        
    }

    

    public NALOperator operator() {
        return term.operator();
    }


    public Term getTerm() {
        return term;
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

    public void print(PrintStream out) {
        print(out, true, true, true, true);
    }
    /** prints a summary of all termlink, tasklink, etc.. */
    public void print(PrintStream out, boolean showbeliefs, boolean showgoals, boolean showtermlinks, boolean showtasklinks) {
        final String indent = "\t";
        long now = memory.time();

        out.println("CONCEPT: " + toInstanceString() + " @ " + now);

        if (showbeliefs) {
            out.print(" Beliefs:");
            if (beliefs.isEmpty()) out.println(" none");
            else out.println();
            for (Task s : beliefs) {
                out.print(indent);
                out.println((int) (rankBelief(s, now) * 100.0) + "%: " + s);
            }
        }

        if (showgoals) {
            out.print(" Goals:");
            if (goals.isEmpty()) out.println(" none");
            else out.println();
            for (Task s : goals) {
                out.print(indent);
                out.println((int) (rankBelief(s, now) * 100.0) + "%: " + s);
            }
        }

        if (showtermlinks) {

            out.println(" TermLinks:");
            for (TLink t : termLinks) {
                out.print(indent);
                TLink.print(t, out);
                out.println();
            }

        }

        if (showtasklinks) {
            out.println(" TaskLinks:");
            for (TLink t : taskLinks) {
                out.print(indent);
                TLink.print(t, out);
                out.println();
            }
        }

        out.println();
    }

    /** called when concept is activated; empty and subclassable */
    protected void onActive() {

    }

    /**
     * Methods to be implemented by Concept meta instances
     */
    public static interface Meta {

        /** called before the state changes to the given nextState */
        public void onState(Concept c, State nextState);

    }
}
