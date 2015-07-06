package nars.nal.nal7;

import nars.NAR;
import nars.nar.Default;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by me on 7/1/15.
 */
public class SequenceTest {

    @Test public void testConstuction() {
        NAR nar = new NAR(new Default());

        String seq = "(&/, /1, a, /3, b, /6)";
        Sequence s = nar.term(seq);
        assertNotNull(s);
        assertNotNull(s.intervals());
        assertEquals("only non-interval terms are allowed as subterms", 2, s.length());

        String ss = s.toString();


        assertEquals(s.length() + 1, s.intervals().length);
        assertEquals("[1, 3, 6]", Arrays.toString(s.intervals()));
        assertEquals(10, s.intervalLength());

        assertEquals("output matches input", seq, ss);

    }

    @Test public void testDistance1() {
        NAR nar = new NAR(new Default());

        Sequence a = nar.term("(&/, x, /1, y)");
        Sequence b = nar.term("(&/, x, /2, y)");
        assertEquals(1, a.distance1(b));
        assertEquals(1, b.distance1(a));
        assertEquals(0, a.distance1(a));

        Sequence c = nar.term("(&/, x, /1, y)");
        Sequence d = nar.term("(&/, x, /9, y)");
        assertEquals(Long.MAX_VALUE, c.distance1(d, 2));
        assertEquals(1, c.distance1(b, 2));


    }

}