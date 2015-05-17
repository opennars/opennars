package nars.nal.nal8;

import com.google.common.collect.Lists;
import nars.Memory;
import nars.nal.Task;
import nars.nal.concept.Concept;
import nars.nal.term.Term;
import nars.op.io.Echo;

import java.util.List;

/**
 * Operator which executes synchronously (in current reasoner thread).
 * Should be used only if the operation procedure will not take long
 * and block the reasoner thread.
 */
abstract public class SynchOperator extends Operator {

    public SynchOperator(Term term) {
        super(term);
    }

    public SynchOperator(String name) {
        super(name);
    }

    public SynchOperator() {
    }

    /**
     * The standard way to carry out an operation, which invokes the execute
     * method defined for the operate, and handles feedback tasks as input
     *
     * @param op     The operate to be executed
     * @param memory The memory on which the operation is executed
     * @return true if successful, false if an error occurred
     */
    public boolean execute(final Operation op, final Concept c, final Memory memory) {

        final Term[] args = op.arg().term;

        List<Task> feedback;
        try {
            feedback = execute(op, memory);
        } catch (Exception e) {
            feedback = Lists.newArrayList(new Echo(getClass(), e.toString()).newTask());
            e.printStackTrace();
        }

        executed(op, feedback, memory);

        return true;

    }

}
