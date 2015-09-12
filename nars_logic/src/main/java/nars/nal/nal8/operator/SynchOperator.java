package nars.nal.nal8.operator;

import nars.nal.nal8.OpReaction;
import nars.nal.nal8.Operation;
import nars.term.Term;

/**
 * Operator which executes synchronously (in current reasoner thread).
 * Should be used only if the operation procedure will not take long
 * and block the reasoner thread.
 */
abstract public class SynchOperator extends OpReaction {

    public SynchOperator(Term term) {
        super(term);
    }

    public SynchOperator(String name) {
        super(name);
    }

    /** uses the implementation class's simpleName as the term */
    public SynchOperator() {
        super((Term)null);
    }

    /**
     * The standard way to carry out an operation, which invokes the execute
     * method defined for the operate, and handles feedback tasks as input
     *
     * @param op     The operate to be executed
     * @return true if successful, false if an error occurred
     */
    @Override
    public boolean execute(final Operation op) {

        try {
            executed(op, apply(op));
        } catch (Exception e) {
            op.getMemory().eventError.emit(e);
            return false;
        }

        return true;
    }

}
