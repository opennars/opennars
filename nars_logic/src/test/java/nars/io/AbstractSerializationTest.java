package nars.io;

import nars.narsese.NarseseParser;
import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.marshall.core.JBossMarshaller;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Round-trip serialization and deserialization tests for various types
 */
abstract public class AbstractSerializationTest<S,T> {

    final S input;

    public static final NarseseParser p = NarseseParser.the();

    abstract public void testEquality(T a, T b);

    public AbstractSerializationTest(S input) {
        this.input = input;
    }

    abstract T parse(S input);

    final static Marshaller jbossMarshaller = new JBossMarshaller() {

        @Override
        public String toString() {
            return "JBossMarshaller";
        }
    };
    final static Marshaller javaSerializationMarshaller = new JavaSerializationMarshaller() {
        @Override
        public String toString() {
            return "JavaSerializationMarshaller";
        }
    };

    @Test public void assertEqualsJBossMarshaller() throws Exception {
        assertEqualsMarshaller(jbossMarshaller);
    }
    @Test public void assertEqualsJavaSerializationMarshaller() throws Exception {
        assertEqualsMarshaller(javaSerializationMarshaller);
    }

    public void assertEqualsMarshaller(Marshaller m) throws Exception {

        assertEqualSerialization((T y) -> {
            try {
                assertTrue(m.isMarshallable(y));
                byte[] b = m.objectToByteBuffer(y);
                assertTrue(b.length > 1);
                System.out.println(m + ": " + y + " (" + y.getClass() + ") to " + b.length + " bytes");
                return b;
            } catch (Exception e) {
                e.printStackTrace();
                assertTrue(e.toString(), false);
                return null;
            }

        }, (byte[] s) -> {
            try {
                return (T)m.objectFromByteBuffer(s);
            } catch (Exception e) {
                e.printStackTrace();
                assertTrue(e.toString(), false);
                return null;
            }
        });
    }

    public <I> void assertEqualSerialization(Function<T,I> marshaller, Function<I,T> unmarshaller)  {

        final T y = parse(input);

        assertNotNull(y);

        I serialized = marshaller.apply(y);
        assertNotNull(serialized);

        T deserialized = post( unmarshaller.apply(serialized) );
        assertNotNull(deserialized);

        testEquality(y, deserialized);
    }

    /** abstract method for post-processing the deserialized value */
    protected T post(T deserialized) {
        return deserialized;
    }

}
