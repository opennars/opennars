package prolog;


import junit.framework.TestCase;
import nars.tuprolog.event.SpyEvent;
import nars.tuprolog.event.SpyListener;

public class PrologTestCase extends TestCase {
	
	public void testEngineInitialization() {
		Prolog engine = new Prolog();
		assertEquals(4, engine.getCurrentLibraries().length);
		assertNotNull(engine.getLibrary("nars.prolog.lib.BasicLibrary"));
		assertNotNull(engine.getLibrary("nars.prolog.lib.ISOLibrary"));
		assertNotNull(engine.getLibrary("nars.prolog.lib.IOLibrary"));
		assertNotNull(engine.getLibrary("nars.prolog.lib.JavaLibrary"));
	}
	
	public void testLoadLibraryAsString() throws InvalidLibraryException {
		Prolog engine = new Prolog();
		engine.loadLibrary("nars.prolog.StringLibrary");
		assertNotNull(engine.getLibrary("nars.prolog.StringLibrary"));
	}
	
	public void testLoadLibraryAsObject() throws InvalidLibraryException {
		Prolog engine = new Prolog();
		Library stringLibrary = new StringLibrary();
		engine.loadLibrary(stringLibrary);
		assertNotNull(engine.getLibrary("nars.prolog.StringLibrary"));
		Library javaLibrary = new nars.tuprolog.lib.JavaLibrary();
		engine.loadLibrary(javaLibrary);
		assertSame(javaLibrary, engine.getLibrary("nars.prolog.lib.JavaLibrary"));
	}
	
	public void testGetLibraryWithName() throws InvalidLibraryException {
		Prolog engine = new Prolog(new String[] {"nars.prolog.TestLibrary"});
		assertNotNull(engine.getLibrary("TestLibraryName"));
	}
	
	public void testUnloadLibraryAfterLoadingTheory() throws Exception {
		Prolog engine = new Prolog();
		assertNotNull(engine.getLibrary("nars.prolog.lib.IOLibrary"));
		Theory t = new Theory("a(1).\na(2).\n");
		engine.setTheory(t);
		engine.unloadLibrary("nars.prolog.lib.IOLibrary");
		assertNull(engine.getLibrary("nars.prolog.lib.IOLibrary"));
	}
	
	public void testAddTheory() throws InvalidTheoryException {
		Prolog engine = new Prolog();
		Theory t = new Theory("test :- notx existing(s).");
		try {
			engine.addTheory(t);
			fail();
		} catch (InvalidTermException expected) {
			assertEquals("", engine.getTheory().toString());
		}
	}
	
	public void testSpyListenerManagement() {
		Prolog engine = new Prolog();
		SpyListener listener1 = new SpyListener() {
			public void onSpy(SpyEvent e) {}
		};
		SpyListener listener2 = new SpyListener() {
			public void onSpy(SpyEvent e) {}
		};
		engine.addSpyListener(listener1);
		engine.addSpyListener(listener2);
		assertEquals(2, engine.getSpyListenerList().size());
	}
	
	public void testLibraryListener() throws InvalidLibraryException {
		Prolog engine = new Prolog(new String[]{});
		engine.loadLibrary("nars.prolog.lib.BasicLibrary");
		engine.loadLibrary("nars.prolog.lib.IOLibrary");
		TestPrologEventAdapter a = new TestPrologEventAdapter();
		engine.addLibraryListener(a);
		engine.loadLibrary("nars.prolog.lib.JavaLibrary");
		assertEquals("nars.prolog.lib.JavaLibrary", a.firstMessage);
		engine.unloadLibrary("nars.prolog.lib.JavaLibrary");
		assertEquals("nars.prolog.lib.JavaLibrary", a.firstMessage);
	}
	
	public void testTheoryListener() throws InvalidTheoryException {
		Prolog engine = new Prolog();
		TestPrologEventAdapter a = new TestPrologEventAdapter();
		engine.addTheoryListener(a);
		Theory t = new Theory("a(1).\na(2).\n");
		engine.setTheory(t);
		assertEquals("", a.firstMessage);
		assertEquals("a(1).\n\na(2).\n\n", a.secondMessage);
		t = new Theory("a(3).\na(4).\n");
		engine.addTheory(t);
		assertEquals("a(1).\n\na(2).\n\n", a.firstMessage);
		assertEquals("a(1).\n\na(2).\n\na(3).\n\na(4).\n\n", a.secondMessage);
	}
	
	public void testQueryListener() throws Exception {
		Prolog engine = new Prolog();
		TestPrologEventAdapter a = new TestPrologEventAdapter();
		engine.addQueryListener(a);
		engine.setTheory(new Theory("a(1).\na(2).\n"));
		engine.solve("a(X).");
		assertEquals("a(X)", a.firstMessage);
		assertEquals("yes.\nX / 1", a.secondMessage);
		engine.solveNext();
		assertEquals("a(X)", a.firstMessage);
		assertEquals("yes.\nX / 2", a.secondMessage);
	}

}
