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
import nars.bag.Bag;
import nars.budget.Budget;
import nars.Symbols;
import nars.nal.*;
import nars.nal.term.Termed;
import nars.nal.stamp.Stamp;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.tlink.*;

import java.io.PrintStream;
import java.util.*;

import static com.google.common.collect.Iterators.*;
import static nars.budget.BudgetFunctions.divide;
import static nars.nal.UtilityFunctions.or;
import static nars.nal.nal1.LocalRules.*;
import static nars.nal.nal7.TemporalRules.solutionQuality;

abstract public class Concept extends Item<Term> implements Termed {

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
    public final List<Sentence> beliefs;



    /**
     * Desire values on the term, similar to the above one
     */
    public final List<Sentence> goals;

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
        
        this.term = term;
        this.memory = memory;

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
    public boolean directProcess(final DirectProcess nal) {

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
     * To accept a new judgment as belief, and check for revisions and solutions
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected boolean processJudgment(final DirectProcess nal, final Task task) {
        final Sentence judg = task.sentence;
        final Sentence oldBelief;

        oldBelief = selectCandidate(judg, beliefs);   // only revise with the strongest -- how about projection?

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

    protected boolean addToTable(final Task task, final List<Sentence> table, final int max, final Class eventAdd, final Class eventRemove) {
        final Sentence newSentence = task.sentence;
        int preSize = table.size();

        Sentence removed = addToTable(memory, newSentence, table, max);

        if (removed != null) {
            if (removed == newSentence) return false;

            memory.event.emit(eventRemove, this, removed, task);
        }
        if ((preSize != table.size()) || (removed != null)) {
            memory.event.emit(eventAdd, this, task);
        }
        return true;
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
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected boolean processGoal(final DirectProcess nal, final Task task) {


        final Sentence goal = task.sentence, oldGoal;

        oldGoal = selectCandidate(goal, goals); // revise with the existing desire values

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


        // check if the Goal is already satisfied
        trySolution(selectCandidate(goal, beliefs), task, nal);

        // still worth pursuing?
        if (!task.aboveThreshold()) {
            return false;
        }


        if (!addToTable(task, goals, memory.param.conceptGoalsMax.get(), ConceptGoalAdd.class, ConceptGoalRemove.class)) {
            //wasnt added to table
            memory.removed(task, "Insufficient Rank"); //irrelevant
            return false;
        }

        memory.decide(this, task);
        return true;
    }

    /**
     * To answer a quest or question by existing beliefs
     *
     * @param newTask The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected void processQuestion(final DirectProcess nal, final Task newTask) {

        Sentence ques = newTask.sentence;

        List<Task> existing = ques.isQuestion() ? questions : quests;

        if (Global.DEBUG) {
            if (newTask.sentence.truth!=null) {
                System.err.println(newTask.sentence + " has non-null truth");
                System.err.println(newTask.getExplanation());
                throw new RuntimeException(newTask.sentence + " has non-null truth");
            }
        }


        boolean newQuestion = existing.isEmpty();

        for (final Task t : existing) {

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
            if (existing.size() + 1 > memory.param.conceptQuestionsMax.get()) {
                Task removed = existing.remove(0);    // FIFO
                memory.event.emit(ConceptQuestionRemove.class, this, removed, newTask);
            }

            existing.add(newTask);
            memory.event.emit(ConceptQuestionAdd.class, this, newTask);
        }


        if (ques.isQuest()) {
            trySolution(selectCandidate(ques, goals), newTask, nal);
        }
        else {
            trySolution(selectCandidate(ques, beliefs), newTask, nal);
        }
    }


    /**
     * Link to a new task from all relevant concepts for continued processing in
     * the near future for unspecified time.
     * <p>
     * The only method that calls the TaskLink constructor.
     *
     * @param task The task to be linked
     */
    public boolean linkTask(final Task task) {
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

    /**
     * Add a new belief (or goal) into the table Sort the beliefs/desires by
     * rank, and remove redundant or low rank one
     *
     * @param newSentence The judgment to be processed
     * @param table The table to be revised
     * @param capacity The capacity of the table
     * @return whether table was modified
     */
    public Sentence addToTable(final Memory memory, final Sentence newSentence, final List<Sentence> table, final int capacity) {

        long now = memory.time();

        float rank1 = rankBelief(newSentence, now);    // for the new isBelief

        float rank2;
        int i;


        //TODO decide if it's better to iterate from bottom up, to find the most accurate replacement index rather than top
        for (i = 0; i < table.size(); i++) {
            Sentence existingSentence = table.get(i);

            rank2 = rankBelief(existingSentence, now);

            if (rank1 >= rank2) {
                if (newSentence.equivalentTo(existingSentence, false, false, true, true)) {
                    //System.out.println(" ---------- Equivalent Belief: " + newSentence + " == " + judgment2);
                    return newSentence;
                }
                table.add(i, newSentence);
                break;
            }            
        }
        
        if (table.size() == capacity) {
            // nothing
        }
        else if (table.size() > capacity) {
            Sentence removed = table.remove(table.size() - 1);
            return removed;
        }
        else if (i == table.size()) { // branch implies implicit table.size() < capacity
            table.add(newSentence);
        }
        
        return null;
    }

    /**
     * Select a belief value or desire value for a given query
     *
     * @param query The query to be processed
     * @param list The list of beliefs or desires to be used
     * @return The best candidate selected
     */
    private Sentence selectCandidate(final Sentence query, final List<Sentence>... lists) {
        float currentBest = 0;
        float beliefQuality;
        Sentence candidate = null;

        final long now = memory.time();
        for (List<Sentence> list : lists) {
            for (int i = 0; i < list.size(); i++) {
                Sentence judg = list.get(i);
                beliefQuality = solutionQuality(query, judg, now);
                if (beliefQuality > currentBest) {
                    currentBest = beliefQuality;
                    candidate = judg;
                }
            }
        }

        return candidate;
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
    public boolean linkTerms(final Budget taskBudget, boolean updateTLinks) {


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
            Sentence belief = beliefs.get(i);

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
        Truth topValue = goals.get(0).truth;
        return topValue;
    }


    @Override public void delete() {
        super.delete();

        memory.emit(Events.ConceptDelete.class, this);

        //dont delete the tasks themselves because they may be referenced from othe concepts.
        questions.clear();
        quests.clear();

        goals.clear();
        beliefs.clear();

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
    @Deprecated public Sentence getStrongestSentence(List<Sentence> table, boolean eternal, boolean temporal) {
        for (Sentence s : table) {
            boolean e = s.isEternal();
            if (e && eternal) return s;
            if (!e && temporal) return s;
        }
        return null;
    }
    protected static Sentence getStrongestSentence(List<Sentence> table) {
        if (table.isEmpty()) return null;
        return table.get(0);
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


    public static final class TermLinkNovel implements Predicate<TermLink>    {

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
            for (final Sentence s : beliefs)
                s.discountConfidence();
        } else {
            for (final Sentence s : goals) {
                s.discountConfidence();
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
            s = beliefs.get(i);            
            r -= s.truth.getConfidence();
            if (r < 0)
                return s;
        }
        
        return s;
    }
    
    public static float getConfidenceSum(Iterable<Sentence> beliefs) {
        float t = 0;
        for (final Sentence s : beliefs)
            t += s.truth.getConfidence();
        return t;
    }

    public static float getMeanFrequency(Collection<Sentence> beliefs) {
        if (beliefs.isEmpty()) return 0.5f;
        
        float t = 0;
        for (final Sentence s : beliefs)
            t += s.truth.getFrequency();
        return t / beliefs.size();        
    }

    

    public NALOperator operator() {
        return term.operator();
    }


    public Term getTerm() {
        if (Global.DEBUG) {
            term.ensureNormalized("Concept term");
        }
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

        out.println("CONCEPT: " + term + " @ " + now);

        if (showbeliefs) {
            out.print(" Beliefs:");
            if (beliefs.isEmpty()) out.println(" none");
            else out.println();
            for (Sentence s : beliefs) {
                out.print(indent);
                out.println((int) (rankBelief(s, now) * 100.0) + "%: " + s);
            }
        }

        if (showgoals) {
            out.print(" Goals:");
            if (goals.isEmpty()) out.println(" none");
            else out.println();
            for (Sentence s : goals) {
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

}
