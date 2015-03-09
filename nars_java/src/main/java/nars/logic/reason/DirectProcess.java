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
 * "Direct" processing of a new task, in constant time Local processing,
 * involving one concept only
 */
public class DirectProcess extends NAL {


    public DirectProcess(Memory mem, Task task) {
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
    public void process() {
        Concept c = memory.conceptualize(currentTask.budget, getCurrentTask().getTerm());
        if (c == null) return;



        if (c.directProcess(this)) {

            c.link(currentTask);

            emit(Events.TaskImmediateProcessed.class, getCurrentTask(), this, c);
            memory.logic.TASK_IMMEDIATE_PROCESS.hit();
        }
    }


}
