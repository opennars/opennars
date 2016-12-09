/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.control;

import nars.util.Events;
import nars.storage.Memory;
import nars.entity.Task;

/**
 * Immediate processing of a new task, in constant time Local processing, in
 * one concept only
 */
public class ImmediateProcess extends DerivationContext {
    final Task task;

    public ImmediateProcess(Memory mem, Task currentTask) {
        super(mem);
        this.task = currentTask;
    }

    @Override
    public void run() {
        setCurrentTask(task);
        setCurrentTerm(currentTask.getTerm());
        setCurrentConcept(memory.conceptualize(currentTask.budget, getCurrentTerm()));
        if (getCurrentConcept() != null) {
            boolean processed = getCurrentConcept().directProcess(this, currentTask);
            if (processed) {
                memory.event.emit(Events.ConceptDirectProcessedTask.class, currentTask);
            }
        }
        
         if (!currentTask.sentence.isEternal()) {
            boolean stmUpdated = memory.eventInference(currentTask, this);
            //if (stmUpdated) {
                //memory.logic.SHORT_TERM_MEMORY_UPDATE.commit();
            //}
        }
        
        //memory.logic.TASK_IMMEDIATE_PROCESS.commit();
        emit(Events.TaskImmediateProcess.class, task, this);
    }
    
}
