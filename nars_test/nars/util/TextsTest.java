package nars.util;

import nars.io.Texts;
import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class TextsTest {
 

    @Test
    public void testN2() {
        assertEquals("1.00", Texts.n2(1.00f).toString());
        assertEquals("0.50", Texts.n2(0.5f).toString());
        assertEquals("0.09", Texts.n2(0.09f).toString());
        assertEquals("0.10", Texts.n2(0.1f).toString());
        assertEquals("0.01", Texts.n2(0.009f).toString());
        assertEquals("0.00", Texts.n2(0.001f).toString());
        assertEquals("0.01", Texts.n2(0.01f).toString());
        assertEquals("0.00", Texts.n2(0f).toString());
    }
}
