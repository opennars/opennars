/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal;

import nars.Events;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.nal.concept.Concept;

/**
 * "Direct" processing of a new task, in constant time Local processing,
 * involving one concept only
 */
public class DirectProcess extends NAL {


    public DirectProcess(Memory mem, Task task) {
        super(mem, task);
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


    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("DirectProcess[");
        getCurrentTask().appendWithBudget(sb);
        sb.append(']');

        return sb.toString();

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


        if (!task.aboveThreshold())
            return null;
            //throw new RuntimeException("ImmediateProcess created for sub-threshold task: " + task);


        //System.err.println("direct: " + task);
        DirectProcess d = new DirectProcess(m, task);

//        if (task.isInput())
//            m.emit(Events.IN.class, task); //TODO use a different event than IN

        d.run();
        return d;
    }

}
