package nars.obj.util.bytecode;//// CHECKSTYLE:OFF
//package objenome.util.bytecode;
//
//import java.beans.BeanInfo;
//import java.io.File;
//import java.io.Serializable;
//import java.security.acl.Acl;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//
//public class SgClassTest {
//
//    private static final File DATA_DIR = new File(SgClassTest.class.getResource("./src/test/java/objenome/util/bytecode/data").getFile());// new File("./ata");
//
//    private SgClassPool pool;
//
//    private ByteCodeGenerator generator;
//
//    public SgClassTest() {
//    }
//
//    
//    
//    @Before
//    public void setup() {
//        pool = new SgClassPool();
//        generator = new ByteCodeGenerator();
//    }
//
//
//    @Test
//    public void testEmptyClass() {
//
//        // Create class
//        final SgClass clasz = new SgClass("org.fuin.onthefly", "TestEmptyClass");
//
//        // Equal to file in data directory?
//        SgAssert.assertEqualToFile(DATA_DIR, clasz);
//
//        // Just tests if the class is constructable
//        Assert.assertNull(generator.loadClass(clasz));
//        generator.createClass(clasz);
//    }
//
//    @Test
//    public void testEmptyInterface() {
//
//        // Create interface
//        final SgClass clasz = new SgClass("public", "org.fuin.onthefly", "TestEmptyInterface",
//                true, null);
//
//        // Equal to file in data directory?
//        SgAssert.assertEqualToFile(DATA_DIR, clasz);
//
//        // Just tests if the class is constructable
//        Assert.assertNull(generator.loadClass(clasz));
//        generator.createClass(clasz);
//
//    }
//
//    @Test
//    public void testExtendingSingleInterface() {
//
//        // Create interface
//        final SgClass clasz = new SgClass("public", "org.fuin.onthefly",
//                "TestExtendingInterface", true, null);
//        clasz.addInterface(SgClass.create(pool, Serializable.class));
//
//        // Equal to file in data directory?
//        SgAssert.assertEqualToFile(DATA_DIR, clasz);
//
//        // Just tests if the class is constructable
//        Assert.assertNull(generator.loadClass(clasz));
//        generator.createClass(clasz);
//
//    }
//
//    @Test
//    public void testExtendingMultipleInterfaces() {
//
//        // Create interface
//        final SgClass clasz = new SgClass("public", "org.fuin.onthefly",
//                "TestExtendingMultipleInterfaces", true, null);
//        clasz.addInterface(SgClass.create(pool, Serializable.class));
//        clasz.addInterface(SgClass.create(pool, Acl.class));
//        clasz.addInterface(SgClass.create(pool, BeanInfo.class));
//
//        // Equal to file in data directory?
//        SgAssert.assertEqualToFile(DATA_DIR, clasz);
//
//        // Just tests if the class is constructable
//        Assert.assertNull(generator.loadClass(clasz));
//        generator.createClass(clasz);
//
//    }
//
//    @Test
//    public void testInnerClass() {
//
//        // Create class
//        final SgClass clasz = new SgClass("org.fuin.onthefly", "TestInnerClass");
//        new SgClass("public", "org.fuin.onthefly", "Inner", false, clasz);
//
//        // Equal to file in data directory?
//        SgAssert.assertEqualToFile(DATA_DIR, clasz);
//
//        // Just tests if the class is constructable
//        Assert.assertNull(generator.loadClass(clasz));
//        generator.createClass(clasz);
//
//    }
//
//    @Test
//    public void testInnerInterface() {
//
//        // Create class
//        final SgClass clasz = new SgClass("public", "org.fuin.onthefly", "TestInnerInterface",
//                true, null);
//        new SgClass("public", "org.fuin.onthefly", "Inner", true, clasz);
//
//        // Equal to file in data directory?
//        SgAssert.assertEqualToFile(DATA_DIR, clasz);
//
//        // Just tests if the class is constructable
//        Assert.assertNull(generator.loadClass(clasz));
//        generator.createClass(clasz);
//
//    }
//
//    @Test
//    public void testDefaultConstructor() {
//
//        // Create class
//        final SgClass clasz = new SgClass("org.fuin.onthefly", "TestDefaultConstructor");
//        final SgConstructor constructor = new SgConstructor(clasz);
//        constructor.addBodyLine("super();");
//
//        // Equal to file in data directory?
//        SgAssert.assertEqualToFile(DATA_DIR, clasz);
//
//        // Just tests if the class is constructable
//        Assert.assertNull(generator.loadClass(clasz));
//        generator.createClass(clasz);
//
//    }
//
//    @Test
//    public void testSingleConstructorWithMultipleArgumentsAndFields() {
//
//        // Create class
//        final SgClass clasz = new SgClass("org.fuin.onthefly",
//                "TestSingleConstructorWithMultipleArguments");
//        new SgField(clasz, "private final", SgClass.INT, "count", "");
//        new SgField(clasz, "private final", SgClass.BOOLEAN, "ok", "");
//        final SgConstructor constructor = new SgConstructor(clasz);
//        new SgArgument(constructor, SgClass.INT, "count");
//        new SgArgument(constructor, SgClass.BOOLEAN, "ok");
//        constructor.addBodyLine("super();");
//        constructor.addBodyLine("this.count = count;");
//        constructor.addBodyLine("this.ok = ok;");
//
//        // Equal to file in data directory?
//        SgAssert.assertEqualToFile(DATA_DIR, clasz);
//
//        // Just tests if the class is constructable
//        Assert.assertNull(generator.loadClass(clasz));
//        generator.createClass(clasz);
//
//    }
//
//    @Test
//    public void testMultipleConstructorsWithMultipleArgumentsAndFields() {
//
//        // Create class
//        final SgClass clasz = new SgClass("org.fuin.onthefly",
//                "TestMultipleConstructorsWithMultipleArgumentsAndFields");
//        new SgField(clasz, "private final", SgClass.INT, "count", "");
//        new SgField(clasz, "private final", SgClass.BOOLEAN, "ok", "");
//
//        final SgConstructor constructor1 = new SgConstructor(clasz);
//        new SgArgument(constructor1, SgClass.INT, "count");
//        new SgArgument(constructor1, SgClass.BOOLEAN, "ok");
//        constructor1.addBodyLine("super();");
//        constructor1.addBodyLine("this.count = count;");
//        constructor1.addBodyLine("this.ok = ok;");
//
//        final SgConstructor constructor2 = new SgConstructor(clasz);
//        constructor2.addBodyLine("super();");
//        constructor2.addBodyLine("this.count = 0;");
//        constructor2.addBodyLine("this.ok = false;");
//
//        // Equal to file in data directory?
//        SgAssert.assertEqualToFile(DATA_DIR, clasz);
//
//        // Just tests if the class is constructable
//        Assert.assertNull(generator.loadClass(clasz));
//        generator.createClass(clasz);
//
//    }
//
//    @Test
//    public void testMultipleMethodsWithMultipleArgumentsAndFields() {
//
//        // Create class
//        final SgClass clasz = new SgClass("org.fuin.onthefly",
//                "TestMultipleMethodsWithMultipleArgumentsAndFields");
//        new SgField(clasz, "private", SgClass.INT, "count", "");
//        new SgField(clasz, "private", SgClass.BOOLEAN, "ok", "");
//        final SgMethod getCountMethod = new SgMethod(clasz, "public", SgClass.INT, "getCount");
//        getCountMethod.addBodyLine("return count;");
//        final SgMethod getOkMethod = new SgMethod(clasz, "public", SgClass.BOOLEAN, "getOk");
//        getOkMethod.addBodyLine("return ok;");
//        final SgMethod setCountMethod = new SgMethod(clasz, "public", SgClass.VOID, "setCount");
//        setCountMethod.addArgument(new SgArgument(setCountMethod, SgClass.INT, "count"));
//        setCountMethod.addBodyLine("this.count = count;");
//        final SgMethod setOkMethod = new SgMethod(clasz, "public", SgClass.VOID, "setOk");
//        setOkMethod.addArgument(new SgArgument(setOkMethod, SgClass.BOOLEAN, "ok"));
//        setOkMethod.addBodyLine("this.ok = ok;");
//
//        // Equal to file in data directory?
//        SgAssert.assertEqualToFile(DATA_DIR, clasz);
//
//        // Just tests if the class is constructable
//        Assert.assertNull(generator.loadClass(clasz));
//        generator.createClass(clasz);
//    }
//
//    @Test
//    public void testMethodAnnotation() {
//
//        // Create class
//        final SgClass clasz = new SgClass("org.fuin.srcgen4javassist", "TestMethodAnnotation");
//        // Implicitly adds the field to the class
//        new SgField(clasz, "private", SgClass.INT, "count", "0");
//        final SgMethod getCountMethod = new SgMethod(clasz, "public", SgClass.INT, "getCount");
//        getCountMethod.addBodyLine("return count;");
//        getCountMethod.addAnnotation(new SgAnnotation("org.fuin.srcgen4javassist",
//                "XMethodAnnotation"));
//
//        // Equal to file in data directory?
//        SgAssert.assertEqualToFile(DATA_DIR, clasz);
//
//        // Just tests if the class is constructable
//        // Assert.assertNull(generator.loadClass(clasz));
//        // TODO Javassist compiler seems to have problems with annotations
//        // generator.createClass(clasz);
//    }
//
//}
// // CHECKSTYLE:ON
