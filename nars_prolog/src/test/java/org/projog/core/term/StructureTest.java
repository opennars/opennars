package org.projog.core.term;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.projog.TestUtils.*;

/**
 * @see TermTest
 */
public class StructureTest {
   @Test
   public void testCreationWithArguments() {
      PTerm[] args = {atom(), structure(), integerNumber(), decimalFraction(), variable()};
      PStruct p = structure("test", args);
      assertEquals("test", p.getName());
      assertArrayEquals(args, p.terms());
      assertEquals(5, p.length());
      for (int i = 0; i < args.length; i++) {
         assertSame(args[i], p.term(i));
      }
      assertSame(PrologOperator.STRUCTURE, p.type());
      assertEquals("test(test, test(test), 1, 1.0, X)", p.toString());
   }

   @Test
   public void testGetValueNoVariables() {
      PStruct p = structure("p", atom(), structure("p", atom()), list(integerNumber(), decimalFraction()));
      PStruct p2 = p.get();
      assertSame(p, p2);
   }

   @Test
   public void testGetValueUnassignedVariables() {
      PStruct p = structure("p", variable(), structure("p", variable()), list(variable(), variable()));
      assertSame(p, p.get());
   }

   @Test
   public void testGetValueAssignedVariable() {
      PVar x = variable("X");
      PStruct p1 = structure("p", atom(), structure("p", atom(), x, integerNumber()), list(integerNumber(), decimalFraction()));
      x.unify(atom());
      PStruct p2 = p1.get();
      assertNotSame(p1, p2);
      assertEquals(p1.toString(), p2.toString());
      assertStrictEquality(p1, p2, true);
   }

   @Test
   public void testCreationList() {
      PTerm t = PStruct.make(".", new PTerm[]{atom("a"), atom("b")});
      assertEquals(PrologOperator.LIST, t.type());
      assertTrue(t instanceof PList);
      PTerm l = parseSentence("[a | b].");
      assertEquals(l.toString(), t.toString());
   }

   @Test
   public void testUnifyWhenBothPredicatesHaveVariableArguments() {
      // test(x, Y)
      PStruct p1 = structure("test", new PAtom("x"), new PVar("Y"));
      // test(X, y)
      PStruct p2 = structure("test", new PVar("X"), new PAtom("y"));
      assertTrue(p1.unify(p2));
      assertEquals("test(x, y)", p1.toString());
      assertEquals(p1.toString(), p2.toString());
   }

   @Test
   public void testUnifyWhenPredicateHasSameVariableTwiceAsArgument() {
      // test(x, y)
      PStruct p1 = structure("test", new PAtom("x"), new PAtom("y"));
      // test(X, X)
      PVar v = new PVar("X");
      PStruct p2 = structure("test", v, v);

      assertFalse(p2.unify(p1));
      assertEquals("test(x, y)", p1.toString());
      // Note: following is expected quirk - predicate doesn't automatically backtrack on failure
      assertEquals("test(x, x)", p2.toString());

      p2.backtrack();
      assertEquals("test(X, X)", p2.toString());

      assertFalse(p1.unify(p2));
      assertEquals("test(x, y)", p1.toString());
      // Note: following is expected quirk - predicate doesn't automatically backtrack on failure
      assertEquals("test(x, x)", p2.toString());

      p2.backtrack();
      assertEquals("test(X, X)", p2.toString());
   }

   @Test
   public void testUnifyVariableThatIsPredicateArgument() {
      // test(X, X)
      PVar v = new PVar("X");
      PStruct p = structure("test", v, v);
      assertEquals("test(X, X)", p.toString());
      assertTrue(v.unify(new PAtom("x")));
      assertEquals("test(x, x)", p.toString());
   }

   @Test
   public void testUnifyDifferentNamesSameArguments() {
      PTerm[] args = {atom(), integerNumber(), decimalFraction()};
      PStruct p1 = structure("test1", args);
      PStruct p2 = structure("test2", args);
      PStruct p3 = structure("test", args);
      assertStrictEqualityAndUnify(p1, p2, false);
      assertStrictEqualityAndUnify(p1, p3, false);
   }

   @Test
   public void testSameNamesDifferentArguments() {
      PStruct[] predicates = {
                  structure("test1", new PAtom("a"), new PAtom("b"), new PAtom("c")),
                  structure("test2", new PAtom("a"), new PAtom("b"), new PAtom("d")),
                  structure("test3", new PAtom("a"), new PAtom("c"), new PAtom("b")),
                  structure("test4", new PAtom("a"), new PAtom("b"))};
      for (int i = 0; i < predicates.length; i++) {
         for (int j = i; j < predicates.length; j++) {
            if (i == j) {
               // check they all compare to a copy of themselves
               assertStrictEqualityAndUnify(predicates[i], predicates[i].copy(null), true);
            } else {
               assertStrictEqualityAndUnify(predicates[i], predicates[j], false);
            }
         }
      }
   }

   @Test
   public void testUnifyWrongType() {
      PStruct p = structure("1", new PTerm[] {atom()});
      assertStrictEqualityAndUnify(p, new PAtom("1"), false);
      assertStrictEqualityAndUnify(p, new IntegerNumber(1), false);
      assertStrictEqualityAndUnify(p, new DecimalFraction(1), false);
   }

   @Test
   public void testNoArguments() {
      try {
         structure("test", TermUtils.EMPTY_ARRAY);
         fail();
      } catch (IllegalArgumentException e) {
         assertEquals("Cannot create structure with no arguments", e.getMessage());
      }
   }

   @Test
   public void testCopyWithoutVariablesOrNestedArguments() {
      PStruct p = structure("test", atom(), integerNumber(), decimalFraction());
      PStruct copy = p.copy(null);
      assertSame(p, copy);
   }

   @Test
   public void testCopyWithVariables() {
      PStruct p = structure("test", atom(), integerNumber(), decimalFraction(), variable());
      PStruct copy = p.copy(new HashMap<PVar, PVar>());
      assertNotSame(p, copy);
      assertEquals("test", copy.getName());
      assertEquals(PrologOperator.STRUCTURE, copy.type());
      assertEquals(p.length(), copy.length());
      assertEquals(p.terms().length, copy.terms().length);
      assertTrue(p.terms()[0] == copy.terms()[0]);
      assertTrue(p.terms()[1] == copy.terms()[1]);
      assertTrue(p.terms()[2] == copy.terms()[2]);
      assertTrue(p.terms()[3] != copy.terms()[3]);
   }

   @Test
   public void testCopyWithAssignedVariable() {
      PVar X = new PVar("X");
      PStruct arg = structure("p", X);
      PStruct original = structure("p", arg);

      assertSame(original, original.get());

      Map<PVar, PVar> sharedVariables = new HashMap<>();
      PStruct copy1 = original.copy(sharedVariables);
      assertNotSame(original, copy1);
      assertStrictEquality(original, copy1, false);
      assertEquals(1, sharedVariables.size());
      assertTrue(sharedVariables.containsKey(X));
      assertEquals(original.toString(), copy1.toString());

      X.unify(atom("a"));

      PStruct copy2 = original.copy(null);
      assertNotSame(original, copy2);
      assertStrictEquality(original, copy2, true);
      assertEquals(original.toString(), copy2.toString());
      assertSame(copy2, copy2.copy(null));
      assertSame(copy2, copy2.get());

      X.backtrack();

      assertStrictEquality(original, copy2, false);

      assertEquals("p(p(X))", original.toString());
      assertEquals("p(p(a))", copy2.toString());
   }

   @Test
   public void testIsImmutable() {
      PVar v = variable("X");
      PAtom a = atom("test");
      PStruct p1 = structure("p", atom(), structure("p", atom(), v, integerNumber()), list(integerNumber(), decimalFraction()));
      assertFalse(p1.constant());
      v.unify(a);
      PStruct p2 = p1.copy(null);
      assertFalse(p1.constant());
      assertTrue(p2.constant());
      assertSame(v, p1.term(1).term(1));
      assertSame(a, p2.term(1).term(1));
   }

   private void assertStrictEqualityAndUnify(PTerm t1, PTerm t2, boolean expectedResult) {
      assertStrictEquality(t1, t2, expectedResult);
      assertTrue(t1.unify(t2) == expectedResult);
   }
}