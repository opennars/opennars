package prolog;

import junit.framework.TestCase;
import nars.tuprolog.Int;
import nars.tuprolog.InvalidTermException;
import nars.tuprolog.Struct;
import nars.tuprolog.Var;

public class DoubleTestCase extends TestCase {
	
	public void testIsAtomic() {
		assertTrue(new nars.tuprolog.Double(0).isAtomic());
	}
	
	public void testIsAtom() {
		assertFalse(new nars.tuprolog.Double(0).isAtom());
	}
	
	public void testIsCompound() {
		assertFalse(new nars.tuprolog.Double(0).isCompound());
	}
	
	public void testEqualsToStruct() {
		nars.tuprolog.Double zero = new nars.tuprolog.Double(0);
		Struct s = new Struct();
		assertFalse(zero.equals(s));
	}
	
	public void testEqualsToVar() throws InvalidTermException {
		nars.tuprolog.Double one = new nars.tuprolog.Double(1);
		Var x = new Var("X");
		assertFalse(one.equals(x));
	}
	
	public void testEqualsToDouble() {
		nars.tuprolog.Double zero = new nars.tuprolog.Double(0);
		nars.tuprolog.Double one = new nars.tuprolog.Double(1);
		assertFalse(zero.equals(one));
		nars.tuprolog.Double anotherZero = new nars.tuprolog.Double(0.0);
		assertTrue(anotherZero.equals(zero));
	}
	
	public void testEqualsToFloat() {
		// TODO Test Double numbers for equality with Float numbers
	}
	
	public void testEqualsToInt() {
		nars.tuprolog.Double doubleOne = new nars.tuprolog.Double(1.0);
		Int integerOne = new Int(1);
		assertFalse(doubleOne.equals(integerOne));
	}
	
	public void testEqualsToLong() {
		// TODO Test Double numbers for equality with Long numbers
	}

}
