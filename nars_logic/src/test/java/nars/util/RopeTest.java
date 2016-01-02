/*
 *  RopeTest.java
 *  Copyright (C) 2007 Amin Ahmad. 
 *  
 *  This file is part of Java Ropes.
 *  
 *  Java Ropes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Java Ropes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with Java Ropes.  If not, see <http://www.gnu.org/licenses/>.
 *  	
 *  Amin Ahmad can be contacted at amin.ahmad@gmail.com or on the web at 
 *  www.ahmadsoft.org.
 */
package nars.util;

import nars.util.data.rope.Rope;
import nars.util.data.rope.impl.ConcatenationRope;
import nars.util.data.rope.impl.FlatCharSequenceRope;
import nars.util.data.rope.impl.ReverseRope;
import nars.util.data.rope.impl.SubstringRope;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.Iterator;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@Ignore
public class RopeTest  {
	
	private String fromRope(Rope rope, int start, int end) {
		try {
			Writer out = new StringWriter(end - start);
			rope.write(out, start, end - start);
			return out.toString();
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
        
        @Test
	public void testSubstringDeleteBug() {
		   String s = "12345678902234567890";

		   Rope rope = Rope.build(s.toCharArray()); // bugs

		   rope = rope.delete(0, 1);
		   assertEquals("23", fromRope(rope, 0, 2));
		   assertEquals("", fromRope(rope, 0, 0));
		   assertEquals("902", fromRope(rope, 7, 10));
		   
		   
		   rope = Rope.build(s); // no bugs
		   rope = rope.delete(0, 1);
		   assertEquals("23", fromRope(rope, 0, 2));
		   assertEquals("", fromRope(rope, 0, 0));
		   assertEquals("902", fromRope(rope, 7, 10));
		}

	/**
	 * Bug reported by ugg.ugg@gmail.com.
	 */
        @Test
	public void testRopeWriteBug() {
		Rope r = Rope.build("");
		r = r.append("round ");
		r = r.append(Integer.toString(0));
		r = r.append(" 1234567890");

		assertEquals("round ", fromRope(r,0,6));
		assertEquals("round 0", fromRope(r,0,7));
		assertEquals("round 0 ", fromRope(r,0,8));
		assertEquals("round 0 1", fromRope(r,0,9));
		assertEquals("round 0 12", fromRope(r,0,10));
		assertEquals("round 0 1234567890", fromRope(r,0,18));
		assertEquals("round 0 1234567890", fromRope(r,0,r.length()));
	}

	   
	public void testTemp() {
		// insert temporary code here.
	}
	
        @Test
	public void testLengthOverflow() {
		Rope x1 = Rope.build("01");
		for (int j=2;j<31;++j) 
			x1 = x1.append(x1);
		assertEquals(1073741824, x1.length());
		try {
			x1 = x1.append(x1);
			fail("Expected overflow.");
		} catch (IllegalArgumentException e) {
			// this is what we expect
		}
	}
	
        @Test
	public void testMatches() {
		Rope x1 = new FlatCharSequenceRope("0123456789");
		Rope x2 = new ConcatenationRope(x1, x1);

		assertTrue(x2.matches("0.*9"));
		assertTrue(x2.matches(Pattern.compile("0.*9")));

		assertTrue(x2.matches("0.*90.*9"));
		assertTrue(x2.matches(Pattern.compile("0.*90.*9")));
	}
	
        @Test
	public void testConcatenationFlatFlat() {
		Rope r1 = Rope.build("alpha");
		Rope r2 = Rope.build("beta");
		Rope r3 = r1.append(r2);
		Assert.assertEquals("alphabeta", r3.toString());

		r1 = Rope.build("The quick brown fox jumped over");
		r3 = r1.append(r1);
		Assert.assertEquals("The quick brown fox jumped overThe quick brown fox jumped over", r3.toString());
	}
	
        @Test
	public void testIterator() {
		Rope x1 = new FlatCharSequenceRope("0123456789");
		Rope x2 = new FlatCharSequenceRope("0123456789");
		Rope x3 = new FlatCharSequenceRope("0123456789");
		ConcatenationRope c1 = new ConcatenationRope(x1, x2);
		ConcatenationRope c2 = new ConcatenationRope(c1, x3);
		
		Iterator<Character> i = c2.iterator();
		for (int j = 0; j < c2.length(); ++j) {
			assertTrue("Has next (" + j + '/' + c2.length() + ')', i.hasNext());
			i.next();
		}
		assertTrue(!i.hasNext());
		
		FlatCharSequenceRope z1 = new FlatCharSequenceRope("0123456789");
		Rope z2 = new SubstringRope(z1, 2, 0);
		Rope z3 = new SubstringRope(z1, 2, 2);
		Rope z4 = new ConcatenationRope(z3, new SubstringRope(z1, 6, 2)); // 2367
		
		i = z2.iterator();
		assertTrue(!i.hasNext());
		i = z3.iterator();
		assertTrue(i.hasNext());
		assertEquals('2',(char) i.next());
		assertTrue(i.hasNext());
		assertEquals('3', (char) i.next());
		assertTrue(!i.hasNext());
		for (int j=0; j<=z3.length(); ++j) {
			try {
				z3.iterator(j);
			} catch (Exception e) {
				fail(j + " " + e);
			}
		}
		assertTrue(4 == z4.length());
		for (int j=0; j<=z4.length(); ++j) {
			try {
				z4.iterator(j);
			} catch (Exception e) {
				fail(j + " " + e);
			}
		}
		i=z4.iterator(4);
		assertTrue(!i.hasNext());
		i=z4.iterator(2);
		assertTrue(i.hasNext());
		assertEquals('6',(char) i.next());
		assertTrue(i.hasNext());
		assertEquals('7',(char) i.next());
		assertTrue(!i.hasNext());
		
		
	}
	
        @Test
	public void testReverse() {
		Rope x1 = new FlatCharSequenceRope("012345");
		Rope x2 = new FlatCharSequenceRope("67");
		Rope x3 = new ConcatenationRope(x1, x2);
		
		assertEquals("543210", x1.reverse().toString());
		assertEquals("76543210", x3.reverse().toString());
		assertEquals(x3.reverse(), x3.reverse().reverse().reverse());
		assertEquals("654321", x3.reverse().subSequence(1, 7).toString());
	}
	

        @Test
	public void testTrim() {
		Rope x1 = new FlatCharSequenceRope("\u0012  012345");
		Rope x2 = new FlatCharSequenceRope("\u0002 67	       \u0007");
		Rope x3 = new ConcatenationRope(x1, x2);

		assertEquals("012345", x1.trimStart().toString());
		assertEquals("67	       \u0007", x2.trimStart().toString());
		assertEquals("012345\u0002 67	       \u0007", x3.trimStart().toString());

		assertEquals("\u0012  012345", x1.trimEnd().toString());
		assertEquals("\u0002 67", x2.trimEnd().toString());
		assertEquals("\u0012  012345\u0002 67", x3.trimEnd().toString());
		assertEquals("012345\u0002 67", x3.trimEnd().reverse().trimEnd().reverse().toString());

		assertEquals(x3.trimStart().trimEnd(), x3.trimEnd().trimStart());
		//assertEquals(x3.trimStart().trimEnd(), x3.trimStart().reverse().trimStart().reverse());
		assertEquals(x3.trimStart().trimEnd(), x3.trim());
	}

        @Test
	public void testCreation() {
		try {
			Rope.build("The quick brown fox jumped over");
		} catch (Exception e) {
			Assert.fail("Nonempty string: " + e.getMessage());
		}
		try {
			Rope.build("");
		} catch (Exception e) {
			Assert.fail("Empty string: " + e.getMessage());
		}
	}

        @Test
	public void testEquals() {
		Rope r1 = Rope.build("alpha");
		Rope r2 = Rope.build("beta");
		Rope r3 = Rope.build("alpha");

		Assert.assertEquals(r1, r3);
		Assert.assertFalse(r1.equals(r2));
	}

        @Test
	public void testHashCode() {
		Rope r1 = Rope.build("alpha");
		Rope r2 = Rope.build("beta");
		Rope r3 = Rope.build("alpha");

		Assert.assertEquals(r1.hashCode(), r3.hashCode());
		Assert.assertFalse(r1.hashCode() == r2.hashCode());
	}
	

        @Test
	public void testIndexOf() {
		Rope r1 = Rope.build("alpha");
		Rope r2 = Rope.build("beta");
		Rope r3 = r1.append(r2);
		Assert.assertEquals(1, r3.indexOf('l'));
		Assert.assertEquals(6, r3.indexOf('e'));
		

		Rope r = Rope.build("abcdef");
		assertEquals(-1, r.indexOf('z'));
		assertEquals(0, r.indexOf('a'));
		assertEquals(1, r.indexOf('b'));
		assertEquals(5, r.indexOf('f'));
		

		assertEquals(1, r.indexOf('b', 0));
		assertEquals(0, r.indexOf('a', 0));
		assertEquals(-1, r.indexOf('z', 0));
		assertEquals(-1, r.indexOf('b',2));
		assertEquals(5, r.indexOf('f',5));
		
		assertEquals(2, r.indexOf("cd", 1));
		
		r = Rope.build("The quick brown fox jumped over the jumpy brown dog.");
		assertEquals(0, r.indexOf("The"));
		assertEquals(10, r.indexOf("brown"));
		assertEquals(10, r.indexOf("brown", 10));
		assertEquals(42, r.indexOf("brown",11));
		assertEquals(-1, r.indexOf("brown",43));
		assertEquals(-1, r.indexOf("hhe"));
		
		r = Rope.build("zbbzzz");
		assertEquals(-1, r.indexOf("ab",1));
	}

        @Test
	public void testInsert() {
		Rope r1 = Rope.build("alpha");
		Assert.assertEquals("betaalpha", r1.insert(0, "beta").toString());
		Assert.assertEquals("alphabeta", r1.insert(r1.length(), "beta").toString());
		Assert.assertEquals("abetalpha", r1.insert(1, "beta").toString());
	}

        @Test
	public void testPrepend() {
		Rope r1 = Rope.build("alphabeta");
		for (int j=0;j<2;++j)
			r1 = r1.subSequence(0, 5).append(r1);
		Assert.assertEquals("alphaalphaalphabeta", r1.toString());
		r1 = r1.append(r1.subSequence(5, 15));
		Assert.assertEquals("alphaalphaalphabetaalphaalpha", r1.toString());
	}
	
        @Test
	public void testCompareTo() {
		Rope r1 = Rope.build("alpha");
		Rope r2 = Rope.build("beta");
		Rope r3 = Rope.build("alpha");
		Rope r4 = Rope.build("alpha1");
		String s2 = "beta";

		assertTrue(r1.compareTo(r3) == 0);
		assertTrue(r1.compareTo(r2) < 0);
		assertTrue(r2.compareTo(r1) > 0);
		assertTrue(r1.compareTo(r4) < 0);
		assertTrue(r4.compareTo(r1) > 0);
		assertTrue(r1.compareTo(s2) < 0);
		assertTrue(r2.compareTo(s2) == 0);
	}
	
        @Test
	public void testToString() {
		String phrase = "The quick brown fox jumped over the lazy brown dog. Boy am I glad the dog was asleep.";
		Rope r1 = Rope.build(phrase);
		assertTrue(phrase.equals(r1.toString()));
		assertTrue(phrase.subSequence(7, 27).equals(r1.subSequence(7, 27).toString()));
	}
	
        @Test
	public void testReverseIterator() {
		FlatCharSequenceRope r1 = new FlatCharSequenceRope("01234");
		ReverseRope r2 = new ReverseRope(r1);
		SubstringRope r3 = new SubstringRope(r1, 0, 3);
		ConcatenationRope r4 = new ConcatenationRope(new ConcatenationRope(r1,r2),r3);	//0123443210012
		
		Iterator<Character> x = r1.reverseIterator();
		assertTrue(x.hasNext());
		assertEquals('4',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('3',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r1.reverseIterator(4);
		assertTrue(x.hasNext());
		assertEquals('0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r2.reverseIterator();
		assertTrue(x.hasNext());
		assertEquals('0',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('3',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('4',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r2.reverseIterator(4);
		assertTrue(x.hasNext());
		assertEquals('4',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r3.reverseIterator();
		assertTrue(x.hasNext());
		assertEquals('2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('0',(char)  x.next());
		assertFalse(x.hasNext());

		x = r3.reverseIterator(1);
		assertTrue(x.hasNext());
		assertEquals('1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r4.reverseIterator(); //0123443210012
		assertTrue(x.hasNext());
		assertEquals('2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('0',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('0',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('3',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('4',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('4',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('3',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r4.reverseIterator(7);
		assertEquals('4',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('4',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('3',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals('0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r4.reverseIterator(12);
		assertTrue(x.hasNext());
		assertEquals('0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r4.reverseIterator(13);
		assertFalse(x.hasNext());
		
	}

        @Test
	public void testSerialize() {
		FlatCharSequenceRope r1 = new FlatCharSequenceRope("01234");
		ReverseRope r2 = new ReverseRope(r1);
		SubstringRope r3 = new SubstringRope(r1, 0, 1);
		ConcatenationRope r4 = new ConcatenationRope(new ConcatenationRope(r1,r2),r3);	//01234432100
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(r4);
			oos.close();
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(in);
			Rope r = (Rope) ois.readObject();
			assertTrue(r instanceof FlatCharSequenceRope);
		} catch (Exception e) {
			fail(e.toString());
		}
		
		
	}
	
        @Test
	public void testPadStart() {
		Rope r = Rope.build("hello");
		assertEquals("hello", r.padStart(5).toString());
		assertEquals("hello", r.padStart(0).toString());
		assertEquals("hello", r.padStart(-1).toString());
		assertEquals(" hello", r.padStart(6).toString());
		assertEquals("  hello", r.padStart(7).toString());
		assertEquals("~hello", r.padStart(6, '~').toString());
		assertEquals("~~hello", r.padStart(7, '~').toString());
		assertEquals("~~~~~~~~~~~~~~~~~~~~~~~~~hello", r.padStart(30, '~').toString());
	}
	
        @Test
	public void testPadEnd() {
		Rope r = Rope.build("hello");
		assertEquals("hello", r.padEnd(5).toString());
		assertEquals("hello", r.padEnd(0).toString());
		assertEquals("hello", r.padEnd(-1).toString());
		assertEquals("hello ", r.padEnd(6).toString());
		assertEquals("hello  ", r.padEnd(7).toString());
		assertEquals("hello~", r.padEnd(6, '~').toString());
		assertEquals("hello~~", r.padEnd(7, '~').toString());
		assertEquals("hello~~~~~~~~~~~~~~~~~~~~~~~~~", r.padEnd(30, '~').toString());
	}
	
        @Test
	public void testSubstringBounds() {
		Rope r  = Rope.build("01234567890123456789012345678901234567890123456789012345678901234567890123456789".toCharArray());
		Rope r2 = r.subSequence(0, 30);
		try{
			r2.charAt(31);
			fail("Expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// success
		}
	}
	
        @Test
	public void testAppend() {
		Rope r = Rope.build("");
		r=r.append('a');
		assertEquals("a", r.toString());
		r=r.append("boy");
		assertEquals("aboy", r.toString());
		r=r.append("test", 0, 4);
		assertEquals("aboytest", r.toString());
	}
	
        @Test
	public void testEmpty() {
		Rope r1 = Rope.build("");
		Rope r2 = Rope.build("012345");
		
		assertTrue(r1.isEmpty());
		assertFalse(r2.isEmpty());
		assertTrue(r2.subSequence(2, 2).isEmpty());
	}
        
        @Test
	public void testCharAt() {
		FlatCharSequenceRope r1 = new FlatCharSequenceRope("0123456789");
		SubstringRope r2 = new SubstringRope(r1,0,1);
		SubstringRope r3 = new SubstringRope(r1,9,1);
		ConcatenationRope r4 = new ConcatenationRope(r1, r3);

		assertEquals('0', r1.charAt(0));
		assertEquals('9', r1.charAt(9));
		assertEquals('0', r2.charAt(0));
		assertEquals('9', r3.charAt(0));
		assertEquals('0', r4.charAt(0));
		assertEquals('9', r4.charAt(9));
		assertEquals('9', r4.charAt(10));
	}
	
        @Test
	public void testRegexp() {
		ConcatenationRope r = new ConcatenationRope(new FlatCharSequenceRope("012345"), new FlatCharSequenceRope("6789"));
		CharSequence c = r.getForSequentialAccess();
		for (int j=0; j<10; ++j) {
			assertEquals(r.charAt(j), c.charAt(j));
		}
		c = r.getForSequentialAccess();
		
		int[] indices={1,2,1,3,5,0,6,7,8,1,7,7,7};
		for (int i: indices) {
			assertEquals("Index: " + i, r.charAt(i), c.charAt(i));
		}
	}

        @Test
	public void testStartsEndsWith() {
		Rope r = Rope.build("Hello sir, how do you do?");
		assertTrue(r.startsWith(""));
		assertTrue(r.startsWith("H"));
		assertTrue(r.startsWith("He"));
		assertTrue(r.startsWith("Hello "));
		assertTrue(r.startsWith("", 0));
		assertTrue(r.startsWith("H", 0));
		assertTrue(r.startsWith("He", 0));
		assertTrue(r.startsWith("Hello ", 0));
		assertTrue(r.startsWith("", 1));
		assertTrue(r.startsWith("e", 1));
		assertTrue(r.endsWith("?"));
		assertTrue(r.endsWith("do?"));
		assertTrue(r.endsWith("o", 1));
		assertTrue(r.endsWith("you do", 1));
	}
	
	/**
	 * Reported by Blake Watkins <blakewatkins@gmail.com> on
	 * 21 Mar 2009.
	 */
        @Test
	public void testIndexOfBug() {
		{   // original test, bwatkins
			String s1 = "CCCCCCPIFPCFFP";
			String s2 = "IFPCFFP";

			Rope r1 = Rope.build(s1);
			Assert.assertEquals(s1.indexOf(s2), r1.indexOf(s2));
		}
			// extra test, aahmad
			String s1 = "ABABAABBABABBAAABBBAAABABABABBBBAA";
			String s2 = "ABABAB";

			Rope r1 = Rope.build(s1);
			Assert.assertEquals(s1.indexOf(s2), r1.indexOf(s2));
		}
}


///**
// * Performs an extensive performance test comparing Ropes, Strings, and
// * StringBuffers.
// * @author aahmad
// */
//public class PerformanceTest {
//
//	private static int seed=342342;
//	private static Random random = new Random(PerformanceTest.seed);
//	private static int lenCC = 182029;
//	private static int lenBF = 467196;
//	
//	private static final int ITERATION_COUNT = 7;
//	private static final int PLAN_LENGTH = 500;
//	
//
//	private static String complexString=null; 
//	private static StringBuffer complexStringBuffer=null; 
//	private static Rope complexRope=null;
//	private static Text complexText=null;
//
//	/**
//	 * @param args
//	 */
//	public static void main(final String[] args) throws Exception {
//		
//		if (args.length == 1) {
//			seed = Integer.parseInt(args[0]);
//		}
//		
//		long x,y;
//
//		x=System.nanoTime();
//		final char[] aChristmasCarol_RAW = PerformanceTest.readCC();
//		final char[] bensAuto_RAW        = PerformanceTest.readBF();
//		final String aChristmasCarol = new String(aChristmasCarol_RAW);
//		final String bensAuto        = new String(bensAuto_RAW);
//		y=System.nanoTime();
//		System.out.println("Read " + aChristmasCarol.length() + " bytes in " + PerformanceTest.time(x,y));
//
//		System.out.println();
//		System.out.println("**** DELETE PLAN TEST ****");
//		System.out.println();
//
//		int newSize = PerformanceTest.lenCC;
//		final int[][] deletePlan=new int[PLAN_LENGTH][2];
//		for (int j=0;j<deletePlan.length;++j) {
//			deletePlan[j][0] = PerformanceTest.random.nextInt(newSize);
//			deletePlan[j][1] = PerformanceTest.random.nextInt(Math.min(100, newSize - deletePlan[j][0]));
//			newSize -= deletePlan[j][1];
//		}
//
//		for (int k=20; k<=deletePlan.length; k+=20) {
//		System.out.println("Delete plan length: " + k);
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT];
//		for (int j=0;j<ITERATION_COUNT;++j){
//		stats0[j] = PerformanceTest.stringDeleteTest(aChristmasCarol, deletePlan);
//		stats1[j] = PerformanceTest.stringBufferDeleteTest(aChristmasCarol, deletePlan);
//		stats2[j] = PerformanceTest.ropeDeleteTest(aChristmasCarol, deletePlan);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope]");
//		}
//		}
//
//		System.out.println();
//		System.out.println("**** PREPEND PLAN TEST ****");
//		System.out.println();
//
//		final int[][] prependPlan=new int[PLAN_LENGTH][2];
//		for (int j=0;j<prependPlan.length;++j) {
//			prependPlan[j][0] = PerformanceTest.random.nextInt(PerformanceTest.lenCC);
//			prependPlan[j][1] = PerformanceTest.random.nextInt(PerformanceTest.lenCC - prependPlan[j][0]);
//		}
//
//		for (int k=20; k<=prependPlan.length; k+=20) {
//		System.out.println("Prepend plan length: " + k);
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT];
//		for (int j=0;j<ITERATION_COUNT;++j){
//		stats0[j] = PerformanceTest.stringPrependTest(aChristmasCarol, prependPlan, k);
//		stats1[j] = PerformanceTest.stringBufferPrependTest(aChristmasCarol, prependPlan, k);
//		stats2[j] = PerformanceTest.ropePrependTest(aChristmasCarol, prependPlan, k);
//		stats3[j] = PerformanceTest.textPrependTest(aChristmasCarol, prependPlan, k);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope]");
//		stat(System.out, stats3, "ns", "[Text]");
//		}
//		}
//
//		System.out.println();
//		System.out.println("**** APPEND PLAN TEST ****");
//		System.out.println();
//
//		final int[][] appendPlan=new int[PLAN_LENGTH][2];
//		for (int j=0;j<appendPlan.length;++j) {
//			appendPlan[j][0] = PerformanceTest.random.nextInt(PerformanceTest.lenCC);
//			appendPlan[j][1] = PerformanceTest.random.nextInt(PerformanceTest.lenCC - appendPlan[j][0]);
//		}
//
//
//		for (int k=20; k<=appendPlan.length; k+=20) {
//		System.out.println("Append plan length: " + k);
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT];
//		for (int j=0;j<ITERATION_COUNT;++j){
//		stats0[j] = PerformanceTest.stringAppendTest(aChristmasCarol, appendPlan, k);
//		stats1[j] = PerformanceTest.stringBufferAppendTest(aChristmasCarol, appendPlan, k);
//		stats2[j] = PerformanceTest.ropeAppendTest(aChristmasCarol, appendPlan, k);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope]");
//		}
//		}
//
//
//		System.out.println();
//		System.out.println("**** INSERT PLAN TEST ****");
//		System.out.println("* Insert fragments of A Christmas Carol back into itself.\n");
//
//		final int[][] insertPlan=new int[PLAN_LENGTH][3];
//		for (int j=0;j<insertPlan.length;++j) {
//			insertPlan[j][0] = PerformanceTest.random.nextInt(PerformanceTest.lenCC);                      //location to insert
//			insertPlan[j][1] = PerformanceTest.random.nextInt(PerformanceTest.lenCC);                      //clip from
//			insertPlan[j][2] = PerformanceTest.random.nextInt(PerformanceTest.lenCC - insertPlan[j][1]);   //clip length
//		}
//
//
//
//		for (int k=insertPlan.length; k<=insertPlan.length; k+=20) {
//		System.out.println("Insert plan length: " + k);
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT];
//		for (int j=0;j<1;++j){
//		stats0[j] = PerformanceTest.stringInsertTest(aChristmasCarol_RAW, insertPlan, k);
//		stats1[j] = PerformanceTest.stringBufferInsertTest(aChristmasCarol_RAW, insertPlan, k);
//		stats2[j] = PerformanceTest.ropeInsertTest(aChristmasCarol_RAW, insertPlan, k);
//		stats3[j] = PerformanceTest.textInsertTest(aChristmasCarol_RAW, insertPlan, k);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope]");
//		stat(System.out, stats3, "ns", "[Text]");
//		}
//		}
//
//		System.out.println();
//		System.out.println("**** INSERT PLAN TEST 2 ****");
//		System.out.println("* Insert fragments of Benjamin Franklin's Autobiography into\n" +
//				           "* A Christmas Carol.\n");
//
//		final int[][] insertPlan2=new int[PLAN_LENGTH][3];
//		for (int j=0;j<insertPlan2.length;++j) {
//			insertPlan2[j][0] = PerformanceTest.random.nextInt(PerformanceTest.lenCC);                      //location to insert
//			insertPlan2[j][1] = PerformanceTest.random.nextInt(PerformanceTest.lenBF);                      //clip from
//			insertPlan2[j][2] = PerformanceTest.random.nextInt(PerformanceTest.lenBF - insertPlan2[j][1]);  //clip length
//		}
//
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT];
//		for (int j=0;j<ITERATION_COUNT;++j){
//		stats0[j] = PerformanceTest.stringInsertTest2(aChristmasCarol, bensAuto, insertPlan2);
//		stats1[j] = PerformanceTest.stringBufferInsertTest2(aChristmasCarol, bensAuto, insertPlan2);
//		stats2[j] = PerformanceTest.ropeInsertTest2(aChristmasCarol, bensAuto, insertPlan2);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope]");
//		}
//
//		System.out.println();
//		System.out.println("**** TRAVERSAL TEST 1 (SIMPLY-CONSTRUCTED DATASTRUCTURES) ****");
//		System.out.println("* A traversal test wherein the datastructures are simply\n" +
//				           "* constructed, meaning constructed straight from the data\n" +
//				           "* file with no further modifications. In this case, we expect\n" +
//				           "* rope performance to be competitive, with the charAt version\n" +
//				           "* performing better than the iterator version.");
//		System.out.println();
//
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT];
//		for (int j=0;j<ITERATION_COUNT;++j){
//		stats0[j] = PerformanceTest.stringTraverseTest(aChristmasCarol_RAW);
//		stats1[j] = PerformanceTest.stringBufferTraverseTest(aChristmasCarol_RAW);
//		stats2[j] = PerformanceTest.ropeTraverseTest_1(aChristmasCarol_RAW);
//		stats3[j] = PerformanceTest.ropeTraverseTest_2(aChristmasCarol_RAW);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope/charAt]");
//		stat(System.out, stats3, "ns", "[Rope/itr]");
//		}
//
//		System.out.println();
//		System.out.println("**** TRAVERSAL TEST 2 (COMPLEXLY-CONSTRUCTED DATASTRUCTURES) ****");
//		System.out.println("* A traversal test wherein the datastructures are complexly\n" +
//				           "* constructed, meaning constructed through hundreds of insertions,\n" +
//				           "* substrings, and deletions (deletions not yet implemented). In\n" +
//				           "* this case, we expect rope performance to suffer, with the\n" +
//				           "* iterator version performing better than the charAt version.");
//		System.out.println();
//
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT], stats4 = new long[ITERATION_COUNT];
//		for (int j=0;j<3;++j){
//		stats0[j] = PerformanceTest.stringTraverseTest2(complexString);
//		stats1[j] = PerformanceTest.stringBufferTraverseTest2(complexStringBuffer);
//		stats2[j] = PerformanceTest.ropeTraverseTest2_1(complexRope);
//		stats3[j] = PerformanceTest.ropeTraverseTest2_2(complexRope);
//		stats4[j] = PerformanceTest.textTraverseTest2(complexText);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope/charAt]");
//		stat(System.out, stats3, "ns", "[Rope/itr]");
//		stat(System.out, stats4, "ns", "[Text/charAt]");
//		}
//
//		System.out.println();
//		System.out.println("**** REGULAR EXPRESSION TEST (SIMPLY-CONSTRUCTED DATASTRUCTURES) ****");
//		System.out.println("* Using a simply-constructed rope and the pattern 'Crachit'.");
//		
//		Pattern p1 = Pattern.compile("Cratchit");
//
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT], stats4 = new long[ITERATION_COUNT];
//		for (int j=0;j<ITERATION_COUNT;++j){
//		stats0[j] = PerformanceTest.stringRegexpTest(aChristmasCarol_RAW, p1);
//		stats1[j] = PerformanceTest.stringBufferRegexpTest(aChristmasCarol_RAW, p1);
//		stats2[j] = PerformanceTest.ropeRegexpTest(aChristmasCarol_RAW, p1);
//		stats3[j] = PerformanceTest.ropeMatcherRegexpTest(aChristmasCarol_RAW, p1);
//		stats4[j] = PerformanceTest.textRegexpTest(aChristmasCarol_RAW, p1);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope]");
//		stat(System.out, stats3, "ns", "[Rope.matcher]");
//		stat(System.out, stats4, "ns", "[Text]");
//		}
//
//		System.out.println();
//		System.out.println("**** REGULAR EXPRESSION TEST (SIMPLY-CONSTRUCTED DATASTRUCTURES) ****");
//		System.out.println("* Using a simply-constructed rope and the pattern 'plea.*y'.");
//
//		p1 = Pattern.compile("plea.*y");
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT];
//		for (int j=0;j<ITERATION_COUNT;++j){
//		stats0[j] = PerformanceTest.stringRegexpTest(aChristmasCarol_RAW, p1);
//		stats1[j] = PerformanceTest.stringBufferRegexpTest(aChristmasCarol_RAW, p1);
//		stats2[j] = PerformanceTest.ropeRegexpTest(aChristmasCarol_RAW, p1);
//		stats3[j] = PerformanceTest.ropeMatcherRegexpTest(aChristmasCarol_RAW, p1);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope]");
//		stat(System.out, stats3, "ns", "[Rope.matcher]");
//		}
//
//		System.out.println();
//		System.out.println("**** REGULAR EXPRESSION TEST (COMPLEXLY-CONSTRUCTED DATASTRUCTURES) ****");
//		System.out.println("* Using a complexly-constructed rope and the pattern 'Crachit'.");
//
//		p1 = Pattern.compile("Cratchit");
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT], stats3 = new long[ITERATION_COUNT], stats4 = new long[ITERATION_COUNT], stats5 = new long[ITERATION_COUNT];
//		for (int j=0;j<ITERATION_COUNT;++j){
//		stats0[j] = PerformanceTest.stringRegexpTest2(complexString, p1);
//		stats1[j] = PerformanceTest.stringBufferRegexpTest2(complexStringBuffer, p1);
//		stats2[j] = PerformanceTest.ropeRegexpTest2(complexRope, p1);
//		stats3[j] = PerformanceTest.ropeRebalancedRegexpTest2(complexRope, p1);
//		stats4[j] = PerformanceTest.ropeMatcherRegexpTest2(complexRope, p1);
//		stats5[j] = PerformanceTest.textRegexpTest2(complexText, p1);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope]");
//		stat(System.out, stats3, "ns", "[Reblncd Rope]");
//		stat(System.out, stats4, "ns", "[Rope.matcher]");
//		stat(System.out, stats5, "ns", "[Text]");
//		}
//
//		System.out.println();
//		System.out.println("**** STRING SEARCH TEST ****");
//		System.out.println("* Using a simply constructed rope and the pattern 'Bob was very\n" +
//						   "* cheerful with them, and spoke pleasantly to'.");
//
//		String toFind = "consumes faster than Labor wears; while the used key is always bright,";
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT];
//		for (int j=0;j<ITERATION_COUNT;++j){
//		stats0[j] = PerformanceTest.stringFindTest(bensAuto_RAW, toFind);
//		stats1[j] = PerformanceTest.stringBufferFindTest(bensAuto_RAW, toFind);
//		stats2[j] = PerformanceTest.ropeFindTest(bensAuto_RAW, toFind);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope]");
//		}
//
//		System.out.println();
//		System.out.println("**** STRING SEARCH TEST (COMPLEXLY-CONSTRUCTED DATASTRUCTURES)****");
//		System.out.println("* Using a complexly constructed rope and the pattern 'consumes faster\n" +
//						   "* than Labor wears; while the used key is always bright,'.");
//
//		toFind = "Bob was very cheerful with them, and spoke pleasantly to";
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT], stats2 = new long[ITERATION_COUNT];
//		for (int j=0;j<ITERATION_COUNT;++j){
//		stats0[j] = PerformanceTest.stringFindTest2(complexString, toFind);
//		stats1[j] = PerformanceTest.stringBufferFindTest2(complexStringBuffer, toFind);
//		stats2[j] = PerformanceTest.ropeFindTest2(complexRope, toFind);
//		}
//		stat(System.out, stats0, "ns", "[String]");
//		stat(System.out, stats1, "ns", "[StringBuffer]");
//		stat(System.out, stats2, "ns", "[Rope]");
//		}
//
//
//		System.out.println();
//		System.out.println("**** WRITE TEST ****");
//		System.out.println("* Illustrates how to write a Rope to a stream efficiently.");
//
//		{
//		long[] stats0 = new long[ITERATION_COUNT], stats1 = new long[ITERATION_COUNT];
//		for (int j=0;j<ITERATION_COUNT;++j){
//		stats0[j] = PerformanceTest.ropeWriteBad(complexRope);
//		stats1[j] = PerformanceTest.ropeWriteGood(complexRope);
//		}
//		stat(System.out, stats0, "ns", "[Out.write]");
//		stat(System.out, stats1, "ns", "[Rope.write]");
//		}
//	}
//
//	private static long stringFindTest(char[] aChristmasCarol, String toFind) {
//		long x,y;
//
//		String b = new String(aChristmasCarol);
//		x = System.nanoTime();
//		int loc = b.indexOf(toFind);
//		y = System.nanoTime();
//		System.out.printf("[String.find]       indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
//		return (y-x);
//	}
//
//	private static long stringBufferFindTest(char[] aChristmasCarol, String toFind) {
//		long x,y;
//
//		StringBuffer b = new StringBuffer(aChristmasCarol.length); b.append(aChristmasCarol);
//		x = System.nanoTime();
//		int loc = b.indexOf(toFind);
//		y = System.nanoTime();
//		System.out.printf("[StringBuffer.find] indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
//		return (y-x);
//	}
//
//	private static long ropeFindTest(char[] aChristmasCarol, String toFind) {
//		long x,y;
//
//		Rope b = Rope.BUILDER.build(aChristmasCarol);
//		x = System.nanoTime();
//		int loc = b.indexOf(toFind);
//		y = System.nanoTime();
//		System.out.printf("[Rope.find]         indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
//		return (y-x);
//	}
//
//	private static long stringFindTest2(String aChristmasCarol, String toFind) {
//		long x,y;
//
//		x = System.nanoTime();
//		int loc = aChristmasCarol.indexOf(toFind);
//		y = System.nanoTime();
//		System.out.printf("[String.find]       indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
//		return (y-x);
//	}
//
//	private static long stringBufferFindTest2(StringBuffer aChristmasCarol, String toFind) {
//		long x,y;
//
//		x = System.nanoTime();
//		int loc = aChristmasCarol.indexOf(toFind);
//		y = System.nanoTime();
//		System.out.printf("[StringBuffer.find] indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
//		return (y-x);
//	}
//
//	private static long ropeFindTest2(Rope aChristmasCarol, String toFind) {
//		long x,y;
//
//		x = System.nanoTime();
//		int loc = aChristmasCarol.indexOf(toFind);
//		y = System.nanoTime();
//		System.out.printf("[Rope.find]         indexOf needle length %d found at index %d in % ,18d ns.\n", toFind.length(), loc, (y-x));
//		return (y-x);
//	}
//
//	private static long ropeWriteGood(Rope complexRope) {
//		long x,y;
//
//		Writer out = new StringWriter(complexRope.length());
//		x = System.nanoTime();
//		try {
//			complexRope.write(out);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		y = System.nanoTime();
//		System.out.printf("[Rope.write]   Executed write in % ,18d ns.\n", (y-x));
//		return (y-x);
//	}
//
//	private static long ropeWriteBad(Rope complexRope) {
//		long x,y;
//
//		Writer out = new StringWriter(complexRope.length());
//		x = System.nanoTime();
//		try {
//			out.write(complexRope.toString());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		y = System.nanoTime();
//		System.out.printf("[Out.write]    Executed write in % ,18d ns.\n", (y-x));
//		return (y-x);
//	}
//
//	private static char[] readBF() throws Exception {
//		final CharArrayWriter out = new CharArrayWriter(467196);
//		final BufferedReader in = new BufferedReader(new FileReader("AutobiographyOfBenjaminFranklin_BenjaminFranklin.txt"));
//
//		final char[] c = new char[256];
//		int x = -1;
//		while (-1 != (x=in.read(c))) {
//			out.write(c, 0, x);
//		}
//		out.close();
//		return out.toCharArray();
//	}
//
//	private static char[] readCC() throws Exception {
//		final CharArrayWriter out = new CharArrayWriter(182029);
//		final BufferedReader in = new BufferedReader(new FileReader("AChristmasCarol_CharlesDickens.txt"));
//
//		final char[] c = new char[256];
//		int x = -1;
//		while (-1 != (x=in.read(c))) {
//			out.write(c, 0, x);
//		}
//		out.close();
//		return out.toCharArray();
//	}
//
//	private static long ropeAppendTest(final String aChristmasCarol, final int[][] appendPlan, final int planLength) {
//		long x,y;
//
//		x = System.nanoTime();
//		Rope result=Rope.BUILDER.build(aChristmasCarol);
//
//		for (int j=0; j<planLength; ++j) {
//			final int offset = appendPlan[j][0];
//			final int length = appendPlan[j][1];
//			result = result.append(result.subSequence(offset, offset+length));
//		}
//		y = System.nanoTime();
//		System.out.printf("[Rope]         Executed append plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n", (y-x), result.length(), ((AbstractRope)result).depth());
//		return (y-x);
//	}
//
//	private static long ropeDeleteTest(final String aChristmasCarol, final int[][] prependPlan) {
//		long x,y;
//
//		x = System.nanoTime();
//		Rope result=Rope.BUILDER.build(aChristmasCarol);
//
//		for (int j=0; j<prependPlan.length; ++j) {
//			final int offset = prependPlan[j][0];
//			final int length = prependPlan[j][1];
//			result = result.delete(offset, offset + length);
//		}
//		y = System.nanoTime();
//		System.out.printf("[Rope]         Executed delete plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n", (y-x), result.length(), ((AbstractRope)result).depth());
//		return (y-x);
//	}
//
//	private static long ropeInsertTest(final char[] aChristmasCarol, final int[][] insertPlan, int planLength) {
//		long x,y;
//		Rope result=Rope.BUILDER.build(aChristmasCarol);
//
//		x = System.nanoTime();
//
//		for (int j=0; j<planLength; ++j) {
//			final int into   = insertPlan[j][0];
//			final int offset = insertPlan[j][1];
//			final int length = insertPlan[j][2];
//			result = result.insert(into, result.subSequence(offset, offset+length));
//		}
//		y = System.nanoTime();
//		System.out.printf("[Rope]         Executed insert plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n", (y-x), result.length(), ((AbstractRope)result).depth());
//		complexRope = result;
//		return (y-x);
//	}
//
//	private static long textInsertTest(final char[] aChristmasCarol, final int[][] insertPlan, int planLength) {
//		long x,y;
//		Text result=new Text(new String(aChristmasCarol));
//
//		x = System.nanoTime();
//
//		for (int j=0; j<planLength; ++j) {
//			final int into   = insertPlan[j][0];
//			final int offset = insertPlan[j][1];
//			final int length = insertPlan[j][2];
//			result = result.insert(into, result.subtext(offset, offset+length));
//		}
//		y = System.nanoTime();
//		System.out.printf("[Text]         Executed insert plan in % ,18d ns. Result has length: %d.\n", (y-x), result.length());
//		complexText = result;
//		return (y-x);
//	}
//
//	private static long ropeInsertTest2(final String aChristmasCarol, final String bensAuto, final int[][] insertPlan) {
//		long x,y;
//
//		x = System.nanoTime();
//		Rope result=Rope.BUILDER.build(aChristmasCarol);
//
//		for (int j=0; j<insertPlan.length; ++j) {
//			final int into   = insertPlan[j][0];
//			final int offset = insertPlan[j][1];
//			final int length = insertPlan[j][2];
//			result = result.insert(into, bensAuto.subSequence(offset, offset+length));
//		}
//		y = System.nanoTime();
//		System.out.printf("[Rope]         Executed insert plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n", (y-x), result.length(), ((AbstractRope)result).depth());
//		return (y-x);
//	}
//
//	private static long ropePrependTest(final String aChristmasCarol, final int[][] prependPlan, int planLength) {
//		long x,y;
//
//		x = System.nanoTime();
//		Rope result=Rope.BUILDER.build(aChristmasCarol);
//
//		for (int j=0; j<planLength; ++j) {
//			final int offset = prependPlan[j][0];
//			final int length = prependPlan[j][1];
//			result = result.subSequence(offset, offset+length).append(result);
//		}
//		y = System.nanoTime();
//		System.out.printf("[Rope]         Executed prepend plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n", (y-x), result.length(), ((AbstractRope)result).depth());
//		return (y-x);
//	}
//
//	private static long textPrependTest(final String aChristmasCarol, final int[][] prependPlan, int planLength) {
//		long x,y;
//
//		x = System.nanoTime();
//		Text result=new Text(aChristmasCarol);
//
//		for (int j=0; j<planLength; ++j) {
//			final int offset = prependPlan[j][0];
//			final int length = prependPlan[j][1];
//			result = result.subtext(offset, offset+length).concat(result);
//		}
//		y = System.nanoTime();
//		System.out.printf("[Text]         Executed prepend plan in % ,18d ns. Result has length: %d.\n", (y-x), result.length());
//		return (y-x);
//	}
//
//	private static long ropeTraverseTest_1(final char[] aChristmasCarol) {
//		long x,y,result=0;
//		final Rope r=Rope.BUILDER.build(aChristmasCarol);
//
//		x = System.nanoTime();
//
//		for (int j=0; j<r.length(); ++j) result+=r.charAt(j);
//
//		y = System.nanoTime();
//		System.out.printf("[Rope/charAt]  Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static long ropeTraverseTest_2(final char[] aChristmasCarol) {
//		long x,y,result=0;
//		final Rope r=Rope.BUILDER.build(aChristmasCarol);
//		
//		x = System.nanoTime();
//
//		for (final char c: r) result+=c;
//
//		y = System.nanoTime();
//		System.out.printf("[Rope/itr]     Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static long ropeTraverseTest2_1(Rope aChristmasCarol) {
//		long x,y;
//
//		Rope result=aChristmasCarol;
//		
//		int r=0;
//		x = System.nanoTime();
//		for (int j=0; j<result.length(); ++j) r+=result.charAt(j);
//		y = System.nanoTime();
//		System.out.printf("[Rope/charAt]  Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), r);
//		return (y-x);
//	}
//
//	private static long textTraverseTest2(Text aChristmasCarol) {
//		long x,y;
//
//		Text result=aChristmasCarol;
//		
//		int r=0;
//		x = System.nanoTime();
//		for (int j=0; j<result.length(); ++j) r+=result.charAt(j);
//		y = System.nanoTime();
//		System.out.printf("[Text/charAt]  Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), r);
//		return (y-x);
//	}
//
//	private static long ropeTraverseTest2_2(Rope aChristmasCarol) {
//		long x,y;
//
//		Rope result=aChristmasCarol;
//		
//		int r=0;
//		x = System.nanoTime();
//		for (final char c: result) r+=c;
//		y = System.nanoTime();
//		System.out.printf("[Rope/itr]     Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), r);
//		return (y-x);
//	}
//
//	private static long stringAppendTest(final String aChristmasCarol, final int[][] appendPlan, final int planLength) {
//		long x,y;
//
//		x = System.nanoTime();
//		String result=aChristmasCarol;
//
//		for (int j=0; j<planLength; ++j) {
//			final int offset = appendPlan[j][0];
//			final int length = appendPlan[j][1];
//			result = result.concat(result.substring(offset, offset + length));
//		}
//		y = System.nanoTime();
//		System.out.printf("[String]       Executed append plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
//		return (y-x);
//	}
//
//	private static long stringBufferAppendTest(final String aChristmasCarol, final int[][] appendPlan, final int planLength) {
//		long x,y;
//
//		x = System.nanoTime();
//		final StringBuffer result=new StringBuffer(aChristmasCarol);
//
//		for (int j=0; j<planLength; ++j) {
//			final int offset = appendPlan[j][0];
//			final int length = appendPlan[j][1];
//			result.append(result.subSequence(offset, offset+length));
//		}
//		y = System.nanoTime();
//		System.out.printf("[StringBuffer] Executed append plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
//		return (y-x);
//	}
//
//	private static long stringBufferDeleteTest(final String aChristmasCarol, final int[][] prependPlan) {
//		long x,y;
//
//		x = System.nanoTime();
//		final StringBuffer result=new StringBuffer(aChristmasCarol);
//
//		for (int j=0; j<prependPlan.length; ++j) {
//			final int offset = prependPlan[j][0];
//			final int length = prependPlan[j][1];
//			result.delete(offset, offset+length);
//		}
//		y = System.nanoTime();
//		System.out.printf("[StringBuffer] Executed delete plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
//		return (y-x);
//	}
//
//	private static long stringBufferInsertTest(final char[] aChristmasCarol, final int[][] insertPlan, int planLength) {
//		long x,y;
//		final StringBuffer result=new StringBuffer(aChristmasCarol.length); result.append(aChristmasCarol);
//
//		x = System.nanoTime();
//
//		for (int j=0; j<planLength; ++j) {
//			final int into   = insertPlan[j][0];
//			final int offset = insertPlan[j][1];
//			final int length = insertPlan[j][2];
//			result.insert(into, result.subSequence(offset, offset+length));
//		}
//		y = System.nanoTime();
//		System.out.printf("[StringBuffer] Executed insert plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
//		complexStringBuffer = result;
//		return (y-x);
//	}
//
//	private static long stringBufferInsertTest2(final String aChristmasCarol, final String bensAuto, final int[][] insertPlan) {
//		long x,y;
//
//		x = System.nanoTime();
//		final StringBuffer result=new StringBuffer(aChristmasCarol);
//
//		for (int j=0; j<insertPlan.length; ++j) {
//			final int into   = insertPlan[j][0];
//			final int offset = insertPlan[j][1];
//			final int length = insertPlan[j][2];
//			result.insert(into, bensAuto.subSequence(offset, offset+length));
//		}
//		y = System.nanoTime();
//		System.out.printf("[StringBuffer] Executed insert plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
//		return (y-x);
//	}
//
//	private static long stringBufferPrependTest(final String aChristmasCarol, final int[][] prependPlan, int planLength) {
//		long x,y;
//
//		x = System.nanoTime();
//		final StringBuffer result=new StringBuffer(aChristmasCarol);
//
//		for (int j=0; j<planLength; ++j) {
//			final int offset = prependPlan[j][0];
//			final int length = prependPlan[j][1];
//			result.insert(0, result.subSequence(offset, offset+length));
//		}
//		y = System.nanoTime();
//		System.out.printf("[StringBuffer] Executed prepend plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
//		return (y-x);
//	}
//
//	private static long stringBufferTraverseTest(final char[] aChristmasCarol) {
//		long x,y,result=0;
//		final StringBuffer b=new StringBuffer(aChristmasCarol.length); b.append(aChristmasCarol);
//
//		x = System.nanoTime();
//
//		for (int j=0; j<b.length(); ++j) result+=b.charAt(j);
//
//		y = System.nanoTime();
//		System.out.printf("[StringBuffer] Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), result);
//		return (y-x);
//
//	}
//
//	private static long stringBufferTraverseTest2(final StringBuffer aChristmasCarol) {
//		long x,y;
//
//		final StringBuffer result=aChristmasCarol;
//		
//		int r=0;
//		x = System.nanoTime();
//		for (int j=0; j<result.length(); ++j) r+=result.charAt(j);
//		y = System.nanoTime();
//		System.out.printf("[StringBuffer] Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), r);
//		return (y-x);
//	}
//
//	private static long stringDeleteTest(final String aChristmasCarol, final int[][] prependPlan) {
//		long x,y;
//
//		x = System.nanoTime();
//		String result=aChristmasCarol;
//
//		for (int j=0; j<prependPlan.length; ++j) {
//			final int offset = prependPlan[j][0];
//			final int length = prependPlan[j][1];
//			result = result.substring(0, offset).concat(result.substring(offset+length));
//		}
//		y = System.nanoTime();
//		System.out.printf("[String]       Executed delete plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
//		return (y-x);
//	}
//
//	private static long stringInsertTest(final char[] aChristmasCarol, final int[][] insertPlan, int planLength) {
//		long x,y;
//		String result=new String(aChristmasCarol);
//
//		x = System.nanoTime();
//
//		for (int j=0; j<planLength; ++j) {
//			final int into   = insertPlan[j][0];
//			final int offset = insertPlan[j][1];
//			final int length = insertPlan[j][2];
//			result = result.substring(0, into).concat(result.substring(offset, offset + length)).concat(result.substring(into));
//			
//		}
//		y = System.nanoTime();
//		System.out.printf("[String]       Executed insert plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
//		complexString = result;
//		return (y-x);
//	}
//
//	private static long stringInsertTest2(final String aChristmasCarol, final String bensAuto, final int[][] insertPlan) {
//		long x,y;
//
//		x = System.nanoTime();
//		String result=aChristmasCarol;
//
//		for (int j=0; j<insertPlan.length; ++j) {
//			final int into   = insertPlan[j][0];
//			final int offset = insertPlan[j][1];
//			final int length = insertPlan[j][2];
//			result = result.substring(0, into).concat(bensAuto.substring(offset, offset + length)).concat(result.substring(into));
//		}
//		y = System.nanoTime();
//		System.out.printf("[String]       Executed insert plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
//		return (y-x);
//	}
//
//	private static long stringPrependTest(final String aChristmasCarol, final int[][] prependPlan, int planLength) {
//		long x,y;
//
//		x = System.nanoTime();
//		String result=aChristmasCarol;
//
//		for (int j=0; j<planLength; ++j) {
//			final int offset = prependPlan[j][0];
//			final int length = prependPlan[j][1];
//			result = result.substring(offset, offset + length).concat(result);
//		}
//		y = System.nanoTime();
//		System.out.printf("[String]       Executed prepend plan in % ,18d ns. Result has length: %d\n", (y-x), result.length());
//		return (y-x);
//	}
//
//	private static long stringTraverseTest(final char[] aChristmasCarol) {
//		long x,y,result=0;
//		String s = new String(aChristmasCarol);
//
//		x = System.nanoTime();
//
//		for (int j=0; j<s.length(); ++j) result+=s.charAt(j);
//
//		y = System.nanoTime();
//		System.out.printf("[String]       Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static long stringTraverseTest2(final String aChristmasCarol) {
//		long x,y;
//
//		String result=aChristmasCarol;
//
//		int r=0;
//		x = System.nanoTime();
//		for (int j=0; j<result.length(); ++j) r+=result.charAt(j);
//		y = System.nanoTime();
//		System.out.printf("[String]       Executed traversal in % ,18d ns. Result checksum: %d\n", (y-x), r);
//		return (y-x);
//	}
//
//	private static long stringRegexpTest(final char[] aChristmasCarol, Pattern pattern) {
//		long x,y;
//		String s = new String(aChristmasCarol);
//
//		x = System.nanoTime();
//
//		int result = 0;
//		Matcher m = pattern.matcher(s);
//		while (m.find()) ++result;
//		
//		y = System.nanoTime();
//		System.out.printf("[String]       Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static long textRegexpTest(final char[] aChristmasCarol, Pattern pattern) {
//		long x,y;
//		Text s = new Text(new String(aChristmasCarol));
//
//		x = System.nanoTime();
//
//		int result = 0;
//		Matcher m = pattern.matcher(s);
//		while (m.find()) ++result;
//		
//		y = System.nanoTime();
//		System.out.printf("[Text]         Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static long stringBufferRegexpTest(final char[] aChristmasCarol, Pattern pattern) {
//		long x,y;
//		StringBuffer buffer = new StringBuffer(aChristmasCarol.length); buffer.append(aChristmasCarol);
//
//		x = System.nanoTime();
//
//		int result = 0;
//		Matcher m = pattern.matcher(buffer);
//		while (m.find()) ++result;
//		
//		y = System.nanoTime();
//		System.out.printf("[StringBuffer] Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static long ropeRegexpTest(final char[] aChristmasCarol, Pattern pattern) {
//		long x,y;
//		Rope rope = Rope.BUILDER.build(aChristmasCarol);
//
//		x = System.nanoTime();
//
//		int result = 0;
//		Matcher m = pattern.matcher(rope);
//		while (m.find()) ++result;
//		
//		y = System.nanoTime();
//		System.out.printf("[Rope]         Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static long ropeMatcherRegexpTest(final char[] aChristmasCarol, Pattern pattern) {
//		long x,y;
//		Rope rope = Rope.BUILDER.build(aChristmasCarol);
//
//		x = System.nanoTime();
//
//		int result = 0;
//		Matcher m = rope.matcher(pattern); 
//		while (m.find()) ++result;
//		
//		y = System.nanoTime();
//		System.out.printf("[Rope.matcher] Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
//		return (y-x);
//	}
//	
//
//
//	private static long stringRegexpTest2(final String aChristmasCarol, Pattern pattern) {
//		long x,y;
//
//		x = System.nanoTime();
//
//		int result = 0;
//		Matcher m = pattern.matcher(aChristmasCarol);
//		while (m.find()) ++result;
//		
//		y = System.nanoTime();
//		System.out.printf("[String]       Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
//		return (y-x);
//	}
//	
//
//
//	private static long textRegexpTest2(final Text aChristmasCarol, Pattern pattern) {
//		long x,y;
//
//		x = System.nanoTime();
//
//		int result = 0;
//		Matcher m = pattern.matcher(aChristmasCarol);
//		while (m.find()) ++result;
//		
//		y = System.nanoTime();
//		System.out.printf("[Text]         Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static long stringBufferRegexpTest2(final StringBuffer aChristmasCarol, Pattern pattern) {
//		long x,y;
//
//		x = System.nanoTime();
//
//		int result = 0;
//		Matcher m = pattern.matcher(aChristmasCarol);
//		while (m.find()) ++result;
//		
//		y = System.nanoTime();
//		System.out.printf("[StringBuffer] Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static long ropeRegexpTest2(final Rope aChristmasCarol, Pattern pattern) {
//		long x,y;
//
//		x = System.nanoTime();
//
//		int result = 0;
//		Matcher m = pattern.matcher(aChristmasCarol);
//		while (m.find()) ++result;
//		
//		y = System.nanoTime();
//		System.out.printf("[Rope]         Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static long ropeRebalancedRegexpTest2(final Rope aChristmasCarol, Pattern pattern) {
//		long x,y;
//
//		x = System.nanoTime();
//
//		CharSequence adaptedRope = aChristmasCarol.rebalance(); //Rope.BUILDER.buildForRegexpSearching(aChristmasCarol);
//		int result = 0;
//		Matcher m = pattern.matcher(adaptedRope);
//		while (m.find()) ++result;
//		
//		y = System.nanoTime();
//		System.out.printf("[Reblncd Rope] Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static long ropeMatcherRegexpTest2(final Rope aChristmasCarol, Pattern pattern) {
//		long x,y;
//
//		x = System.nanoTime();
//		
//		int result = 0;
//		Matcher m = aChristmasCarol.matcher(pattern);
//		while (m.find()) ++result;
//		
//		y = System.nanoTime();
//		System.out.printf("[Rope.matcher] Executed regexp test in % ,18d ns. Found %d matches.\n", (y-x), result);
//		return (y-x);
//	}
//
//	private static String time(final long x, final long y) {
//		return (y-x) + "ns";
//	}
//	
//	private static void stat(PrintStream out, long[] stats, String unit, String prefix) {
//		if (stats.length < 3) 
//			System.err.println("Cannot printMeaning stats.");
//		Arrays.sorted(stats);
//		
//		double median = ((stats.length & 1) == 1 ? stats[stats.length >> 1]: (stats[stats.length >> 1] + stats[1 + (stats.length >> 1)]) / 2);
//		double average = 0;
//		for (int j=1;j<stats.length-1;++j) {
//			average += stats[j];
//		}
//		average /= stats.length - 2;
//		out.printf("%-14s Average=% ,16.0f %s Median=% ,16.0f%s\n", prefix, average, unit, median, unit);
//	}
//
//}
