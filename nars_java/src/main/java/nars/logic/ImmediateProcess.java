/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic;

import nars.core.Events;
import nars.core.Memory;
import nars.logic.entity.Task;

/**
 * Immediate processing of a new task, in constant time Local processing, in
 * one concept only
 */
public class ImmediateProcess extends NAL {
    final Task task;

    public ImmediateProcess(Memory mem, Task task) {
        super(mem);

        this.task = task;        
    }
    
    

    @Override
    public String toString() {
        return "ImmediateProcess[" + task.toString() + "]";
    }
    
    

    @Override
    public void run() {
        setCurrentTask(task);
        setCurrentTerm(currentTask.getTerm());
        setCurrentConcept(memory.conceptualize(currentTask.budget, getCurrentTerm()));
        if (getCurrentConcept() != null) {
            boolean processed = getCurrentConcept().directProcess(this, currentTask);
            if (processed) {
                memory.event.emit(Events.ConceptDirectProcessedTask.class, currentTask, getCurrentConcept());
            }
        }

        memory.logic.TASK_IMMEDIATE_PROCESS.hit();
        emit(Events.TaskImmediateProcess.class, task, this);
    }
    
}
