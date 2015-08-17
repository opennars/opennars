package nars.nal.nal8.operator;

import com.google.common.collect.Lists;
import nars.Memory;
import nars.nal.nal8.OpReaction;
import nars.nal.nal8.Operation;
import nars.op.io.Echo;
import nars.task.Task;

import java.util.List;

/**
 * Executes in the NAR's threadpool
 */
abstract public class AsynchOperator extends SynchOperator {

    @Override public boolean execute(final Operation op) {
        op.getMemory().taskLater(() -> {
            super.execute(op);
        });
        return true;
    }

}
