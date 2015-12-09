package objenome.util;

import nars.util.data.id.LiteralUTF8Identifier;
import nars.util.data.id.UTF8Identifier;
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
        testDynamic("\u2342");
        testDynamic("\u2342a");
        testDynamic("\u2342abc\u2344");

        //testLazyHash("abcd31342");


        testEquality("abcdefg1234");
    }

    private void testEquality(String b) {
        UTF8Identifier x = new LiteralUTF8Identifier(b);
        UTF8Identifier y = new LiteralUTF8Identifier(b);
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



    public void testDynamic(String b) {
        UTF8Identifier x = new LiteralUTF8Identifier(b);
        assertTrue(x.hashCode()!=0);
        assertEquals(b.length(), x.toString().length());
        assertEquals(b, x.toString());
    }


//    private static class DynamicallyConstructedConstantUTF8Identifier extends DynamicUTF8Identifier {
//        private final String b;
//
//        public DynamicallyConstructedConstantUTF8Identifier(String b) {
//            this.b = b;
//        }
//
//        @Override public byte[] init() {
//            ByteBuf bb = ByteBuf.create(8);
//            bb.append(b);
//            return bb.toBytes();
//        }
//    }




}
