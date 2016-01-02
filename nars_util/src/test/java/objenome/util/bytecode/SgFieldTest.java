// CHECKSTYLE:OFF
package objenome.util.bytecode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SgFieldTest extends SgVariableTest {

    @Override
    @Before
    public void setup() {
        super.setup();
    }

    

    @Test
    public void testConstruction() {
        String modifiers = "public";
        SgClass type = SgClass.INT;
        String name = "myField";
        SgField field = new SgField(getDummyClass(), modifiers, type, name, null);
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
        SgField field = new SgField(getDummyClass(), "public", SgClass.INT, "myField", "0");
        Assert.assertEquals(field.getInitializer(), "0");
        Assert.assertEquals(field.toString(), "public int myField = 0;\n");
    }

}
// CHECKSTYLE:ON
