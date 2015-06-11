/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.process;

import nars.Memory;
import nars.NAR;
import nars.nal.NAL;
import nars.nal.Task;
import nars.nal.concept.Concept;

/**
 * "Direct" processing of a new task, in constant time Local processing,
 * involving one concept only
 */
public class TaskProcess extends NAL {

    public TaskProcess(Memory mem, Task task) {
        super(mem, task);
    }

    /** runs the entire process in a constructor, for when a Concept is provided */
    public TaskProcess(Concept c, Task task) {
        this(c.getMemory(), task);

        onStart();
        process(c); //WARNING this will avoid conceptualizing the concept
        onFinished();
    }


    @Override
    protected void onFinished() {


    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        getCurrentTask().appendWithBudget(sb);
        sb.append(']');

        return sb.toString();

    }



    @Override
    public void process() {
        Concept c = memory.conceptualize(getCurrentTask(), getCurrentTask().getTerm());
        if (c!=null)
            process(c);
        
    }

    protected void process(final Concept c) {
        setCurrentTerm(currentTask.getTerm());

        if (c.process(this)) {

            c.link(currentTask);

            emit(TaskProcess.class, getCurrentTask(), this, c);
            memory.logic.TASK_IMMEDIATE_PROCESS.hit();
        }
    }

    public static TaskProcess run(final NAR nar, final String task) {
        return run(nar.memory, nar.task(task));
    }

    /** create and execute a direct process immediately */
    public static TaskProcess run(final NAR nar, final Task task) {
        return run(nar.memory, task);
    }


    public static TaskProcess get(final Memory m, final Task task) {
        if (!task.aboveThreshold(m.param.taskProcessThreshold)) {
            m.removed(task, "Insufficient budget");
            return null;
        }
        //throw new RuntimeException("ImmediateProcess created for sub-threshold task: " + task);


        return new TaskProcess(m, task);
    }

    /** create and execute a direct process immediately */
    public static TaskProcess run(final Memory m, final Task task) {
        TaskProcess d = get(m, task);
        if (d == null)
            return null;


//        if (task.isInput())
//            m.emit(Events.IN.class, task); //TODO use a different event than IN

        d.run();
        return d;
    }


}
