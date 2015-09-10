package nars.term;

import nars.narsese.NarseseParser;
import org.junit.Test;

import static org.jgroups.util.Util.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 9/9/15.
 */
public class CommonVariableTest {

    NarseseParser p = NarseseParser.the();

    @Test
    public void commonVariableTest1() {
        assertEquals("%1%2",
                CommonVariable.make(
                        p.term("%1"),
                        p.term("%2")).toString());

        //reverse order
        assertEquals("%1%2",
                CommonVariable.make(
                        p.term("%2"),
                        p.term("%1")).toString());
    }

    @Test
    public void commonVariableTest2() {
        //different lengths
        assertEquals("%2%00",
                CommonVariable.make(
                        p.term("%00"),
                        p.term("%2")).toString());
        //different lengths
        assertEquals("%2%00",
                CommonVariable.make(
                        p.term("%2"),
                        p.term("%00")).toString());

    }

    @Test
    public void commonVariableInstancing() {
        //different lengths

        CommonVariable ca = CommonVariable.make(
                p.term("%1"),
                p.term("%2"));
        CommonVariable cb = CommonVariable.make(
                p.term("%2"),
                p.term("%1"));

        //same instance
        assertTrue(ca == cb);
    }
}