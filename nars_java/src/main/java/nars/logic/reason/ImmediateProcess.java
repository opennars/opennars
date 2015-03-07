/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic.reason;

import nars.core.Events;
import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.NAL;
import nars.logic.entity.*;

/**
 * Immediate processing of a new task, in constant time Local processing, in
 * one concept only
 */
public class ImmediateProcess extends NAL {


    public ImmediateProcess(Memory mem, Task task) {
        super(mem, task);

        if (Parameters.DEBUG) {
            if (!task.aboveThreshold())
                throw new RuntimeException("ImmediateProcess created for sub-threshold task: " + task);
        }
    }

    @Override
    protected void onFinished() {

        if (newTasks!=null && !newTasks.isEmpty())
            memory.taskAdd(newTasks);

    }

    @Override
    public String toString() {
        return "ImmediateProcess[" + getCurrentTask().toString() + ']';
    }



    @Override
    public void reason() {
        Term t = getCurrentTask().getTerm();
//        if (t instanceof Variable) {
//            throw new RuntimeException("Variables, like " + t +", are invalid task term for ImmediateProcess");
//        }
//        if (t.hasVar()) {
//            CompoundTerm ct = (CompoundTerm)t;
//
//        }

        Concept c = memory.conceptualize(currentTask.budget, t);
        if (c == null) return;

        if (c.directProcess(this, currentTask)) {

            c.link(currentTask);

            emit(Events.TaskImmediateProcessed.class, getCurrentTask(), this, c);
            memory.logic.TASK_IMMEDIATE_PROCESS.hit();
        }
    }


}
