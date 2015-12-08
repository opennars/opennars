package nars.nal.nal8;


import nars.$;
import nars.nal.nal4.Product;
import nars.nal.nal8.operator.NullOperator;
import nars.task.Task;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;

import java.util.List;
import java.util.function.Consumer;

/** an operation that executes immediately, and without logical consequences;
 *  used for system control functions  */
abstract public class ImmediateOperator extends NullOperator implements Consumer<Task<Operation>> {

    public final Operator op;

    public ImmediateOperator() {
        super();
        op = Operator.the(getOperatorTerm());
    }

    public Operation newOperation(Object...args) {
        return new Operation(op, termizedProduct(args));
    }

    /** apply Atom.quoteI */
    static Product termizedProduct(final Object... args) {
        if (args.length == 0) return Product.empty;
        Term[] x = termized(args);
        return Product.make(x);
    }

    static Term[] termized(Object... args) {
        Term[] x = new Term[args.length];
        for (int i = 0; i < args.length; i++) {
            final Term y;
            final Term xx = x[i];
            if (!(args[i] instanceof Term)) {
                y = Atom.quote(args[i].toString());
            }
            else {
                y = xx;
            }
            x[i] = y;
        }
        return x;
    }

//    /** create a new task that wraps this operation */
//    public Task<Operation> newTask(Operation o) {
//        return new AbstractTask(newOperation(o.args()),
//                Symbols.COMMAND,
//                null, 0, 0, 0);
//    }

    @Override
    public List<Task> apply(Task<Operation> o) {
        accept(o);
        return null;
    }

    public static Task command(Class<? extends ImmediateOperator> opClass, Object... args) {
        return Task.command(operation(opClass, args));
    }

    public static Compound operation(Class<? extends ImmediateOperator> opClass, Object... args) {
        return operation( opClass, termizedProduct(args));
    }
    public static Compound operation(Class<? extends ImmediateOperator> opClass, Product args) {
        return $.oper(
                $.$(opClass.getSimpleName().toString()),
                args);
    }


    //    //TODO make Task an interface so this is lightweight
//    public static class ImmediateTask extends DefaultTask<Compound> {
//
//        public final ImmediateOperator operation;
//
//        ImmediateTask(ImmediateOperator o) {
//            super(null,null,null,null);
//            this.operation = o;
//        }
//
//
//        public ImmediateOperator immediateOperation() {
//            return operation;
//        }
//
//        @Override
//        public CharSequence stampAsStringBuilder() {
//            return "";
//        }
//
//        /**
//         * @return true if it was immediate
//         */
//        @Override
//        public boolean executeIfImmediate(Memory memory) {
////            final Term taskTerm = get();
////            if (taskTerm instanceof Operation) {
////                Operation o = (Operation) taskTerm;
////                o.setTask((Task<Operation>) this);
////
////
////                if (o instanceof ImmediateOperation) {
//                    if (getPunctuation()!= Symbols.GOAL)
//                        throw new RuntimeException("ImmediateOperation " + immediateOperation() + " was not specified with goal punctuation");
//
//                    immediateOperation().execute(memory);
//
//                    return true;
//                //}
////            else if (o.getOperator().isImmediate()) {
////                if (sentence!=null && getPunctuation()!= Symbols.GOAL)
////                    throw new RuntimeException("ImmediateOperator call " + o + " was not specified with goal punctuation");
////
////                o.getOperator().execute(o, memory);
////                return true;
////            }
//            //}
//
////            return false;
//        }
//
//
//        @Override
//        public String toString() {
//            return operation.toString();
//        }
//    }



}
