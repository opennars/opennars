package objenome.util;

import nars.util.utf8.IntBuf;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 5/21/15.
 */
public class IntBufTest {

    @Test
    public void testByteAddress() {
        int[] x = new int[1];
        IntBuf.p(x, 0, 0x01);
        assertEquals(0x01, x[0]);

        int[] x1 = new int[1];
        IntBuf.p(x1, 1, 0x01);
        assertEquals(0x0100, x1[0]);

        IntBuf.p(x1, 1, 0x02); //replace second byte
        assertEquals(0x0200, x1[0]);

        int[] x2 = new int[1];
        IntBuf.p(x2, 0, 0xff);
        IntBuf.p(x2, 1, 0xff);
        IntBuf.p(x2, 2, 0xff);
        IntBuf.p(x2, 3, 0xff);
        assertEquals(0xffffffff, x2[0]);
    }

    @Test public void testEncode() {
        testEncodeString("a");
        testEncodeString("ab");
        testEncodeString("abc");
        testEncodeString("abcd");
        testEncodeString("abcdefghi");
        testEncodeString("abcde");
        testEncodeString("\u0344abcdf");
        testEncodeString("<a --> b>.");
        testEncodeString("<a-->b>.");
        testEncodeString("<a->b>.");

    }

    public void testEncodeString(String s) {
        int[] i = new int[1 + s.length() / 2];

        int ints = IntBuf.encode(s, i);


        System.out.println('\'' + s + "' : ints=" + Arrays.toString(Arrays.copyOf(i, ints)) + ", bytes=" + Arrays.toString(IntBuf.toBytes(i, ints)));

        String e = IntBuf.asString(i, ints);
        assertEquals(s, e);

    }
}
