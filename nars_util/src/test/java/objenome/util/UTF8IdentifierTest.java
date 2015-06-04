package objenome.util;

import nars.util.data.id.DynamicUTF8Identifier;
import nars.util.data.id.UTF8Identifier;
import nars.util.utf8.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 6/1/15.
 */
public class UTF8IdentifierTest {



    @Test
    public void test1() {
        testDynamic("abcd1234");
        testDynamic("\u2342abc\u2344");

        testLazyHash("abcd31342");


        testConstantAndDynamicEquality("abcdefg1234");
    }

    private void testConstantAndDynamicEquality(String b) {
        UTF8Identifier x = new UTF8Identifier(b);
        UTF8Identifier y = new DynamicallyConstructedConstantUTF8Identifier(b);
        assertEquals(x, y);
        assertEquals(y, x);
        assertEquals(x, x);
        assertEquals(y, y);
        assertEquals(b, x.toString());
        assertEquals(b, y.toString());
        assertEquals(0, x.compareTo(y));
        assertEquals(0, y.compareTo(x));
        assertEquals(0, y.compareTo(y));
        assertEquals(0, x.compareTo(x));

    }

    public void testLazyHash(final String b) {
        UTF8Identifier y = new UTF8Identifier(b);
        assertTrue(y.hasName());
        assertTrue(!y.hasHash());
        assertTrue(y.hashCode()!=0);
        assertTrue(y.hasHash());
    }

    public void testDynamic(final String b) {
        UTF8Identifier x = new DynamicallyConstructedConstantUTF8Identifier(b);
        assertTrue(x.hashCode()!=0);
        assertEquals(b, x.toString());
    }


    private static class DynamicallyConstructedConstantUTF8Identifier extends DynamicUTF8Identifier {
        private final String b;

        public DynamicallyConstructedConstantUTF8Identifier(String b) {
            this.b = b;
        }

        @Override public byte[] newName() {
            ByteBuf bb = ByteBuf.create(8);
            bb.append(b);
            return bb.toBytes();
        }
    }




}
