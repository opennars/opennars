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

import java.util.ArrayList;
import java.util.HashMap;
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
import nars.io.IInferenceRecorder;
import nars.language.Term;
import nars.main_nogui.Parameters;
import nars.main_nogui.ReasonerBatch;

/**
 * The memory of the system.
 */
public class Memory {

	/** Backward pointer to the reasoner */
    private ReasonerBatch reasoner;

    /* ---------- Long-term storage for multiple cycles ---------- */
    /** Concept bag. Containing all Concepts of the system */
    private ConceptBag concepts;
    /** New tasks with novel composed terms, for delayed and selective processing */
    private NovelTaskBag novelTasks;
    /** Inference record text to be written into a log file */
    private IInferenceRecorder recorder;

	private AtomicInteger beliefForgettingRate = new AtomicInteger( Parameters.TERM_LINK_FORGETTING_CYCLE );
	private AtomicInteger taskForgettingRate = new AtomicInteger( Parameters.TASK_LINK_FORGETTING_CYCLE );
	private AtomicInteger conceptForgettingRate = new AtomicInteger( Parameters.CONCEPT_FORGETTING_CYCLE );

	/* ---------- Short-term workspace for a single cycle ---------- */
    /** List of new tasks accumulated in one cycle, to be processed in the next cycle */
    private ArrayList<Task> newTasks;
    /** List of Strings or Tasks to be sent to the output channels */
    private ArrayList<String> exportStrings;
    /** The selected Term */
    public Term currentTerm;
    /** The selected Concept */
    public Concept currentConcept;
    /** The selected TaskLink */
    public TaskLink currentTaskLink;
    /** The selected Task */
    public Task currentTask;
    /** The selected TermLink */
    public TermLink currentBeliefLink;
    /** The selected belief */
    public Sentence currentBelief;
    /** The new Stamp */
    public Stamp newStamp;
    /** The substitution that unify the common term in the Task and the Belief
     * TODO unused */
    protected HashMap<Term, Term> substitute;


    /* ---------- Constructor ---------- */
    /**
     * Create a new memory
     * <p>
     * Called in Reasoner.reset only
     * @param reasoner
     */
    public Memory(ReasonerBatch reasoner) {
        this.reasoner = reasoner;
        recorder = new NullInferenceRecorder();
        concepts = new ConceptBag(this);
        novelTasks = new NovelTaskBag(this);
        newTasks = new ArrayList<Task>();
        exportStrings = new ArrayList<String>();
    }

    public void init() {
        concepts.init();
        novelTasks.init();
        newTasks.clear();
        exportStrings.clear();
//      reasoner.getMainWindow().initTimer();
        reasoner.initTimer();
        recorder.append("\n-----RESET-----\n");
    }

    /* ---------- access utilities ---------- */
    public ArrayList<String> getExportStrings() {
        return exportStrings;
    }

    public IInferenceRecorder getRecorder() {
        return recorder;
    }

    public void setRecorder( IInferenceRecorder recorder ) {
        this.recorder = recorder;
    }
    
    public long getTime() {
        return reasoner.getTime();
    }

//    public MainWindow getMainWindow() {
//        return reasoner.getMainWindow();
//    }

    /** Actually means that there are no new Tasks */
    public boolean noResult() {
        return newTasks.isEmpty();
    }

    /* ---------- conversion utilities ---------- */
    /**
     * Get an existing Concept for a given name
     * <p>
     * called from Term and ConceptWindow.
     * @param name the name of a concept
     * @return a Concept or null
     */
    public Concept nameToConcept(String name) {
        return concepts.get(name);
    }

    /**
     * Get a Term for a given name of a Concept or Operator
     * <p>
     * called in StringParser and the make methods of compound terms.
     * @param name the name of a concept or operator
     * @return a Term or null (if no Concept/Operator has this name)
     */
    public Term nameToListedTerm(String name) {
        Concept concept = concepts.get(name);
        if (concept != null) {
            return concept.getTerm();
        }
        return null;
    }

    /**
     * Get an existing Concept for a given Term.
     * @param term The Term naming a concept
     * @return a Concept or null
     */
    public Concept termToConcept(Term term) {
        return nameToConcept(term.getName());
    }

    /**
     * Get the Concept associated to a Term, or create it.
     * @param term indicating the concept
     * @return an existing Concept, or a new one, or null ( TODO bad smell )
     */
    public Concept getConcept(Term term) {
        if (!term.isConstant()) {
            return null;
        }
        String n = term.getName();
        Concept concept = concepts.get(n);
        if (concept == null) {
            concept = new Concept(term, this); // the only place to make a new Concept
            boolean created = concepts.putIn(concept);
            if (!created) {
                return null;
            }
        }
        return concept;
    }

    /* ---------- adjustment functions ---------- */
    /**
     * Adjust the activation level of a Concept
     * <p>
     * called in Concept.insertTaskLink only
     * @param c the concept to be adjusted
     * @param b the new BudgetValue
     */
    public void activateConcept(Concept c, BudgetValue b) {
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
     * Outside: StringParser (input); Inside: Operator (feedback).
     * Input tasks with low priority are ignored, and the others are put into task buffer.
     * @param task The input task
     */
    public void inputTask(Task task) {
        if (task.getBudget().aboveThreshold()) {
            recorder.append("!!! Perceived: " + task + "\n");
            report(task.getSentence(), true);    // report input
            newTasks.add(task);       // wait to be processed in the next workCycle
        } else {
            recorder.append("!!! Neglected: " + task + "\n");
        }
    }

    /**
     * Activated task called in MatchingRules.trySolution and Concept.processGoal
     * @param budget The budget value of the new Task
     * @param sentence The content of the new Task
     * @param candidateBelief The belief to be used in future inference, for forward/backward correspondence
     */
    public void activatedTask(BudgetValue budget, Sentence sentence, Sentence candidateBelief) {
        Task task = new Task(sentence, budget, currentTask, sentence, candidateBelief);
        recorder.append("!!! Activated: " + task.toString() + "\n");
        if (sentence.isQuestion()) {
            float s = task.getBudget().summary();
//            float minSilent = reasoner.getMainWindow().silentW.value() / 100.0f;
            float minSilent = reasoner.getSilenceValue().get() / 100.0f;
            if (s > minSilent) {  // only report significant derived Tasks
                report(task.getSentence(), false);
            }
        }
        newTasks.add(task);
    }

    /**
     * Derived task comes from the inference rules.
     * @param task the derived task
     */
    private void derivedTask(Task task) {
        if (task.getBudget().aboveThreshold()) {
            recorder.append("!!! Derived: " + task + "\n");
            float budget = task.getBudget().summary();
//            float minSilent = reasoner.getMainWindow().silentW.value() / 100.0f;
            float minSilent = reasoner.getSilenceValue().get() / 100.0f;
            if (budget > minSilent) {  // only report significant derived Tasks
                report(task.getSentence(), false);
            }
            newTasks.add(task);
        } else {
            recorder.append("!!! Ignored: " + task + "\n");
        }
    }

    /* --------------- new task building --------------- */
    /**
     * Shared final operations by all double-premise rules, called from the rules except StructuralRules
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public void doublePremiseTask(Term newContent, TruthValue newTruth, BudgetValue newBudget) {
        if (newContent != null) {
            Sentence newSentence = new Sentence(newContent, currentTask.getSentence().getPunctuation(), newTruth, newStamp);
            Task newTask = new Task(newSentence, newBudget, currentTask, currentBelief);
            derivedTask(newTask);
        }
    }

    /**
     * Shared final operations by all double-premise rules, called from the rules except StructuralRules
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     * @param revisible Whether the sentence is revisible
     */
    public void doublePremiseTask(Term newContent, TruthValue newTruth, BudgetValue newBudget, boolean revisible) {
        if (newContent != null) {
            Sentence taskSentence = currentTask.getSentence();
            Sentence newSentence = new Sentence(newContent, taskSentence.getPunctuation(), newTruth, newStamp, revisible);
            Task newTask = new Task(newSentence, newBudget, currentTask, currentBelief);
            derivedTask(newTask);
        }
    }

    /**
     * Shared final operations by all single-premise rules, called in StructuralRules
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public void singlePremiseTask(Term newContent, TruthValue newTruth, BudgetValue newBudget) {
        singlePremiseTask(newContent, currentTask.getSentence().getPunctuation(), newTruth, newBudget);
    }

    /**
     * Shared final operations by all single-premise rules, called in StructuralRules
     * @param newContent The content of the sentence in task
     * @param punctuation The punctuation of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public void singlePremiseTask(Term newContent, char punctuation, TruthValue newTruth, BudgetValue newBudget) {
        Sentence taskSentence = currentTask.getSentence();
        if (taskSentence.isJudgment() || currentBelief == null) {
            newStamp = new Stamp(taskSentence.getStamp(), getTime());
        } else {
            newStamp = new Stamp(currentBelief.getStamp(), getTime());
        }
        Sentence newSentence = new Sentence(newContent, punctuation, newTruth, newStamp, taskSentence.getRevisible());
        Task newTask = new Task(newSentence, newBudget, currentTask, null);
        derivedTask(newTask);
    }

    /* ---------- system working workCycle ---------- */
    /**
     * An atomic working workCycle of the system: process new Tasks, then fire a concept
     * <p>
     * Called from Reasoner.tick only
     * @param clock The current time to be displayed
     */
    public void workCycle(long clock) {
        recorder.append(" --- " + clock + " ---\n");
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
     * Process the newTasks accumulated in the previous workCycle, accept input ones
     * and those that corresponding to existing concepts, plus one from the buffer.
     */
    private void processNewTask() {
        Task task;
        int counter = newTasks.size();  // don't include new tasks produced in the current workCycle
        while (counter-- > 0) {
            task = newTasks.remove(0);
            if (task.isInput() || (termToConcept(task.getContent()) != null)) { // new input or existing concept
                immediateProcess(task);
            } else {
                Sentence s = task.getSentence();
                if (s.isJudgment()) {
                    double d = s.getTruth().getExpectation();
                    if (d > Parameters.DEFAULT_CREATION_EXPECTATION) {
                        novelTasks.putIn(task);    // new concept formation
                    } else {
                        recorder.append("!!! Neglected: " + task + "\n");
                    }
                }
            }
        }
    }

    /**
     * Select a novel task to process.
     */
    private void processNovelTask() {
        Task task = novelTasks.takeOut();       // select a task from novelTasks
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
            recorder.append(" * Selected Concept: " + currentTerm + "\n");
            concepts.putBack(currentConcept);   // current Concept remains in the bag all the time
            currentConcept.fire();              // a working workCycle
        }
    }

    /* ---------- task processing ---------- */
    /**
     * Immediate processing of a new task, in constant time
     * Local processing, in one concept only
     * @param task the task to be accepted
     */
    private void immediateProcess(Task task) {
        currentTask = task; // one of the two places where this variable is set
        recorder.append("!!! Insert: " + task + "\n");
        currentTerm = task.getContent();
        currentConcept = getConcept(currentTerm);
        if (currentConcept != null) {
            currentConcept.directProcess(task);
        }
    }

    /* ---------- display ---------- */
    /**
     * Display active concepts, called from MainWindow.
     * 
     * TODO we don't want to expose fields concepts and novelTasks,
     * AND we want to separate GUI and inference,
     * so this method will become
     * conceptsStartPlay( BagObserver bagObserver, String s)
     *  and this method will call
     * concepts.addBagObserver( bagObserver, s)
     * see design for Bag and {@link BagWindow} in {@link Bag#addBagObserver(BagObserver, String)}
     * @param bagObserver TODO
     * @param s the window title
     */
    public void conceptsStartPlay(BagObserver bagObserver, String s) {
    	bagObserver.setBag(concepts);
        concepts.addBagObserver(bagObserver, s);
    }

    /**
     * Display new tasks, called from MainWindow.
     * TODO see {@link #conceptsStartPlay(BagObserver, String)}
     * @param bagObserver TODO
     * @param s the window title
     */
    public void taskBuffersStartPlay(BagObserver bagObserver, String s) {
        novelTasks.addBagObserver(bagObserver, s);
    }

    /**
     * Display input/output sentence in the output channels.
     * The only place to add Objects into exportStrings. Currently only Strings
     * are added, though in the future there can be outgoing Tasks;
     * also if exportStrings is empty display the current value of timer
     * ( exportStrings is emptied in {@link ReasonerBatch#doTick()} - TODO fragile mechanism) 
     * @param sentence the sentence to be displayed
     * @param input whether the task is input
     */
    public void report(Sentence sentence, boolean input) {
    	if (ReasonerBatch.DEBUG) {
    		System.out.println("// report( clock " + reasoner.getTime()
    				+ ", input " + input
    				+ ", timer " + reasoner.getTimer()
    				+ ", Sentence " + sentence
    				+ ", exportStrings " + exportStrings
    				);
    		System.out.flush();
    	}
        if (exportStrings.isEmpty()) {
//          long timer = reasoner.getMainWindow().updateTimer();
            long timer = reasoner.updateTimer();
            if (timer > 0) {
                exportStrings.add(String.valueOf(timer));
            }
        }

        String s;
        if (input) {
            s = "  IN: ";
        } else {
            s = " OUT: ";
        }
        s += sentence.toStringBrief();
        exportStrings.add(s);
    }
    
	@Override
	public String toString() {
		return
				 toStringLongIfNotNull(concepts, "concepts")
				+ toStringLongIfNotNull(novelTasks, "novelTasks")
				+ toStringIfNotNull(newTasks, "newTasks")
				+ toStringLongIfNotNull(currentTask, "currentTask")
				+ toStringLongIfNotNull(currentBeliefLink, "currentBeliefLink")
				+ toStringIfNotNull(currentBelief, "currentBelief")
		;
	}

	private String toStringLongIfNotNull(Bag<?> item, String title) {
		return item == null ? "" : "\n " + title + ":\n" + 
				item.toStringLong();
	}
	
	private String toStringLongIfNotNull(Item item, String title) {
		return item == null ? "" : "\n " + title + ":\n" + 
				item.toStringLong();
	}
	
	private String toStringIfNotNull(Object item, String title) {
		return item == null ? "" : "\n " + title + ":\n" + 
				item.toString();
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

	class NullInferenceRecorder implements IInferenceRecorder {
		@Override
		public void init() {}
		@Override
		public void show() {}
		@Override
		public void play() {}
		@Override
		public void stop() {}
		@Override
		public void append(String s) {}
		@Override
		public void openLogFile() {}
		@Override
		public void closeLogFile() {}
		@Override
		public boolean isLogging() {
			return false;
		}
	}
}
