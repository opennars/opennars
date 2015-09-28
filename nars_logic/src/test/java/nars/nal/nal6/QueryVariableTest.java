package nars.nal.nal6;

import nars.Global;
import nars.NAR;
import nars.nal.AbstractNALTest;
import nars.nar.Default;
import nars.task.Task;
import nars.util.data.Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.jgroups.util.Util.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(Parameterized.class)
public class QueryVariableTest extends AbstractNALTest {

    public QueryVariableTest(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Supplier[][]{
                {() -> new Default()},
                //{() -> new Default().nal(5)}
                //{new Neuromorphic(4)},
        });
    }



    @Test public void testQueryVariableAnswer() {
        //$0.25;0.23;0.50$ <?1 <-> a>? {?: 1;2}
        //$0.63;0.34;0.50$ <?1 --> a>? {?: 1;2}

        Global.DEBUG = true;

        final String term = "<a --> b>";

        Set<Task> derivations = new HashSet();

        NAR n = nar();

        int[] answers = new int[1];

        n.memory.eventDerived.on( d-> {
            if (d.getTerm().hasVarQuery())
                derivations.add(d);
            if (d.isJudgment() && d.getTerm().toString().equals(term))
                assertFalse(d + " should not have been derived", Util.isEqual(d.getConfidence(), 0.81f, 0.01f));
        } );
        n.memory.eventAnswer.on( p -> {
            System.out.println("answer: " + p.getOne() + " " + p.getTwo());
            answers[0]++;
        });

        n.believe(term);
        n.ask("<?x --> b>");


        n.frame(16);

        assertEquals(derivations.toString() + " should contain only 3 items that have query variables", 3, derivations.size() );

        assertEquals("Answer/Solution reported?", 1, answers[0]);



    }


//    /** simple test for solutions to query variable questions */
//    @Test public void testQueryVariableSolution() throws InvalidInputException {
//
//        Global.DEBUG = true;
//
//        /*
//        int time1 = 5;
//        int time2 = 15;
//        int time3 = 5;
//        */
//
//        int time1 = 55;
//        int time2 = 115;
//        int time3 = 115;
//
//        //TextOutput.out(n);
//        //new TraceWriter(n, System.out);
//
//        NAR nar = nar();
//
//        nar.frame(time1);
//        String term = "<a --> b>";
//        nar.believe(term);
//        nar.frame(time2);
//
//        //should not output 0.81
//        nar.memory.eventDerived.on( d-> {
//            if (d.isJudgment() && d.getTerm().toString().equals(term)) {
//                assertFalse(d + " should not have been derived", Util.isEqual(d.getConfidence(), 0.81f, 0.01f));
//            }
//        } );
//
//        nar.ask("<?x --> b>");
//
//        nar.frame(time3);
//    }

}

