/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core.control;

import nars.core.Events;
import nars.core.Memory;
import nars.entity.Task;

/**
 * Immediate processing of a new task, in constant time Local processing, in
 * one concept only
 */
public class ImmediateProcess extends NAL {
    Task task;
    int numSiblingTasks;

    /** when using this constructor, make sure to setup with init(..) before use */
    public ImmediateProcess(Memory mem) {
        super(mem);
    }

    public ImmediateProcess(Memory mem, Task currentTask, int numSiblingTasks) {
        super(mem);
        init(currentTask, numSiblingTasks);
    }

    public void init(Task task, int numSiblingTasks) {
        super.reset();
        this.task = task;
        this.numSiblingTasks = numSiblingTasks;
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
                memory.event.emit(Events.ConceptDirectProcessedTask.class, currentTask);
            }
        }
        if (!currentTask.sentence.isEternal()) {
            boolean stmUpdated = memory.inductionOnSucceedingEvents(currentTask, this);
            if (stmUpdated) {
                memory.logic.SHORT_TERM_MEMORY_UPDATE.commit();
            }
        }
        memory.logic.TASK_IMMEDIATE_PROCESS.commit();
        emit(Events.TaskImmediateProcess.class, task, this);
    }
    
}
