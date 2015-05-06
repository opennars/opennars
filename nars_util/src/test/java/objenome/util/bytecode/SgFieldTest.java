// CHECKSTYLE:OFF
package nars.obj.util.bytecode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SgFieldTest extends SgVariableTest {

    @Before
    public void setup() {
        super.setup();
    }

    

    @Test
    public void testConstruction() {
        final String modifiers = "public";
        final SgClass type = SgClass.INT;
        final String name = "myField";
        final SgField field = new SgField(getDummyClass(), modifiers, type, name, null);
        Assert.assertSame(field.getOwner(), getDummyClass());
        Assert.assertSame(field.getType(), type);
        Assert.assertEquals(field.getName(), name);
        Assert.assertEquals(field.getModifiers(), modifiers);
        Assert.assertEquals(field.getAnnotations().size(), 0);
        Assert.assertNull(field.getInitializer());
        Assert.assertEquals(field.toString(),
                "public int myField /** No initializer source available */ ;\n");
    }

    @Test
    public void testSetGetInitializer() {
        final SgField field = new SgField(getDummyClass(), "public", SgClass.INT, "myField", "0");
        Assert.assertEquals(field.getInitializer(), "0");
        Assert.assertEquals(field.toString(), "public int myField = 0;\n");
    }

}
// CHECKSTYLE:ON
