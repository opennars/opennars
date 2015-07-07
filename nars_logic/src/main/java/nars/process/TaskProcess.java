/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.meter.LogicMetrics;
import nars.task.Task;
import nars.term.Compound;

/**
 * "Direct" processing of a new task, in constant time Local processing,
 * involving one concept only
 */
public class TaskProcess extends NAL {

    public TaskProcess(Memory mem, Task task) {
        super(mem, task);
    }

    /** runs the entire process in a constructor, for when a Concept is provided */
    public TaskProcess(DefaultConcept c, Task task) {
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
        getTask().appendWithBudget(sb);
        sb.append(']');

        return sb.toString();

    }



    @Override
    public void process() {
        Concept c = memory.conceptualize(getTask(), getTask().getTerm());
        if (c!=null)
            process(c);
        
    }

    protected void process(final Concept c) {
        setCurrentTerm(currentTask.getTerm());

        if (processConcept(c)) {

            c.link(currentTask);

            emit(TaskProcess.class, getTask(), this, c);
            memory.logic.TASK_IMMEDIATE_PROCESS.hit();
        }
    }

    /**
     * Directly process a new task. Called exactly once on each task. Using
     * local information and finishing in a constant time. Provide feedback in
     * the taskBudget value of the task.
     * <p>
     * called in Memory.immediateProcess only
     *
     * @param nal  reasoning context it is being processed in
     * @param task The task to be processed
     * @return whether it was processed
     */
    protected boolean processConcept(final Concept c) {


        if (!c.isActive()) return false;

        final Task task = getTask();

        if (!c.processable(task)) {
            memory.removed(task, "Filtered by Concept");
            return false;
        }

        //share the same Term instance for fast comparison and reduced memory usage (via GC)
        task.setTermInstance((Compound) c.getTerm());

        final char type = task.sentence.punctuation;
        final LogicMetrics logicMeter = memory.logic;

        switch (type) {
            case Symbols.JUDGMENT:

                if (c.hasBeliefs() && c.isConstant())
                    return false;

                if (!c.processBelief(this, task))
                    return false;

                logicMeter.JUDGMENT_PROCESS.hit();
                break;
            case Symbols.GOAL:
                if (!c.processGoal(this, task))
                    return false;
                logicMeter.GOAL_PROCESS.hit();
                break;
            case Symbols.QUESTION:
                c.processQuest(this, task);
                logicMeter.QUESTION_PROCESS.hit();
                break;
            case Symbols.QUEST:
                c.processQuestion(this, task);
                logicMeter.QUESTION_PROCESS.hit();
                break;
            default:
                throw new RuntimeException("Invalid sentence type: " + task);
        }

        return true;

    }

    public static TaskProcess run(final NAR nar, final String task) {
        return run(nar.memory, nar.task(task));
    }

    /** create and execute a direct process immediately */
    public static TaskProcess run(final NAR nar, final Task task) {
        return run(nar.memory, task);
    }


    public static TaskProcess get(final Memory m, final Task task) {
        if (!task.summaryGreaterOrEqual(m.param.taskProcessThreshold)) {
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

        d.run();
        return d;
    }


}
