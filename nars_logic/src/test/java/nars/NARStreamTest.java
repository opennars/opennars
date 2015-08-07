package nars;

import com.google.common.collect.Iterators;
import nars.nar.Default;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jgroups.util.Util.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 8/7/15.
 */
public class NARStreamTest {

    @Test
    public void testNARStreamBasics() throws Exception {
        int frames = 32;
        AtomicInteger cycled = new AtomicInteger(0),
            conceptsIterated = new AtomicInteger(0);
        StringWriter sw = new StringWriter( );

        new NARStream(new Default())
                .input("<a --> b>.", "<b --> c>.")
                .stopIf( (n) -> { return false; } )
                .forEachCycle(() -> cycled.incrementAndGet() )
                .forEachEvent(new PrintWriter(sw), Events.OUT.class)
                .run(frames)
                .conceptsActive(i -> conceptsIterated.set(Iterators.size(i)));

        //System.out.println(sw.getBuffer());
        assertTrue(sw.toString().length() > 0);
        assertEquals(frames, cycled.get());
        assertTrue(conceptsIterated.get() > 4);


    }
}