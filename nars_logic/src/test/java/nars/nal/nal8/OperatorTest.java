package nars.nal.nal8;

import nars.Memory;
import nars.NAR;
import nars.model.impl.Default;
import nars.nal.Task;
import nars.nal.term.Atom;
import nars.nal.term.Term;
import nars.util.event.Reaction;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertTrue;

public class OperatorTest {

    //create a completely empty NAR, no default operators
    NAR n = new NAR(new Default() {
        @Override
        public Operator[] newDefaultOperators(NAR n) {
            return new Operator[] { };
        }
    });

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


    @Test public void testTermReactionRegistration() {

        AtomicBoolean executed = new AtomicBoolean(false);

        n.on(new Reaction<Term>() {

            @Override
            public void event(Term event, Object... args) {
                //System.out.println("executed: " + Arrays.toString(args));
                executed.set(true);
            }

        }, Atom.the("exe"));

        n.input("exe(a,b,c)!");

        n.run(1);

        assertTrue(executed.get());

    }

    @Test public void testSynchOperator() {


        AtomicBoolean executed = new AtomicBoolean(false);

        n.on(new SynchOperator("exe") {


            @Override
            protected List<Task> execute(Operation operation, Memory memory) {
                executed.set(true);
                return null;
            }
        });

        n.input("exe(a,b,c)!");

        n.run(1);

        assertTrue(executed.get());

    }

    @Test public void testCompoundOperator() {

        AtomicBoolean executed = new AtomicBoolean(false);

        n.on(new SynchOperator(n.term("<a --> b>")) {
            @Override protected List<Task> execute(Operation operation, Memory memory) {
                executed.set(true);
                return null;
            }
        });

        n.input("<a --> b>(a,b,c)!");

        n.run(1);

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
