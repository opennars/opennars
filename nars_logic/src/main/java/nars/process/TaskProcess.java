/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.budget.Budget;
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

    public TaskProcess(NAR nar, Task task) {
        super(nar);

        if (task.isDeleted()) {
            throw new RuntimeException("task is deleted");
        }

        this.task = task;
    }

    @Override
    public Task getBelief() {
        return null;
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
        return nar.concept(getTerm());
    }



    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getSimpleName()).append('[');

        getTask().toString(sb, nar.mem(), true, false, true);

        sb.append(']');

        return sb.toString();

    }




    @Override final public void derive() {

        final Task task = getTask();

        /** deleted in the time between this was created, and run() */
        if (task.isDeleted()) {
            return;
        }

        final Memory memory = this.nar.mem();

        final Concept c = memory.conceptualize(task, task.getBudget());

        if (c==null) {
            memory.removed(task, "Unable to conceptualize");
            return;
        }

        if (processConcept(c)) {

            c.link(task);

            memory.eventTaskProcess.emit(this);

            memory.logic.TASK_PROCESS.hit();
            memory.emotion.busy(task, this);
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



        //share the same Term instance for fast comparison and reduced memory usage (via GC)
        task.setTermShared((Compound) c.getTerm());

        final LogicMeter logicMeter = nar.memory().logic;

        switch (task.getPunctuation()) {

            case Symbols.JUDGMENT:

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
    final protected void afterDerive() {
        inputDerivations();
    }

//    /** create and execute a direct process immediately */
//    public static Premise queue(final NAR nar, final Task task) {
//        return run(nar.memory, task);
//    }


    public static TaskProcess get(final NAR m, final Task task) {
        return get(m, task, 1f);
    }

    public static TaskProcess get(final NAR nar, final Task task, float inputPriorityFactor) {

        final Budget taskBudget = task.getBudget();

        if (inputPriorityFactor!=1f) {
            taskBudget.mulPriority( inputPriorityFactor );
        }

        if (!taskBudget.summaryGreaterOrEqual(nar.mem().taskProcessThreshold)) {
            nar.mem().removed(task, "Insufficient budget");
            return null;
        }

        return new TaskProcess(nar, task);
    }

    /** create and execute a direct process immediately */
    public static Premise run(final NAR m, final Task task) {
        TaskProcess d = get(m, task);
        if (d == null)
            return null;

        d.run();

        return d;
    }


//    /**
//     * queues a batch process run of TaskProcess's to their
//     * relevant concepts after conceptualizing them with
//     * the budget
//     *
//     * the input reverse sorted task list is divided into 3 sections:
//     *
//     *      0      ..    discarded   == to remove from the system
//     *           --ignore--          == (they are still in the newTaskBuffer this sort was generated from)
//     *      (last-remaining)..last   == to input (highest pri first)
//     *
//     * */
//    public static void run(final Memory memory, final List<Task> reverseSorted, final int toRun, final int toDiscard) {
//
//        final int size = reverseSorted.size();
//        if (size == 0) return;
//
//        if (toRun + toDiscard > size)
//            throw new RuntimeException("invalid buffer positions; size=" + size + ", toRun=" + toRun + ", toDiscard=" + toDiscard);
//
//        for (int i = 0; i < toDiscard; i++) {
//            memory.removed( reverseSorted.get(i) );
//        }
//
//        final int bottomPoint = Math.max(size-toRun, toDiscard);
//        //final TaskProcess[] r = new TaskProcess[size-bottomPoint];
//
//        //int j = 0;
//        for (int i = size-1; i >= bottomPoint; i--) {
//
//            final TaskProcess tp = new TaskProcess(memory, reverseSorted.get(i));
//            tp.run();
//
//            //target.accept();
//
//            //r[j++] =
//        }
//
//    }


}
