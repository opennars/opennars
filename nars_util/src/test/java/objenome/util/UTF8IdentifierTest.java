package objenome.util;

import nars.util.data.id.UTF8Identifier;
import nars.util.utf8.ByteBuf;
import org.junit.Test;

/**
 * Created by me on 6/1/15.
 */
public class UTF8IdentifierTest {

    @Test
    public void test1() {
        UTF8Identifier x = new UTF8Identifier() {


            @Override
            public byte[] makeName() {
                ByteBuf bb = ByteBuf.create(8);
                bb.add("abcd1234");
                return bb.toBytes();
            }
        };
    }
}
