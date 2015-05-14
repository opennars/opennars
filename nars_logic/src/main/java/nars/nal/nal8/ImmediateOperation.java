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
    public static class ImmediateTask extends Task {

        public final ImmediateOperation operation;

        ImmediateTask(ImmediateOperation o) {
            super();
            this.operation = o;
        }

        @Override
        public Compound getTerm() {
            return operation;
        }

        @Override
        public String toString() {
            return operation.toString();
        }
    }

    /** create a new task that wraps this operation */
    public ImmediateTask newTask() {
        return new ImmediateTask(this);
    }

    @Override
    public String toString() { return getClass().getSimpleName(); }

}
