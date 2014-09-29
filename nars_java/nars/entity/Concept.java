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
package nars.entity;

import java.util.ArrayList;
import java.util.List;
import nars.core.Events.ConceptBeliefAdd;
import nars.core.Events.ConceptBeliefRemove;
import nars.core.Events.ConceptGoalAdd;
import nars.core.Events.ConceptGoalRemove;
import nars.core.Events.ConceptQuestionAdd;
import nars.core.Events.ConceptQuestionRemove;
import nars.core.Memory;
import nars.core.NARRun;
import static nars.entity.Stamp.make;
import static nars.inference.BudgetFunctions.distributeAmongLinks;
import static nars.inference.BudgetFunctions.rankBelief;
import nars.inference.Executive;
import static nars.inference.LocalRules.revisible;
import static nars.inference.LocalRules.revision;
import static nars.inference.LocalRules.trySolution;
import static nars.inference.RuleTables.reason;
import static nars.inference.RuleTables.transformTask;
import static nars.inference.TemporalRules.solutionQuality;
import static nars.inference.UtilityFunctions.or;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.storage.BagObserver;
import nars.storage.NullBagObserver;


public class Concept extends Item {

    /**
     * The term is the unique ID of the concept
     */
    public final Term term;

    /**
     * Task links for indirect processing
     */
    public final AbstractBag<TaskLink> taskLinks;

    /**
     * Term links between the term and its components and compounds; beliefs
     */
    public final AbstractBag<TermLink> termLinks;

    /**
     * Link templates of TermLink, only in concepts with CompoundTerm Templates
     * are used to improve the efficiency of TermLink building
     */
    private final List<TermLink> termLinkTemplates;

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
    public final ArrayList<Task> quests;

    /**
     * Judgments directly made about the term
     * Use ArrayList because of access and insertion in the middle
     */
    public final ArrayList<Sentence> beliefs;

    /**
     * Desire values on the term, similar to the above one
     */
    public final ArrayList<Sentence> desires;

    /**
     * Reference to the memory to which the Concept belongs
     */
    public final Memory memory;
    /**
     * The display window
     */

    @Deprecated private static final EntityObserver defaultNullEntityObserver = new NullEntityObserver();
    @Deprecated private EntityObserver entityObserver = defaultNullEntityObserver;
    public final CharSequence key;


    /* ---------- constructor and initialization ---------- */
    /**
     * Constructor, called in Memory.getConcept only
     *
     * @param tm A term corresponding to the concept
     * @param memory A reference to the memory
     */
    public Concept(final Term tm, AbstractBag<TaskLink> taskLinks, AbstractBag<TermLink> termLinks, final Memory memory) {
        super();
        this.key = tm.name();
        this.term = tm;
        this.memory = memory;

        this.questions = new ArrayList();
        this.beliefs = new ArrayList();
        this.quests = new ArrayList<>();
        this.desires = new ArrayList<>();       

        this.taskLinks = taskLinks;
        this.termLinks = termLinks;


        if (tm instanceof CompoundTerm) {
            this.termLinkTemplates = ((CompoundTerm) tm).prepareComponentLinks();
        }
        else {
            this.termLinkTemplates = null;
        }

    }

    @Override public CharSequence getKey() {
        return key;
    }
    
    

    /* ---------- direct processing of tasks ---------- */
    /**
     * Directly process a new task. Called exactly once on each task. Using
     * local information and finishing in a constant time. Provide feedback in
     * the taskBudget value of the task.
     * <p>
     * called in Memory.immediateProcess only
     *
     * @param task The task to be processed
     * @return whether it was processed
     */
    public boolean directProcess(final Task task) {
        char type = task.sentence.punctuation;
        switch (type) {
            case Symbols.JUDGMENT_MARK:
                memory.logic.JUDGMENT_PROCESS.commit();
                processJudgment(task);
                break;
            case Symbols.GOAL_MARK:
                memory.logic.GOAL_PROCESS.commit();
                processGoal(task);
                break;
            case Symbols.QUESTION_MARK:
            case Symbols.QUEST_MARK:
                memory.logic.QUESTION_PROCESS.commit();
                processQuestion(task);
                break;
            default:
                return false;
        }

        if (task.aboveThreshold()) {    // still need to be processed
            memory.logic.LINK_TO_TASK.commit();
            linkToTask(task);
        }
                
        if (entityObserver.isActive()) {
            entityObserver.refresh(displayContent());
        }
        return true;
    }

    /**
     * To accept a new judgment as belief, and check for revisions and solutions
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected void processJudgment(final Task task) {        
        final Sentence judg = task.sentence;
        final Sentence oldBelief = selectCandidate(judg, beliefs);   // only revise with the strongest -- how about projection?
        if (oldBelief != null) {
            final Stamp newStamp = judg.stamp;
            final Stamp oldStamp = oldBelief.stamp;
            if (newStamp.equals(oldStamp)) {
                if (task!=null && task.getParentTask()!=null && task.getParentTask().sentence.isJudgment()) {
                    task.budget.decPriority(0);    // duplicated task
                }   // else: activated belief
                return;
            } else if (revisible(judg, oldBelief)) {
                memory.setTheNewStamp(make(newStamp, oldStamp, memory.getTime()));
                if (memory.getTheNewStamp() != null) {
                    Sentence projectedBelief = oldBelief.projection(newStamp.getOccurrenceTime(), memory.getTime());
                    if (projectedBelief.getOccurenceTime() != oldBelief.getOccurenceTime()) {
                        memory.singlePremiseTask(projectedBelief, task.budget);
                    }
                    memory.setCurrentBelief(projectedBelief);
                    revision(judg, projectedBelief, false, memory);
                }
            }
        }
        if (task.aboveThreshold()) {
            for (final Task ques : questions) {
//                LocalRules.trySolution(ques.getSentence(), judg, ques, memory);
                trySolution(judg, ques, memory);
            }
            
            addToTable(task, judg, beliefs, memory.param.conceptBeliefsMax.get(), ConceptBeliefAdd.class, ConceptBeliefRemove.class);            
        }
    }

    protected void addToTable(final Task task, final Sentence newSentence, final ArrayList<Sentence> table, final int max, final Class eventAdd, final Class eventRemove, final Object... extraEventArguments) {
        int preSize = table.size();

        Sentence removed = addToTable(newSentence, table, max);
        
        if (removed!=null)
            memory.event.emit(eventRemove, this, removed, task, extraEventArguments);
        if ((preSize!=table.size()) || (removed!=null))
            memory.event.emit(eventAdd, this, newSentence, task, extraEventArguments);        
    }
    
    
    
    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected void processGoal(final Task task) {
        final Sentence goal = task.sentence;
        final Sentence oldGoal = selectCandidate(goal, desires); // revise with the existing desire values
        boolean revised = false;
        
        if (oldGoal != null) {
            final Stamp newStamp = goal.stamp;
            final Stamp oldStamp = oldGoal.stamp;
            
            if (newStamp.equals(oldStamp)) {
                return;
            }
            
            if (revisible(goal, oldGoal)) {
                memory.setTheNewStamp(make(newStamp, oldStamp, memory.getTime()));
                if (memory.getTheNewStamp() != null) {
                    revision(goal, oldGoal, false, memory);
                    revised = true;
                }
            }
        }
        
        if (task.aboveThreshold()) {
            
            final Sentence belief = selectCandidate(goal, beliefs); // check if the Goal is already satisfied
            
            if (belief!=null)
                trySolution(belief, task, memory);

            
            // still worth pursuing?
            if (task.aboveThreshold()) {    
                                
                addToTable(task, goal, desires, memory.param.conceptBeliefsMax.get(), ConceptGoalAdd.class, ConceptGoalRemove.class, revised);
                
                if (!revised || Executive.isExecutableTerm(task.sentence.content)) {
                    //hm or conjunction in time and temporal order forward                    
                    memory.executive.decisionMaking(task, this);
               }
                
            }
        }
    }

    /**
     * To answer a question by existing beliefs
     *
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected void processQuestion(final Task task) {

        Sentence ques = task.sentence;
        
        boolean newQuestion = true;
        for (final Task t : questions) {
            final Sentence q = t.sentence;
            if (q.equalsContent(ques)) {
                ques = q;
                newQuestion = false;
                break;
            }
        }

        if (newQuestion) {
            if (questions.size()+1 > memory.param.conceptQuestionsMax.get()) {
                Task removed = questions.remove(0);    // FIFO
                memory.event.emit(ConceptQuestionRemove.class, this, removed);
            }

            questions.add(task);            
            memory.event.emit(ConceptQuestionAdd.class, this, task);
        }


        final Sentence newAnswer = (ques.isQuestion()) ? 
                                       selectCandidate(ques, beliefs) :
                                       selectCandidate(ques, desires);
        
        if (newAnswer != null) {
            trySolution(newAnswer, task, memory);
        }
    }

    /**
     * Link to a new task from all relevant concepts for continued processing in
     * the near future for unspecified time.
     * <p>
     * The only method that calls the TaskLink constructor.
     *
     * @param task The task to be linked
     * @param content The content of the task
     */
    protected void linkToTask(final Task task) {
        final BudgetValue taskBudget = task.budget;
        
        insertTaskLink(new TaskLink(task, null, taskBudget, 
                memory.param.termLinkRecordLength.get()));  // link type: SELF
        
        if (term instanceof CompoundTerm) {
            if (!termLinkTemplates.isEmpty()) {
                final BudgetValue subBudget = distributeAmongLinks(taskBudget, termLinkTemplates.size());
                if (subBudget.aboveThreshold()) {
                    
                    for (int t = 0; t < termLinkTemplates.size(); t++) {
                        TermLink termLink = termLinkTemplates.get(t);
                        
//                        if (!(task.isStructural() && (termLink.getType() == TermLink.TRANSFORM))) { // avoid circular transform
                        Term componentTerm = termLink.target;
                        
                        Concept componentConcept = memory.conceptualize(componentTerm);
                        
                        if (componentConcept != null) {
                            
                            componentConcept.insertTaskLink(
                                new TaskLink(task, termLink, subBudget, 
                                        memory.param.termLinkRecordLength.get()));
                        }
//                        }
                    }
                    
                    buildTermLinks(taskBudget);  // recursively insert TermLink
                }
            }
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
    private static Sentence addToTable(final Sentence newSentence, final List<Sentence> table, final int capacity) {
        final float rank1 = rankBelief(newSentence);    // for the new isBelief
        float rank2;
        int i = 0;
        for (final Sentence judgment2 : table) {
            rank2 = rankBelief(judgment2);
            if (rank1 >= rank2) {
                if (newSentence.equivalentTo(judgment2)) {
                    return null;
                }                                
                table.add(i, newSentence);
                break;
            }
            i++;
        }
        if (table.size() >= capacity) {
            if (table.size() > capacity) {
                Sentence removed = table.remove(table.size() - 1);
                return removed;
            }
        } else if (i == table.size()) {            
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
 //        if (list == null) {
 //            return null;
 //        }
        float currentBest = 0;
        float beliefQuality;
        Sentence candidate = null;
        for (final Sentence judg : list) {
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
     */
    public void insertTaskLink(final TaskLink taskLink) {
        final BudgetValue taskBudget = taskLink.budget;
        taskLinks.putIn(taskLink);
        memory.conceptActivate(this, taskBudget);
    }

    /**
     * Recursively build TermLinks between a compound and its components
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskBudget The BudgetValue of the task
     */
    public void buildTermLinks(final BudgetValue taskBudget) {
        if (termLinkTemplates.size() > 0) {
            BudgetValue subBudget = distributeAmongLinks(taskBudget, termLinkTemplates.size());
            if (subBudget.aboveThreshold()) {
                for (final TermLink template : termLinkTemplates) {
                    if (template.type != TermLink.TRANSFORM) {
                        Term t = template.target;
                        final Concept concept = memory.conceptualize(t);
                        if (concept != null) {
                            
                            // this termLink to that
                            TermLink termLink1 = insertTermLink(new TermLink(t, template, subBudget));
                            
                            // that termLink to this
                            TermLink termLink2 = concept.insertTermLink(new TermLink(term, template, subBudget));
                            
                            if (t instanceof CompoundTerm) {
                                concept.buildTermLinks(subBudget);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Insert a TermLink into the TermLink bag
     * <p>
     * called from buildTermLinks only
     *
     * @param termLink The termLink to be inserted
     */
    public TermLink insertTermLink(final TermLink termLink) {
        termLinks.putIn(termLink);
        return termLink;
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
        String res = toStringExternal() + " " + key
                + toStringIfNotNull(termLinks, "termLinks")
                + toStringIfNotNull(taskLinks, "taskLinks");
        res += toStringIfNotNull(null, "questions");
        for (Task t : questions) {
            res += t.toString();
        }
        // TODO other details?
        return res;
    }

    private String toStringIfNotNull(final Object item, final String title) {
        if (item == null) {
            return "";
        }

        final String itemString = item.toString();

        return new StringBuilder(2 + title.length() + itemString.length() + 1).
                append("\n ").append(title).append(':').append(itemString).toString();
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
            throw new RuntimeException("Concept.getQuality < 0:  result=" + result + ", linkPriority=" + linkPriority + " ,termComplexityFactor=" + termComplexityFactor + ", termLinks.size=" + termLinks.size() );
        }
        return result;
        
    }

    /**
     * Return the templates for TermLinks, only called in
     * Memory.continuedProcess
     *
     * @return The template get
     */
    public List<TermLink> getTermLinkTemplates() {
        return termLinkTemplates;
    }

    /**
     * Select a isBelief to interact with the given task in inference
     * <p>
     * get the first qualified one
     * <p>
     * only called in RuleTables.reason
     *
     * @param task The selected task
     * @return The selected isBelief
     */
    public Sentence getBelief(final Task task) {
        final Stamp taskStamp = task.sentence.stamp;
        final long currentTime = memory.getTime();

        for (Sentence belief : beliefs) {
            if (memory.getRecorder().isActive()) {
                memory.getRecorder().append("Belief Select", belief.toString());
            }

            memory.setTheNewStamp(make(taskStamp, belief.stamp, currentTime));
////            if (memory.newStamp != null) {
            //               return belief.projection(taskStamp.getOccurrenceTime(), currentTime);
////            }
            Sentence projectedBelief = belief.projection(taskStamp.getOccurrenceTime(), memory.getTime());
            if (projectedBelief.getOccurenceTime() != belief.getOccurenceTime()) {
                memory.singlePremiseTask(projectedBelief, task.budget);
            }
            return projectedBelief;     // return the first satisfying belief
        }
        return null;
    }

    
    
    /** Get the current overall desire value. 
     *  TODO to be refined */
    public TruthValue getDesire() {
        if (desires.isEmpty()) {
            return null;
        }
        TruthValue topValue = desires.get(0).truth;
        return topValue;
    }    
    
    /* ---------- main loop ---------- */
    /**
     * An atomic step in a concept
     */
    public void fire() {
        memory.setCurrentTerm(term);
        
        final TaskLink currentTaskLink = taskLinks.takeOut();
        if (currentTaskLink == null)
            return;
        
        fire(currentTaskLink);
        taskLinks.putBack(currentTaskLink);
    }
    
    protected void fire(TaskLink currentTaskLink) {
        memory.logic.TASKLINK_FIRE.commit(currentTaskLink.budget.getPriority());
        
        memory.setCurrentTaskLink(currentTaskLink);
        memory.setCurrentBeliefLink(null);
        if (memory.getRecorder().isActive()) {            
            memory.getRecorder().append("TaskLink Select", currentTaskLink.toStringBrief());            
        }
        final Task task = currentTaskLink.getTargetTask();
        memory.setCurrentTask(task);  // one of the two places where this variable is set
//      memory.getRecorder().append(" * Selected Task: " + task + "\n");    // for debugging
        if (currentTaskLink.type == TermLink.TRANSFORM) {
            memory.setCurrentBelief(null);
            transformTask(currentTaskLink, memory);  // to turn this into structural inference as below?
        } else {
            int termLinkCount = memory.param.termLinkMaxReasoned.get();
//        while (memory.noResult() && (termLinkCount > 0)) {
            while (termLinkCount > 0) {
                final TermLink termLink = selectTermLink(currentTaskLink, memory.getTime());
                if (termLink != null) {
                    if (memory.getRecorder().isActive()) {                        
                        memory.getRecorder().append("TermLink Select", termLink.toString());
                    }
                    memory.setCurrentBeliefLink(termLink);
                    
                    reason(currentTaskLink, termLink, memory);                    
                    
                    termLinks.putBack(termLink);
                    termLinkCount--;
                } else {
                    termLinkCount = 0;
                }
            }
        }        

    }

    /* ---------- display ---------- */
    /**
     * Start displaying contents and links, called from ConceptWindow,
     * TermWindow or Memory.processTask only
     *
     * same design as for {@link nars.storage.Bag} and
     * {@link nars.gui.BagWindow}; see
     * {@link nars.storage.Bag#addBagObserver(BagObserver, String)}
     *
     * @param entityObserver {@link EntityObserver} to set; TODO make it a real
     * observer pattern (i.e. with a plurality of observers)
     * @param showLinks Whether to display the task links
     */
    @SuppressWarnings("unchecked")
    @Deprecated public void startPlay(EntityObserver entityObserver, boolean showLinks) {
        this.entityObserver = entityObserver;
        entityObserver.startPlay(this, showLinks);
        entityObserver.post(displayContent());
//        if (showLinks) {
//            taskLinks.addBagObserver(entityObserver.createBagObserver(), "Task Links in " + term);
//            termLinks.addBagObserver(entityObserver.createBagObserver(), "Term Links in " + term);
//        }
    }

    /**
     * Resume display, called from ConceptWindow only
     */
    public void play() {
        entityObserver.post(displayContent());
    }

    /**
     * Stop display, called from ConceptWindow only
     */
    public void stop() {
        entityObserver.stop();
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

    /**
     * Replace default to prevent repeated inference, by checking TaskLink
     * @param taskLink The selected TaskLink
     * @param time The current time
     * @return The selected TermLink
     */    
    protected TermLink selectTermLink(final TaskLink taskLink, final long time) {        
        
        int toMatch = memory.param.termLinkMaxMatched.get(); //Math.min(memory.param.termLinkMaxMatched.get(), termLinks.size());
        for (int i = 0; i < toMatch; i++) {
            final TermLink termLink = termLinks.takeOut(false);
            if (termLink == null) {
                return null;
            }
            if (taskLink.novel(termLink, time)) {
                termLinks.removeKey(termLink.getKey());
                return termLink;
            }
            termLinks.putBack(termLink, false);
        }
        return null;        
    }


    static final class NullEntityObserver implements EntityObserver {

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public void post(String str) {
        }

        @Override
        public BagObserver<TermLink> createBagObserver() {
            return new NullBagObserver<>();
        }

        @Override
        public void startPlay(Concept concept, boolean showLinks) {
        }

        @Override
        public void stop() {
        }

        @Override
        public void refresh(String message) {
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
            for (final Sentence s : beliefs) {
                s.discountConfidence();
            }
        } else {
            for (final Sentence s : desires) {
                s.discountConfidence();
            }            
        }
    }    
}
