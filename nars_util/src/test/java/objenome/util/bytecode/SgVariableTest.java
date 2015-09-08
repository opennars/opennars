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
        final SgField field = new SgField(dummyClass, "public", SgClass.INT, "myField", "0");
        final SgAnnotation annotation = new SgAnnotation(this.getClass().getPackage()
                .getName(), this.getClass().getSimpleName() + "DummyAnnotation");
        field.addAnnotation(annotation);
        Assert.assertEquals(field.getAnnotations().size(), 1);
        Assert.assertSame(field.getAnnotations().get(0), annotation);
        Assert.assertTrue(field.hasAnnotation(annotation.getName()));
        Assert.assertFalse(field.hasAnnotation(this.getClass().getSimpleName()
                + "DummyAnnotation2"));
        
        try {
            field.getAnnotations().add(new SgAnnotation("foo", "bar"));
            Assert.fail("The list is excepected to be unmodifiable!");
        } catch (final UnsupportedOperationException ex) {
            // OK
        }
        
    }

    @Test
    public final void testAddAnnotations() {
        final SgField field = new SgField(dummyClass, "public", SgClass.INT, "myField", "0");
        final SgAnnotation annotation1 = new SgAnnotation(this.getClass().getPackage()
                .getName(), this.getClass().getSimpleName() + "DummyAnnotation1");
        field.addAnnotation(annotation1);
        final SgAnnotation annotation2 = new SgAnnotation(this.getClass().getPackage()
                .getName(), this.getClass().getSimpleName() + "DummyAnnotation2");
        field.addAnnotation(annotation2);
        Assert.assertEquals(field.getAnnotations().size(), 2);
        Assert.assertSame(field.getAnnotations().get(0), annotation1);
        Assert.assertSame(field.getAnnotations().get(1), annotation2);
        Assert.assertTrue(field.hasAnnotation(annotation1.getName()));
        Assert.assertTrue(field.hasAnnotation(annotation2.getName()));
        Assert.assertFalse(field.hasAnnotation(this.getClass().getSimpleName()
                + "DummyAnnotation3"));
    }

}
// CHECKSTYLE:ON
