// CHECKSTYLE:OFF
package objenome.util.bytecode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


public class SgConstructorTest extends SgBehaviorTest {

    private SgClass clasz;

    private SgConstructor testee;

    @Before
    public void setup() {
        clasz = new SgClass("org.fuin.onthefly", "DummyClass");
        testee = new SgConstructor(clasz);
    }


    @Override
    protected SgBehavior getTestee() {
        return testee;
    }

    @Test
    public void testConctructionSgClass() {
        Assert.assertSame(testee.getOwner(), clasz);
        Assert.assertEquals(testee.getArguments().size(), 0);
        Assert.assertEquals(testee.getExceptions().size(), 0);
        Assert.assertEquals(testee.getBody().size(), 0);
        Assert.assertEquals(testee.getModifiers(), "public");
        Assert.assertEquals(testee.getCommaSeparatedArgumentNames(), "");
        Assert.assertEquals(testee.getSignature(), "public DummyClass()");
    }

    @Test
    public void testGetSignature() {
        SgConstructor constructor = new SgConstructor(clasz, "public");
        new SgArgument(constructor, SgClass.INT, "count");
        Assert.assertEquals(constructor.getSignature(), "public DummyClass(int count)");
        new SgArgument(constructor, SgClass.BOOLEAN, "ok");
        Assert.assertEquals(constructor.getSignature(),
                "public DummyClass(int count, boolean ok)");
        SgClassPool pool = new SgClassPool();
        constructor.addException(SgClass.create(pool, IOException.class));
        Assert.assertEquals(constructor.getSignature(),
                "public DummyClass(int count, boolean ok) throws java.io.IOException");
        constructor.addException(SgClass.create(pool, IllegalArgumentException.class));
        Assert.assertEquals(constructor.getSignature(),
                "public DummyClass(int count, boolean ok) throws java.io.IOException,"
                        + "java.lang.IllegalArgumentException");
    }

    @Test
    public void testConctructionSgClassString() {
        SgConstructor constructor = new SgConstructor(clasz, "private");
        Assert.assertEquals(constructor.getArguments().size(), 0);
        Assert.assertEquals(constructor.getExceptions().size(), 0);
        Assert.assertEquals(constructor.getBody().size(), 0);
        Assert.assertEquals(constructor.getModifiers(), "private");
        Assert.assertEquals(constructor.getCommaSeparatedArgumentNames(), "");
        Assert.assertEquals(constructor.getSignature(), "private DummyClass()");

        new SgArgument(constructor, "final", SgClass.INT, "count");
        Assert.assertEquals(constructor.getSignature(), "private DummyClass(final int count)");

        new SgArgument(constructor, "final", SgClass.BOOLEAN, "ok");
        Assert.assertEquals(constructor.getSignature(),
                "private DummyClass(final int count, final boolean ok)");

    }

}
// CHECKSTYLE:ON
