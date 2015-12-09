// CHECKSTYLE:OFF
package objenome.util.bytecode;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public abstract class SgVariableTest {

    private SgClass dummyClass;

    public void setup() {
        dummyClass = new SgClass("org.fuin.onthefly", "DummyClass");
    }

    public void teardown() {
        dummyClass = null;
    }

    protected final SgClass getDummyClass() {
        return dummyClass;
    }

    @Test
    public final void testAddGetAnnotation() {
        SgField field = new SgField(dummyClass, "public", SgClass.INT, "myField", "0");
        SgAnnotation annotation = new SgAnnotation(getClass().getPackage()
                .getName(), getClass().getSimpleName() + "DummyAnnotation");
        field.addAnnotation(annotation);
        Assert.assertEquals(field.getAnnotations().size(), 1);
        Assert.assertSame(field.getAnnotations().get(0), annotation);
        Assert.assertTrue(field.hasAnnotation(annotation.getName()));
        Assert.assertFalse(field.hasAnnotation(getClass().getSimpleName()
                + "DummyAnnotation2"));
        
        try {
            field.getAnnotations().add(new SgAnnotation("foo", "bar"));
            Assert.fail("The list is excepected to be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // OK
        }
        
    }

    @Test
    public final void testAddAnnotations() {
        SgField field = new SgField(dummyClass, "public", SgClass.INT, "myField", "0");
        SgAnnotation annotation1 = new SgAnnotation(getClass().getPackage()
                .getName(), getClass().getSimpleName() + "DummyAnnotation1");
        field.addAnnotation(annotation1);
        SgAnnotation annotation2 = new SgAnnotation(getClass().getPackage()
                .getName(), getClass().getSimpleName() + "DummyAnnotation2");
        field.addAnnotation(annotation2);
        Assert.assertEquals(field.getAnnotations().size(), 2);
        Assert.assertSame(field.getAnnotations().get(0), annotation1);
        Assert.assertSame(field.getAnnotations().get(1), annotation2);
        Assert.assertTrue(field.hasAnnotation(annotation1.getName()));
        Assert.assertTrue(field.hasAnnotation(annotation2.getName()));
        Assert.assertFalse(field.hasAnnotation(getClass().getSimpleName()
                + "DummyAnnotation3"));
    }

}
// CHECKSTYLE:ON
