/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nars.core.Events;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.entity.TruthValue;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Implication;
import nars.language.Negation;
import nars.language.Term;
import nars.language.Variable;
import nars.operator.Operation;

/**
 * NAL Reasoner Process.  Includes all reasoning process state.
 */
public abstract class NAL implements Runnable {
    public final Memory memory;
    protected Term currentTerm;
    protected Concept currentConcept;
    protected Task currentTask;
    protected TermLink currentBeliefLink;
    protected TaskLink currentTaskLink;
    protected Sentence currentBelief;
    protected Stamp newStamp;
    protected StampBuilder newStampBuilder;

    /** stores the tasks added by this inference process */
    protected List<Task> tasksAdded = new ArrayList();
    //TODO tasksDicarded
    
    public NAL(Memory mem) {
        super();
        this.memory = mem;
    }


    public void emit(final Class c, final Object... o) {
        memory.emit(c, o);
    }

    /**
     * Derived task comes from the inference rules.
     *
     * @param task the derived task
     */
    public boolean derivedTask(final Task task, final boolean revised, final boolean single, Sentence occurence, Sentence occurence2) {
        if(task.sentence.content instanceof Operation) {
            if(Parameters.BE_RATIONAL && (((Operation)task.sentence.content).getPredicate()==memory.getOperator("^want") || ((Operation)task.sentence.content).getPredicate()==memory.getOperator("^believe")) && task.sentence.punctuation==Symbols.GOAL_MARK) {
                return false;
            }
        }
        if (!task.budget.aboveThreshold()) {
            memory.removeTask(task, "Insufficient Budget");
            return false;
        }
        if (task.sentence != null && task.sentence.truth != null) {
            float conf = task.sentence.truth.getConfidence();
            if (conf == 0) {
                //no confidence - we can delete the wrongs out that way.
                memory.removeTask(task, "Ignored (zero confidence)");
                return false;
            }
        }
        
        if (Parameters.DERIVE_ONLY_DEMANDED_TASKS) {
            if ((task.sentence.punctuation==Symbols.JUDGMENT_MARK) && !(task.sentence.content instanceof Operation)) {             
                boolean noConcept = memory.concept(task.sentence.content) == null;
                
                if (noConcept) { 
                    //there is no question and goal of this, return
                    memory.removeTask(task, "No demand exists");
                    return false;
                }
            }
        }
    
        
        
        
        final Stamp stamp = task.sentence.stamp;
        if (occurence != null && !occurence.isEternal()) {
            stamp.setOccurrenceTime(occurence.getOccurenceTime());
        }
        if (occurence2 != null && !occurence2.isEternal()) {
            stamp.setOccurrenceTime(occurence2.getOccurenceTime());
        }
        if (stamp.latency > 0) {
            memory.logic.DERIVATION_LATENCY.commit(stamp.latency);
        }
        
        final Term currentTaskContent = getCurrentTask().getContent();
        if (getCurrentBelief() != null && getCurrentBelief().isJudgment()) {
            final Term currentBeliefContent = getCurrentBelief().content;
            stamp.chainRemove(currentBeliefContent);
            stamp.chainAdd(currentBeliefContent);
        }
        //workaround for single premise task issue:
        if (currentBelief == null && single && currentTask != null && currentTask.sentence.isJudgment()) {
            stamp.chainRemove(currentTaskContent);
            stamp.chainAdd(currentTaskContent);
        }
        //end workaround
        if (currentTask != null && !single && currentTask.sentence.isJudgment()) {
            stamp.chainRemove(currentTaskContent);
            stamp.chainAdd(currentTaskContent);
        }
        //its a inference rule, so we have to do the derivation chain check to hamper cycles
        if (!revised) {
            Term tc = task.getContent();
            
            if (task.sentence.isJudgment()) { 
                
                Term ptc = task.getParentTask() != null ? task.getParentTask().getContent() : null;
                
                if (
                    (task.getParentTask() == null) || 
                    (!(ptc.equals(Negation.make(tc))) && !(tc.equals(Negation.make(ptc))))
                   ) {
                
                    final Collection<Term> chain = stamp.getChain();

                    for (final Term chain1 : chain) {                
                        if (tc.equals(chain1)) {
                            memory.removeTask(task, "Cyclic Reasoning");
                            return false;
                        }
                    }
                }
            }
            
        } else {
            //its revision, of course its cyclic, apply evidental base policy
            final int stampLength = stamp.baseLength;
            for (int i = 0; i < stampLength; i++) {
                final long baseI = stamp.evidentialBase[i];
                for (int j = 0; j < stampLength; j++) {
                    if ((i != j) && (baseI == stamp.evidentialBase[j])) {
                        memory.removeTask(task, "Overlapping Revision Evidence");
                        //"(i=" + i + ",j=" + j +')' /* + " in " + stamp.toString()*/
                        return false;
                    }
                }
            }
        }
        if (task.sentence.content instanceof Operation) {
            Operation op = (Operation) task.sentence.content;
            if (op.getSubject() instanceof Variable || op.getPredicate() instanceof Variable) {
                return false;
            }
        }
        memory.event.emit(Events.TaskDerive.class, task, revised, single, occurence, occurence2);
        memory.logic.TASK_DERIVED.commit(task.budget.getPriority());
        addTask(task, "Derived");
        return true;
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
    public boolean doublePremiseTaskRevised(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget) {
        Sentence newSentence = new Sentence(newContent, getCurrentTask().sentence.punctuation, newTruth, getTheNewStamp());
        Task newTask = new Task(newSentence, newBudget, getCurrentTask(), getCurrentBelief());
        return derivedTask(newTask, true, false, null, null);
    }

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public Task doublePremiseTask(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget, boolean temporalAdd) {
        if (!newBudget.aboveThreshold()) {
            return null;
        }
        Task derived = null;
        if (newContent != null) {
            {
                final Sentence newSentence = new Sentence(newContent, getCurrentTask().sentence.punctuation, newTruth, getTheNewStamp());
                final Task newTask = Task.make(newSentence, newBudget, getCurrentTask(), getCurrentBelief());
                if (newTask!=null) {
                    boolean added = derivedTask(newTask, false, false, null, null);
                    if (added && temporalAdd) {
                        memory.temporalRuleOutputToGraph(newSentence, newTask);
                    }
                    if(added) {
                        derived=newTask;
                    }
                }
            }
        }
        return derived;
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
    public boolean singlePremiseTask(CompoundTerm newContent, TruthValue newTruth, BudgetValue newBudget) {
        return singlePremiseTask(newContent, getCurrentTask().sentence.punctuation, newTruth, newBudget);
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
    public boolean singlePremiseTask(final CompoundTerm newContent, final char punctuation, final TruthValue newTruth, final BudgetValue newBudget) {
        
        if (!newBudget.aboveThreshold())
            return false;
        
        Task parentTask = getCurrentTask().getParentTask();
        if (parentTask != null) {
            if (parentTask.getContent() == null) {
                return false;
            }
            if (newContent == null) {
                return false;
            }
            if (newContent.equals(parentTask.getContent())) {
                return false;
            }
        }
        Sentence taskSentence = getCurrentTask().sentence;
        if (taskSentence.isJudgment() || getCurrentBelief() == null) {
            setTheNewStamp(new Stamp(taskSentence.stamp, getTime()));
        } else {
            // to answer a question with negation in NAL-5 --- move to activated task?
            setTheNewStamp(new Stamp(getCurrentBelief().stamp, getTime()));
        }
        Sentence newSentence = new Sentence(newContent, punctuation, newTruth, getTheNewStamp());
        Task newTask = Task.make(newSentence, newBudget, getCurrentTask());
        if (newTask!=null) {
            return derivedTask(newTask, false, true, null, null);
        }
        return false;
    }

    public boolean singlePremiseTask(Sentence newSentence, BudgetValue newBudget) {
        if (!newBudget.aboveThreshold()) {
            return false;
        }
        Task newTask = new Task(newSentence, newBudget, getCurrentTask());
        return derivedTask(newTask, false, true, null, null);
    }

    //    protected void reset(Memory currentMemory) {
    //        mem = currentMemory;
    //        setCurrentTerm(null);
    //        setCurrentBelief(null);
    //        setCurrentConcept(null);
    //        setCurrentTask(null);
    //        setCurrentBeliefLink(null);
    //        setCurrentTaskLink(null);
    //        setNewStamp(null);
    //    }
    public long getTime() {
        return memory.time();
    }

    public Stamp getNewStamp() {
        return newStamp;
    }

    public void setNewStamp(Stamp newStamp) {
        this.newStamp = newStamp;
    }

    /**
     * @return the currentTask
     */
    public Task getCurrentTask() {
        return currentTask;
    }

    /**
     * @param currentTask the currentTask to set
     */
    public void setCurrentTask(Task currentTask) {        
        this.currentTask = currentTask;
    }

    public void setCurrentConcept(Concept currentConcept) {
        this.currentConcept = currentConcept;
    }

    /**
     * @return the newStamp
     */
    public Stamp getTheNewStamp() {
        if (newStamp == null) {
            //if newStamp==null then newStampBuilder must be available. cache it's return value as newStamp
            newStamp = newStampBuilder.build();
            newStampBuilder = null;
        }
        return newStamp;
    }

    /**
     * @param newStamp the newStamp to set
     */
    public Stamp setTheNewStamp(Stamp newStamp) {
        this.newStamp = newStamp;
        this.newStampBuilder = null;
        return newStamp;
    }

    interface StampBuilder {

        Stamp build();
    }

    /** creates a lazy/deferred StampBuilder which only constructs the stamp if getTheNewStamp() is actually invoked */
    public void setTheNewStamp(final Stamp first, final Stamp second, final long time) {
        newStamp = null;
        newStampBuilder = new StampBuilder() {
            @Override
            public Stamp build() {
                return new Stamp(first, second, time);
            }
        };
    }

    /**
     * @return the currentBelief
     */
    public Sentence getCurrentBelief() {
        return currentBelief;
    }

    /**
     * @param currentBelief the currentBelief to set
     */
    public void setCurrentBelief(Sentence currentBelief) {
        this.currentBelief = currentBelief;
    }

    /**
     * @return the currentBeliefLink
     */
    public TermLink getCurrentBeliefLink() {
        return currentBeliefLink;
    }

    /**
     * @param currentBeliefLink the currentBeliefLink to set
     */
    public void setCurrentBeliefLink(TermLink currentBeliefLink) {
        this.currentBeliefLink = currentBeliefLink;
    }

    /**
     * @return the currentTaskLink
     */
    public TaskLink getCurrentTaskLink() {
        return currentTaskLink;
    }

    /**
     * @param currentTaskLink the currentTaskLink to set
     */
    public void setCurrentTaskLink(TaskLink currentTaskLink) {
        this.currentTaskLink = currentTaskLink;
    }

    /**
     * @return the currentTerm
     */
    public Term getCurrentTerm() {
        return currentTerm;
    }

    /**
     * @param currentTerm the currentTerm to set
     */
    public void setCurrentTerm(Term currentTerm) {
        this.currentTerm = currentTerm;
    }

    /**
     * @return the currentConcept
     */
    public Concept getCurrentConcept() {
        return currentConcept;
    }

    public Memory mem() {
        return memory;
    }
    
    /** tasks added with this method will be remembered by this NAL instance; useful for feedback */
    public void addTask(Task t, String reason) {
        
        memory.addNewTask(t, reason);
        
        tasksAdded.add(t);
        
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
    public void addTask(final Task currentTask, final BudgetValue budget, final Sentence sentence, final Sentence candidateBelief) {        
        addTask(new Task(sentence, budget, currentTask, sentence, candidateBelief),
                "Activated");        
    }    
    
}
