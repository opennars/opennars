package nars.nal.nal8;

import com.google.common.collect.Lists;
import nars.$;
import nars.NAR;
import nars.Op;
import nars.budget.Budget;
import nars.nar.Default;
import nars.nar.Terminal;
import nars.task.Task;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.transform.Subst;
import nars.util.meter.TestNAR;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static nars.$.$;
import static nars.util.Texts.i;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperatorTest {

    @Test public void testMustExecuteSuccess() {


        NAR n = new Default(100, 1, 1, 1);
        TestNAR t = new TestNAR(n);
        t.mustExecute(0, 1, "operator");

        n.input("operator()!");


    }

    @Test public void testMustExecuteFailure() {

        try {
            NAR n = new Default(100, 1, 1, 1);
            TestNAR t = new TestNAR(n);
            t.mustExecute(0, 1, "operator");

            n.input("xoperator()!");

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
        Compound o = $.oper(Operator.the("x"), $.p("x"));
        assertEquals(Op.INHERIT, o.op());
    }

    @Test public void testInhIsOperation() {
        Compound o = $("<(a,b,c)-->^x>");
        assertTrue(o.term(0).op(Op.PRODUCT));
        assertTrue(o.term(1) instanceof Operator);
        assertTrue(o.term(1).op(Op.OPERATOR));
        assertEquals("x(a,b,c)", o.toString());
        assertEquals(Op.INHERIT, o.op());
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
        n.onExec("exe", (exec) -> {
            executed.set(true);
        });

        //n.trace();
        n.input("exe(a,b,c)!");

        n.frame(1);

        assertTrue(executed.get());

        assertNotNull("should have conceptualized or linked to ^exe",
                n.concept("^exe"));
        assertNull("^exe should not conceptualize or link to atom exe",
                n.memory.index.getTermIfPresent($.the("exe")));


    }

    @Test public void testCompoundOperator() {

        AtomicBoolean executed = new AtomicBoolean(false);

        NAR n = new Default();

        n.onExec((Term)n.term("<a --> b>"), (exec) -> {
            executed.set(true);
        });

        n.input("<a --> b>(a,b,c)!");

        n.frame(1);

        assertTrue(executed.get());

    }

    @Test public void testPatternOperation() {
        AtomicInteger count = new AtomicInteger();

        PatternOperation f = new PatternOperation("(%A,%B)") {
            @Override
            public List<Task> run(Task operationTask, Subst map1) {
                //System.out.println(this.pattern + " " + operationTask + "\n\t" + map1);
                count.getAndIncrement();
                return null;
            }
        };
        Terminal t = new Terminal();

        Task matching = t.task("(x,y)!");
        f.apply(matching);

        assertEquals(1, count.get());

        Task nonMatching = t.task("(x,y,z)!");
        f.apply(nonMatching);

        //should only be triggered once, by the matching term
        assertEquals(1, count.get());
    }

    @Test public void testPatternAnswerer() {
        AtomicInteger count = new AtomicInteger();

        PatternAnswer f = new PatternAnswer("add(%a,%b,#x)") {
            @Override
            public List<Task> run(Task t, Subst s) {
                System.out.println(t + " " + s);
                assertEquals($("x"), s.getXY($("%a")));
                assertEquals($("y"), s.getXY($("%b")));
                count.getAndIncrement();
                return null;
            }
        };
        Terminal t = new Terminal();

        Task matching = t.task("add(x,y,#x)?");
        f.apply(matching);

        assertEquals(1, count.get()); //should only be triggered once, by the matching term

        Task nonMatching = t.task("add(x)?");
        f.apply(nonMatching);

        assertEquals(1, count.get()); //should only be triggered once, by the matching term

        Task nonMatching3 = t.task("add(x,y,z)?");
        f.apply(nonMatching3);

        assertEquals(1, count.get()); //should only be triggered once, by the matching term

        Task nonMatching2 = t.task("add(x,y,$x)?");
        f.apply(nonMatching2);

        assertEquals(1, count.get()); //should only be triggered once, by the matching term
    }

    @Test public void testPatternAnswererInNAR() {
        NAR n = new Default(100,1,1,1);

        PatternAnswer addition = new PatternAnswer("add(%a,%b,#x)") {
            final Term A = $("%a"), B = $("%b");
            @Override public List<Task> run(Task question, Subst s) {
                int a = i(s.getXY(A).toString());
                int b = i(s.getXY(B).toString());

                return Lists.newArrayList(
                        $.$("add(" + a + ',' + b + ',' +
                            Integer.toString(a+b) + ')', '.')
                            .eternal()
                            .truth(1.0f, 0.99f)
                            .parent(question)
                            .budget((Budget)question)
                            .because("Addition")
                );
            }
        };
        n.onQuestion(addition);
        //n.log();

        TestNAR t = new TestNAR(n);
        n.input("add(1,2,#x)?");
        n.input("add(1,1,#x)?");
        t.mustBelieve(8, "add(1, 1, 2)", 1.0f, 0.99f);
        t.mustBelieve(8, "add(1, 2, 3)", 1.0f, 0.99f);
        t.test();

        assertEquals(1, n.concept("add(1, 1, 2)").getBeliefs().size());
        assertEquals(1, n.concept("add(1, 1, #x)").getQuestions().size());
        n.concept("add(1, 1, 2)").print(System.out);
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
