package org.projog.core.term;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.projog.TestUtils.*;
import static org.projog.core.term.TermComparator.TERM_COMPARATOR;

public class TermComparatorTest {
   /**
    * selection of terms ordered in lowest precedence first order
    * <p>
    * Note: only one variable and no ANONYMOUS_VARIABLE as ANONYMOUS_VARIABLE and "variable against variable"
    * comparisons tested separately.
    */
   private static final PTerm[] TERMS_ORDERED_IN_LOWEST_PRECEDENCE = {variable("A"),

   decimalFraction(-2.1), decimalFraction(-1.9), decimalFraction(0), decimalFraction(1),

   integerNumber(-2), integerNumber(0), integerNumber(1),

   EmptyList.EMPTY_LIST,

   atom("a"), atom("z"),

   structure("a", atom("b")), structure("b", atom("a")), structure("b", structure("a", atom())),

   structure("!", atom("a"), atom("b")),

   list(atom("a"), atom("b")), list(atom("b"), atom("a")), list(atom("b"), atom("a"), atom("b")), list(atom("c"), atom("a")), list(structure("a", atom()), atom("b")),

   structure("a", atom("a"), atom("b")), structure("a", atom("a"), atom("z")), structure("a", atom("a"), structure("z", atom()))};

   @Test
   public void testCompareTerms() {
      for (int i = 0; i < TERMS_ORDERED_IN_LOWEST_PRECEDENCE.length; i++) {
         PTerm t1 = TERMS_ORDERED_IN_LOWEST_PRECEDENCE[i];
         testEqual(t1, t1);
         for (int z = i + 1; z < TERMS_ORDERED_IN_LOWEST_PRECEDENCE.length; z++) {
            PTerm t2 = TERMS_ORDERED_IN_LOWEST_PRECEDENCE[z];
            testIsGreater(t2, t1);

            PTerm v1 = variable("X");
            PTerm v2 = variable("Y");
            v1.unify(t1);
            v2.unify(t2);
            testIsGreater(v2, t1);
            testIsGreater(t2, v1);
            testIsGreater(v2, v1);
         }
      }
   }

   @Test
   public void testVariablesAssignedToEachOther() {
      PAtom a = atom("a");
      PVar x = new PVar("X");
      PVar y = new PVar("Y");
      PVar z = new PVar("Z");

      testNotEqual(z, y);
      testNotEqual(z, x);
      testNotEqual(y, x);

      x.unify(z);

      testNotEqual(z, y);
      testEqual(z, x);
      testNotEqual(x, y);

      x.unify(atom("a"));

      testEqual(x, z);
      testEqual(x, a);
      testEqual(z, a);
      testIsGreater(x, y);
      testIsGreater(z, y);

      y.unify(x);
      testEqual(x, y);
      testEqual(x, z);
      testEqual(y, z);
      testEqual(z, a);
   }

   private void testNotEqual(PTerm t1, PTerm t2) {
      assertTrue(t1 + " " + t2, TERM_COMPARATOR.compare(t1, t2) != 0);
      assertTrue(t2 + " " + t1, TERM_COMPARATOR.compare(t2, t1) != 0);
   }

   private void testEqual(PTerm t1, PTerm t2) {
      assertEquals(t1 + " " + t2, 0, TERM_COMPARATOR.compare(t1, t2));
      assertEquals(t2 + " " + t1, 0, TERM_COMPARATOR.compare(t2, t1));
   }

   private void testIsGreater(PTerm t1, PTerm t2) {
      assertTrue(t1 + " " + t2, TERM_COMPARATOR.compare(t1, t2) > 0);
      assertTrue(t2 + " " + t1, TERM_COMPARATOR.compare(t2, t1) < 0);
   }
}