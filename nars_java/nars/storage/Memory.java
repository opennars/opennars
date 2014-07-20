/*
 * Memory.java
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
package nars.storage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.inference.InferenceRecorder;
import nars.language.Term;
import nars.core.Parameters;
import nars.core.NAR;
import nars.io.Output.OUT;

/**
 * The memory of the system.
 */
public class Memory {    
    public static Random randomNumber = new Random(1);

    /**
     * Backward pointer to the reasoner
     */
    public final NAR reasoner;

    /* ---------- Long-term storage for multiple cycles ---------- */
    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final ConceptBag concepts;
    /**
     * New tasks with novel composed terms, for delayed and selective processing
     */
    public final NovelTaskBag novelTasks;
    /**
     * Inference record text to be written into a log file
     */
    private InferenceRecorder recorder;
    
    public final AtomicInteger beliefForgettingRate = new AtomicInteger(Parameters.TERM_LINK_FORGETTING_CYCLE);
    public final AtomicInteger taskForgettingRate = new AtomicInteger(Parameters.TASK_LINK_FORGETTING_CYCLE);
    public final AtomicInteger conceptForgettingRate = new AtomicInteger(Parameters.CONCEPT_FORGETTING_CYCLE);

    /* ---------- Short-term workspace for a single cycle ---------- */
    /**
     * List of new tasks accumulated in one cycle, to be processed in the next
     * cycle
     */
    public final LinkedList<Task> newTasks;

    /**
     * The selected Term
     */
    public Term currentTerm;
    /**
     * The selected Concept
     */
    public Concept currentConcept;
    /**
     * The selected TaskLink
     */
    public TaskLink currentTaskLink;
    /**
     * The selected Task
     */
    public Task currentTask;
    /**
     * The selected TermLink
     */
    public TermLink currentBeliefLink;
    /**
     * The selected belief
     */
    public Sentence currentBelief;
    /**
     * The new Stamp
     */
    public Stamp newStamp;
    /**
     * The substitution that unify the common term in the Task and the Belief
     * TODO unused
     */
    protected HashMap<Term, Term> substitute;
    


    /* ---------- Constructor ---------- */
    /**
     * Create a new memory
     * <p>
     * Called in Reasoner.reset only
     *
     * @param nar
     */
    public Memory(NAR nar) {
        this.reasoner = nar;
        recorder = NullInferenceRecorder.global;
        concepts = new ConceptBag(nar.param.getBagLevels(), nar.param.getConceptBagSize(), conceptForgettingRate);
        novelTasks = new NovelTaskBag(nar.param.getBagLevels(), Parameters.TASK_BUFFER_SIZE);
        newTasks = new LinkedList<>();
    }

    public void init() {
        concepts.clear();
        novelTasks.clear();
        newTasks.clear();
        randomNumber = new Random(1);
        if (getRecorder().isActive()) {
            getRecorder().append("--reset--");
        }
    }

    public InferenceRecorder getRecorder() {
        return recorder;
    }

    public void setRecorder(InferenceRecorder recorder) {
        this.recorder = recorder;
    }

    public long getTime() {
        return reasoner.getTime();
    }


    /**
     * Actually means that there are no new Tasks
     */
    public boolean noResult() {
        return newTasks.isEmpty();
    }

    /* ---------- conversion utilities ---------- */
    /**
     * Get an existing Concept for a given name
     * <p>
     * called from Term and ConceptWindow.
     *
     * @param name the name of a concept
     * @return a Concept or null
     */
    public Concept nameToConcept(final String name) {
        return concepts.get(name);
    }

    /**
     * Get a Term for a given name of a Concept or Operator
     * <p>
     * called in StringParser and the make methods of compound terms.
     *
     * @param name the name of a concept or operator
     * @return a Term or null (if no Concept/Operator has this name)
     */
    public Term nameToListedTerm(final String name) {
        final Concept concept = concepts.get(name);
        if (concept != null) {
            return concept.getTerm();
        }
        return null;
    }

    /**
     * Get an existing Concept for a given Term.
     *
     * @param term The Term naming a concept
     * @return a Concept or null
     */
    public Concept termToConcept(final Term term) {
        return nameToConcept(term.getName());
    }

    /**
     * Get the Concept associated to a Term, or create it.
     *
     * @param term indicating the concept
     * @return an existing Concept, or a new one, or null ( TODO bad smell )
     */
    public Concept getConcept(final Term term) {
        if (!term.isConstant()) {
            return null;
        }
        final String n = term.getName();
        Concept concept = concepts.get(n);
        if (concept == null) {
            concept = new Concept(term, this); // the only place to make a new Concept
            final boolean created = concepts.putIn(concept);
            if (!created) {
                return null;
            }
        }
        return concept;
    }

    /**
     * Get the current activation level of a concept.
     *
     * @param t The Term naming a concept
     * @return the priority value of the concept
     */
    public float getConceptActivation(final Term t) {
        final Concept c = termToConcept(t);
        return (c == null) ? 0f : c.getPriority();
    }

    /* ---------- adjustment functions ---------- */
    /**
     * Adjust the activation level of a Concept
     * <p>
     * called in Concept.insertTaskLink only
     *
     * @param c the concept to be adjusted
     * @param b the new BudgetValue
     */
    public void activateConcept(final Concept c, final BudgetValue b) {
        concepts.pickOut(c.getKey());
        BudgetFunctions.activate(c, b);
        concepts.putBack(c);
    }

    /* ---------- new task entries ---------- */

    /* There are several types of new tasks, all added into the
     newTasks list, to be processed in the next workCycle.
     Some of them are reported and/or logged. */
    /**
     * Input task processing. Invoked by the outside or inside environment.
 Outside: StringParser (addInput); Inside: Operator (feedback). Input tasks
 with low priority are ignored, and the others are put into task buffer.
     *
     * @param task The addInput task
     */
    public void inputTask(final Task task) {
        if (task.getBudget().aboveThreshold()) {
            if (recorder.isActive()) {
                recorder.append("!!! Perceived: " + task + "\n");
            }
            newTasks.add(task);       // wait to be processed in the next workCycle
        } else {
            if (recorder.isActive()) {
                recorder.append("!!! Neglected: " + task + "\n");
            }
        }
    }

    /**
     * Activated task called in MatchingRules.trySolution and
     * Concept.processGoal
     *
     * @param budget The budget value of the new Task
     * @param sentence The content of the new Task
     * @param candidateBelief The belief to be used in future inference, for
     * forward/backward correspondence
     */
    public void activatedTask(final BudgetValue budget, final Sentence sentence, final Sentence candidateBelief) {
        final Task task = new Task(sentence, budget, currentTask, sentence, candidateBelief);
        if (recorder.isActive()) {
            recorder.append("!!! Activated: " + task.toString() + "\n");
        }
        if (sentence.isQuestion()) {
            final float s = task.getBudget().summary();
//            float minSilent = reasoner.getMainWindow().silentW.value() / 100.0f;
            final float minSilent = reasoner.param.getSilenceLevel() / 100.0f;
            if (s > minSilent) {  // only report significant derived Tasks
                reasoner.output(OUT.class, task.getSentence());
            }
        }
        newTasks.add(task);
    }

    /**
     * Derived task comes from the inference rules.
     *
     * @param task the derived task
     */
    private void derivedTask(final Task task, final boolean revised, final boolean single) {
        if (task.getBudget().aboveThreshold()) {
            if (task.getSentence() != null && task.getSentence().getTruth() != null) {
                float conf = task.getSentence().getTruth().getConfidence();
                if (conf == 0) { //no confidence - we can delete the wrongs out that way.
                    if (recorder.isActive()) {
                        recorder.append("!!! Ignored (confidence): " + task + "\n");
                    }
                    return;
                }
            }
            final Stamp stamp = task.getSentence().getStamp();
            final List<Term> chain = stamp.getChain();
            
	    if (currentBelief != null) {
                final Term currentBeliefContent = currentBelief.getContent();
                if(chain.contains(currentBeliefContent)) {
                    chain.remove(currentBeliefContent);
                }
                stamp.addToChain(currentBeliefContent);
            }
            if (currentTask != null && !single) {
                final Term currentTaskContent = currentTask.getContent();
                if(chain.contains(currentTaskContent)) {
                    chain.remove(currentTaskContent);
                }
                stamp.addToChain(currentTaskContent);
            }
            if (!revised) { //its a inference rule, we have to do the derivation chain check to hamper cycles
                for (final Term chain1 : chain) {
                    if (task.getContent() == chain1) {
                        if (recorder.isActive()) {
                            recorder.append("!!! Cyclic Reasoning detected: " + task + "\n");
                        }
                        return;
                    }
                }
            } else { //its revision, of course its cyclic, apply evidental base policy
                final int stampLength = stamp.length();
                for (int i = 0; i < stampLength; i++) {
                    final long baseI = stamp.getBase()[i];
                    
                    for (int j = 0; j < stampLength; j++) {
                        if ((i != j) && (baseI == stamp.getBase()[j])) {
                            if (recorder.isActive()) {
                                recorder.append("!!! Overlapping Evidence on Revision detected: " + task + "\n");
                            }
                            return;
                        }
                    }
                }
            }
            if (recorder.isActive()) {
                recorder.append("!!! Derived: " + task + "\n");
            }
            float budget = task.getBudget().summary();
            float minSilent = reasoner.param.getSilenceLevel() / 100.0f;
            if (budget > minSilent) {  // only report significant derived Tasks
                reasoner.output(OUT.class, task.getSentence());
            }
            newTasks.add(task);
        } else {            
            if (recorder.isActive()) {
                recorder.append("!!! Ignored: " + task + "\n");
            }            
        }
    }

    /* --------------- new task building --------------- */
    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public void doublePremiseTaskRevised(Term newContent, TruthValue newTruth, BudgetValue newBudget) {
        if (newContent != null) {
            Sentence newSentence = new Sentence(newContent, currentTask.getSentence().getPunctuation(), newTruth, newStamp);
            Task newTask = new Task(newSentence, newBudget, currentTask, currentBelief);
            derivedTask(newTask, true, false);
        }
    }

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public void doublePremiseTask(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget) {
        if (newContent != null) {
            final Sentence newSentence = new Sentence(newContent, currentTask.getSentence().getPunctuation(), newTruth, newStamp);
            final Task newTask = new Task(newSentence, newBudget, currentTask, currentBelief);
            derivedTask(newTask, false, false);
        }
    }

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     * @param revisible Whether the sentence is revisible
     */
//    public void doublePremiseTask(Term newContent, TruthValue newTruth, BudgetValue newBudget, boolean revisible) {
//        if (newContent != null) {
//            Sentence taskSentence = currentTask.getSentence();
//            Sentence newSentence = new Sentence(newContent, taskSentence.getPunctuation(), newTruth, newStamp, revisible);
//            Task newTask = new Task(newSentence, newBudget, currentTask, currentBelief);
//            derivedTask(newTask, false, false);
//        }
//    }

    /**
     * Shared final operations by all single-premise rules, called in
     * StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public void singlePremiseTask(Term newContent, TruthValue newTruth, BudgetValue newBudget) {
        singlePremiseTask(newContent, currentTask.getSentence().getPunctuation(), newTruth, newBudget);
    }

    /**
     * Shared final operations by all single-premise rules, called in
     * StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param punctuation The punctuation of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public void singlePremiseTask(Term newContent, char punctuation, TruthValue newTruth, BudgetValue newBudget) {
        Task parentTask = currentTask.getParentTask();
        if (parentTask != null && newContent.equals(parentTask.getContent())) { // circular structural inference
            return;
        }
        Sentence taskSentence = currentTask.getSentence();
        if (taskSentence.isJudgment() || currentBelief == null) {
            newStamp = new Stamp(taskSentence.getStamp(), getTime());
        } else {    // to answer a question with negation in NAL-5 --- move to activated task?
            newStamp = new Stamp(currentBelief.getStamp(), getTime());
        }

        Sentence newSentence = new Sentence(newContent, punctuation, newTruth, newStamp);
        Task newTask = new Task(newSentence, newBudget, currentTask, null);
        derivedTask(newTask, false, true);
    }

    /* ---------- system working workCycle ---------- */
    /**
     * An atomic working cycle of the system: process new Tasks, then fire a
     * concept
     * <p>
     * Called from Reasoner.tick only
     *
     * @param clock The current time to be displayed
     */
    public void workCycle(final long clock) {
        if (recorder.isActive()) {
            recorder.append(" --- " + clock + " ---\n");
        }
        
        processNewTask();
        
        if (noResult()) {       // necessary?
            processNovelTask();
        }
        
        if (noResult()) {       // necessary?
            processConcept();
        }
        
        novelTasks.refresh();
    }

    /**
     * Process the newTasks accumulated in the previous workCycle, accept addInput
 ones and those that corresponding to existing concepts, plus one from the
 buffer.
     */
    private void processNewTask() {
                
        // don't include new tasks produced in the current workCycle
        int counter = newTasks.size();  
        while (counter-- > 0) {
            final Task task = newTasks.removeFirst();
            if (task.isInput() || (termToConcept(task.getContent()) != null)) { 
                // new addInput or existing concept
                immediateProcess(task);
            } else {
                final Sentence s = task.getSentence();
                if (s.isJudgment()) {
                    final double exp = s.getTruth().getExpectation();
                    if (exp > Parameters.DEFAULT_CREATION_EXPECTATION) {
                        novelTasks.putIn(task);    // new concept formation
                    } else {
                        if (recorder.isActive()) {
                            recorder.append("!!! Neglected: " + task + "\n");
                        }
                    }
                }
            }
        }
    }

    /**
     * Select a novel task to process.
     */
    private void processNovelTask() {
        final Task task = novelTasks.takeOut();       // select a task from novelTasks
        if (task != null) {
            immediateProcess(task);
        }
    }

    /**
     * Select a concept to fire.
     */
    private void processConcept() {
        currentConcept = concepts.takeOut();
        if (currentConcept != null) {
            currentTerm = currentConcept.getTerm();
            
            if (recorder.isActive()) {
                recorder.append(" * Selected Concept: " + currentTerm + "\n");
            }
            
            concepts.putBack(currentConcept);   // current Concept remains in the bag all the time
            
            currentConcept.fire();              // a working workCycle
        }
    }

    /* ---------- task processing ---------- */
    /**
     * Immediate processing of a new task, in constant time Local processing, in
     * one concept only
     *
     * @param task the task to be accepted
     */
    private void immediateProcess(final Task task) {
        currentTask = task; // one of the two places where this variable is set
        
        if (recorder.isActive()) {
            recorder.append("!!! Insert: " + task + "\n");
        }
        
        currentTerm = task.getContent();
        currentConcept = getConcept(currentTerm);
        
        if (currentConcept != null) {
            activateConcept(currentConcept, task.getBudget());
            currentConcept.directProcess(task);
        }
    }

    /* ---------- display ---------- */
    /**
     * Start display active concepts on given bagObserver, called from
     * MainWindow.
     *
     * we don't want to expose fields concepts and novelTasks, AND we want to
     * separate GUI and inference, so this method takes as argument a
     * {@link BagObserver} and calls
     * {@link ConceptBag#addBagObserver(BagObserver, String)} ;
     *
     * see design for {@link Bag} and {@link nars.gui.BagWindow} in
     * {@link Bag#addBagObserver(BagObserver, String)}
     *
     * @param bagObserver bag Observer that will receive notifications
     * @param title the window title
     */
    public void conceptsStartPlay(final BagObserver<Concept> bagObserver, final String title) {
        bagObserver.setBag(concepts);
        concepts.addBagObserver(bagObserver, title);
    }

    /**
     * Display new tasks, called from MainWindow. see
     * {@link #conceptsStartPlay(BagObserver, String)}
     *
     * @param bagObserver
     * @param s the window title
     */
    public void taskBuffersStartPlay(final BagObserver<Task> bagObserver, final String s) {
        bagObserver.setBag(novelTasks);
        novelTasks.addBagObserver(bagObserver, s);
    }



    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(1024);
        sb.append(toStringLongIfNotNull(concepts, "concepts"))
                .append(toStringLongIfNotNull(novelTasks, "novelTasks"))
                .append(toStringIfNotNull(newTasks, "newTasks"))
                .append(toStringLongIfNotNull(currentTask, "currentTask"))
                .append(toStringLongIfNotNull(currentBeliefLink, "currentBeliefLink"))
                .append(toStringIfNotNull(currentBelief, "currentBelief"));
        return sb.toString();
    }

    private String toStringLongIfNotNull(Bag<?> item, String title) {
        return item == null ? "" : "\n " + title + ":\n"
                + item.toStringLong();
    }

    private String toStringLongIfNotNull(Item item, String title) {
        return item == null ? "" : "\n " + title + ":\n"
                + item.toStringLong();
    }

    private String toStringIfNotNull(Object item, String title) {
        return item == null ? "" : "\n " + title + ":\n"
                + item.toString();
    }

    public AtomicInteger getTaskForgettingRate() {
        return taskForgettingRate;
    }

    public AtomicInteger getBeliefForgettingRate() {
        return beliefForgettingRate;
    }

    public AtomicInteger getConceptForgettingRate() {
        return conceptForgettingRate;
    }

    public static final class NullInferenceRecorder implements InferenceRecorder {

        public static final NullInferenceRecorder global = new NullInferenceRecorder();

        
        private NullInferenceRecorder() {
            
        }
        
        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public void init() {
        }

        @Override
        public void show() {
        }

        @Override
        public void play() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void append(String s) {
        }

        @Override
        public void openLogFile() {
        }

        @Override
        public void closeLogFile() {
        }

        @Override
        public boolean isLogging() {
            return false;
        }
    }
    
}
