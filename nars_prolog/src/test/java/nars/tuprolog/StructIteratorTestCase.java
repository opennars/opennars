package nars.tuprolog;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * @author <a href="mailto:giulio.piancastelli@unibo.it">Giulio Piancastelli</a>
 */
public class StructIteratorTestCase extends TestCase {
	
	public void testEmptyIterator() {
		Struct list = new Struct();
		Iterator<? extends Term> i = list.listIterator();
		assertFalse(i.hasNext());
		try {
			i.next();
			fail();
		} catch (NoSuchElementException expected) {}
	}
	
	public void testIteratorCount() {
		Struct list = new Struct(new Term[] {new Int(1), new Int(2), new Int(3), new Int(5), new Int(7)});
		Iterator<? extends Term> i = list.listIterator();
		int count = 0;
		for (; i.hasNext(); count++)
			i.next();
		assertEquals(5, count);
		assertFalse(i.hasNext());
	}
	
	public void testMultipleHasNext() {
		Struct list = new Struct(new Term[] {new Struct("p"), new Struct("q"), new Struct("r")});
		Iterator<? extends Term> i = list.listIterator();
		assertTrue(i.hasNext());
		assertTrue(i.hasNext());
		assertTrue(i.hasNext());
		assertEquals(new Struct("p"), i.next());
	}
	
	public void testMultipleNext() {
		Struct list = new Struct(new Term[] {new Int(0), new Int(1), new Int(2), new Int(3), new Int(5), new Int(7)});
		Iterator<? extends Term> i = list.listIterator();
		assertTrue(i.hasNext());
		i.next(); // skip the first term
		assertEquals(new Int(1), i.next());
		assertEquals(new Int(2), i.next());
		assertEquals(new Int(3), i.next());
		assertEquals(new Int(5), i.next());
		assertEquals(new Int(7), i.next());
		// no more terms
		assertFalse(i.hasNext());
		try {
			i.next();
			fail();
		} catch (NoSuchElementException expected) {}
	}
	
	public void testRemoveOperationNotSupported() {
		Struct list = new Struct(new Int(1), new Struct());
		Iterator<? extends Term> i = list.listIterator();
		assertNotNull(i.next());
		try {
			i.remove();
			fail();
		} catch (UnsupportedOperationException expected) {}
	}

}
