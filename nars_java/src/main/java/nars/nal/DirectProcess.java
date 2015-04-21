/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal;

import nars.Events;
import nars.Memory;
import nars.Global;
import nars.NAR;
import nars.budget.Budget;
import nars.nal.concept.Concept;

/**
 * "Direct" processing of a new task, in constant time Local processing,
 * involving one concept only
 */
public class DirectProcess extends NAL {


    public DirectProcess(Memory mem, Task task) {
        super(mem, task);

        if (Global.DEBUG) {
            if (!task.aboveThreshold())
                throw new RuntimeException("ImmediateProcess created for sub-threshold task: " + task);
        }
    }

    /** runs the entire process in a constructor, for when a Concept is provided */
    public DirectProcess(Concept c, Task task) {
        this(c.memory, task);

        onStart();
        process(c);
        onFinished();
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
        Concept c = memory.conceptualize(getCurrentTask(), getCurrentTask().getTerm());
        if (c == null) return;

        process(c);

    }

    protected void process(Concept c) {
        if (c.directProcess(this)) {

            c.link(currentTask);

            emit(Events.TaskImmediateProcessed.class, getCurrentTask(), this, c);
            memory.logic.TASK_IMMEDIATE_PROCESS.hit();
        }
    }

    public static DirectProcess run(NAR nar, String task) {
        return run(nar.memory, nar.task(task));
    }

    /** create and execute a direct process immediately */
    public static DirectProcess run(NAR nar, Task task) {
        return run(nar.memory, task);
    }

    /** create and execute a direct process immediately */
    public static DirectProcess run(Memory m, Task task) {
        //System.err.println("direct: " + task);
        DirectProcess d = new DirectProcess(m, task);
        d.run();
        return d;
    }
}
