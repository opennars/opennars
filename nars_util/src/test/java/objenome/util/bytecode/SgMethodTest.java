// CHECKSTYLE:OFF
package objenome.util.bytecode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;



public class SgMethodTest extends SgBehaviorTest {

    private SgClass clasz;

    private SgMethod testee;

    @Before
    public void setup() {
        clasz = new SgClass("org.fuin.onthefly", "DummyClass");
        testee = new SgMethod(clasz, "public", SgClass.INT, "getCount");
    }

    @Override
    protected SgBehavior getTestee() {
        return testee;
    }

    @Test
    public void testConstruction() {
        Assert.assertSame(testee.getOwner(), clasz);
        Assert.assertEquals(testee.getModifiers(), "public");
        Assert.assertEquals(testee.getArguments().size(), 0);
        Assert.assertEquals(testee.getExceptions().size(), 0);
        Assert.assertEquals(testee.getReturnType(), SgClass.INT);
        Assert.assertEquals(testee.getName(), "getCount");
        Assert.assertEquals(testee.getBody().size(), 0);
        Assert.assertEquals(testee.getAnnotations().size(), 0);
        Assert.assertEquals(testee.getSignature(), "public int getCount()");
    }

    @Test
    public void testGetSignature() {
        SgMethod method = new SgMethod(clasz, "public", SgClass.VOID, "setCount");
        new SgArgument(method, SgClass.INT, "count");
        Assert.assertEquals(method.getSignature(), "public void setCount(int count)");
        new SgArgument(method, SgClass.BOOLEAN, "ok");
        Assert.assertEquals(method.getSignature(),
                "public void setCount(int count, boolean ok)");
        SgClassPool pool = new SgClassPool();
        method.addException(SgClass.create(pool, IOException.class));
        Assert.assertEquals(method.getSignature(),
                "public void setCount(int count, boolean ok) throws java.io.IOException");
        method.addException(SgClass.create(pool, IllegalArgumentException.class));
        Assert.assertEquals(method.getSignature(),
                "public void setCount(int count, boolean ok) "
                        + "throws java.io.IOException,java.lang.IllegalArgumentException");
    }

    @Test
    public void testAddBodyLine() {
        SgMethod method = new SgMethod(clasz, "public", SgClass.VOID, "setCount");
        new SgArgument(method, SgClass.INT, "count");
        String line = "this.count = count;";
        Assert.assertEquals(method.getBody().size(), 0);
        method.addBodyLine(line);
        Assert.assertEquals(method.getBody().size(), 1);
        Assert.assertEquals(method.getBody().get(0), line);
    }

}
// CHECKSTYLE:ON
