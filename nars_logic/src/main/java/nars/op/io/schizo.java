package nars.op.io;


import com.google.common.collect.Lists;
import nars.Memory;
import nars.nal.Task;
import nars.nal.nal1.Inheritance;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SynchOperator;
import nars.nal.term.Atom;
import nars.nal.term.Term;
import nars.nal.term.Variable;

import java.util.List;

/** sets the memory's current SELF term; warning: can cause mental disturbance */
public class schizo extends SynchOperator {


    @Override
    protected List<Task> execute(Operation operation, Memory memory) {

        Term x = operation.arg().term(0);
        if (x instanceof Atom) {
            Atom oldSelf = memory.self();
            Atom newSelf = (Atom) x;
            memory.setSelf(newSelf);

            Variable v = Variable.theDependent();

            return Lists.newArrayList(
                memory.newTask(
                        Implication.make(
                                Inheritance.make(v, oldSelf),
                                Inheritance.make(v, newSelf),
                                TemporalRules.ORDER_FORWARD
                        )
                ).belief().present().get()
            );
        }
        else {
            return Lists.newArrayList(
                    new Echo("Invalid non-Atom parameter " + x + " for " + this).newTask()
            );
        }
    }

//    @Override
//    public boolean isImmediate() {
//        return true;
//    }
}
