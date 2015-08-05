package org.projog.core.term;

import org.junit.Test;
import org.projog.TestUtils;

import static org.junit.Assert.*;
import static org.projog.TestUtils.*;
import static org.projog.core.term.EmptyList.EMPTY_LIST;
import static org.projog.core.term.TermUtils.createAnonymousVariable;

public class ListUtilsTest {
   @Test
   public void testIsMember_True() {
      PList list = TestUtils.list(atom("x"), atom("y"), atom("z"));
      assertTrue(ListUtils.isMember(atom("x"), list));
      assertTrue(ListUtils.isMember(atom("y"), list));
      assertTrue(ListUtils.isMember(atom("z"), list));
   }

   @Test
   public void testIsMember_Failure() {
      PList list = TestUtils.list(atom("x"), atom("y"), atom("z"));
      assertFalse(ListUtils.isMember(atom("w"), list));
   }

   @Test
   public void testIsMember_EmptyList() {
      assertFalse(ListUtils.isMember(atom(), EMPTY_LIST));
   }

   @Test
   public void testIsMember_Variable() {
      PAtom x = atom("x");
      PList list = TestUtils.list(x, atom("y"), atom("z"));
      PVar v = variable();
      assertTrue(ListUtils.isMember(v, list));
      assertSame(x, v.get());
   }

   @Test
   public void testIsMember_VariablesAsArgumentsOfStructures() {
      PTerm list = parseTerm("[p(a, B, 2),p(q, b, C),p(A, b, 5)]");
      PTerm element = parseTerm("p(X,b,5)");
      assertTrue(ListUtils.isMember(element, list));
      assertEquals("[p(a, B, 2),p(q, b, 5),p(A, b, 5)]", write(list));
      assertEquals("p(q, b, 5)", write(element));
   }

   @Test
   public void testIsMember_InvalidArgumentList() {
      try {
         ListUtils.isMember(atom(), atom("a"));
         fail();
      } catch (IllegalArgumentException e) {
         assertEquals("Expected list but got: a", e.getMessage());
      }
   }

   @Test
   public void testToJavaUtilList() {
      final PTerm[] arguments = createArguments();
      final PList projogList = (PList) ListFactory.createList(arguments);
      final java.util.List<PTerm> javaUtilList = ListUtils.toJavaUtilList(projogList);
      assertEquals(arguments.length, javaUtilList.size());
      for (int i = 0; i < arguments.length; i++) {
         assertSame(arguments[i], javaUtilList.get(i));
      }
   }

   @Test
   public void testToJavaUtilList_PartialList() {
      final PList projogList = (PList) ListFactory.createList(createArguments(), atom("tail"));
      assertNull(ListUtils.toJavaUtilList(projogList));
   }

   @Test
   public void testToJavaUtilList_EmptyList() {
      final java.util.List<PTerm> javaUtilList = ListUtils.toJavaUtilList(EMPTY_LIST);
      assertTrue(javaUtilList.isEmpty());
   }

   @Test
   public void testToJavaUtilList_NonListArguments() {
      assertNull(ListUtils.toJavaUtilList(variable()));
      assertNull(ListUtils.toJavaUtilList(atom()));
      assertNull(ListUtils.toJavaUtilList(structure()));
      assertNull(ListUtils.toJavaUtilList(integerNumber()));
      assertNull(ListUtils.toJavaUtilList(decimalFraction()));
      assertNull(ListUtils.toJavaUtilList(createAnonymousVariable()));
   }

   @Test
   public void testToSortedJavaUtilList() {
      PAtom z = atom("z");
      PAtom a = atom("a");
      PAtom h = atom("h");
      PAtom q = atom("q");
      // include multiple 'a's to test duplicates are not removed
      final PList projogList = (PList) ListFactory.createList(new PTerm[] {z, a, a, h, a, q});
      final java.util.List<PTerm> javaUtilList = ListUtils.toSortedJavaUtilList(projogList);
      assertEquals(6, javaUtilList.size());
      assertSame(a, javaUtilList.get(0));
      assertSame(a, javaUtilList.get(1));
      assertSame(a, javaUtilList.get(2));
      assertSame(h, javaUtilList.get(3));
      assertSame(q, javaUtilList.get(4));
      assertSame(z, javaUtilList.get(5));
   }

   @Test
   public void testToSortedJavaUtilList_EmptyList() {
      final java.util.List<PTerm> javaUtilList = ListUtils.toSortedJavaUtilList(EMPTY_LIST);
      assertTrue(javaUtilList.isEmpty());
   }

   @Test
   public void testToSortedJavaUtilList_EmptyList_NonListArguments() {
      assertNull(ListUtils.toSortedJavaUtilList(variable()));
      assertNull(ListUtils.toSortedJavaUtilList(atom()));
      assertNull(ListUtils.toSortedJavaUtilList(structure()));
      assertNull(ListUtils.toSortedJavaUtilList(integerNumber()));
      assertNull(ListUtils.toSortedJavaUtilList(decimalFraction()));
      assertNull(ListUtils.toSortedJavaUtilList(createAnonymousVariable()));
   }

   private PTerm[] createArguments() {
      return new PTerm[] {atom(), structure(), integerNumber(), decimalFraction(), variable()};
   }
}
