package nars.io;

import nars.narsese.NarseseParser;
import nars.util.io.JSON;
import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.marshall.jboss.AbstractJBossMarshaller;
import org.infinispan.commons.marshall.jboss.DefaultContextClassResolver;
import org.junit.Ignore;
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

    /** adapted from GenericJBossMarshaller which was final: */
    final static Marshaller jbossMarshaller = new MyAbstractJBossMarshaller();
    final static Marshaller javaSerializationMarshaller = new JavaSerializationMarshaller() {
        @Override
        public String toString() {
            return "JavaSerializationMarshaller";
        }
    };

    @Test public void assertEqualsJBossMarshaller() {
        assertEqualsMarshaller(jbossMarshaller);
    }
    @Test public void assertEqualsJavaSerializationMarshaller() {
        assertEqualsMarshaller(javaSerializationMarshaller);
    }


    public void assertEqualsMarshaller(Marshaller m) {

        final Class<? extends Object>[] classs = new Class[1];

        assertEqualSerialization((T y) -> {
            classs[0] = y.getClass();
            try {
                assertTrue(m.isMarshallable(y));
                byte[] b = m.objectToByteBuffer(y);
                int len = b.length;
                log(m.toString(), y, len);
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
                assertTrue(new String(s) + " " + e.toString(), false);
                return null;
            }
        });
    }

    private void log(String m, T y, int len) {
        assertTrue(len > 1);
        System.out.print("\n" + m + ": " + y + " (" + y.getClass() + ") to " + len + " bytes");
    }

    @Ignore /* not working yet */ @Test
    public void assertEqualJSONMarshaller()  {
        final Class<? extends Object>[] classs = new Class[1];

        assertEqualSerialization((T y) -> {

            classs[0] = y.getClass();

            String s = null;

            try {
                /*serializer  = JSON.omDeep.getSerializerProvider().findValueSerializer(y.getClass());
                if (serializer == null) {
                    throw new RuntimeException("no serializer exists for " + classs[0]);
                }*/

                assertTrue(y.getClass() + " serializable?", JSON.omDeep.canSerialize(y.getClass()));
                s = JSON.omDeep.writeValueAsString(y);
                log("JSON: " + s, y, s.length());
                return s;
            } catch (Exception e) {
                e.printStackTrace();
                assertTrue("Unable to serialize: " + y +  " " + e, false);
                return null;
            }

        }, (String s) -> {
            try {
                return (T)JSON.omDeep.readerFor(classs[0]).readValue(s);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Unable to deserialize: " + s);
                assertTrue(s + "\n\t" + e.toString(), false);
                return null;
            }
        });
    }


    public <I> void assertEqualSerialization(Function<T,I> marshaller, Function<I,T> unmarshaller)  {

        final T original = parse(input);

        assertNotNull(input + " not parseable", original);

        I serialized = marshaller.apply(original);
        assertNotNull(serialized);

        T deserializedCopy = post( unmarshaller.apply(serialized) );
        assertNotNull(deserializedCopy);
        assertTrue("different instances", original != deserializedCopy);


        testEquality(original, deserializedCopy);
    }

    /** abstract method for post-processing the deserialized value */
    protected T post(T deserialized) {
        return deserialized;
    }

    static class MyAbstractJBossMarshaller extends AbstractJBossMarshaller {

        public MyAbstractJBossMarshaller() {
            super();
            baseCfg.setClassResolver(
                    new DefaultContextClassResolver(this.getClass().getClassLoader()));
        }


        @Override
        public String toString() {
            return "JBossMarshaller";
        }
    }
}
