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
import nars.core.NAR;
import nars.core.NARRun;
import nars.core.Parameters;
import nars.inference.BudgetFunctions;
import nars.inference.LocalRules;
import nars.inference.RuleTables;
import nars.inference.UtilityFunctions;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Term;
import nars.storage.BagObserver;
import nars.storage.Memory;
import nars.storage.NullBagObserver;
import nars.storage.TaskLinkBag;
import nars.storage.TermLinkBag;

/**
 * A concept contains information associated with a term, including directly and
 * indirectly related tasks and beliefs.
 * <p>
 * To make sure the space will be released, the only allowed reference to a
 * concept are those in a ConceptBag. All other access go through the Term that
 * names the concept.
 */
public final class Concept extends Item {

    /**
     * The term is the unique ID of the concept
     */
    public final Term term;

    /**
     * Task links for indirect processing
     */
    public final TaskLinkBag taskLinks;

    /**
     * Term links between the term and its components and compounds; beliefs
     */
    public final TermLinkBag termLinks;

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
    public final ArrayList<Task> questions;

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

    private static final EntityObserver defaultNullEntityObserver = new NullEntityObserver();
    private EntityObserver entityObserver = defaultNullEntityObserver;


    /* ---------- constructor and initialization ---------- */
    /**
     * Constructor, called in Memory.getConcept only
     *
     * @param tm A term corresponding to the concept
     * @param memory A reference to the memory
     */
    public Concept(final Term tm, final Memory memory) {
        super(tm.getName());
        term = tm;
        this.memory = memory;

        questions = new ArrayList();
        beliefs = new ArrayList();
        quests = new ArrayList<>();
        desires = new ArrayList<>();

        final NAR nar = memory.nar;

        taskLinks = new TaskLinkBag(nar.config.getTaskLinkBagLevels(), nar.config.getTaskLinkBagSize(), memory.taskForgettingRate);
        termLinks = new TermLinkBag(nar.config.getTermLinkBagLevels(), nar.config.getTermLinkBagSize(), memory.beliefForgettingRate);

        if (tm instanceof CompoundTerm) {
            termLinkTemplates = ((CompoundTerm) tm).prepareComponentLinks();
        }
        else {
            termLinkTemplates = null;
        }

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
     */
    public void directProcess(final Task task) {
        char type = task.sentence.punctuation;
        switch (type) {
            case Symbols.JUDGMENT_MARK:
                processJudgment(task);
                break;
            case Symbols.GOAL_MARK:
                processGoal(task);
                break;
            case Symbols.QUESTION_MARK:
            case Symbols.QUEST_MARK:
                processQuestion(task);
                break;
            default:
                return;
        }

        if (task.aboveThreshold()) {    // still need to be processed
            linkToTask(task);
        }
        if (entityObserver.isActive()) {
            entityObserver.refresh(displayContent());
        }
    }

    /**
     * To accept a new judgment as belief, and check for revisions and solutions
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    private void processJudgment(final Task task) {
        final Sentence judg = task.sentence;
        final Sentence oldBelief = selectCandidate(judg, beliefs);   // only revise with the strongest -- how about projection?
        if (oldBelief != null) {
            final Stamp newStamp = judg.stamp;
            final Stamp oldStamp = oldBelief.stamp;
            if (newStamp.equals(oldStamp)) {
                if (task.getParentTask().sentence.isJudgment()) {
                    task.budget.decPriority(0);    // duplicated task
                }   // else: activated belief
                return;
            } else if (LocalRules.revisible(judg, oldBelief)) {
                memory.setNewStamp(Stamp.make(newStamp, oldStamp, memory.getTime()));
                if (memory.getNewStamp() != null) {
                    Sentence projectedBelief = oldBelief.projection(newStamp.getOccurrenceTime(), memory.getTime());
                    if (projectedBelief.getOccurenceTime() != oldBelief.getOccurenceTime()) {
                        memory.singlePremiseTask(projectedBelief, task.budget);
                    }
                    memory.setCurrentBelief(projectedBelief);
                    LocalRules.revision(judg, projectedBelief, false, memory);
                }
            }
        }
        if (task.aboveThreshold()) {
            for (final Task ques : questions) {
//                LocalRules.trySolution(ques.getSentence(), judg, ques, memory);
                LocalRules.trySolution(judg, ques, memory);
            }
            addToTable(judg, beliefs, Parameters.MAXIMUM_BELIEF_LENGTH);
        }
    }

    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    private void processGoal(final Task task) {
        final Sentence goal = task.sentence;
        final Sentence oldGoal = selectCandidate(goal, desires); // revise with the existing desire values
        boolean noRevision = true;
        if (oldGoal != null) {
            final Stamp newStamp = goal.stamp;
            final Stamp oldStamp = oldGoal.stamp;
            if (newStamp.equals(oldStamp)) {
                return;
            }
            if (LocalRules.revisible(goal, oldGoal)) {
                memory.setNewStamp(Stamp.make(newStamp, oldStamp, memory.getTime()));
                if (memory.getNewStamp() != null) {
                    LocalRules.revision(goal, oldGoal, false, memory);
                    noRevision = false;
                }
            }
        }
        if (task.aboveThreshold()) {
            final Sentence belief = selectCandidate(goal, beliefs); // check if the Goal is already satisfied
            if (belief!=null)
                LocalRules.trySolution(belief, task, memory);

            if (task.aboveThreshold()) {    // still worth pursuing
                addToTable(goal, desires, Parameters.MAXIMUM_BELIEF_LENGTH);
                if (noRevision) {
                    LocalRules.decisionMaking(task, this);
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
    public void processQuestion(final Task task) {

        Sentence ques = task.sentence;
        boolean newQuestion = true;
        for (final Task t : questions) {
            final Sentence q = t.sentence;
            if (q.content.equals(ques.content)) {
                ques = q;
                newQuestion = false;
                break;
            }
        }

        if (newQuestion) {
            questions.add(task);
        }

        if (questions.size() > Parameters.MAXIMUM_QUESTIONS_LENGTH) {
            questions.remove(0);    // FIFO
        }

        final Sentence newAnswer = (ques.isQuestion()) ? 
                                       selectCandidate(ques, beliefs) :
                                       selectCandidate(ques, desires);
        
        if (newAnswer != null) {
            LocalRules.trySolution(newAnswer, task, memory);
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
    private void linkToTask(final Task task) {
        final BudgetValue taskBudget = task.budget;
        insertTaskLink(new TaskLink(task, null, taskBudget));  // link type: SELF
        if (term instanceof CompoundTerm) {
            if (!termLinkTemplates.isEmpty()) {
                final BudgetValue subBudget = BudgetFunctions.distributeAmongLinks(taskBudget, termLinkTemplates.size());
                if (subBudget.aboveThreshold()) {

                    for (final TermLink termLink : termLinkTemplates) {
//                        if (!(task.isStructural() && (termLink.getType() == TermLink.TRANSFORM))) { // avoid circular transform
                        Term componentTerm = termLink.target;
                        Concept componentConcept = memory.getConcept(componentTerm);
                        if (componentConcept != null) {
                            componentConcept.insertTaskLink(new TaskLink(task, termLink, subBudget));
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
     */
    private void addToTable(final Sentence newSentence, final List<Sentence> table, final int capacity) {
        final float rank1 = BudgetFunctions.rankBelief(newSentence);    // for the new isBelief
        float rank2;
        int i = 0;
        for (final Sentence judgment2 : table) {
            rank2 = BudgetFunctions.rankBelief(judgment2);
            if (rank1 >= rank2) {
                if (newSentence.equivalentTo(judgment2)) {
                    return;
                }
                table.add(i, newSentence);
                break;
            }
            i++;
        }
        if (table.size() >= capacity) {
            while (table.size() > capacity) {
                table.remove(table.size() - 1);
            }
        } else if (i == table.size()) {
            table.add(newSentence);
        }
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
            beliefQuality = LocalRules.solutionQuality(query, judg, memory);
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
        memory.activateConcept(this, taskBudget);
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
            BudgetValue subBudget = BudgetFunctions.distributeAmongLinks(taskBudget, termLinkTemplates.size());
            if (subBudget.aboveThreshold()) {
                for (final TermLink template : termLinkTemplates) {
                    if (template.type != TermLink.TRANSFORM) {
                        Term t = template.target;
                        final Concept concept = memory.getConcept(t);
                        if (concept != null) {
                            TermLink termLink1 = new TermLink(t, template, subBudget);
                            insertTermLink(termLink1);   // this termLink to that
                            TermLink termLink2 = new TermLink(term, template, subBudget);
                            concept.insertTermLink(termLink2);   // that termLink to this
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
    public void insertTermLink(final TermLink termLink) {
        termLinks.putIn(termLink);
    }

    /**
     * Return a string representation of the concept, called in ConceptBag only
     *
     * @return The concept name, with taskBudget in the full version
     */
    @Override
    public String toString() {  // called from concept bag
        //return (super.toStringBrief() + " " + key);
        return super.toStringBrief();
    }

    /**
     * called from {@link NARRun}
     */
    @Override
    public String toStringLong() {
        String res = toStringBrief() + " " + key
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
        float result = UtilityFunctions.or(linkPriority, termComplexityFactor);
        if (result < 0) {
            throw new RuntimeException("Concept.getQuality < 0:  result=" + result + ", linkPriority=" + linkPriority + " ,termComplexityFactor=" + termComplexityFactor );
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
                memory.getRecorder().append(" * Selected Belief: " + belief);
            }

            memory.setNewStamp(Stamp.make(taskStamp, belief.stamp, currentTime));
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
     * An atomic step in a concept, only called in {@link Memory#processConcept}
     */
    public void fire() {
        final TaskLink currentTaskLink = taskLinks.takeOut();
        if (currentTaskLink == null) {
            return;
        }
        memory.setCurrentTaskLink(currentTaskLink);
        memory.setCurrentBeliefLink(null);
        if (memory.getRecorder().isActive()) {
            memory.getRecorder().append(" * Selected TaskLink: " + currentTaskLink);
        }
        final Task task = currentTaskLink.getTargetTask();
        memory.setCurrentTask(task);  // one of the two places where this variable is set
//      memory.getRecorder().append(" * Selected Task: " + task + "\n");    // for debugging
        if (currentTaskLink.type == TermLink.TRANSFORM) {
            memory.setCurrentBelief(null);
            RuleTables.transformTask(currentTaskLink, memory);  // to turn this into structural inference as below?
        } else {
            int termLinkCount = Parameters.MAX_REASONED_TERM_LINK;
//        while (memory.noResult() && (termLinkCount > 0)) {
            while (termLinkCount > 0) {
                final TermLink termLink = termLinks.takeOut(currentTaskLink, memory.getTime());
                if (termLink != null) {
                    if (memory.getRecorder().isActive()) {
                        memory.getRecorder().append(" * Selected TermLink: " + termLink);
                    }
                    memory.setCurrentBeliefLink(termLink);
                    RuleTables.reason(currentTaskLink, termLink, memory);
                    termLinks.putBack(termLink);
                    termLinkCount--;
                } else {
                    termLinkCount = 0;
                }
            }
        }
        taskLinks.putBack(currentTaskLink);
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
    public void startPlay(EntityObserver entityObserver, boolean showLinks) {
        this.entityObserver = entityObserver;
        entityObserver.startPlay(this, showLinks);
        entityObserver.post(displayContent());
        if (showLinks) {
            taskLinks.addBagObserver(entityObserver.createBagObserver(), "Task Links in " + term);
            termLinks.addBagObserver(entityObserver.createBagObserver(), "Term Links in " + term);
        }
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
            return new NullBagObserver<TermLink>();
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

}
