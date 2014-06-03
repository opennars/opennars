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

import nars.inference.BudgetFunctions;
import nars.inference.LocalRules;
import nars.inference.RuleTables;
import nars.inference.UtilityFunctions;
import nars.language.CompoundTerm;
import nars.language.Term;
import nars.main_nogui.NARSBatch;
import nars.main_nogui.Parameters;
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
    private final Term term;
    /**
     * Task links for indirect processing
     */
    private final TaskLinkBag taskLinks;
    /**
     * Term links between the term and its components and compounds
     */
    private final TermLinkBag termLinks;
    /**
     * Link templates of TermLink, only in concepts with CompoundTerm jmv TODO
     * explain more
     */
    private ArrayList<TermLink> termLinkTemplates;
    /**
     * Question directly asked about the term
     */
    private final ArrayList<Task> questions;
    /**
     * Sentences directly made about the term, with non-future tense
     */
    private final ArrayList<Sentence> beliefs;
    /**
     * Reference to the memory
     */
    Memory memory;
    /**
     * The display window
     */
    private EntityObserver entityObserver = new NullEntityObserver();


    /* ---------- constructor and initialization ---------- */
    /**
     * Constructor, called in Memory.getConcept only
     *
     * @param tm A term corresponding to the concept
     * @param memory A reference to the memory
     */
    public Concept(Term tm, Memory memory) {
        super(tm.getName());
        term = tm;
        this.memory = memory;
        questions = new ArrayList<>();
        beliefs = new ArrayList<>();
        taskLinks = new TaskLinkBag(memory);
        termLinks = new TermLinkBag(memory);
        if (tm instanceof CompoundTerm) {
            termLinkTemplates = ((CompoundTerm) tm).prepareComponentLinks();
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
    public void directProcess(Task task) {
        if (task.getSentence().isJudgment()) {
            processJudgment(task);
        } else {
            processQuestion(task);
        }
        if (task.getBudget().aboveThreshold()) {    // still need to be processed
            linkToTask(task);
        }
        entityObserver.refresh(displayContent());
    }

    /**
     * To accept a new judgment as isBelief, and check for revisions and
     * solutions
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    private void processJudgment(Task task) {
        Sentence judg = task.getSentence();
        Sentence oldBelief = evaluation(judg, beliefs);
        if (oldBelief != null) {
            Stamp newStamp = judg.getStamp();
            Stamp oldStamp = oldBelief.getStamp();
            if (newStamp.equals(oldStamp)) {
                if (task.getParentTask().getSentence().isJudgment()) {
                    task.getBudget().decPriority(0);    // duplicated task
                }   // else: activated belief
                return;
            } else if (LocalRules.revisible(judg, oldBelief)) {
                memory.newStamp = Stamp.make(newStamp, oldStamp, memory.getTime());
                if (memory.newStamp != null) {
                    memory.currentBelief = oldBelief;
                    LocalRules.revision(judg, oldBelief, false, memory);
                }
            }
        }
        if (task.getBudget().aboveThreshold()) {
            for (Task ques : questions) {
//                LocalRules.trySolution(ques.getSentence(), judg, ques, memory);
                LocalRules.trySolution(judg, ques, memory);
            }
            addToTable(judg, beliefs, Parameters.MAXIMUM_BELIEF_LENGTH);
        }
    }

    /**
     * To answer a question by existing beliefs
     *
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    public float processQuestion(Task task) {
        Sentence ques = task.getSentence();
        boolean newQuestion = true;
        if (questions != null) {
            for (Task t : questions) {
                Sentence q = t.getSentence();
                if (q.getContent().equals(ques.getContent())) {
                    ques = q;
                    newQuestion = false;
                    break;
                }
            }
        }
        if (newQuestion) {
            questions.add(task);
        }
        if (questions.size() > Parameters.MAXIMUM_QUESTIONS_LENGTH) {
            questions.remove(0);    // FIFO
        }
        Sentence newAnswer = evaluation(ques, beliefs);
        if (newAnswer != null) {
//            LocalRules.trySolution(ques, newAnswer, task, memory);
            LocalRules.trySolution(newAnswer, task, memory);
            return newAnswer.getTruth().getExpectation();
        } else {
            return 0.5f;
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
    private void linkToTask(Task task) {
        BudgetValue taskBudget = task.getBudget();
        TaskLink taskLink = new TaskLink(task, null, taskBudget);   // link type: SELF
        insertTaskLink(taskLink);
        if (term instanceof CompoundTerm) {
            if (termLinkTemplates.size() > 0) {
                BudgetValue subBudget = BudgetFunctions.distributeAmongLinks(taskBudget, termLinkTemplates.size());
                if (subBudget.aboveThreshold()) {
                    Term componentTerm;
                    Concept componentConcept;
                    for (TermLink termLink : termLinkTemplates) {
//                        if (!(task.isStructural() && (termLink.getType() == TermLink.TRANSFORM))) { // avoid circular transform
                        taskLink = new TaskLink(task, termLink, subBudget);
                        componentTerm = termLink.getTarget();
                        componentConcept = memory.getConcept(componentTerm);
                        if (componentConcept != null) {
                            componentConcept.insertTaskLink(taskLink);
                        }
//                        }
                    }
                    buildTermLinks(taskBudget);  // recursively insert TermLink
                }
            }
        }
    }

    /**
     * Add a new belief (or goal) into the table Sort the beliefs/goals by rank,
     * and remove redundant or low rank one
     *
     * @param newSentence The judgment to be processed
     * @param table The table to be revised
     * @param capacity The capacity of the table
     */
    private void addToTable(Sentence newSentence, ArrayList<Sentence> table, int capacity) {
        float rank1 = BudgetFunctions.rankBelief(newSentence);    // for the new isBelief
        Sentence judgment2;
        float rank2;
        int i;
        for (i = 0; i < table.size(); i++) {
            judgment2 = table.get(i);
            rank2 = BudgetFunctions.rankBelief(judgment2);
            if (rank1 >= rank2) {
                if (newSentence.equivalentTo(judgment2)) {
                    return;
                }
                table.add(i, newSentence);
                break;
            }
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
     * Evaluate a query against beliefs (and desires in the future)
     *
     * @param query The question to be processed
     * @param list The list of beliefs to be used
     * @return The best candidate belief selected
     */
    private Sentence evaluation(Sentence query, ArrayList<Sentence> list) {
        if (list == null) {
            return null;
        }
        float currentBest = 0;
        float beliefQuality;
        Sentence candidate = null;
        for (Sentence judg : list) {
            beliefQuality = LocalRules.solutionQuality(query, judg);
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
    public void insertTaskLink(TaskLink taskLink) {
        BudgetValue taskBudget = taskLink.getBudget();
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
    public void buildTermLinks(BudgetValue taskBudget) {
        Term t;
        Concept concept;
        TermLink termLink1, termLink2;
        if (termLinkTemplates.size() > 0) {
            BudgetValue subBudget = BudgetFunctions.distributeAmongLinks(taskBudget, termLinkTemplates.size());
            if (subBudget.aboveThreshold()) {
                for (TermLink template : termLinkTemplates) {
                    if (template.getType() != TermLink.TRANSFORM) {
                        t = template.getTarget();
                        concept = memory.getConcept(t);
                        if (concept != null) {
                            termLink1 = new TermLink(t, template, subBudget);
                            insertTermLink(termLink1);   // this termLink to that
                            termLink2 = new TermLink(term, template, subBudget);
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
    public void insertTermLink(TermLink termLink) {
        termLinks.putIn(termLink);
    }

    /* ---------- access local information ---------- */
    /**
     * Return the associated term, called from Memory only
     *
     * @return The associated term
     */
    public Term getTerm() {
        return term;
    }

    /**
     * Return a string representation of the concept, called in ConceptBag only
     *
     * @return The concept name, with taskBudget in the full version
     */
    @Override
    public String toString() {  // called from concept bag
        if (NARSBatch.isStandAlone()) {
            return (super.toStringBrief() + " " + key);
        } else {
            return key;
        }
    }

    /**
     * called from {@link NARSBatch}
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

    private String toStringIfNotNull(Object item, String title) {
        return item == null ? "" : "\n " + title + ":" + item.toString();
    }

    /**
     * Recalculate the quality of the concept [to be refined to show
     * extension/intension balance]
     *
     * @return The quality value
     */
    @Override
    public float getQuality() {
        float linkPriority = termLinks.averagePriority();
        float termComplexityFactor = 1.0f / term.getComplexity();
        return UtilityFunctions.or(linkPriority, termComplexityFactor);
    }

    /**
     * Return the templates for TermLinks, only called in
     * Memory.continuedProcess
     *
     * @return The template get
     */
    public ArrayList<TermLink> getTermLinkTemplates() {
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
    public Sentence getBelief(Task task) {
        Sentence taskSentence = task.getSentence();
        Sentence belief;
        for (int i = 0; i < beliefs.size(); i++) {
            belief = beliefs.get(i);
            memory.getRecorder().append(" * Selected Belief: " + belief + "\n");
            memory.newStamp = Stamp.make(taskSentence.getStamp(), belief.getStamp(), memory.getTime());
            if (memory.newStamp != null) {
                Sentence belief2 = (Sentence) belief.clone();   // will this mess up priority adjustment?
                return belief2;
            }
        }
        return null;
    }

    /* ---------- main loop ---------- */
    /**
     * An atomic step in a concept, only called in {@link Memory#processConcept}
     */
    public void fire() {
        TaskLink currentTaskLink = taskLinks.takeOut();
        if (currentTaskLink == null) {
            return;
        }
        memory.currentTaskLink = currentTaskLink;
        memory.currentBeliefLink = null;
        memory.getRecorder().append(" * Selected TaskLink: " + currentTaskLink + "\n");
        Task task = currentTaskLink.getTargetTask();
        memory.currentTask = task;  // one of the two places where this variable is set
//      memory.getRecorder().append(" * Selected Task: " + task + "\n");    // for debugging
        if (currentTaskLink.getType() == TermLink.TRANSFORM) {
            memory.currentBelief = null;
            RuleTables.transformTask(currentTaskLink, memory);  // to turn this into structural inference as below?
        } else {
            int termLinkCount = Parameters.MAX_REASONED_TERM_LINK;
//        while (memory.noResult() && (termLinkCount > 0)) {
            while (termLinkCount > 0) {
                TermLink termLink = termLinks.takeOut(currentTaskLink, memory.getTime());
                if (termLink != null) {
                    memory.getRecorder().append(" * Selected TermLink: " + termLink + "\n");
                    memory.currentBeliefLink = termLink;
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
     * same design as for {@link nars.storage.Bag} and {@link nars.gui.BagWindow}; see
     * {@link nars.storage.Bag#addBagObserver(BagObserver, String)}
     *
     * @param entityObserver {@link EntityObserver} to set;
     * TODO make it a real observer pattern (i.e. with a
     * plurality of observers)
     * @param showLinks Whether to display the task links
     */
	@SuppressWarnings("unchecked")
	public void startPlay( EntityObserver entityObserver, boolean showLinks ) {
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
     * Collect direct isBelief, questions, and goals for display
     *
     * @return String representation of direct content
     */
    public String displayContent() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("\n  Beliefs:\n");
        if (beliefs.size() > 0) {
            for (Sentence s : beliefs) {
                buffer.append(s).append("\n");
            }
        }
        buffer.append("\n  Question:\n");
        if (questions.size() > 0) {
            for (Task t : questions) {
                buffer.append(t).append("\n");
            }
        }
        return buffer.toString();
    }

    class NullEntityObserver implements EntityObserver {

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
}
