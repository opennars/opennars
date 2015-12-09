// CHECKSTYLE:OFF
package objenome.util.bytecode;

import org.junit.Assert;
import org.junit.Test;


public class SgAnnotationTest {

    @Test
    public void testConstruction() {

        String packageName = "org.fuin.onthefly";
        String simpleName = "MyAnnotation";
        SgAnnotation annotation = new SgAnnotation(packageName, simpleName);
        Assert.assertEquals(annotation.getPackageName(), packageName);
        Assert.assertEquals(annotation.getSimpleName(), simpleName);
        Assert.assertNotNull(annotation.getArguments());
        Assert.assertEquals(annotation.getArguments().size(), 0);

        annotation.addArgument("count", SgClass.INT);
        Assert.assertEquals(annotation.getArguments().size(), 1);

        try {
            annotation.getArguments().put("dummy", SgClass.BOOLEAN);
            Assert.fail("The map is excepected to be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // OK
        }

    }

}
// CHECKSTYLE:ON
