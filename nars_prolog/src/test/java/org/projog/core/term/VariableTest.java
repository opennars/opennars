package org.projog.core.term;

import org.junit.Test;
import org.projog.core.ProjogException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.projog.TestUtils.*;

/**
 * @see TermTest
 */
public class VariableTest {
   @Test
   public void testUnassignedVariableMethods() {
      PVar v = new PVar("X");

      assertEquals("X", v.getId());
      assertEquals("X", v.toString());
      assertSame(v, v.get());
      assertTrue(v.strictEquals(v));

      try {
         v.getName();
         fail();
      } catch (NullPointerException e) {
      }
      try {
         v.terms();
         fail();
      } catch (NullPointerException e) {
      }
      try {
         v.length();
         fail();
      } catch (NullPointerException e) {
      }
      try {
         v.term(0);
         fail();
      } catch (NullPointerException e) {
      }
      try {
         TermUtils.castToNumeric(v);
         fail();
      } catch (ProjogException e) {
         assertEquals("Expected Numeric but got: NAMED_VARIABLE with value: X", e.getMessage());
      }

      assertTrue(v.unify(v));

      // just check backtrack doesn't throw an exception
      v.backtrack();
   }

   @Test
   public void testUnifyVariables_1() {
      PVar x = new PVar("X");
      PVar y = new PVar("Y");
      assertStrictEquality(x, y, false);
      assertTrue(x.unify(y));
      assertStrictEquality(x, y, true);
      x.backtrack();
      assertStrictEquality(x, y, false);
   }

   @Test
   public void testUnifyVariables_2() {
      PAtom a = atom();
      PVar x = new PVar("X");
      PVar y = new PVar("Y");
      assertTrue(y.unify(a));
      assertTrue(x.unify(y));
      assertSame(a, x.get());
      x.backtrack();
      assertSame(x, x.get());
      assertSame(a, y.get());
   }

   @Test
   public void testUnifyVariables_3() {
      PAtom a = atom();
      PVar x = new PVar("X");
      PVar y = new PVar("Y");
      assertTrue(x.unify(y));
      assertTrue(y.unify(a));
      assertSame(a, x.get());
   }

   @Test
   public void testVariablesUnifiedToTheSameTerm() {
      PAtom a = atom();
      PVar x = new PVar("X");
      PVar y = new PVar("Y");
      assertStrictEquality(x, y, false);
      assertTrue(x.unify(a));
      assertTrue(y.unify(a));
      assertStrictEquality(x, y, true);
      x.backtrack();
      assertStrictEquality(x, y, false);
      assertSame(x, x.get());
      assertSame(a, y.get());
   }

   @Test
   public void testCopy() {
      PVar v = variable();
      Map<PVar, PVar> sharedVariables = new HashMap<>();
      PTerm copy = v.copy(sharedVariables);
      assertEquals(1, sharedVariables.size());
      assertSame(copy, sharedVariables.get(v));
      assertFalse(v.strictEquals(copy));
      assertTrue(v.unify(copy));
      assertTrue(v.strictEquals(copy));
   }

   /**
    * Tests that, when {@link PVar#copy(Map)} is called on a variable whose "copy" (contained in the specified Map)
    * is already instantiated, the term the "copy" is instantiated with gets returned rather than the "copy" itself.
    * <p>
    * This behaviour is required for things like
    * {@link org.projog.core.udp.interpreter.InterpretedTailRecursivePredicate} to work.
    */
   @Test
   public void testCopy_2() {
      PVar v = variable();
      PAtom a = atom();
      PStruct s1 = structure("name", v);
      PStruct s2 = structure("name", v);

      Map<PVar, PVar> sharedVariables = new HashMap<>();

      PStruct c1 = s1.copy(sharedVariables);
      assertTrue(c1.unify(structure("name", a)));

      PStruct c2 = s2.copy(sharedVariables);
      // check that the single argument of the newly copied structure is the atom itself
      // rather than a variable assigned to the atom
      assertSame(a, c2.term(0));
      // check that, while backtracking does affect the first copied structure,
      // it does not alter the second copied structure
      c1.backtrack();
      c2.backtrack();
      assertSame(PVar.class, c1.term(0).getClass());
      assertSame(a, c2.term(0));
   }

   @Test
   public void testIsImmutable() {
      PVar v = new PVar("X");
      assertFalse(v.constant());
      PAtom a = atom();
      assertTrue(v.unify(a));
      assertFalse(v.constant());
   }

   @Test
   public void testUnifyAnonymousVariable() {
      PVar v = variable();
      PVar anon = TermUtils.createAnonymousVariable();
      assertTrue(v.unify(anon));
      assertSame(anon, v.get());
   }

   @Test
   public void testVariableChain() {
      final PVar v1 = variable();
      PVar v2 = v1;
      for (int i = 0; i < 10000; i++) {
         PVar tmpVar = variable("V" + i);
         v2.unify(tmpVar);
         v2 = tmpVar;
      }
      PStruct t = structure("name", atom("a"), atom("b"), atom("c"));
      assertTrue(v2.unify(t));

      assertSame(t, v1.get());
      assertSame(t, v1.copy(null));
      assertEquals(t.toString(), v1.toString());
      assertSame(t.getName(), v1.getName());
      assertSame(t.type(), v1.type());
      assertSame(t.length(), v1.length());
      assertSame(t.terms(), v1.terms());
      assertSame(t.term(0), v1.term(0));
      assertTrue(t.strictEquals(v1));
      assertTrue(v1.strictEquals(t));
      assertTrue(v1.strictEquals(v1));
      assertTrue(t.unify(v1));
      assertTrue(v1.unify(t));
      assertFalse(v1.unify(atom()));
      assertFalse(atom().unify(v1));

      v2.backtrack();
      assertSame(v2, v1.get());

      v1.backtrack();
      assertSame(v1, v1.get());
   }
}