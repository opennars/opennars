package prolog;

import junit.framework.TestCase;
import nars.prolog.Int;
import nars.prolog.InvalidTermException;
import nars.prolog.Struct;
import nars.prolog.Var;

public class DoubleTestCase extends TestCase {
	
	public void testIsAtomic() {
		assertTrue(new nars.prolog.Double(0).isAtomic());
	}
	
	public void testIsAtom() {
		assertFalse(new nars.prolog.Double(0).isAtom());
	}
	
	public void testIsCompound() {
		assertFalse(new nars.prolog.Double(0).isCompound());
	}
	
	public void testEqualsToStruct() {
		nars.prolog.Double zero = new nars.prolog.Double(0);
		Struct s = new Struct();
		assertFalse(zero.equals(s));
	}
	
	public void testEqualsToVar() throws InvalidTermException {
		nars.prolog.Double one = new nars.prolog.Double(1);
		Var x = new Var("X");
		assertFalse(one.equals(x));
	}
	
	public void testEqualsToDouble() {
		nars.prolog.Double zero = new nars.prolog.Double(0);
		nars.prolog.Double one = new nars.prolog.Double(1);
		assertFalse(zero.equals(one));
		nars.prolog.Double anotherZero = new nars.prolog.Double(0.0);
		assertTrue(anotherZero.equals(zero));
	}
	
	public void testEqualsToFloat() {
		// TODO Test Double numbers for equality with Float numbers
	}
	
	public void testEqualsToInt() {
		nars.prolog.Double doubleOne = new nars.prolog.Double(1.0);
		Int integerOne = new Int(1);
		assertFalse(doubleOne.equals(integerOne));
	}
	
	public void testEqualsToLong() {
		// TODO Test Double numbers for equality with Long numbers
	}

}
