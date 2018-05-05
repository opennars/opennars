/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package org.opennars.control;

import java.util.ArrayList;
import java.util.List;
import org.opennars.io.events.Events;
import org.opennars.storage.Memory;
import org.opennars.main.Parameters;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.entity.TaskLink;
import org.opennars.entity.TermLink;
import org.opennars.entity.TruthValue;
import org.opennars.inference.TruthFunctions;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Equivalence;
import org.opennars.language.Implication;
import org.opennars.language.Interval;
import org.opennars.language.Term;
import org.opennars.language.Variable;
import org.opennars.operator.Operation;

/**
 * NAL Reasoner Process.  Includes all reasoning process state.
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
    
    public DerivationContext(Memory mem) {
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
     * @param overlapAllowed //https://groups.google.com/forum/#!topic/open-nars/FVbbKq5En-M
     */
    public boolean derivedTask(final Task task, final boolean revised, final boolean single, boolean overlapAllowed) {
        return derivedTask(task, revised, single, overlapAllowed, true);
    }
    public boolean derivedTask(final Task task, final boolean revised, final boolean single, boolean overlapAllowed, boolean addToMemory) {                        

        if((task.sentence.isGoal() || task.sentence.isQuest()) && (task.sentence.term instanceof Implication ||
                                      task.sentence.term instanceof Equivalence)) {
            return false; //implication and equivalence goals and quests are not supported anymore
        }

        if (!task.budget.aboveThreshold()) {
            memory.removeTask(task, "Insufficient Budget");
            return false;
        } 
        if (task.sentence != null && task.sentence.truth != null) {
            float conf = task.sentence.truth.getConfidence();
            if (conf < Parameters.TRUTH_EPSILON) {
                //no confidence - we can delete the wrongs out that way.
                memory.removeTask(task, "Ignored (zero confidence)");
                return false;
            }
        }
        if (task.sentence.term instanceof Operation) {
            Operation op = (Operation) task.sentence.term;
            if (op.getSubject() instanceof Variable || op.getPredicate() instanceof Variable) {
                memory.removeTask(task, "Operation with variable as subject or predicate");
                return false;
            }
        }

        final Stamp stamp = task.sentence.stamp;
        
        //its revision, of course its cyclic, apply evidental base policy
        if(!overlapAllowed) { //todo reconsider
            final int stampLength = stamp.baseLength;
            for (int i = 0; i < stampLength; i++) {
                final long baseI = stamp.evidentialBase[i];
                for (int j = 0; j < stampLength; j++) {
                    //!single since the derivation shouldn't depend on whether there is a current belief or not!!
                    if ((!single && this.evidentalOverlap) || ((i != j) && (baseI == stamp.evidentialBase[j]))) {
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
            task.getBudget().setDurability(task.getBudget().getDurability()*Parameters.DERIVATION_DURABILITY_LEAK);
            task.getBudget().setPriority(task.getBudget().getPriority()*Parameters.DERIVATION_PRIORITY_LEAK);
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
        Stamp derived_stamp = getTheNewStamp().clone();
        this.resetOccurrenceTime(); //stamp was already obsorbed
        Sentence newSentence = new Sentence(
            newContent,
            getCurrentTask().sentence.punctuation,
            newTruth,
            derived_stamp);
        Task newTask = new Task(newSentence, newBudget, getCurrentBelief());
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
    public List<Task> doublePremiseTask(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget, boolean temporalInduction, boolean overlapAllowed) {
        return doublePremiseTask(newContent, newTruth, newBudget, temporalInduction, overlapAllowed, true);
    }
    public List<Task> doublePremiseTask(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget, boolean temporalInduction, boolean overlapAllowed, boolean addToMemory) {
        
        List<Task> ret = new ArrayList<Task>();
        if(newContent == null) {
            return null;
        }
        
        if (!newBudget.aboveThreshold()) {
            return null;
        }
        
        if ((newContent != null) && (!(newContent instanceof Interval)) && (!(newContent instanceof Variable))) {
            
            if(newContent.subjectOrPredicateIsIndependentVar()) {
                return null;
            }
            Stamp derive_stamp = getTheNewStamp().clone(); //because occurrence time will be reset:
            this.resetOccurrenceTime(); //stamp was already obsorbed into task

            Sentence newSentence = new Sentence(
                newContent,
                getCurrentTask().sentence.punctuation,
                newTruth,
                derive_stamp);

            newSentence.producedByTemporalInduction=temporalInduction;
            Task newTask = Task.make(newSentence, newBudget, getCurrentTask(), getCurrentBelief());

            if (newTask!=null) {
                boolean added = derivedTask(newTask, false, false, overlapAllowed, addToMemory);
                if(added) {
                    ret.add(newTask);
                }
            }
            
            
            //"Since in principle it is always valid to eternalize a tensed belief"
            if(temporalInduction && Parameters.IMMEDIATE_ETERNALIZATION) { //temporal induction generated ones get eternalized directly
                TruthValue truthEt=TruthFunctions.eternalize(newTruth);               
                Stamp st=derive_stamp.clone();
                st.setEternal();
                newSentence = new Sentence(
                    newContent,
                    getCurrentTask().sentence.punctuation,
                    truthEt,
                    st);

                newSentence.producedByTemporalInduction=temporalInduction;
                newTask = Task.make(newSentence, newBudget, getCurrentTask(), getCurrentBelief());
                if (newTask!=null) {
                    boolean added = derivedTask(newTask, false, false, overlapAllowed, addToMemory);
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
    public boolean singlePremiseTask(Term newContent, TruthValue newTruth, BudgetValue newBudget) {
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
    public boolean singlePremiseTask(final Term newContent, final char punctuation, final TruthValue newTruth, final BudgetValue newBudget) {
        
        if (!newBudget.aboveThreshold())
            return false;
        
        Sentence taskSentence = getCurrentTask().sentence;
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
        
        Stamp derive_stamp = this.getTheNewStamp().clone();
        this.resetOccurrenceTime(); //stamp was already obsorbed into task

        Sentence newSentence = new Sentence(
            newContent,
            punctuation,
            newTruth,
            derive_stamp);

        Task newTask = Task.make(newSentence, newBudget, getCurrentTask());
        if (newTask!=null) {
            return derivedTask(newTask, false, true, false);
        }
        return false;
    }

    public boolean singlePremiseTask(Sentence newSentence, BudgetValue newBudget) {
        if (!newBudget.aboveThreshold()) {
            return false;
        }
        Task newTask = new Task(newSentence, newBudget, false);
        return derivedTask(newTask, false, true, false);
    }

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
    private long original_time = 0;
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
    public Stamp setTheNewStamp(Stamp newStamp) {
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
        addTask(new Task(sentence, budget, currentTask, sentence, candidateBelief),
                "Activated");        
    }    
    
    @Override
    public String toString() {
        return "DerivationContext[" + currentConcept + "," + currentTaskLink + "]";
    }
}
