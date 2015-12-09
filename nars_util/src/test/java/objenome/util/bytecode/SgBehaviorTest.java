// CHECKSTYLE:OFF
package objenome.util.bytecode;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Ignore
public abstract class SgBehaviorTest {

    protected abstract SgBehavior getTestee();

    @Test
    public void testAddArgument() {
        Assert.assertNotNull(getTestee().getArguments());
        Assert.assertEquals(getTestee().getArguments().size(), 0);

        SgArgument arg1 = new SgArgument(getTestee(), SgClass.INT, "count");
        Assert.assertEquals(getTestee().getArguments().size(), 1);
        Assert.assertSame(getTestee().getArguments().get(0), arg1);
        Assert.assertEquals(getTestee().getCommaSeparatedArgumentNames(), "count");
        Assert.assertEquals(getTestee().getCommaSeparatedArgumentNames(-1), "");

        SgArgument arg2 = new SgArgument(getTestee(), SgClass.BOOLEAN, "ok");
        Assert.assertEquals(getTestee().getArguments().size(), 2);
        Assert.assertSame(getTestee().getArguments().get(0), arg1);
        Assert.assertSame(getTestee().getArguments().get(1), arg2);
        Assert.assertEquals(getTestee().getCommaSeparatedArgumentNames(), "count,ok");
        Assert.assertEquals(getTestee().getCommaSeparatedArgumentNames(-1), "count");
        
        try {
            getTestee().getArguments().add(new SgArgument(getTestee(), SgClass.FLOAT, "dummy"));
            Assert.fail("The list is excepected to be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // OK
        }
        
    }

    @Test
    public void testAddException() {
        Assert.assertNotNull(getTestee().getExceptions());
        Assert.assertEquals(getTestee().getExceptions().size(), 0);
        
        SgClassPool pool = new SgClassPool();
        
        SgClass exception1 = SgClass.create(pool, IOException.class);
        getTestee().addException(exception1);
        Assert.assertEquals(getTestee().getExceptions().size(), 1);
        Assert.assertEquals(getTestee().getExceptions().get(0), exception1);

        SgClass exception2 = SgClass.create(pool, IllegalArgumentException.class);
        getTestee().addException(exception2);
        Assert.assertEquals(getTestee().getExceptions().size(), 2);
        Assert.assertEquals(getTestee().getExceptions().get(0), exception1);
        Assert.assertEquals(getTestee().getExceptions().get(1), exception2);
        
        try {
            getTestee().getExceptions().add(SgClass.create(pool, UnsupportedOperationException.class));
            Assert.fail("The list is excepected to be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // OK
        }
        
    }

    @Test
    public void testAddAnnotation() {
        Assert.assertNotNull(getTestee().getAnnotations());
        Assert.assertEquals(getTestee().getAnnotations().size(), 0);
        
        SgAnnotation ann1 = new SgAnnotation("org.fuin.onthefly", "TestAnno1");
        getTestee().addAnnotation(ann1);
        Assert.assertEquals(getTestee().getAnnotations().size(), 1);
        Assert.assertSame(getTestee().getAnnotations().get(0), ann1);

        SgAnnotation ann2 = new SgAnnotation("org.fuin.onthefly", "TestAnno2");
        getTestee().addAnnotation(ann2);
        Assert.assertEquals(getTestee().getAnnotations().size(), 2);
        Assert.assertSame(getTestee().getAnnotations().get(0), ann1);
        Assert.assertSame(getTestee().getAnnotations().get(1), ann2);
        
    }

    @Test
    public void testAddAnnotations() {
        
        Assert.assertNotNull(getTestee().getAnnotations());
        Assert.assertEquals(getTestee().getAnnotations().size(), 0);
        SgAnnotation ann1 = new SgAnnotation("org.fuin.onthefly", "TestAnno1");
        SgAnnotation ann2 = new SgAnnotation("org.fuin.onthefly", "TestAnno2");
        List<SgAnnotation> annotations = new ArrayList<>();
        annotations.add(ann1);
        annotations.add(ann2);
        getTestee().addAnnotations(annotations);
        Assert.assertEquals(getTestee().getAnnotations().size(), 2);
        Assert.assertSame(getTestee().getAnnotations().get(0), ann1);
        Assert.assertSame(getTestee().getAnnotations().get(1), ann2);
        Assert.assertTrue(getTestee().hasAnnotation(ann1.getName()));
        Assert.assertTrue(getTestee().hasAnnotation(ann2.getName()));
        
    }
    
    
    
}
//CHECKSTYLE:OFF
