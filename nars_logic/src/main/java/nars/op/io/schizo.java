package nars.op.io;


import nars.Memory;
import nars.nal.Task;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SynchOperator;

import java.util.List;

/** sets the memory's current SELF term; warning: can cause mental disturbance */
public class schizo extends SynchOperator {


    @Override
    protected List<Task> execute(Operation operation, Memory memory) {
        memory.setSelf(operation.arg());
        return null;
    }

//    @Override
//    public boolean isImmediate() {
//        return true;
//    }
}
