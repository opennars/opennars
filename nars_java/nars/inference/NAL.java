package nars.inference;

import java.util.ArrayList;
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
import static nars.inference.RuleTables.reason;
import static nars.inference.RuleTables.transformTask;
import nars.io.Symbols;
import nars.language.Negation;
import nars.language.Term;
import nars.language.Variable;
import nars.operator.Operation;

/**
 * NAL Reasoner Thread.  Includes all reasoning process state.
 */
public class NAL {
    
    protected Memory mem;
    
    protected Term currentTerm;

    protected Concept currentConcept;

    protected Task currentTask;

    protected TermLink currentBeliefLink;
    protected TaskLink currentTaskLink;

    protected Sentence currentBelief;    

    protected Stamp newStamp;       

    public NAL() {

    }

    public void fire(final Memory memory, final Concept concept, final TaskLink currentTaskLink) {
        reset(memory);
        
        final Task task = currentTaskLink.getTargetTask();
        
        setCurrentTerm(concept.term);
        setCurrentTaskLink(currentTaskLink);
        setCurrentBeliefLink(null);
        setCurrentTask(task);  // one of the two places where this variable is set
        
        memory.logic.TASKLINK_FIRE.commit(currentTaskLink.budget.getPriority());        
        if (memory.getRecorder().isActive()) {
            memory.getRecorder().append("TaskLink Select", currentTaskLink.toStringBrief());
        }
        
        
        if (currentTaskLink.type == TermLink.TRANSFORM) {
            setCurrentBelief(null);
            transformTask(currentTaskLink, this);  // to turn this into structural inference as below?
        } else {
            int termLinkCount = memory.param.termLinkMaxReasoned.get();
//        while (memory.noResult() && (termLinkCount > 0)) {
            while (termLinkCount > 0) {
                final TermLink termLink = concept.selectTermLink(currentTaskLink, memory.getTime());
                if (termLink != null) {
                    
                    if (memory.getRecorder().isActive()) {
                        memory.getRecorder().append("TermLink Select", termLink.toString());
                    }
                    
                    setCurrentBeliefLink(termLink);

                    reason(currentTaskLink, termLink, this);
                    
                    concept.returnTermLink(termLink);
                    
                    termLinkCount--;
                    
                } else {
                    termLinkCount = 0;
                }
            }
        }
    }
    
    /**
     * Derived task comes from the inference rules.
     *
     * @param task the derived task
     */
    public boolean derivedTask(final Task task, final boolean revised, final boolean single, Sentence occurence, Sentence occurence2) {
        

        if (task.budget.aboveThreshold()) {
        
            if (task.sentence != null && task.sentence.truth != null) {
                  float conf = task.sentence.truth.getConfidence();                
                  if (conf == 0) { 
                      //no confidence - we can delete the wrongs out that way.
                      if (mem.getRecorder().isActive())
                          mem.getRecorder().onTaskRemove(task, "Ignored (zero confidence)");
                      task.end();
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
                if(chain.contains(currentBeliefContent)) {
                //if(stamp.chainContainsInstance(currentBeliefContent)) {
                    chain.remove(currentBeliefContent);
                }
                stamp.addToChain(currentBeliefContent);
            }


            //workaround for single premise task issue:
            if(currentBelief == null && single && currentTask != null && currentTask.sentence.isJudgment()) {
                if(chain.contains(currentTaskContent)) {
                //if(stamp.chainContainsInstance(currentTaskContent)) {
                    chain.remove(currentTaskContent);
                }
                stamp.addToChain(currentTaskContent);
            }
            //end workaround

            if (currentTask != null && !single && currentTask.sentence.isJudgment()) {
                if(chain.contains(currentTaskContent)) {                
                //if(stamp.chainContainsInstance(currentTaskContent)) {                    
                    chain.remove(currentTaskContent);
                }
                stamp.addToChain(currentTaskContent);
            }


            //its a inference rule, so we have to do the derivation chain check to hamper cycles
            if (!revised) { 

                for (int i = 0; i < chain.size(); i++) {
                    Term chain1 = chain.get(i);
                    Term tc = task.getContent();
                    if (task.sentence.isJudgment() && tc.equals(chain1)) {
                        Term ptc = task.getParentTask().getContent();
                        if(task.getParentTask()==null || 
                           (!(ptc.equals(Negation.make(tc))) && !(tc.equals(Negation.make(ptc))))) {
                            
                            if (mem.getRecorder().isActive()) {
                                mem.getRecorder().onTaskRemove(task, "Cyclic Reasoning (index " + i + ")");
                            }
                            task.end();
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
                            if (mem.getRecorder().isActive()) {                                
                                mem.getRecorder().onTaskRemove(task, "Overlapping Revision Evidence (i=" + i + ",j=" + j +')' /* + " in " + stamp.toString()*/);
                            }
                            task.end();
                            return false;
                        }
                    }
                }
            }
            
            mem.event.emit(Events.TaskDerived.class, task, revised, single, occurence, occurence2);

            if(task.sentence.content instanceof Operation) {
                Operation op=(Operation) task.sentence.content;
                if(op.getSubject() instanceof Variable || op.getPredicate() instanceof Variable) {
                    return false;
                }
            }

            mem.logic.TASK_DERIVED.commit(task.budget.getPriority());

            mem.output(task);

            mem.addNewTask(task, "Derived");
        }
        else {            
            if (mem.getRecorder().isActive())
                mem.getRecorder().onTaskRemove(task, "Ignored (insufficient budget)");
            task.end();
            return false;
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
    public void doublePremiseTaskRevised(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget) {
        Sentence newSentence = new Sentence(newContent, getCurrentTask().sentence.punctuation, newTruth, getTheNewStamp());
        Task newTask = new Task(newSentence, newBudget, getCurrentTask(), getCurrentBelief());
        derivedTask(newTask, true, false, null, null);
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
        if (newContent != null) {
            
            {
                final Sentence newSentence = new Sentence(newContent, getCurrentTask().sentence.punctuation, newTruth, getTheNewStamp());
                final Task newTask = new Task(newSentence, newBudget, getCurrentTask(), getCurrentBelief());
                boolean added=derivedTask(newTask, false, false, null, null);
                if(added && temporalAdd) {
                    mem.temporalRuleOutputToGraph(newSentence,newTask);
                }
            }
            
            if(temporalAdd) {
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
        Task parentTask = getCurrentTask().getParentTask();
                
        if (parentTask != null) {
            if (parentTask.getContent() == null)
                return;     
            if(newContent==null) {
                return;
            }
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
        Task newTask = new Task(newSentence, newBudget, getCurrentTask());
        derivedTask(newTask, false, true, null, null);
    }
    
//    public Future immediateProcess(Memory mem, Task t, ExecutorService exe) {
//        return exe.submit(new Runnable() {
//            @Override public void run() {
//                immediateProcess(mem, t);
//            }            
//        });
//    }
    
    /**
     * Immediate processing of a new task, in constant time Local processing, in
     * one concept only
     *
     * @param task the task to be accepted
     */
    public void immediateProcess(Memory mem, Task task) {
        reset(mem);
        currentTask = task;
        
        mem.logic.TASK_IMMEDIATE_PROCESS.commit();

        setCurrentTask(task); // one of the two places where this variable is set
        
        if (mem.getRecorder().isActive()) {
            mem.getRecorder().append("Task Immediate Process", task.toString());
        }
        
        setCurrentTerm(task.getContent());
        currentConcept = mem.conceptualize(getCurrentTerm());
        
        if (getCurrentConcept() != null) {
            
            mem.conceptActivate(getCurrentConcept(), task.budget);
            
            boolean processed = getCurrentConcept().directProcess(this, task);
            
            if (processed)
                mem.event.emit(Events.ConceptDirectProcessedTask.class, task);            
            
        }
        
    }

    
    
    protected void reset(Memory currentMemory) {
        mem = currentMemory;
        setCurrentTerm(null);
        setCurrentBelief(null);
        setCurrentConcept(null);
        setCurrentTask(null);
        setCurrentBeliefLink(null);
        setCurrentTaskLink(null);
        setNewStamp(null);
    }

    public long getTime() { return mem.getTime(); }
    
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
        return newStamp;
    }

    /**
     * @param newStamp the newStamp to set
     */
    public void setTheNewStamp(Stamp newStamp) {
        this.newStamp = newStamp;
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
}
