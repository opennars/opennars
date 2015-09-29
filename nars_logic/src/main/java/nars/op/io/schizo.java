package nars.op.io;


import nars.nal.nal8.ImmediateOperator;
import nars.nal.nal8.Operation;
import nars.task.Task;

/** sets the memory's current SELF term; warning: can cause mental disturbance */
public class schizo extends ImmediateOperator {

    @Override
    public void accept(Task<Operation> terms) {
//
//        Term x = operation.arg().term(0);
//        if (x instanceof Atom) {
//            Atom oldSelf = memory.self();
//            Atom newSelf = (Atom) x;
//            memory.setSelf(newSelf);
//
//            Variable v = Variable.theDependent();
//
//            return Lists.newArrayList(
//                memory.newTask(
//                        Implication.make(
//                                Inheritance.make(v, oldSelf),
//                                Inheritance.make(v, newSelf),
//                                TemporalRules.ORDER_FORWARD
//                        )
//                ).belief().present().get()
//            );
//        }
//        else {
//            return Lists.newArrayList(
//                    new Echo("Invalid non-Atom parameter " + x + " for " + this).newTask()
//            );
//        }
    }

//    @Override
//    public boolean isImmediate() {
//        return true;
//    }
}
