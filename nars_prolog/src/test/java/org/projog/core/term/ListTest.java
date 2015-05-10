package org.projog.core.term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.assertStrictEquality;
import static org.projog.TestUtils.atom;
import static org.projog.TestUtils.decimalFraction;
import static org.projog.TestUtils.integerNumber;
import static org.projog.TestUtils.list;
import static org.projog.TestUtils.structure;
import static org.projog.TestUtils.variable;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.projog.TestUtils;

/**
 * @see TermTest
 */
public class ListTest {
   private static final PTerm head = new Atom("a");
   private static final PTerm tail = new Atom("b");
   private static final PList testList = new PList(head, tail, true);

   @Test
   public void testGetName() {
      assertEquals(".", testList.getName());
   }

   @Test
   public void testToString() {
      assertEquals(".(a, b)", testList.toString());
   }

   @Test
   public void testGetTerm() {
      PList l = testList.get();
      assertSame(testList, l);
   }

   @Test
   public void testGetType() {
      assertSame(TermType.LIST, testList.type());
   }

   @Test
   public void testGetNumberOfArguments() {
      assertEquals(2, testList.args());
   }

   @Test
   public void testGetArgument() {
      assertSame(head, testList.arg(0));
      assertSame(tail, testList.arg(1));
   }

   @Test
   public void testGetArgs() {
      try {
         testList.getArgs();
         fail();
      } catch (UnsupportedOperationException e) {
         // expected
      }
   }

   @Test
   public void testCopyNoVariableElements() {
      assertSame(testList, testList.copy(null));
   }

   @Test
   public void testCopyVariableElements() {
      Atom a = new Atom("a");
      Atom b = new Atom("b");
      Variable X = new Variable("X");
      Variable Y = new Variable("Y");
      Structure head = structure("p", X);

      PList original = new PList(head, Y, false); // [p(X), Y]

      assertSame(original, original.get());

      Map<Variable, Variable> sharedVariables = new HashMap<Variable, Variable>();
      PList copy1 = original.copy(sharedVariables);
      assertNotSame(original, copy1);
      assertStrictEquality(original, copy1, false);
      assertEquals(2, sharedVariables.size());
      assertTrue(sharedVariables.containsKey(X));
      assertTrue(sharedVariables.containsKey(Y));
      assertEquals(original.toString(), copy1.toString());

      assertTrue(X.unify(a));
      assertTrue(Y.unify(b));

      PList copy2 = original.copy(null);
      assertNotSame(original, copy2);
      assertStrictEquality(original, copy2, true);
      assertEquals(original.toString(), copy2.toString());
      assertSame(copy2, copy2.copy(null));
      assertSame(copy2, copy2.get());

      X.backtrack();
      Y.backtrack();

      assertStrictEquality(original, copy2, false);

      assertEquals(".(p(X), Y)", original.toString());
      assertEquals(".(p(a), b)", copy2.toString());
   }

   @Test
   public void testGetValueNoVariableElements() {
      assertSame(testList, testList.get());
   }

   @Test
   public void testListWithVariableArguments() {
      Atom a = new Atom("a");
      Atom b = new Atom("b");
      Variable X = new Variable("X");
      Variable Y = new Variable("Y");
      PList l1 = new PList(a, Y, false);
      PList l2 = new PList(X, b, false);

      assertStrictEqualityUnifyAndBacktrack(l1, l2);
      assertStrictEqualityUnifyAndBacktrack(l2, l1);
   }

   @Test
   public void testUnifyWhenBothListsHaveVariableArguments_1() {
      // [x, Y]
      PList l1 = new PList(new Atom("x"), new Variable("Y"), false);
      // [X, y]
      PList l2 = new PList(new Variable("X"), new Atom("y"), false);
      assertTrue(l1.unify(l2));
      assertEquals(".(x, y)", l1.toString());
      assertEquals(l1.toString(), l2.toString());
   }

   @Test
   public void testUnifyWhenBothListsHaveVariableArguments_2() {
      // [x, z]
      PList l1 = new PList(new Atom("x"), new Atom("z"), false);
      // [X, y]
      PList l2 = new PList(new Variable("X"), new Atom("y"), false);
      assertFalse(l1.unify(l2));
      assertEquals(".(x, z)", l1.toString());
      // Note: following is expected quirk - list doesn't automatically backtrack on failure
      assertEquals(".(x, y)", l2.toString());

      l2.backtrack();
      assertEquals(".(X, y)", l2.toString());
   }

   @Test
   public void testUnifyWhenBothListsHaveVariableArguments_3() {
      // [X, z]
      PList l1 = new PList(new Variable("X"), new Atom("z"), false);
      // [x, y]
      PList l2 = new PList(new Atom("x"), new Atom("y"), false);
      assertFalse(l1.unify(l2));
      // Note: following is expected quirk - list doesn't automatically backtrack on failure
      assertEquals(".(x, z)", l1.toString());
      assertEquals(".(x, y)", l2.toString());

      l1.backtrack();
      assertEquals(".(X, z)", l1.toString());
   }

   @Test
   public void testLongList() {
      StringBuilder bigListSyntaxBuilder1 = new StringBuilder("[");
      StringBuilder bigListSyntaxBuilder2 = new StringBuilder("[");
      for (int i = 0; i < 10000; i++) {
         if (i != 0) {
            bigListSyntaxBuilder1.append(",");
            bigListSyntaxBuilder2.append(",");
         }
         bigListSyntaxBuilder1.append(i);
         // make one element in second list different than first 
         if (i == 789) {
            bigListSyntaxBuilder2.append(i - 1);
         } else {
            bigListSyntaxBuilder2.append(i);
         }
      }
      bigListSyntaxBuilder1.append("]");
      bigListSyntaxBuilder2.append("]");
      String bigListSyntax1 = bigListSyntaxBuilder1.toString();
      String bigListSyntax2 = bigListSyntaxBuilder2.toString();
      PList t1 = (PList) TestUtils.parseSentence(bigListSyntax1 + ".");
      PList t2 = (PList) TestUtils.parseSentence(bigListSyntax1 + ".");
      PList t3 = (PList) TestUtils.parseSentence(bigListSyntax2 + ".");
      assertNotSame(t1, t2);
      // NOTE important to test write method doesn't throw stackoverflow
      assertEquals(bigListSyntax1, TestUtils.write(t1));
      assertEquals(bigListSyntax2, TestUtils.write(t3));
      assertMatch(t1, t1, true);
      assertMatch(t1, t2, true);
      assertMatch(t1, t3, false);
   }

   @Test
   public void testIsImmutable() {
      Variable v = variable("X");
      Atom a = atom("test");
      PList l1 = list(atom(), structure("p", atom(), v, integerNumber()), list(integerNumber(), decimalFraction()));
      assertFalse(l1.constant());
      v.unify(a);
      PList l2 = l1.copy(null);
      assertFalse(l1.constant());
      assertTrue(l2.toString(), l2.constant());
      assertSame(v, l1.arg(1).arg(0).arg(1));
      assertSame(a, l2.arg(1).arg(0).arg(1));
   }

   private void assertMatch(PList l1, PList l2, boolean expectMatch) {
      // NOTE important to test toString, strictEquality and unify 
      // methods doesn't throw stackoverflow
      assertEquals(expectMatch, l1.strictEquals(l2));
      assertEquals(expectMatch, l1.unify(l2));
      assertEquals(expectMatch, l1.toString().equals(l2.toString()));
   }

   private void assertStrictEqualityUnifyAndBacktrack(PList l1, PList l2) {
      assertStrictEquality(l1, l2, false);
      assertSame(l1, l1.get());
      assertSame(l2, l2.get());

      l1.unify(l2);

      assertStrictEquality(l1, l2, true);
      assertNotSame(l1, l1.get());
      assertNotSame(l2, l2.get());

      l1.backtrack();
      l2.backtrack();

      assertStrictEquality(l1, l2, false);
      assertSame(l1, l1.get());
      assertSame(l2, l2.get());
   }
}