package nars.nal.nal8.operator;

import nars.nal.nal8.Operation;

/**
 * Executes in the NAR's threadpool
 */
abstract public class AsynchOperator extends SynchOperator {

    @Override public boolean execute(final Operation op) {
        nar.taskLater(() -> {
            super.execute(op);
        });
        return true;
    }

}
