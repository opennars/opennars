package nars.inference;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import nars.core.Events;
import nars.core.Events.ConceptFire;
import nars.core.Events.TaskImmediateProcess;
import nars.core.Events.TermLinkSelect;
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
import static nars.inference.RuleTables.reason;
import static nars.inference.RuleTables.transformTask;
import nars.io.Symbols;
import nars.language.Negation;
import nars.language.Term;
import nars.language.Variable;
import nars.operator.Operation;

/**
 * NAL Reasoner Process.  Includes all reasoning process state.
 */
abstract public class NAL implements Callable<NAL> {
    
    public final Memory mem;
    
    protected Term currentTerm;

    protected Concept currentConcept;

    protected Task currentTask;

    protected TermLink currentBeliefLink;
    protected TaskLink currentTaskLink;

    protected Sentence currentBelief;    

    protected Stamp newStamp;       
    protected StampBuilder newStampBuilder;

    public NAL(Memory mem) {
        this.mem = mem;
    }

    


    public static class FireConcept extends NAL  {
        
        public FireConcept(Memory mem, Concept concept, TaskLink currentTaskLink) {
            super(mem);
            this.currentConcept = concept;
            this.currentTaskLink = currentTaskLink;
        }
        
        @Override public NAL call() {
        
            final Task task = currentTaskLink.getTargetTask();

            setCurrentTerm(currentConcept.term);
            setCurrentTaskLink(currentTaskLink);
            setCurrentBeliefLink(null);
            setCurrentTask(task);  // one of the two places where this variable is set

            mem.logic.TASKLINK_FIRE.commit(currentTaskLink.budget.getPriority());
            emit(ConceptFire.class, currentConcept, currentTaskLink);


            if (currentTaskLink.type == TermLink.TRANSFORM) {
                setCurrentBelief(null);
                transformTask(currentTaskLink, this);  // to turn this into structural inference as below?
            } else {
                int termLinkCount = mem.param.termLinkMaxReasoned.get();
    
                while (termLinkCount > 0) {
                    final TermLink termLink = currentConcept.selectTermLink(currentTaskLink, mem.time());
                    if (termLink != null) {

                        emit(TermLinkSelect.class, termLink, currentConcept);

                        setCurrentBeliefLink(termLink);

                        reason(currentTaskLink, termLink, this);

                        currentConcept.returnTermLink(termLink);

                        termLinkCount--;

                    } else {
                        break;
                    }
                }
            }
            return this;
       }
        
    }
    
    public void emit(final Class c, final Object... o) {
        mem.emit(c, o);
    }
    
    
    /**
     * Derived task comes from the inference rules.
     *
     * @param task the derived task
     */
    public boolean derivedTask(final Task task, final boolean revised, final boolean single, Sentence occurence, Sentence occurence2) {
        

        if (!task.budget.aboveThreshold()) {
            mem.removeTask(task, "Insufficient Budget"); 
            return false;
        }
        

        if (task.sentence != null && task.sentence.truth != null) {
              float conf = task.sentence.truth.getConfidence();                
              if (conf == 0) { 
                  //no confidence - we can delete the wrongs out that way.
                  mem.removeTask(task, "Ignored (zero confidence)");
                  return false;
              }
        }


        final Stamp stamp = task.sentence.stamp;
        if(occurence!=null && occurence.getOccurenceTime()!=Stamp.ETERNAL) {
            stamp.setOccurrenceTime(occurence.getOccurenceTime());
        }
        if(occurence2!=null && occurence2.getOccurenceTime()!=Stamp.ETERNAL) {
            stamp.setOccurrenceTime(occurence2.getOccurenceTime());
        }
        if (stamp.latency > 0) {
            mem.logic.DERIVATION_LATENCY.commit(stamp.latency);
        }

        final ArrayList<Term> chain = stamp.getChain();

        final Term currentTaskContent = getCurrentTask().getContent();

        if (getCurrentBelief() != null && getCurrentBelief().isJudgment()) {
            final Term currentBeliefContent = getCurrentBelief().content;

            stamp.chainRemove(currentBeliefContent);
            stamp.chainAdd(currentBeliefContent);
        }


        //workaround for single premise task issue:
        if(currentBelief == null && single && currentTask != null && currentTask.sentence.isJudgment()) {
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

            for (int i = 0; i < chain.size(); i++) {
                Term chain1 = chain.get(i);
                Term tc = task.getContent();
                if (task.sentence.isJudgment() && tc.equals(chain1)) {
                    Term ptc = task.getParentTask().getContent();
                    if(task.getParentTask()==null || (!(ptc.equals(Negation.make(tc))) && !(tc.equals(Negation.make(ptc))))) {                            
                        mem.removeTask(task, "Cyclic Reasoning");
                        return false;
                    }
                }
            }
        } else { //its revision, of course its cyclic, apply evidental base policy
            final int stampLength = stamp.baseLength;
            for (int i = 0; i < stampLength; i++) {
                final long baseI = stamp.evidentialBase[i];

                for (int j = 0; j < stampLength; j++) {     
                    if ((i != j) && (baseI == stamp.evidentialBase[j]) && !(task.sentence.punctuation==Symbols.GOAL_MARK && task.sentence.content instanceof Operation)) {
                        mem.removeTask(task,  "Overlapping Revision Evidence");
                        //"(i=" + i + ",j=" + j +')' /* + " in " + stamp.toString()*/
                        return false;
                    }
                }
            }
        }

        mem.event.emit(Events.TaskDerive.class, task, revised, single, occurence, occurence2);

        if(task.sentence.content instanceof Operation) {
            Operation op=(Operation) task.sentence.content;
            if(op.getSubject() instanceof Variable || op.getPredicate() instanceof Variable) {
                return false;
            }
        }

        mem.logic.TASK_DERIVED.commit(task.budget.getPriority());            
        mem.addNewTask(task, "Derived");
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
    public void doublePremiseTask(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget, boolean temporalAdd) {
        
        if (!newBudget.aboveThreshold()) 
            return;
        
        if (newContent != null) {
            
            {
                final Sentence newSentence = new Sentence(newContent, getCurrentTask().sentence.punctuation, newTruth, getTheNewStamp());
                final Task newTask = new Task(newSentence, newBudget, getCurrentTask(), getCurrentBelief());
                boolean added=derivedTask(newTask, false, false, null, null);
                if(added && temporalAdd) {
                    mem.temporalRuleOutputToGraph(newSentence,newTask);
                }
            }
            
            if(temporalAdd && Parameters.IMMEDIATE_ETERNALIZATION_CONFIDENCE_MUL!=0.0) {
                TruthValue truthEt=newTruth.clone();
                truthEt.setConfidence(newTruth.getConfidence()*Parameters.IMMEDIATE_ETERNALIZATION_CONFIDENCE_MUL);
                final Sentence newSentence = (new Sentence(newContent, getCurrentTask().sentence.punctuation, truthEt, getTheNewStamp())).clone(true);
                final Task newTask = new Task(newSentence, newBudget, getCurrentTask(), getCurrentBelief());
                boolean added=derivedTask(newTask, false, false, null, null);
                if(added && temporalAdd) {
                    mem.temporalRuleOutputToGraph(newSentence,newTask);
                }
            }
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
        singlePremiseTask(newContent, getCurrentTask().sentence.punctuation, newTruth, newBudget);
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
    public void singlePremiseTask(final Term newContent, final char punctuation, final TruthValue newTruth, final BudgetValue newBudget) {
        if (!newBudget.aboveThreshold())
            return;
        
        Task parentTask = getCurrentTask().getParentTask();
                
        if (parentTask != null) {
            if (parentTask.getContent() == null)
                return;     
            if(newContent==null)
                return;            
            if (newContent.equals(parentTask.getContent())) // circular structural inference
                return;            
        }
        Sentence taskSentence = getCurrentTask().sentence;
        if (taskSentence.isJudgment() || getCurrentBelief() == null) {
            setTheNewStamp(new Stamp(taskSentence.stamp, getTime()));
        } else {    // to answer a question with negation in NAL-5 --- move to activated task?
            setTheNewStamp(new Stamp(getCurrentBelief().stamp, getTime()));
        }
        
        Sentence newSentence = new Sentence(newContent, punctuation, newTruth, getTheNewStamp());
        Task newTask = new Task(newSentence, newBudget, getCurrentTask());
        derivedTask(newTask, false, true, null, null);
    }

    public void singlePremiseTask(Sentence newSentence, BudgetValue newBudget) {
        if (!newBudget.aboveThreshold())
            return;
        
        Task newTask = new Task(newSentence, newBudget, getCurrentTask());
        derivedTask(newTask, false, true, null, null);
    }

    
    /**
     * Immediate processing of a new task, in constant time Local processing, in
     * one concept only
     */
    public static class ImmediateProcess extends NAL  {
        private final Task task;
        private final int numSiblingTasks;
        
        public ImmediateProcess(Memory mem, Task currentTask, int numSiblingTasks) {
            super(mem);
            this.task = currentTask;
            this.numSiblingTasks = numSiblingTasks;
        }

        @Override
        public NAL call()  {
            setCurrentTask(task);
            
            mem.logic.TASK_IMMEDIATE_PROCESS.commit();
            emit(TaskImmediateProcess.class, task);

            setCurrentTerm(currentTask.getContent());
            currentConcept = mem.conceptualize(BudgetFunctions.budgetNewTaskConcept(task, numSiblingTasks), getCurrentTerm());
            
            if (getCurrentConcept() != null) {

                mem.conceptActivate(getCurrentConcept(), currentTask.budget);

                boolean processed = getCurrentConcept().directProcess(this, currentTask);

                if (processed)
                    mem.event.emit(Events.ConceptDirectProcessedTask.class, currentTask);            

            }
            
            boolean stmUpdated = mem.executive.inductionOnSucceedingEvents(currentTask, this);
            if (stmUpdated) {
                mem.logic.SHORT_TERM_MEMORY_UPDATE.commit();
            }

            return this;
        }
        
        
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

    public long getTime() { return mem.time(); }
    
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
        if (newStamp==null) {
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
    
    interface StampBuilder {  Stamp build();    }
    
    /** creates a lazy/deferred StampBuilder which only constructs the stamp if getTheNewStamp() is actually invoked */
    public void setTheNewStamp(final Stamp first, final Stamp second, final long time) {        
        newStamp = null;
        newStampBuilder = new StampBuilder() {
            @Override public Stamp build() {
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
        return mem;
    }

    
//    public Future immediateProcess(Memory mem, Task t, ExecutorService exe) {
//        return exe.submit(new Runnable() {
//            @Override public void run() {
//                immediateProcess(mem, t);
//            }            
//        });
//    }

}
