package org.projog.core.term;

import static org.junit.Assert.assertArrayEquals;
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
import static org.projog.TestUtils.parseSentence;
import static org.projog.TestUtils.structure;
import static org.projog.TestUtils.variable;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @see TermTest
 */
public class StructureTest {
   @Test
   public void testCreationWithArguments() {
      PTerm[] args = {atom(), structure(), integerNumber(), decimalFraction(), variable()};
      Structure p = structure("test", args);
      assertEquals("test", p.getName());
      assertArrayEquals(args, p.getArgs());
      assertEquals(5, p.args());
      for (int i = 0; i < args.length; i++) {
         assertSame(args[i], p.arg(i));
      }
      assertSame(TermType.STRUCTURE, p.type());
      assertEquals("test(test, test(test), 1, 1.0, X)", p.toString());
   }

   @Test
   public void testGetValueNoVariables() {
      Structure p = structure("p", atom(), structure("p", atom()), list(integerNumber(), decimalFraction()));
      Structure p2 = p.get();
      assertSame(p, p2);
   }

   @Test
   public void testGetValueUnassignedVariables() {
      Structure p = structure("p", variable(), structure("p", variable()), list(variable(), variable()));
      assertSame(p, p.get());
   }

   @Test
   public void testGetValueAssignedVariable() {
      Variable x = variable("X");
      Structure p1 = structure("p", atom(), structure("p", atom(), x, integerNumber()), list(integerNumber(), decimalFraction()));
      x.unify(atom());
      Structure p2 = p1.get();
      assertNotSame(p1, p2);
      assertEquals(p1.toString(), p2.toString());
      assertStrictEquality(p1, p2, true);
   }

   @Test
   public void testCreationList() {
      PTerm t = Structure.createStructure(".", new PTerm[] {atom("a"), atom("b")});
      assertEquals(TermType.LIST, t.type());
      assertTrue(t instanceof PList);
      PTerm l = parseSentence("[a | b].");
      assertEquals(l.toString(), t.toString());
   }

   @Test
   public void testUnifyWhenBothPredicatesHaveVariableArguments() {
      // test(x, Y)
      Structure p1 = structure("test", new Atom("x"), new Variable("Y"));
      // test(X, y)
      Structure p2 = structure("test", new Variable("X"), new Atom("y"));
      assertTrue(p1.unify(p2));
      assertEquals("test(x, y)", p1.toString());
      assertEquals(p1.toString(), p2.toString());
   }

   @Test
   public void testUnifyWhenPredicateHasSameVariableTwiceAsArgument() {
      // test(x, y)
      Structure p1 = structure("test", new Atom("x"), new Atom("y"));
      // test(X, X)
      Variable v = new Variable("X");
      Structure p2 = structure("test", v, v);

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
      Variable v = new Variable("X");
      Structure p = structure("test", v, v);
      assertEquals("test(X, X)", p.toString());
      assertTrue(v.unify(new Atom("x")));
      assertEquals("test(x, x)", p.toString());
   }

   @Test
   public void testUnifyDifferentNamesSameArguments() {
      PTerm[] args = {atom(), integerNumber(), decimalFraction()};
      Structure p1 = structure("test1", args);
      Structure p2 = structure("test2", args);
      Structure p3 = structure("test", args);
      assertStrictEqualityAndUnify(p1, p2, false);
      assertStrictEqualityAndUnify(p1, p3, false);
   }

   @Test
   public void testSameNamesDifferentArguments() {
      Structure[] predicates = {
                  structure("test1", new Atom("a"), new Atom("b"), new Atom("c")),
                  structure("test2", new Atom("a"), new Atom("b"), new Atom("d")),
                  structure("test3", new Atom("a"), new Atom("c"), new Atom("b")),
                  structure("test4", new Atom("a"), new Atom("b"))};
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
      Structure p = structure("1", new PTerm[] {atom()});
      assertStrictEqualityAndUnify(p, new Atom("1"), false);
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
      Structure p = structure("test", atom(), integerNumber(), decimalFraction());
      Structure copy = p.copy(null);
      assertSame(p, copy);
   }

   @Test
   public void testCopyWithVariables() {
      Structure p = structure("test", atom(), integerNumber(), decimalFraction(), variable());
      Structure copy = p.copy(new HashMap<Variable, Variable>());
      assertNotSame(p, copy);
      assertEquals("test", copy.getName());
      assertEquals(TermType.STRUCTURE, copy.type());
      assertEquals(p.args(), copy.args());
      assertEquals(p.getArgs().length, copy.getArgs().length);
      assertTrue(p.getArgs()[0] == copy.getArgs()[0]);
      assertTrue(p.getArgs()[1] == copy.getArgs()[1]);
      assertTrue(p.getArgs()[2] == copy.getArgs()[2]);
      assertTrue(p.getArgs()[3] != copy.getArgs()[3]);
   }

   @Test
   public void testCopyWithAssignedVariable() {
      Variable X = new Variable("X");
      Structure arg = structure("p", X);
      Structure original = structure("p", arg);

      assertSame(original, original.get());

      Map<Variable, Variable> sharedVariables = new HashMap<>();
      Structure copy1 = original.copy(sharedVariables);
      assertNotSame(original, copy1);
      assertStrictEquality(original, copy1, false);
      assertEquals(1, sharedVariables.size());
      assertTrue(sharedVariables.containsKey(X));
      assertEquals(original.toString(), copy1.toString());

      X.unify(atom("a"));

      Structure copy2 = original.copy(null);
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
      Variable v = variable("X");
      Atom a = atom("test");
      Structure p1 = structure("p", atom(), structure("p", atom(), v, integerNumber()), list(integerNumber(), decimalFraction()));
      assertFalse(p1.constant());
      v.unify(a);
      Structure p2 = p1.copy(null);
      assertFalse(p1.constant());
      assertTrue(p2.constant());
      assertSame(v, p1.arg(1).arg(1));
      assertSame(a, p2.arg(1).arg(1));
   }

   private void assertStrictEqualityAndUnify(PTerm t1, PTerm t2, boolean expectedResult) {
      assertStrictEquality(t1, t2, expectedResult);
      assertTrue(t1.unify(t2) == expectedResult);
   }
}