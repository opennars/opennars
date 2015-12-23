package nars.nal.nal8;

import nars.$;
import nars.Memory;
import nars.NAR;
import nars.budget.Budget;
import nars.budget.UnitBudget;
import nars.nal.Compounds;
import nars.task.Task;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.util.event.Topic;

import java.util.Collections;
import java.util.List;

/**
 * Execution context which Operator implementations
 * receive, supporting any variety of synchronous/
 * asynchronous feedback, access to the invoking NAR,
 * and utility methods for extracting features
 * of the operation task in the context of the executing NAR.
 */
public class Execution implements Runnable {

    public final NAR nar;
    public final Task task;
    private final Topic<Execution> listeners;

    public Execution(NAR nar, Task task, Topic<Execution> listeners) {
        this.nar = nar;
        this.task = task;
        this.listeners = listeners;
    }

    /** should only be called by NAR */
    @Override public final void run() {
        if (task.isDeleted()) return;

        listeners.emit(this);
    }

    public final Compound term() {
        return task.term();
    }

    /**
     * unwrapped (without ^)
     */
    public final Term operator() {
        return Compounds.operatorTerm(term());
    }
    public final Term[] argArray() {
        return Compounds.opArgsArray(term());
    }


    //arg(int i)

    //feedback(Term t)
    //feedback(Task t)
    //feedback(Task[] t)
    //feedback(Object o)

    public void feedback(Task feedback) {
        feedback(Collections.singletonList(feedback));
    }

    /**
     * called after execution completed
     */
    public void feedback(List<Task> feedback) {

        //Display a message in the output stream to indicate the reportExecution of an operation


        if (!nar.memory.eventExecute.isEmpty()) {
            nar.memory.eventExecute.emit(
                new ExecutionResult(task, feedback)
            );
        }


        if (!task.isCommand()) {
            noticeExecuted(task);
        }

        //feedback tasks as input
        //should we allow immediate tasks to create feedback?
        if (feedback != null) {

            //final Operation t = op.getTerm();

            for (Task f : feedback) {
                //if (t == null) continue;

                f.log("Feedback");

                //TODO avoid using a string like this
                //f.log("Feedback: " + t /*"Feedback"*/);

                nar.input(f);
            }
        }

    }

    /**
     * internal notice of the execution
     * @param operation
     */
    protected void noticeExecuted(Task operation) {

        Budget b;
        b = !operation.isDeleted() ? operation.getBudget() : UnitBudget.zero;

        Memory memory = nar.memory;

        nar.input($.belief(operation.term(),

                operation.getTruth()).
                //1f, Global.OPERATOR_EXECUTION_CONFIDENCE).

                        budget(b).
                        present(memory).
                //parent(operation). //https://github.com/opennars/opennars/commit/23d34d5ddaf7c71348d0a70a88e2805ec659ed1c#diff-abb6b480847c96e2dbf488d303fb4962L235
                        because("Executed")
        );

        memory.logic.TASK_EXECUTED.hit();
    }

}
