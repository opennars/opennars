package nars.nar;

import nars.LocalMemory;
import nars.Memory;
import nars.narsese.InvalidInputException;
import nars.util.io.JSON;
import org.infinispan.marshall.core.JBossMarshaller;
import org.junit.Test;

import java.io.IOException;
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
    public void testEmptyMemoryToJSON() throws IOException, InterruptedException, ClassNotFoundException {
        LocalMemory m = new LocalMemory();
        String j = JSON.omDeep.writeValueAsString(m);
        assertTrue(j.length() > 16);

        String pretty = JSON.omDeep.writerWithDefaultPrettyPrinter().writeValueAsString(m);
        System.out.println(pretty);

        assertTrue(pretty.length() > j.length() );
    }

    @Test
    public void testEmptyMemorySerialization() throws IOException, InterruptedException, ClassNotFoundException {
        /** empty memory, and serialize it */
        LocalMemory m = new LocalMemory();
        byte[] bm = m.toBytes();
        assertTrue(bm.length > 64);

        assertEquals(m, new JBossMarshaller().objectFromByteBuffer(bm) );

    }

    @Test
    public void testMemoryTransplant() {
        Memory m = new LocalMemory();
        Default nar = new Default(m, 1000, 1, 5, 5);
        //DefaultAlann nar = new DefaultAlann(m, 32);

        //TextOutput.out(nar);

        nar.input("<a-->b>.", "<b-->c>.").run(25);
        nar.stop();

        assertTrue(nar.concepts().size() > 5);

        int nc;
        assertTrue((nc = nar.concepts().size()) > 0);


        //a new nar with the same memory is allowed to
        //take control of it after the first stops
        Default nar2 = new Default(m, 1000, 1, 1, 3);

        assertTrue(m.time() > 1);

        //it should have existing concepts
        assertEquals(nc, nar2.concepts().size());


    }


    @Test
    public void testFluentBasics() throws Exception {
        int frames = 32;
        AtomicInteger cycCount = new AtomicInteger(0);
        StringWriter sw = new StringWriter( );

        new Default()
                .input("<a --> b>.", "<b --> c>.")
                .stopIf( () -> false )
                .onEachCycle( n -> cycCount.incrementAndGet() )
                .trace(sw)
                .run(frames)
                .forEachConceptTask(true, true, true, true, 1, System.out::println );

        //System.out.println(sw.getBuffer());
        assertTrue(sw.toString().length() > 16);
        assertEquals(frames, cycCount.get());


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

        new Default().nal(2)
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