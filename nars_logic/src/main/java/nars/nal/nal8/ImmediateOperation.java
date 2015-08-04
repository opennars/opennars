package nars.nal.nal8;


import nars.Memory;
import nars.Symbols;
import nars.task.Task;
import nars.term.Compound;

/** an operation that executes immediately, and without logical consequences;
 *  used for system control functions  */
abstract public class ImmediateOperation  {


    public ImmediateOperation() {
        super();
    }

    abstract public void execute(Memory m);


    //TODO make Task an interface so this is lightweight
    public static class ImmediateTask extends Task<Compound> {

        public final ImmediateOperation operation;

        ImmediateTask(ImmediateOperation o) {
            super(Symbols.GOAL);
            this.operation = o;
        }


        public ImmediateOperation immediateOperation() {
            return operation;
        }


        /**
         * @return true if it was immediate
         */
        public boolean executeIfImmediate(Memory memory) {
//            final Term taskTerm = get();
//            if (taskTerm instanceof Operation) {
//                Operation o = (Operation) taskTerm;
//                o.setTask((Task<Operation>) this);
//
//
//                if (o instanceof ImmediateOperation) {
                    if (sentence!=null && getPunctuation()!= Symbols.GOAL)
                        throw new RuntimeException("ImmediateOperation " + immediateOperation() + " was not specified with goal punctuation");

                    immediateOperation().execute(memory);

                    return true;
                //}
//            else if (o.getOperator().isImmediate()) {
//                if (sentence!=null && getPunctuation()!= Symbols.GOAL)
//                    throw new RuntimeException("ImmediateOperator call " + o + " was not specified with goal punctuation");
//
//                o.getOperator().execute(o, memory);
//                return true;
//            }
            //}

//            return false;
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
