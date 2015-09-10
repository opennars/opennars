package nars.nar;

import com.google.common.collect.Iterators;
import nars.Events;
import nars.NAR;
import nars.nar.experimental.Equalized;
import nars.narsese.InvalidInputException;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jgroups.util.Util.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 8/7/15.
 */
public class NARTest {

    @Test
    public void testFluentBasics() throws Exception {
        int frames = 32;
        AtomicInteger cycled = new AtomicInteger(0),
            conceptsIterated = new AtomicInteger(0);
        StringWriter sw = new StringWriter( );

        new NAR(new Equalized())
                .input("<a --> b>.", "<b --> c>.")
                .stopIf( () -> false )
                .forEachCycle( cycled::incrementAndGet )
                .forEachEvent(new PrintWriter(sw), Events.OUT.class)
                .run(frames)
                .conceptActiveIterator(i -> conceptsIterated.set(Iterators.size(i)))
                .forEachConceptTask(true, true, true, true, 1, System.out::println );

        //System.out.println(sw.getBuffer());
        assertTrue(sw.toString().length() > 0);
        assertEquals(frames, cycled.get());
        assertTrue(conceptsIterated.get() > 4);


    }


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

        new NAR(new Default().level(2))
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