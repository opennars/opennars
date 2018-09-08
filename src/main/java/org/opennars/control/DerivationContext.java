/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.control;

import org.opennars.entity.*;
import org.opennars.inference.TruthFunctions;
import org.opennars.interfaces.Timable;
import org.opennars.io.events.Events;
import org.opennars.main.Parameters;
import org.opennars.language.*;
import org.opennars.operator.Operation;
import org.opennars.storage.Memory;

import java.util.ArrayList;
import java.util.List;
import org.opennars.entity.Stamp.BaseEntry;

/**
 * NAL Reasoner Process.  Includes all reasoning process state.
 *
 * @author Patrick Hammer
 */
public class DerivationContext {
    public boolean evidentalOverlap = false;
    public final Memory memory;
    protected Term currentTerm;
    protected Concept currentConcept;
    protected Task currentTask;
    protected TermLink currentBeliefLink;
    protected TaskLink currentTaskLink;
    protected Sentence currentBelief;
    protected Stamp newStamp;
    public StampBuilder newStampBuilder;

    public Parameters narParameters;

    public Timable time;
    
    public DerivationContext(final Memory mem, final Parameters narParameters, final Timable time) {
        super();
        this.memory = mem;
        this.narParameters = narParameters;
        this.time = time;
    }
   
    public void emit(final Class c, final Object... o) {
        memory.emit(c, o);
    }

    /**
     * Derived task comes from the inference rules.
     *
     * @param task the derived task
     * @param overlapAllowed //https://groups.google.com/forum/#!topic/open-nars/FVbbKq5En-M
     */
    public boolean derivedTask(final Task task, final boolean revised, final boolean single, final boolean overlapAllowed) {
        return derivedTask(task, revised, single, overlapAllowed, true);
    }
    public boolean derivedTask(final Task task, final boolean revised, final boolean single, final boolean overlapAllowed, final boolean addToMemory) {

        if((task.sentence.isGoal() || task.sentence.isQuest()) && (task.sentence.term instanceof Implication ||
                                      task.sentence.term instanceof Equivalence)) {
            return false; //implication and equivalence goals and quests are not supported anymore
        }
        if (!task.budget.aboveThreshold()) {
            memory.removeTask(task, "Insufficient Budget");
            return false;
        } 
        if (task.sentence != null && task.sentence.truth != null) {
            final float conf = task.sentence.truth.getConfidence();
            if (conf < narParameters.TRUTH_EPSILON) {
                //no confidence - we can delete the wrongs out that way.
                memory.removeTask(task, "Ignored (zero confidence)");
                return false;
            }
        }
        if (task.sentence.term instanceof Operation) {
            final Operation op = (Operation) task.sentence.term;
            if (op.getSubject() instanceof Variable || op.getPredicate() instanceof Variable) {
                memory.removeTask(task, "Operation with variable as subject or predicate");
                return false;
            }
        }
        if(task.sentence.term.cloneDeep() == null) {
            //sorted subterm version leaded to a invalid term that remained undetected while the term was constructed optimistically
            //example: (&,a,b) --> (&,b,a) which gets normalized to (&,a,b) --> (&,a,b) which is invalid.
            memory.removeTask(task, "Wrong Format");
            return false;
        }

        final Stamp stamp = task.sentence.stamp;
        
        //its revision, of course its cyclic, apply evidental base policy
        if(!overlapAllowed) { //todo reconsider
            final int stampLength = stamp.baseLength;
            for (int i = 0; i < stampLength; i++) {
                final BaseEntry baseI = stamp.evidentialBase[i];
                for (int j = 0; j < stampLength; j++) {
                    //!single since the derivation shouldn't depend on whether there is a current belief or not!!
                    if ((!single && this.evidentalOverlap) || ((i != j) && (baseI.equals(stamp.evidentialBase[j])))) {
                        memory.removeTask(task, "Overlapping Evidenctal Base");
                        //"(i=" + i + ",j=" + j +')' /* + " in " + stamp.toString()*/
                        return false;
                    }
                }
            }
        }
        
        //deactivated, new anticipation handling is attempted instead
        /*if(task.sentence.getOccurenceTime()>memory.time() && ((this.getCurrentTask()!=null && (this.getCurrentTask().isInput() || this.getCurrentTask().sentence.producedByTemporalInduction)) || (this.getCurrentBelief()!=null && this.getCurrentBelief().producedByTemporalInduction))) {
            Anticipate ret = ((Anticipate)memory.getOperator("^anticipate"));
            if(ret!=null) {
                ret.anticipate(task.sentence.term, memory, task.sentence.getOccurenceTime(),task);
            }
        }*/
        
        task.setElemOfSequenceBuffer(false);
        if(!revised) {
            task.getBudget().setDurability(task.getBudget().getDurability()*narParameters.DERIVATION_DURABILITY_LEAK);
            task.getBudget().setPriority(task.getBudget().getPriority()*narParameters.DERIVATION_PRIORITY_LEAK);
        }
        memory.event.emit(Events.TaskDerive.class, task, revised, single);
        //memory.logic.TASK_DERIVED.commit(task.budget.getPriority());
        
        if(addToMemory) {
            addTask(task, "Derived");
        }
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
        final Stamp derived_stamp = getTheNewStamp().clone();
        this.resetOccurrenceTime(); //stamp was already obsorbed
        final Sentence newSentence = new Sentence(
            newContent,
            getCurrentTask().sentence.punctuation,
            newTruth,
            derived_stamp);

        final Task newTask = new Task(newSentence, newBudget, getCurrentBelief());

        return derivedTask(newTask, true, false, true); //allows overlap since overlap was already checked on revisable( function
    }                                                               //which is not the case for other single premise tasks

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     * @param temporalInduction
     * @param overlapAllowed // https://groups.google.com/forum/#!topic/open-nars/FVbbKq5En-M
     */
    public List<Task> doublePremiseTask(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget, final boolean temporalInduction, final boolean overlapAllowed) {
        return doublePremiseTask(newContent, newTruth, newBudget, temporalInduction, overlapAllowed, true);
    }
    public List<Task> doublePremiseTask(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget, final boolean temporalInduction, final boolean overlapAllowed, final boolean addToMemory) {
        
        final List<Task> ret = new ArrayList<>();
        if(newContent == null || !newBudget.aboveThreshold()) {
            return null;
        }
        if ((newContent != null) && (!(newContent instanceof Interval)) && (!(newContent instanceof Variable))) {
            
            if(newContent.subjectOrPredicateIsIndependentVar()) {
                return null;
            }
            final Stamp derive_stamp = getTheNewStamp().clone(); //because occurrence time will be reset:
            this.resetOccurrenceTime(); //stamp was already obsorbed into task

            Sentence newSentence = new Sentence(
                newContent,
                getCurrentTask().sentence.punctuation,
                newTruth,
                derive_stamp);

            newSentence.producedByTemporalInduction=temporalInduction;
            Task newTask = new Task(newSentence, newBudget, getCurrentBelief());

            if (newTask!=null) {
                final boolean added = derivedTask(newTask, false, false, overlapAllowed, addToMemory);
                if(added) {
                    ret.add(newTask);
                }
            }
            
            
            //"Since in principle it is always valid to eternalize a tensed belief"
            if(temporalInduction && narParameters.IMMEDIATE_ETERNALIZATION) { //temporal induction generated ones get eternalized directly
                final TruthValue truthEt=TruthFunctions.eternalize(newTruth, this.narParameters);
                final Stamp st=derive_stamp.clone();
                st.setEternal();
                newSentence = new Sentence(
                    newContent,
                    getCurrentTask().sentence.punctuation,
                    truthEt,
                    st);

                newSentence.producedByTemporalInduction=temporalInduction;
                newTask = new Task(newSentence, newBudget, getCurrentBelief());
                if (newTask!=null) {
                    final boolean added = derivedTask(newTask, false, false, overlapAllowed, addToMemory);
                    if(added) {
                        ret.add(newTask);
                    }
                }
            }
            return ret;
        }
        return null;
    }

    /**
     * Shared final operations by all single-premise rules, called in
     * StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public boolean singlePremiseTask(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget) {
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
    public boolean singlePremiseTask( Term newContent, final char punctuation, final TruthValue newTruth, final BudgetValue newBudget) {
        if (!newBudget.aboveThreshold())
            return false;
        
        final Sentence taskSentence = getCurrentTask().sentence;
        if (taskSentence.isGoal() || taskSentence.isJudgment() || getCurrentBelief() == null) {
            setTheNewStamp(new Stamp(taskSentence.stamp, getTime()));
        } else {
            // to answer a question with negation in NAL-5 --- move to activated task?
            setTheNewStamp(new Stamp(getCurrentBelief().stamp, getTime()));
        }
        
        if(newContent.subjectOrPredicateIsIndependentVar()) {
            return false;
        }
        
        if(newContent instanceof Interval) {
            return false;
        }
        
        final Stamp derive_stamp = this.getTheNewStamp().clone();
        this.resetOccurrenceTime(); //stamp was already obsorbed into task

        final Sentence newSentence = new Sentence(
            newContent,
            punctuation,
            newTruth,
            derive_stamp);

        final Task newTask = new Task(newSentence, newBudget, Task.EnumType.DERIVED);
        if (newTask!=null) {
            return derivedTask(newTask, false, true, false);
        }
        return false;
    }

    public boolean singlePremiseTask(final Sentence newSentence, final BudgetValue newBudget) {
        if (!newBudget.aboveThreshold()) {
            return false;
        }

        final Task newTask = new Task(newSentence, newBudget, Task.EnumType.DERIVED);
        return derivedTask(newTask, false, true, false);
    }

    public long getTime() {
        return time.time();
    }

    public Stamp getNewStamp() {
        return newStamp;
    }

    public void setNewStamp(final Stamp newStamp) {
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
    public void setCurrentTask(final Task currentTask) {
        this.currentTask = currentTask;
    }

    public void setCurrentConcept(final Concept currentConcept) {
        this.currentConcept = currentConcept;
    }


    private long original_time = 0;

    /**
     * @return the created stamp
     */
    public Stamp getTheNewStamp() {
        if (newStamp == null) {
            //if newStamp==null then newStampBuilder must be available. cache it's return value as newStamp
            newStamp = newStampBuilder.build();
            original_time = newStamp.getOccurrenceTime();
            newStampBuilder = null;
        }
        return newStamp;
    }
    
    public void resetOccurrenceTime() {
        newStamp.setOccurrenceTime(original_time);
    }

    /**
     * @param newStamp the newStamp to set
     */
    public Stamp setTheNewStamp(final Stamp newStamp) {
        this.newStamp = newStamp;
        this.newStampBuilder = null;
        return newStamp;
    }

    public interface StampBuilder {

        Stamp build();
    }

    /** creates a lazy/deferred StampBuilder which only constructs the stamp if getTheNewStamp() is actually invoked */
    public void setTheNewStamp(final Stamp first, final Stamp second, final long time) {
        newStamp = null;
        newStampBuilder = () -> new Stamp(first, second, time, this.narParameters);
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
    public void setCurrentBelief(final Sentence currentBelief) {
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
    public void setCurrentBeliefLink(final TermLink currentBeliefLink) {
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
    public void setCurrentTaskLink(final TaskLink currentTaskLink) {
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
    public void setCurrentTerm(final Term currentTerm) {
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
    public void addTask(final Task t, final String reason) {
        if(t.sentence.term==null) {
            return;
        }
        memory.addNewTask(t, reason);
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
        addTask(new Task(sentence, budget, sentence, candidateBelief), "Activated");
    }    
    
    @Override
    public String toString() {
        return "DerivationContext[" + currentConcept + "," + currentTaskLink + "]";
    }
}
