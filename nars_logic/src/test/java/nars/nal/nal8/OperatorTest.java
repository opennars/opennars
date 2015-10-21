package nars.nal.nal8;

import nars.$;
import nars.NAR;
import nars.Op;
import nars.nal.nal4.Product;
import nars.nal.nal8.operator.SyncOperator;
import nars.nar.Default;
import nars.task.Task;
import nars.term.Term;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertTrue;
import static org.jgroups.util.Util.assertEquals;

public class OperatorTest {



//
//    public void testIO(String input, String output) {
//
//        //TextOutput.out(nar);
//
//        nar.mustOutput(16, output);
//        nar.input(input);
//
//        nar.run(4);
//
//    }
//
//    @Test public void testOutputInVariablePosition() {
//        testIO("count({a,b}, #x)!",
//                "<2 --> (/,^count,{a,b},_,SELF)>. :|: %1.00;0.99%");
//    }

    @Test public void testOperationIsInheritance() {
        Operation o = $.opr(Product.make("x"), Operator.the("x"));
        assertEquals(Op.INHERITANCE, o.op());
    }

    @Test public void testTermReactionRegistration() {

        AtomicBoolean executed = new AtomicBoolean(false);

        NAR n = new Default();
        n.onExecTerm("exe", (Term[] event) -> {
            //System.out.println("executed: " + Arrays.toString(args));
            executed.set(true);
            return null;
        });

        n.input("exe(a,b,c)!");

        n.frame(1);

        assertTrue(executed.get());

    }

    @Test public void testSynchOperator() {


        AtomicBoolean executed = new AtomicBoolean(false);

        NAR n = new Default();
        n.on(new SyncOperator("exe") {
            @Override
            public List<Task> apply(Task<Operation> operation) {
                executed.set(true);
                return null;
            }
        });

        n.trace();
        n.input("exe(a,b,c)!");

        n.frame(2);

        assertTrue(executed.get());

    }

    @Test public void testCompoundOperator() {

        AtomicBoolean executed = new AtomicBoolean(false);

        NAR n = new Default();
        n.on(new SyncOperator((Term)n.term("<a --> b>")) {
            public List<Task> apply(Task<Operation> operation) {
                executed.set(true);
                return null;
            }
        });

        n.input("<a --> b>(a,b,c)!");

        n.frame(1);

        assertTrue(executed.get());

    }

//TODO: allow this in a special eval operator

//    //almost finished;  just needs condition to match the occurence time that it outputs. otherwise its ok
//
//    @Test
//    public void testRecursiveEvaluation1() {
//        testIO("add( count({a,b}), 2)!",
//                "<(^add,(^count,{a,b},SELF),2,$1,SELF) =/> <$1 <-> 4>>. :|: %1.00;0.90%"
//        );
//    }
//
//    @Test public void testRecursiveEvaluation2() {
//        testIO("count({ count({a,b}), 2})!",
//                "<(^count,{(^count,{a,b},SELF),2},$1,SELF) =/> <$1 <-> 1>>. :|: %1.00;0.90%"
//        );
//    }
}
