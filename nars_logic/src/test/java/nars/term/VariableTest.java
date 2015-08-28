package nars.term;

import nars.narsese.NarseseParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 8/28/15.
 */
public class VariableTest {

    static final NarseseParser p = NarseseParser.the();

    @Test
    public void testPatternVarVolume() {

        assertEquals(0, p.term("$x").complexity());
        assertEquals(1, p.term("$x").volume());

        assertEquals(0, p.term("%x").complexity());
        assertEquals(1, p.term("%x").volume());

        assertEquals(p.term("<x --> y>").volume(),
                p.term("<%x --> %y>").volume());

    }
}
