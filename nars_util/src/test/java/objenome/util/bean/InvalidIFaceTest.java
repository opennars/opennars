package objenome.util.bean;

import org.junit.Test;

import static org.junit.Assert.fail;

public class InvalidIFaceTest {

    /**
     * Integer and int are incompatible.
     */
    public interface IncompatibeTypes {

        void setA(int a);

        Integer getA();
    }

    /**
     * The setter has a return type (non void setter).
     */
    public interface NonVoidSetter {

        boolean setTest(int test);

        int getTest();
    }

    /**
     * B has no getter.
     */
    public interface MissingGetter {

        void setA(int xx);

        int getA();

        void setB(int y);

        void setCcc(int z);

        int getCcc();
    }

    /**
     * B has no setter.
     */
    public interface MissingSetter {

        void setA(int xx);

        int getA();

        int getB();

        void setCcc(int z);

        int getCcc();
    }

    /**
     * myInt has no valid getter (has is only allowed for boolean types).
     */
    public interface IntInvalidGetterHas {

        void setMyInt(int xy);

        int hasMyInt();
    }

    public interface UnknownMethod {

        void unknown();
    }

    @Test
    public void testIncompatibeTypes() {
        checkLoadRevocation(IncompatibeTypes.class);
    }

    @Test
    public void testNonVoidSetter() {
        checkLoadRevocation(NonVoidSetter.class);
    }

    @Test
    public void testMissingGetter() {
        checkLoadRevocation(MissingGetter.class);
    }

    @Test
    public void testMissingSetter() {
        checkLoadRevocation(MissingSetter.class);
    }

    @Test
    public void testIntInvalidGetterHas() {
        checkLoadRevocation(IntInvalidGetterHas.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownMethod() {
        BeanProxyBuilder.on(UnknownMethod.class).check(true).build().unknown();
    }

    /**
     * First tries to load the class unchecked then checked. If the Interface is loaded successfully
     * the test will fail.
     * 
     * @param clazz invalid interface
     */
    private <T> void checkLoadRevocation(Class<T> clazz) {
        BeanProxyBuilder.on(clazz).check(false).build();
        try {
            BeanProxyBuilder.on(clazz).check(true).build();
            fail("No " + IllegalArgumentException.class.getName() + " was thrown"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (IllegalArgumentException e) {
            // OK, expected!
        }
    }
}
