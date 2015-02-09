package nars.logic.nal8;


import nars.core.Memory;
import nars.logic.entity.CompoundTerm;
import nars.logic.entity.Task;
import nars.logic.entity.Term;

import java.util.List;

/** an operation that executes immediately, and without logical consequences;
 *  used for system control functions  */
abstract public class ImmediateOperation extends Operation {


    public ImmediateOperation() {
        super();
    }

    abstract public void execute(Memory m);


    //TODO make Task an interface so this is lightweight
    static class ImmediateTask extends Task {

        private final ImmediateOperation imm;

        ImmediateTask(ImmediateOperation o) {
            super();
            this.imm = o;
        }

        @Override
        public CompoundTerm getTerm() {
            return imm;
        }
    }

    /** create a new task that wraps this operation */
    public Task newTask() {
        return new ImmediateTask(this);
    }
}
