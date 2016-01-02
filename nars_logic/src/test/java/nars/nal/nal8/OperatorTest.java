package nars.nal.nal8;

import nars.$;
import nars.NAR;
import nars.Op;
import nars.nal.nal4.Product;
import nars.nal.nal8.operator.SyncOperator;
import nars.nar.Default;
import nars.nar.Terminal;
import nars.task.Task;
import nars.term.Term;
import nars.util.meter.TestNAR;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperatorTest {

    @Test public void testMustExecuteSuccess() {


        NAR n = new Default(100, 1, 1, 1);
        TestNAR t = new TestNAR(n);
        t.mustExecute(0, 1, "operator");

        n.input("operator()!");
        t.run();

    }

    @Test public void testMustExecuteFailure() {

        try {
            NAR n = new Default(100, 1, 1, 1);
            TestNAR t = new TestNAR(n);
            t.mustExecute(0, 1, "operator");

            n.input("xoperator()!");

            t.run();
            assertTrue(false);
        }
        catch (AssertionError e) {
            //failure should occurr
            assertTrue(true);
        }
    }


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
        Operation o = $.oper(Operator.the("x"), Product.make("x"));
        assertEquals(Op.INHERITANCE, o.op());
    }

    @Test public void testInhIsOperation() {
        Operation o = $.$("<(a,b,c)-->^x>");
        assertTrue(o.getSubject() instanceof Product);
        assertTrue(o.getPredicate() instanceof Operator);
        assertEquals("x(a, b, c)", o.toString(true));
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
        n.onExec(new SyncOperator("exe") {
            @Override
            public List<Task> apply(Task<Operation> operation) {
                executed.set(true);
                return null;
            }
        });

        //n.trace();
        n.input("exe(a,b,c)!");

        n.frame(1);

        assertTrue(executed.get());

        assertNotNull("should have conceptualized or linked to ^exe",
                n.concept("^exe"));
        assertNull("^exe should not conceptualize or link to atom exe",
                n.concept("exe"));


    }

    @Test public void testCompoundOperator() {

        AtomicBoolean executed = new AtomicBoolean(false);

        NAR n = new Default();
        n.onExec(new SyncOperator((Term)n.term("<a --> b>")) {
            public List<Task> apply(Task<Operation> operation) {
                executed.set(true);
                return null;
            }
        });

        n.input("<a --> b>(a,b,c)!");

        n.frame(1);

        assertTrue(executed.get());

    }

    @Test public void testPatternMap() {
        AtomicInteger count = new AtomicInteger();

        PatternFunction f = new PatternFunction("(%A,%B)") {
            @Override
            public List<Task> run(Task<Operation> operationTask, Map<Term, Term> map1) {
                System.out.println(this.pattern + " " + operationTask + "\n\t" + map1);
                count.getAndIncrement();
                return null;
            }
        };
        Terminal t = new Terminal();

        Task matching = t.task("(x,y).");
        f.apply(matching);

        Task nonMatching = t.task("(x,y,z).");
        f.apply(nonMatching);

        //should only be triggered once, by the matching term
        assertEquals(1, count.get());
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
