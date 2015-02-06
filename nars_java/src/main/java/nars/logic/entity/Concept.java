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
package nars.logic.entity;

import com.google.common.base.Predicate;
import nars.core.Events.*;
import nars.core.Memory;
import nars.core.NARRun;
import nars.core.Parameters;
import nars.io.Symbols;
import nars.logic.NALOperator;
import nars.logic.NAL;
import nars.logic.Terms.Termable;
import nars.logic.entity.tlink.TaskLinkBuilder;
import nars.logic.entity.tlink.TermLinkBuilder;
import nars.logic.entity.tlink.TermLinkTemplate;
import nars.logic.nal8.Operation;
import nars.logic.nal8.Operator;
import nars.util.bag.Bag;
import nars.core.Memory.MemoryAware;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static nars.logic.BudgetFunctions.divide;
import static nars.logic.BudgetFunctions.rankBelief;
import static nars.logic.UtilityFunctions.or;
import static nars.logic.nal1.LocalRules.*;
import static nars.logic.nal7.TemporalRules.solutionQuality;

public class Concept extends Item<Term> implements Termable {

    
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
    public final Bag<String, TermLink> termLinks;



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

    /** remaining unspent budget from previous cycle can be accumulated */
    float taskBudgetBalance = 0;
    float termBudgetBalance = 0;

    /**
     * The display window
     */

    //public final ArrayList<ArrayList<Long>> evidentalDiscountBases=new ArrayList<ArrayList<Long>>();

    /* ---------- constructor and initialization ---------- */
    /**
     * Constructor, called in Memory.getConcept only
     *
     * @param term A term corresponding to the concept
     * @param memory A reference to the memory
     */
    public Concept(final BudgetValue b, final Term term, Bag<Sentence, TaskLink> taskLinks, Bag<String, TermLink> termLinks, final Memory memory) {
        super(b);        
        
        this.term = term;
        this.memory = memory;

        this.questions = Parameters.newArrayList();
        this.beliefs = Parameters.newArrayList();
        this.quests = Parameters.newArrayList();
        this.goals = Parameters.newArrayList();

        this.taskLinks = taskLinks;
        this.termLinks = termLinks;

        if (taskLinks instanceof MemoryAware)  ((MemoryAware)taskLinks).setMemory(memory);
        if (termLinks instanceof MemoryAware)  ((MemoryAware)termLinks).setMemory(memory);
                
        if (term instanceof CompoundTerm) {
            this.termLinkBuilder = new TermLinkBuilder(this) {

                @Override
                public void overflow(TermLink overflow) {
                    if (overflow.name().equals(getKey())) {
                        //the inserted item was returned, so absorb the refund
                        termBudgetBalance += getBudget().getPriority();
                    }
                }
            };
        } else {
            this.termLinkBuilder = null;
        }

        this.taskLinkBuilder = new TaskLinkBuilder(memory) {
            @Override
            public void overflow(TaskLink overflow) {
                if (overflow.name().equals(getKey())) {
                    //the inserted item was returned, so absorb the refund
                    termBudgetBalance += getBudget().getPriority();
                }
                else {
                    //another item was displaced
                }
            }
        };

    }

//    @Override public int hashCode() {
//        return term.hashCode();
//    }
//
//    @Override public boolean equals(final Object obj) {
//        if (this == obj) return true;
//        if (obj instanceof Concept) {
//            Concept t = (Concept)obj;
//            return (t.term.equals(term));
//        }
//        return false;
//    }
//    

    @Override public boolean equals(Object obj) {
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
    public boolean directProcess(final NAL nal, final Task task) {
        if (!task.aboveThreshold()) {
            //available credit to boost priority (maximum=1-priority)
            float credit = Math.min( taskBudgetBalance, 1.0f - budget.getPriority());
            if (task.aboveThreshold(credit)) {
                //spend credit
                taskBudgetBalance -= credit;
            }
            else {
                //save credit
                this.taskBudgetBalance += task.getPriority();
                return false;
            }
        }

        char type = task.sentence.punctuation;
        switch (type) {
            case Symbols.JUDGMENT:
                memory.logic.JUDGMENT_PROCESS.hit();
                processJudgment(nal, task);
                break;
            case Symbols.GOAL:
                memory.logic.GOAL_PROCESS.hit();
                processGoal(nal, task);
                break;
            case Symbols.QUESTION:
            case Symbols.QUEST:
                memory.logic.QUESTION_PROCESS.hit();
                processQuestion(nal, task);
                break;
            default:
                throw new RuntimeException("Invalid sentence type");
        }


        return true;
    }

    public void link(Task t) {
        if (linkToTask(t))
            linkToTerms(t.budget);  // recursively insert TermLink
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
        BudgetValue aggregateBudget = null;
        for (Task t : tasks) {
            if (linkToTask(t)) {
                if (aggregateBudget == null) aggregateBudget = new BudgetValue(t.budget);
                else {
                    aggregateBudget.merge(t.budget);
                }
            }
        }

        //linkToTerms the aggregate budget, rather than each task's budget separately
        if (aggregateBudget!=null) {
            linkToTerms(aggregateBudget);
        }
    }

    /**
     * To accept a new judgment as belief, and check for revisions and solutions
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected void processJudgment(final NAL nal, final Task task) {
        final Sentence judg = task.sentence;
        final Sentence oldBelief;
        synchronized(beliefs) {
            oldBelief = selectCandidate(judg, beliefs);   // only revise with the strongest -- how about projection?
        }
        if (oldBelief != null) {
            final Stamp newStamp = judg.stamp;
            final Stamp oldStamp = oldBelief.stamp;
            if (newStamp.equals(oldStamp,false,false,true,true)) {
                if (task.getParentTask() != null && task.getParentTask().sentence.isJudgment()) {
                    //task.budget.decPriority(0);    // duplicated task
                }   // else: activated belief
                
                memory.removeTask(task, "Duplicated");                
                return;
            } else if (revisible(judg, oldBelief)) {
                final long now = memory.time();
                nal.setTheNewStamp(newStamp, oldStamp, now);
                
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
                    if (projectedBelief.getOccurenceTime()!=oldBelief.getOccurenceTime()) {
                        nal.singlePremiseTask(projectedBelief, task.budget);
                    }
                    nal.setCurrentBelief(projectedBelief);
                    revision(judg, projectedBelief, false, nal);
                }

            }
        }
        /*if (task.aboveThreshold())*/ {
            int nnq = questions.size();       
            for (int i = 0; i < nnq; i++) {                
                trySolution(judg, questions.get(i), nal);
            }

            synchronized (beliefs) {
                addToTable(task, beliefs, memory.param.conceptBeliefsMax.get(), ConceptBeliefAdd.class, ConceptBeliefRemove.class);
            }
        }
    }

    protected void addToTable(final Task task, final List<Sentence> table, final int max, final Class eventAdd, final Class eventRemove) {
        final Sentence newSentence = task.sentence;
        int preSize = table.size();

        Sentence removed = addToTable(memory, newSentence, table, max);

        if (removed != null) {
            memory.event.emit(eventRemove, this, removed, task);
        }
        if ((preSize != table.size()) || (removed != null)) {
            memory.event.emit(eventAdd, this, task);
        }
    }

    /**
     * whether a concept's desire exceeds decision threshold
     */
    public boolean isDesired() {
        TruthValue desire=this.getDesire();
        if(desire==null) {
            return false;
        }
        return desire.getExpectation() > memory.param.decisionThreshold.get();
    }

    /**
     * Entry point for all potentially executable tasks.
     * Returns true if the Task has a Term which can be executed
     */
    public boolean executeDecision(final Task t) {

        if (isDesired()) {

            Term content = term;

            if(content instanceof Operation) {

                Operation op=(Operation)content;
                Operator oper = op.getOperator();

                op.setTask(t);
                if(!oper.call(op, memory)) {
                    return false;
                }

                return true;
            }
        }
        return false;
    }
    
    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected void processGoal(final NAL nal, final Task task) {        
        
        final Sentence goal = task.sentence, oldGoal;

        synchronized (goals) {
            oldGoal = selectCandidate(goal, goals); // revise with the existing desire values
        }

        if (oldGoal != null) {
            final Stamp newStamp = goal.stamp;
            final Stamp oldStamp = oldGoal.stamp;
            
            if (newStamp.equals(oldStamp,false,false,true,true)) {
                return; // duplicate
            } else if (revisible(goal, oldGoal)) {
                nal.setTheNewStamp(newStamp, oldStamp, memory.time());
                boolean revisionSucceeded = revision(goal, oldGoal, false, nal);
                if(revisionSucceeded) { // it is revised, so there is a new task for which this function will be called
                    return; // with higher/lower desire
                } 
            } 
        } 
        
        /*if (task.aboveThreshold())*/ {

            final Sentence belief;

            synchronized (beliefs) {
                belief = selectCandidate(goal, beliefs); // check if the Goal is already satisfied
            }

            if (belief != null) {
                trySolution(belief, task, nal); // check if the Goal is already satisfied
            }

            // still worth pursuing?
            if (task.aboveThreshold()) {

                synchronized (goals) {
                    addToTable(task, goals, memory.param.conceptGoalsMax.get(), ConceptGoalAdd.class, ConceptGoalRemove.class);
                }


                if(task.sentence.getOccurenceTime()==Stamp.ETERNAL || task.sentence.getOccurenceTime()>=memory.time()-memory.param.duration.get()) {
                    if(!executeDecision(task)) {
                        memory.emit(UnexecutableGoal.class, task, this, nal);
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
    protected void processQuestion(final NAL nal, final Task newTask) {

        Sentence ques = newTask.sentence;

        List<Task> existing = ques.isQuestion() ? questions : quests;

        if (Parameters.DEBUG) {
            if (newTask.sentence.truth!=null)
                throw new RuntimeException(newTask.sentence + " has non-null truth");
        }

        synchronized (existing) {

            boolean newQuestion = existing.isEmpty();

            for (final Task t : existing) {

                //equality test only needs to considers parent
                // (truth==null in all cases, and term will be equal)

                if (Parameters.DEBUG) {
                    if (!t.equalPunctuations(newTask))
                        throw new RuntimeException("Sentence punctuation mismatch: " + t.sentence.punctuation + " != " + newTask.sentence.punctuation);
                    if (t.sentence.truth!=null)
                        throw new RuntimeException("Non-null truth value in existing tasks buffer");
                    //Not necessary, and somewhat expensive:
                    /*if (!t.sentence.equalTerms(newTask.sentence))
                        throw new RuntimeException("Term mismatch");*/
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
        }

        final Sentence newAnswer = (ques.isQuestion())
                ? selectCandidate(ques, beliefs)
                : selectCandidate(ques, goals);

        if (newAnswer != null) {
            trySolution(newAnswer, newTask, nal);
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
    public boolean linkToTask(final Task task) {
        BudgetValue taskBudget = task.budget;
        taskLinkBuilder.setBudget(taskBudget);
        taskLinkBuilder.setTask(task);
        taskLinkBuilder.setTemplate(null);
        activateTaskLink(taskLinkBuilder);  // tlink type: SELF

        if (!(term instanceof CompoundTerm)) {
            return false;
        }

        List<TermLinkTemplate> templates = termLinkBuilder.templates();

        if (templates.isEmpty()) {
            //distribute budget to incoming termlinks?
            return false;
        }

        //TODO parameter to use linear division, conserving total budget
        //float linkSubBudgetDivisor = (float)Math.sqrt(termLinkTemplates.size());
        final int numTemplates = templates.size();
        //final int numTemplates = (int) Math.sqrt(templates.size());
        float linkSubBudgetDivisor = (float)numTemplates;

        final BudgetValue subBudget = divide(taskBudget, linkSubBudgetDivisor);
        taskLinkBuilder.setBudget(subBudget);

        if (!subBudget.aboveThreshold()) {
            //unused
            taskBudgetBalance += taskBudget.getPriority();
            return false;
        }

        taskLinkBuilder.setTask(task);
        taskLinkBuilder.setBudget(taskBudget);

        for (int i = 0; i < numTemplates; i++) {
            TermLinkTemplate termLink = templates.get(i);

//              if (!(task.isStructural() && (termLink.getType() == TermLink.TRANSFORM))) { // avoid circular transform
            Term componentTerm = termLink.target;

            Concept componentConcept = memory.conceptualize(subBudget, componentTerm);

            if (componentConcept != null) {

                taskLinkBuilder.setTemplate(termLink);

                /** activate the task tlink */
                componentConcept.activateTaskLink(taskLinkBuilder);                }

            else {
                taskBudgetBalance += subBudget.getPriority();
            }
//             }
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
    public static Sentence addToTable(final Memory memory, final Sentence newSentence, final List<Sentence> table, final int capacity) {
        float rank1 = rankBelief(newSentence);    // for the new isBelief

        float rank2;
        int i;

        boolean eternal = newSentence.isEternal();
        long now = memory.time();
        if (!eternal && newSentence.getOccurenceTime() < now) {
            //discount newSentence by its delta-time
            rank1 /= (1.0 + Math.abs(now - newSentence.getOccurenceTime()) / memory.getDuration());
            //rank1 *= TruthFunctions.temporalProjection(now, newSentence.getOccurenceTime(), now); //DOESNT WORK
        }

        //TODO decide if it's better to iterate from bottom up, to find the most accurate replacement index rather than top
        for (i = 0; i < table.size(); i++) {
            Sentence existingSentence = table.get(i);

            rank2 = rankBelief(existingSentence);

            if (!eternal && !existingSentence.isEternal()  && existingSentence.getOccurenceTime() < now) {
                //discount existingSentence by delta-time
                rank2 /= (1.0 + Math.abs( now - existingSentence.getOccurenceTime() ) / memory.getDuration());
                //rank2 *= TruthFunctions.temporalProjection(now, existingSentence.getOccurenceTime(), now); //DOESNT WORK
            }
            else if (!eternal && existingSentence.isEternal()) {
                //allow an eternal belief to be replaced by a newer non-eternal belief if its creation time is old
                //this divisor may need to be reduced to make age cost much less than how occurrence time is calculated above
                rank2 /= (1.0 + Math.abs( now - existingSentence.getCreationTime() ) / memory.getDuration());
            }

            if (rank1 >= rank2) {
                if (newSentence.equivalentTo(existingSentence, false, true, true)) {
                    //System.out.println(" ---------- Equivalent Belief: " + newSentence + " == " + judgment2);
                    return null;
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
    private Sentence selectCandidate(final Sentence query, final List<Sentence> list) {
        float currentBest = 0;
        float beliefQuality;
        Sentence candidate = null;

        for (int i = 0; i < list.size(); i++) {
            Sentence judg = list.get(i);
            beliefQuality = solutionQuality(query, judg, memory);
            if (beliefQuality > currentBest) {
                currentBest = beliefQuality;
                candidate = judg;
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

        return taskLinks.UPDATE(taskLink);

    }



    /**
     * Recursively build TermLinks between a compound and its components
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskBudget The BudgetValue of the task
     * @return whether any activity happened as a result of this invocation
     */
    public /* synchronized */ boolean linkToTerms(final BudgetValue taskBudget) {

        if (termLinkBuilder.size() == 0)
            return false;

        List<TermLinkTemplate> templates = termLinkBuilder.templates();
        int recipients = termLinkBuilder.getNonTransforms();


        float subBudget = 0;
        if (recipients == 0) {
            termBudgetBalance += subBudget;
            subBudget = 0;
            termLinkBuilder.set(0,0,0);
            return false;
        }

        //TODO make this parameterizable

        //float linkSubBudgetDivisor = (float)Math.sqrt(recipients);

        //half of each subBudget is spent on this concept and the other concept's termlink
        float linkSubBudgetDivisor = (float) recipients;
        subBudget = taskBudget.getPriority() / (2 * linkSubBudgetDivisor);
        //subBudget = taskBudget.getPriority() / (int)(Math.sqrt(linkSubBudgetDivisor));

        if (!termLinkBuilder.set(subBudget, taskBudget.getDurability(), taskBudget.getQuality()).aboveThreshold()) {
            //account for unused priority
            termBudgetBalance += taskBudget.getPriority();
            return false;
        }

        boolean activity = false;

        for (int i = 0; i < recipients; i++) {
            TermLinkTemplate template = templates.get(i);

            //only apply this loop to non-transform termlink templates
            if (template.type == TermLink.TRANSFORM)
                continue;

            Term target = template.target;

            final Concept otherConcept = memory.conceptualize(taskBudget, target);
            if (otherConcept == null) {
                termBudgetBalance += subBudget*2;
                continue;
            }

            activity = true;

            // this concept termLink to that concept
            activateTermLink(termLinkBuilder.set(template, term, target));

            // that concept termLink to this concept
            otherConcept.activateTermLink(termLinkBuilder.set(template, target, term));

            if (target instanceof CompoundTerm) {
                otherConcept.linkToTerms(termLinkBuilder.getBudget());
            }
        }
        return activity;
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

        return termLinks.UPDATE(termLink);

    }

    /**
     * Return a string representation of the concept, called in ConceptBag only
     *
     * @return The concept name, with taskBudget in the full version
     */
    @Override
    public String toString() {  // called from concept bag
        //return (super.toStringBrief() + " " + key);
        return super.toStringExternal();
    }

    /**
     * called from {@link NARRun}
     */
    @Override
    public String toStringLong() {
        String res = 
                toStringExternal() + " " + term.name()
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

    /**
     * Recalculate the quality of the concept [to be refined to show
     * extension/intension balance]
     *
     * @return The quality value
     */
    @Override
    public float getQuality() {
        float linkPriority = termLinks.getAveragePriority();
        float termComplexityFactor = 1.0f / term.getComplexity();
        float result = or(linkPriority, termComplexityFactor);
        if (result < 0) {
            throw new RuntimeException("Concept.getQuality < 0:  result=" + result + ", linkPriority=" + linkPriority + " ,termComplexityFactor=" + termComplexityFactor + ", termLinks.size=" + termLinks.size());
        }
        return result;

    }



    /**
     * Select a isBelief to interact with the given task in logic
     * <p>
     * get the first qualified one
     * <p>
     * only called in RuleTables.reason
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
            nal.emit(BeliefSelect.class, belief);

            nal.setTheNewStamp(taskStamp, belief.stamp, currentTime);
            
////            if (memory.newStamp != null) {
            //               return belief.projection(taskStamp.getOccurrenceTime(), currentTime);
////            }

            Sentence projectedBelief = belief.projection(occurrenceTime, currentTime);
            if (projectedBelief.getOccurenceTime()!=belief.getOccurenceTime()) {
                nal.singlePremiseTask(projectedBelief, task.budget);
            }
            
            return projectedBelief;     // return the first satisfying belief
        }
        return null;
    }

    /**
     * Get the current overall desire value. TODO to be refined
     */
    public TruthValue getDesire() {
        if (goals.isEmpty()) {
            return null;
        }
        TruthValue topValue = goals.get(0).truth;
        return topValue;
    }



    @Override
    public void end() {
    }

    /** not to be used normally */
    protected void delete() {
        for (Task t : questions) t.end();
        for (Task t : quests) t.end();
        
        questions.clear();
        quests.clear();                
        goals.clear();
        //evidentalDiscountBases.clear();
        termLinks.clear();
        taskLinks.clear();        
        beliefs.clear();

        if (termLinkBuilder != null)
            termLinkBuilder.clear();
    }
    

    /**
     * Collect direct isBelief, questions, and desires for display
     *
     * @return String representation of direct content
     */
    public String displayContent() {
        final StringBuilder buffer = new StringBuilder(18);
        buffer.append("\n  Beliefs:\n");
        if (!beliefs.isEmpty()) {
            for (Sentence s : beliefs) {
                buffer.append(s).append('\n');
            }
        }
        if (!questions.isEmpty()) {
            buffer.append("\n  Question:\n");
            for (Task t : questions) {
                buffer.append(t).append('\n');
            }
        }
        return buffer.toString();
    }

    static final class TermLinkNovel implements Predicate<TermLink>    {

        TaskLink taskLink;
        private long now;
        private int noveltyHorizon;

        public void set(TaskLink t, long now, int noveltyHorizon) {
            this.taskLink = t;
            this.now = now;
            this.noveltyHorizon = noveltyHorizon;
        }

        @Override
        public boolean apply(TermLink termLink) {
            return taskLink.novel(termLink, now, noveltyHorizon);
        }
    }

    final TermLinkNovel termLinkNovel = new TermLinkNovel();

    /**
     * Replace default to prevent repeated logic, by checking TaskLink
     *
     * @param taskLink The selected TaskLink
     * @param time The current time
     * @return The selected TermLink
     */
    public TermLink selectTermLink(final TaskLink taskLink, final long time, int noveltyHorizon) {

        int toMatch = memory.param.termLinkMaxMatched.get();

        termLinkNovel.set(taskLink, time, noveltyHorizon);

        for (int i = 0; (i < toMatch); i++) {

            final TermLink termLink = termLinks.TAKENEXT(termLinkNovel);


            if (termLink!=null) {
                //return it, to be re-inserted in caller method when finished processing it
                return termLink;
            }
        }

        return null;
    }

    public void returnTermLink(TermLink termLink, boolean used) {
        termLinks.putBack(termLink);
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
            for (final Sentence s : beliefs) {
                s.discountConfidence();
            }
        } else {
            for (final Sentence s : goals) {
                s.discountConfidence();
            }
        }
    }

    /** get a random belief, weighted by their sentences confidences */
    public Sentence getBeliefRandomByConfidence() {        
        if (beliefs.isEmpty()) return null;
        
        float totalConfidence = getBeliefConfidenceSum();
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
    
    public float getBeliefConfidenceSum() {
        float t = 0;
        for (final Sentence s : beliefs)
            t += s.truth.getConfidence();
        return t;
    }
    public float getBeliefFrequencyMean() {
        if (beliefs.isEmpty()) return 0.5f;
        
        float t = 0;
        for (final Sentence s : beliefs)
            t += s.truth.getFrequency();
        return t / beliefs.size();        
    }

    
    public CharSequence getBeliefsSummary() {
        if (beliefs.isEmpty())
            return "0 beliefs";        
        StringBuilder sb = new StringBuilder();
        for (Sentence s : beliefs)
            sb.append(s.toString()).append('\n');       
        return sb;
    }
    public CharSequence getGoalSummary() {
        if (goals.isEmpty())
            return "0 goals";
        StringBuilder sb = new StringBuilder();
        for (Sentence s : goals)
            sb.append(s.toString()).append('\n');       
        return sb;
    }

    public NALOperator operator() {
        return term.operator();
    }

    public Collection<Sentence> getSentences(char punc) {
        switch(punc) {
            case Symbols.JUDGMENT: return beliefs;
            case Symbols.GOAL: return goals;
            case Symbols.QUESTION: return Task.getSentences(questions);
            case Symbols.QUEST: return Task.getSentences(quests);
        }
        throw new RuntimeException("Invalid punctuation: " + punc);
    }

    public Term getTerm() {
        return term;
    }

    /** returns unmodifidable collection wrapping beliefs */
    public List<Sentence> getBeliefs() {
        return Collections.unmodifiableList(beliefs);
    }

    
}
