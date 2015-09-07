package nars.io;

import nars.narsese.NarseseParser;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.marshall.core.JBossMarshaller;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Round-trip serialization and deserialization tests for various types
 */
abstract public class AbstractSerializationTest<T> {

    final String input;

    public static final NarseseParser p = NarseseParser.the();

    abstract public void testEquality(T a, T b);

    public AbstractSerializationTest(String input) {
        this.input = input;
    }

    abstract T parse(String input);

    @Test
    public void assertEquals() throws Exception {
        final T y = parse(input);

        assertNotNull(y);

        //JavaSerializationMarshaller m = new JavaSerializationMarshaller();
        Marshaller m = new JBossMarshaller();


        assertTrue(m.isMarshallable(y));
        byte[] b = m.objectToByteBuffer(y);
        assertTrue(b.length > 1);

        T y2 = post( (T)m.objectFromByteBuffer(b) );

        testEquality(y, y2);
    }

    /** abstract method for post-processing the deserialized value */
    protected T post(T deserialized) {
        return deserialized;
    }

}
