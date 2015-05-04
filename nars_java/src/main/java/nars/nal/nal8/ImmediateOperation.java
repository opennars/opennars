package nars.nal.nal8;


import nars.Memory;
import nars.nal.term.Compound;
import nars.nal.Task;

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
        public Compound getTerm() {
            return imm;
        }

        @Override
        public String toString() {
            return imm.toString();
        }
    }

    /** create a new task that wraps this operation */
    public ImmediateTask newTask() {
        return new ImmediateTask(this);
    }

    @Override
    public String toString() { return getClass().getSimpleName(); }

}
