package prolog;

import junit.framework.TestCase;
import nars.tuprolog.Var;

public class VarTestCase extends TestCase {
	
	public void testIsAtomic() {
		assertFalse(new Var("X").isAtomic());
	}
	
	public void testIsAtom() {
		assertFalse(new Var("X").isAtom());
	}
	
	public void testIsCompound() {
		assertFalse(new Var("X").isCompound());
	}

}
