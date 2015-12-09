package nars.nal.nal8.operator;

import nars.nal.nal8.OperatorReaction;

/**
 * Executes in the NAR's threadpool
 */
public abstract class AsynchOperator extends OperatorReaction {

//    @Override public boolean execute(final Task<Operation> op) {
//        nar.runAsync(() -> super.execute(op));
//        return true;
//    }

    @Override public boolean async() { return true; }

}
