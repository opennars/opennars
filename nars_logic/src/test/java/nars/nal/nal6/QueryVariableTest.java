package nars.nal.nal6;

import nars.Global;
import nars.NAR;
import nars.nal.AbstractNALTester;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(Parameterized.class)
public class QueryVariableTest extends AbstractNALTester {

    public QueryVariableTest(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Supplier[][]{
                {Default::new},
                //{() -> new Default().nal(5)}
                //{new Neuromorphic(4)},
        });
    }



    @Test public void testQueryVariableAnswer() {
        testQueryVariableAnswer("<a --> b>", "<a --> b>");
    }
    @Test public void testQueryVariableAnswerUnified() {
        testQueryVariableAnswer("<a --> b>", "<?x --> b>");
    }

    void testQueryVariableAnswer(String belief, String question) {
        //$0.25;0.23;0.50$ <?1 <-> a>? {?: 1;2}
        //$0.63;0.34;0.50$ <?1 --> a>? {?: 1;2}

        Global.DEBUG = true;


        Set<Task> derivations = new HashSet();

        NAR n = nar();
        //n.stdout();

        int[] answers = new int[1];

        n.memory.eventDerived.on( d-> {
            if (d.term().hasVarQuery())
                derivations.add(d);
            if (d.isJudgment() && d.term().toString().equals(belief))
                assertFalse(d + " should not have been derived", Util.equal(d.getConfidence(), 0.81f, 0.01f));
        } );
        n.memory.eventAnswer.on( p -> {
            //System.out.println("q: " + p.getOne() + " a: " + p.getTwo());
            answers[0]++;
        });

        n.believe(belief);
        n.ask(question);


        n.frame(16);


        assertTrue("Answer/Solution reported?", 0 < answers[0]);



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

