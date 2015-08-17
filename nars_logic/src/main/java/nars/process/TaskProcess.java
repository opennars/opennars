/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.meter.LogicMeter;
import nars.premise.Premise;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;

/**
 * "Direct" processing of a new task, in constant time Local processing,
 * involving one concept only
 */
public class TaskProcess extends NAL {

    public final Task task;

    public TaskProcess(Memory mem, Task task) {
        super(mem);
        this.task = task;
    }

    @Override public Task getTask() {
        return task;
    }

    @Override public final Term getTerm() {
        return getTask().getTerm();
    }

    @Override public TermLink getTermLink() {
        return null;
    }

    @Override public TaskLink getTaskLink() {
        return null;
    }

    @Override public Concept getConcept() {
        return memory.concept(getTerm());
    }



    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        getTask().appendWithBudget(sb);
        sb.append(']');

        return sb.toString();

    }



    @Override public void derive() {
        Concept c = memory.conceptualize(getTask().getTerm(), getTask());
        if (c==null) return;

        if (processConcept(c)) {

            final Task t = getTask();

            emit(TaskProcess.class, t, this, c);

            c.link(t);

            memory.logic.TASK_PROCESS.hit();
            memory.emotion.busy(t, this);
        }
        
    }



    /**
     * Directly process a new task. Called exactly once on each task. Using
     * local information and finishing in a constant time. Provide feedback in
     * the taskBudget value of the task.
     * <p>
     * called in Memory.immediateProcess only
     *
     * @return whether it was processed
     */
    protected boolean processConcept(final Concept c) {

        final Task task = getTask();

        if (!c.processable(task)) {
            removed(task, "Filtered by Concept");
            return false;
        }

        //share the same Term instance for fast comparison and reduced memory usage (via GC)
        task.setTermShared((Compound) c.getTerm());

        final LogicMeter logicMeter = memory.logic;

        switch (task.getPunctuation()) {

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

            default: throw new RuntimeException("Invalid sentence type: " + task);
        }

        return true;

    }

    @Override
    protected void afterDerive() {
        if (derived!=null) {
            memory.add(derived);
        }
    }

    public static Premise run(final NAR nar, final String task) {
        return run(nar.memory, nar.task(task));
    }

    /** create and execute a direct process immediately */
    public static Premise run(final NAR nar, final Task task) {
        return run(nar.memory, task);
    }


    public static TaskProcess get(final Memory m, final Task task) {
        return get(m, task, 1f);
    }

    public static TaskProcess get(final Memory m, final Task task, float inputPriorityFactor) {

        if (inputPriorityFactor!=1f)
            task.mulPriority( inputPriorityFactor );

        if (!task.summaryGreaterOrEqual(m.param.taskProcessThreshold)) {
            m.removed(task, "Insufficient budget");
            return null;
        }

        return new TaskProcess(m, task);
    }

    /** create and execute a direct process immediately */
    public static Premise run(final Memory m, final Task task) {
        TaskProcess d = get(m, task);
        if (d == null)
            return null;

        d.run();

        return d;
    }



}
