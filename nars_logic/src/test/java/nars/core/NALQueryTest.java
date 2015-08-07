/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;

import nars.NAR;
import nars.NARStream;
import nars.io.qa.AnswerReaction;
import nars.nar.Default;
import nars.narsese.InvalidInputException;
import nars.task.Sentence;
import nars.task.Task;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static nars.nal.nal7.Tense.Eternal;
import static org.junit.Assert.assertTrue;

/**
 * @author me
 */
public class NALQueryTest {


    @Test
    public void testQuery2() throws InvalidInputException {
        testQueryAnswered(16, 0);
    }

//    @Test
//    public void testQuery1() throws InvalidInputException {
//        testQueryAnswered(1, 32);
//    }


    public void testQueryAnswered(int cyclesBeforeQuestion, int cyclesAfterQuestion) throws InvalidInputException {

        final AtomicBoolean b = new AtomicBoolean(false);

        String question = cyclesBeforeQuestion == 0 ?
                "<a --> b>" /* unknown solution to be derived */ :
                "<b --> a>" /* existing solution, to test finding existing solutions */;

        new NARStream(new Default().level(2))
                .stdout()
                .input("<a <-> b>. %1.0;0.5%",
                       "<b --> a>. %1.0;0.5%")
                .run(cyclesBeforeQuestion)
                .answer(question, t -> b.set(true) )
                .stopIf( () -> b.get() )
                .run(cyclesAfterQuestion);

        assertTrue(b.get());

    }


}
