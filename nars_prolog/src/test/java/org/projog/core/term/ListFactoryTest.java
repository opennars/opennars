package org.projog.core.term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.projog.TestUtils.atom;
import static org.projog.TestUtils.decimalFraction;
import static org.projog.TestUtils.integerNumber;
import static org.projog.TestUtils.structure;
import static org.projog.TestUtils.variable;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

public class ListFactoryTest {
   @Test
   public void testCreationWithoutTail() {
      final PTerm[] args = createArguments();
      PTerm l = ListFactory.createList(args);

      for (PTerm arg : args) {
         testIsList(l);
         assertEquals(arg, l.term(0));
         l = l.term(1);
      }

      assertSame(PrologOperator.EMPTY_LIST, l.type());
      assertSame(EmptyList.EMPTY_LIST, l);
   }

   @Test
   public void testCreationWithTail() {
      final PTerm[] args = createArguments();
      final PTerm tail = new PAtom("tail");
      PTerm l = ListFactory.createList(args, tail);

      for (PTerm arg : args) {
         testIsList(l);
         assertEquals(arg, l.term(0));
         l = l.term(1);
      }

      assertSame(tail, l);
   }

   /** Check {@link ListFactory#createList(Collection)} works the same as {@link ListFactory#createList(PTerm[])} */
   @Test
   public void testCreationWithJavaCollection() {
      final PTerm[] args = createArguments();
      final Collection<PTerm> c = Arrays.asList(args);
      final PTerm listFromArray = ListFactory.createList(args);
      final PTerm listFromCollection = ListFactory.createList(c);
      assertTrue(listFromCollection.strictEquals(listFromArray));
   }

   @Test
   public void testCreateListOfLengthZero() {
      assertSame(EmptyList.EMPTY_LIST, ListFactory.createListOfLength(0));
   }

   @Test
   public void testCreateListOfLengthOne() {
      PTerm t = ListFactory.createListOfLength(1);
      assertSame(PList.class, t.getClass());
      assertTrue(t.term(0).type().isVariable());
      assertSame(EmptyList.EMPTY_LIST, t.term(1));
      assertEquals(".(E0, [])", t.toString());
   }

   @Test
   public void testCreateListOfLengthThree() {
      PTerm t = ListFactory.createListOfLength(3);
      assertSame(PList.class, t.getClass());
      assertSame(PList.class, t.getClass());
      assertTrue(t.term(0).type().isVariable());
      assertSame(PList.class, t.term(1).getClass());
      assertEquals(".(E0, .(E1, .(E2, [])))", t.toString());
   }

   private PTerm[] createArguments() {
      return new PTerm[] {atom(), structure(), integerNumber(), decimalFraction(), variable()};
   }

   private void testIsList(PTerm l) {
      assertEquals(".", l.getName());
      assertEquals(PrologOperator.LIST, l.type());
      assertEquals(2, l.length());
   }
}