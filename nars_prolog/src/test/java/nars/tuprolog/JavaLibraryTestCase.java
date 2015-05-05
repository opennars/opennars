package nars.tuprolog;

import junit.framework.TestCase;
import nars.tuprolog.lib.InvalidObjectIdException;
import nars.tuprolog.lib.JavaLibrary;
import com.google.common.util.concurrent.AtomicDouble;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JavaLibraryTestCase extends TestCase {
	String theory = null;
	Prolog engine = new Prolog();
	SolveInfo info = null;
	String result = null;
	String paths = null;
	
	public void testGetPrimitives() {
		Library library = new JavaLibrary();
		Map<Integer, List<PrimitiveInfo>> primitives = library.getPrimitives();
		assertEquals(3, primitives.size());
		assertEquals(0, primitives.get(PrimitiveInfo.DIRECTIVE).size());
		assertTrue(primitives.get(PrimitiveInfo.PREDICATE).size() > 0);
		assertEquals(0, primitives.get(PrimitiveInfo.FUNCTOR).size());
	}

	public void testAnonymousObjectRegistration() throws InvalidTheoryException, InvalidObjectIdException {	
		JavaLibrary lib = (JavaLibrary) engine.getLibrary("nars.prolog.lib.JavaLibrary");
		String theory = "demo(X) :- X <- update. \n";
		engine.setTheory(new Theory(theory));

		TestCounter counter = new TestCounter();
		// check registering behaviour
		Struct t = lib.register(counter);

		engine.solve(new Struct("demo", t));
		assertEquals(1, counter.getValue());
		// check unregistering behaviour
		assertEquals(true, lib.unregister(t));
		SolveInfo goal = engine.solve(new Struct("demo", t));
		assertFalse(goal.isSuccess());
	}

	public void testDynamicObjectsRetrival() throws PrologException {
		Prolog engine = new Prolog();
		JavaLibrary lib = (JavaLibrary) engine.getLibrary("nars.prolog.lib.JavaLibrary");
		String theory = "demo(C) :- \n" +
				"java_object('nars.prolog.TestCounter', [], C), \n" +
				"C <- update, \n" +
				"C <- update. \n";			
		engine.setTheory(new Theory(theory));
		SolveInfo info = engine.solve("demo(Obj).");
		Struct id = (Struct) info.getVarValue("Obj");
		TestCounter counter = (TestCounter) lib.getRegisteredDynamicObject(id);
		assertEquals(2, counter.getValue());
	}

	
	public void test_java_object() throws PrologException, IOException {
        // Testing URLClassLoader with a paths' array
        setPath(true);
        theory = "demo(C) :- \n" +
                "set_classpath([" + paths + "]), \n" +
                "java_object('Counter', [], Obj), \n" +
                "Obj <- inc, \n" +
                "Obj <- inc, \n" +
                "Obj <- getValue returns C.";
        engine.setTheory(new Theory(theory));
        info = engine.solve("demo(Value).");
        assertEquals(true, info.isSuccess());
        nars.tuprolog.Number result2 = (nars.tuprolog.Number) info.getVarValue("Value");
        assertEquals(2, result2.intValue());
    }

    public void test_java_object_absolute() throws PrologException, IOException {
		// Testing URLClassLoader with java.lang.String class
		theory = 	"demo_string(S) :- \n" +
				"java_object('java.lang.String', ['MyString'], Obj_str), \n" +
				"Obj_str <- toString returns S.";
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo_string(StringValue).");
		assertEquals(true, info.isSuccess());
		result = info.getVarValue("StringValue").toString().replace("'", "");
		assertEquals("MyString", result);
	}
	

	public void test_java_object_2() throws PrologException, IOException
	{
		setPath(true);
		theory = "demo_hierarchy(Gear) :- \n"
					+ "set_classpath([" + paths + "]), \n" 
					+ "java_object('Bicycle', [3, 4, 5], MyBicycle), \n"
					+ "java_object('MountainBike', [5, 6, 7, 8], MyMountainBike), \n"
					+ "MyMountainBike <- getGear returns Gear.";
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo_hierarchy(Res).");
		assertEquals(false, info.isHalted());
		nars.tuprolog.Number result2 = (nars.tuprolog.Number) info.getVarValue("Res");
		assertEquals(8, result2.intValue());
	}
	
	public void test_invalid_path_java_object() throws PrologException, IOException
	{
		//Testing incorrect path
		setPath(false);
		theory = "demo(Res) :- \n" +
				"set_classpath([" + paths + "]), \n" + 
				"java_object('Counter', [], Obj_inc), \n" +
				"Obj_inc <- inc, \n" +
				"Obj_inc <- inc, \n" +
				"Obj_inc <- getValue returns Res.";
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(Value).");
		assertEquals(true, info.isHalted());
	}

	public void test_java_call_3() throws PrologException, IOException
	{
		//Testing java_call_3 using URLClassLoader 
		setPath(true); 
		theory = "demo(Value) :- set_classpath([" + paths + "]), class('TestStaticClass') <- echo('Message') returns Value.";
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(StringValue).");
		assertEquals(true, info.isSuccess());
		result = info.getVarValue("StringValue").toString().replace("'", "");
		assertEquals("Message", result);

		//Testing get/set static Field 
		setPath(true);
		theory = "demo_2(Value) :- set_classpath([" + paths + "]), class('TestStaticClass').'id' <- get(Value).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo_2(Res).");
		assertEquals(true, info.isSuccess());		
		assertEquals(0, Integer.parseInt(info.getVarValue("Res").toString()));
		
		theory = "demo_2(Value, NewValue) :- set_classpath([" + paths + "]), class('TestStaticClass').'id' <- set(Value), \n" +
				"class('TestStaticClass').'id' <- get(NewValue).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo_2(5, Val).");
		assertEquals(true, info.isSuccess());		
		assertEquals(5, Integer.parseInt(info.getVarValue("Val").toString()));
		
	}

	public void test_invalid_path_java_call_4() throws PrologException, IOException
	{
		//Testing java_call_4 with invalid path
		setPath(false);
		theory = "demo(Value) :- set_classpath([" + paths + "]), class('TestStaticClass') <- echo('Message') returns Value.";
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(StringValue).");
		assertEquals(true, info.isHalted());
	}

	public void test_java_array() throws PrologException, IOException
	{
		//Testing java_array_length using URLClassLoader 
		setPath(true);
		theory =  "demo(Size) :- set_classpath([" + paths + "]), java_object('Counter', [], MyCounter), \n"
				+ "java_object('Counter[]', [10], ArrayCounters), \n"
				+ "java_array_length(ArrayCounters, Size).";

		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(Value).");
		assertEquals(info.toString(), true, info.isSuccess());
		nars.tuprolog.Number resultInt = (nars.tuprolog.Number) info.getVarValue("Value");
		assertEquals(10, resultInt.intValue());

		//Testing java_array_set and java_array_get
		setPath(true);
		theory =  "demo(Res) :- set_classpath([" + paths + "]), java_object('Counter', [], MyCounter), \n"
				+ "java_object('Counter[]', [10], ArrayCounters), \n"
				+ "MyCounter <- inc, \n"
				+ "java_array_set(ArrayCounters, 0, MyCounter), \n"
				+ "java_array_get(ArrayCounters, 0, C), \n"
				+ "C <- getValue returns Res.";

		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(Value).");
		assertEquals(true, info.isSuccess());
		nars.tuprolog.Number resultInt2 = (nars.tuprolog.Number) info.getVarValue("Value");
		assertEquals(1, resultInt2.intValue());
	}

	public void test_set_classpath() throws PrologException, IOException
	{
		//Testing java_array_length using URLClassLoader 
		setPath(true);


		theory =  "demo(Size) :- set_classpath([" + paths + "]), \n "
				+ "java_object('Counter', [], MyCounter), \n"
				+ "java_object('Counter[]', [10], ArrayCounters), \n"
				+ "java_array_length(ArrayCounters, Size).";

		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(Value).");
		assertEquals(true, info.isSuccess());
		nars.tuprolog.Number resultInt = (nars.tuprolog.Number) info.getVarValue("Value");
		assertEquals(10, resultInt.intValue());
	}
	
	public void test_get_classpath() throws PrologException, IOException
	{
		//Testing get_classpath using DynamicURLClassLoader with not URLs added
		theory =  "demo(P) :- get_classpath(P).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(Value).");
		assertEquals(true, info.isSuccess());
		assertEquals(true, info.getTerm("Value").isList());
		assertEquals("[]", info.getTerm("Value").toString());

		//Testing get_classpath using DynamicURLClassLoader with not URLs added
		setPath(true);

		theory =  "demo(P) :- set_classpath([" + paths + "]), get_classpath(P).";

		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(Value).");
		assertEquals(true, info.isSuccess());
		assertEquals(true, info.getTerm("Value").isList());
		assertEquals("[" + paths + "]", info.getTerm("Value").toString());
		
//		// Test if get_classpath(PathList) unifies with the DynamicURLClassLoader urls
//		theory =  "demo(P) :- set_classpath([" + paths + "]), get_classpath([" + paths + "]).";
//		
//		engine.setTheory(new Theory(theory));
//		info = engine.solve("demo(S).");
//		assertEquals(true, info.isSuccess());
	}


	public static class Counter extends AtomicDouble {

		public void inc() {
			addAndGet(1);
		}


	}

	public static final String theory1 =
			"demo(Obj) :- \n" +
			"java_object('" + Counter.class.getName() + "', [], Obj), \n" +
			"Obj <- inc, \n" +
			"Obj <- inc, \n" +
			"register(Obj)";

	public void test_register_1() throws PrologException, IOException
	{


		//setPath(true);
		theory = theory1 + '.';
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(R).");
		assertEquals(true, info.isSuccess());
		
		theory = "demo2(Obj, Val) :- \n"
				+ "Obj <- inc, \n"
				+ "Obj <- get returns Val.";
		engine.addTheory(new Theory(theory));
		String obj =  info.getTerm("R").toString();
		SolveInfo info2 = engine.solve("demo2(" + obj + ", V).");
		assertEquals(true, info2.isSuccess());
		assertEquals(3.0, java.lang.Double.parseDouble(info2.getVarValue("V").toString()));
	
		// Test invalid object_id registration
		theory = "demo(Obj1) :- register(Obj1).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(Res).");
		assertEquals(true, info.isHalted());		
	}
	
	
	public void test_unregister_1() throws PrologException, IOException
	{
		// Test invalid object_id unregistration
		theory = "demo(Obj1) :- unregister(Obj1).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(Res).");
		assertEquals(true, info.isHalted());	
		
		theory = theory1 + ", unregister(Obj).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(Res).");
		assertEquals(true, info.isSuccess());



		//JavaLibrary lib = (JavaLibrary) engine.getLibrary("nars.prolog.lib.JavaLibrary");
		//Struct id = (Struct) info.getTerm("Res");
		//Object obj = lib.getRegisteredObject(id);
		//assertNull(obj);
	}
	
	public void test_java_catch() throws PrologException, IOException
	{
		setPath(true);
		theory = "goal :- set_classpath([" + paths + "]), java_object('TestStaticClass', [], Obj), Obj <- testMyException. \n"
				+"demo(StackTrace) :- java_catch(goal, [('java.lang.IllegalArgumentException'( \n"
						+ "Cause, Msg, StackTrace),write(Msg))], \n"
						+ "true).";
				
		engine.setTheory(new Theory(theory));
		info = engine.solve("demo(S).");
		assertEquals(true, info.isSuccess());
	}
	
	public void test_interface() throws PrologException, IOException
	{
		setPath(true);
		theory = "goal1 :- set_classpath([" + paths + "])," +
				"java_object('Pippo', [], Obj), class('Pluto') <- method(Obj).";
				
		engine.setTheory(new Theory(theory));
		info = engine.solve("goal1.");
		assertEquals(true, info.isSuccess());
		
		theory = "goal2 :- set_classpath([" + paths + "])," +
				"java_object('Pippo', [], Obj), class('Pluto') <- method2(Obj).";
				
		engine.setTheory(new Theory(theory));
		info = engine.solve("goal2.");
		assertEquals(true, info.isSuccess());
		
		theory = "goal3 :- java_object('Pippo', [], Obj), set_classpath([" + paths + "]), class('Pluto') <- method(Obj).";
				
		engine.setTheory(new Theory(theory));
		info = engine.solve("goal3.");
		assertEquals(true, info.isSuccess());
		
		theory = "goal4 :- set_classpath([" + paths + "]), " +
					"java_object('IPippo[]', [5], Array), " +
					"java_object('Pippo', [], Obj), " +
					"java_array_set(Array, 0, Obj)," +
					"java_array_get(Array, 0, Obj2)," +
					"Obj2 <- met.";
		
		engine.setTheory(new Theory(theory));
		info = engine.solve("goal4.");
		assertEquals(true, info.isSuccess());
		
		theory = "goal5 :- set_classpath([" + paths + "])," +
				"java_object('Pippo', [], Obj)," +
				"class('Pluto') <- method(Obj as 'IPippo').";
		
		engine.setTheory(new Theory(theory));
		info = engine.solve("goal5.");
		assertEquals(true, info.isSuccess());
		
	}
	
	/**
	 * @param valid: used to change a valid/invalid array of paths
	 */
	private void setPath(boolean valid) throws IOException {
		paths = getPath(valid);
	}

	public static String getPath(boolean valid) throws IOException {
		String paths;
		File file = new File(".");
		
		// Array paths contains a valid path
		if(valid)
		{
			/*
			null'/home/me/share/opennars/nars_prolog','/home/me/share/opennars/nars_prolog/src/main/java/nars/prolog/test/unit/TestURLClassLoader.jar''/home/me/share/opennars/nars_prolog','/home/me/share/opennars/nars_prolog/src/main/java/nars/prolog/test/unit/TestInterfaces.jar'
			 */
			//paths = "'" + file.getCanonicalPath() + "',";

			String prefix = file.getCanonicalPath()
					+ File.separator + "src"
					+ File.separator + "main"
					+ File.separator + "java"
					+ File.separator + "nars"
					+ File.separator + "prolog"
					+ File.separator + "test"
					+ File.separator + "unit";
			paths =  "'" + prefix + "',";
			paths += "'" + prefix + File.separator + "TestURLClassLoader.jar',";
			paths += "'" + prefix + File.separator + "TestInterfaces.jar'";

			//System.err.println(paths);

		}
		// Array paths does not contain a valid path
		else
		{
			paths = "'" + file.getCanonicalPath() + "'";
		}
		return paths;
	}
}
