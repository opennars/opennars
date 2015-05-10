package org.projog.core.udp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.projog.TestUtils.atom;
import static org.projog.TestUtils.variable;

import java.util.Iterator;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.KnowledgeBase;
import org.projog.core.Predicate;
import org.projog.core.PredicateKey;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermType;

public class DynamicUserDefinedPredicateFactoryTest {
   private static final String TEST_PREDICATE_NAME = "test";

   @Test
   public void testSimpleAdditionAndIteration_1() {
      DynamicUserDefinedPredicateFactory dp = createDynamicPredicate();
      assertIterator(dp);

      addLast(dp, "a");
      assertIterator(dp, "a");

      addLast(dp, "b");
      assertIterator(dp, "a", "b");

      addFirst(dp, "c");
      assertIterator(dp, "c", "a", "b");
   }

   @Test
   public void testSimpleAdditionAndIteration_2() {
      DynamicUserDefinedPredicateFactory dp = createDynamicPredicate();
      assertIterator(dp);

      addFirst(dp, "a");
      assertIterator(dp, "a");

      addFirst(dp, "b");
      assertIterator(dp, "b", "a");

      addLast(dp, "c");
      assertIterator(dp, "b", "a", "c");
   }

   @Test
   public void testRemoval() {
      DynamicUserDefinedPredicateFactory dp = createDynamicPredicate();
      addLast(dp, "a");
      addLast(dp, "b");
      addLast(dp, "c");
      assertIterator(dp, "a", "b", "c");

      Iterator<ClauseModel> itr1 = dp.getImplications();
      Iterator<ClauseModel> itr2 = dp.getImplications();
      Iterator<ClauseModel> itr3 = dp.getImplications();
      Iterator<ClauseModel> itr4 = dp.getImplications();
      Iterator<ClauseModel> itr5 = dp.getImplications();

      // iterate to a
      itr2.next();

      // iterate to b
      itr3.next();
      itr3.next();

      // iterate to c
      itr4.next();
      itr4.next();
      itr4.next();

      // add d
      addLast(dp, "d");

      // delete b and c
      itr5.next();
      itr5.next();
      itr5.remove();
      itr5.next();
      itr5.remove();

      assertIterator(dp, "a", "d");
      assertIterator(itr1, "a", "d");
      assertIterator(itr2, "d");
      assertIterator(itr3, "c", "d");
      assertIterator(itr4, "d");
   }

   @Test
   public void testRemoveFirstAndLast() {
      DynamicUserDefinedPredicateFactory dp = createDynamicPredicate();
      addLast(dp, "a");
      addLast(dp, "b");
      addLast(dp, "c");
      addLast(dp, "d");
      addLast(dp, "e");
      assertIterator(dp, "a", "b", "c", "d", "e");

      Iterator<ClauseModel> itr = dp.getImplications();
      itr.next();
      itr.remove(); // remove a
      itr.next();
      itr.next();
      itr.next();
      itr.next();
      itr.remove(); // remove e

      assertIterator(dp, "b", "c", "d");
   }

   @Test
   public void testRemoveAll() {
      DynamicUserDefinedPredicateFactory dp = createDynamicPredicate();
      addLast(dp, "a");
      addLast(dp, "b");
      addLast(dp, "c");

      Iterator<ClauseModel> itr = dp.getImplications();
      itr.next();
      itr.remove();
      itr.next();
      itr.remove();
      itr.next();
      itr.remove();

      assertFalse(dp.getImplications().hasNext());
   }

   @Test
   public void testGetClauseModel() {
      DynamicUserDefinedPredicateFactory dp = createDynamicPredicate();
      ClauseModel ci1 = createClauseModel("a");
      dp.addLast(ci1);
      ClauseModel ci2 = createClauseModel("b");
      dp.addLast(ci2);
      ClauseModel ci3 = createClauseModel("c");
      dp.addLast(ci3);

      assertClauseModel(dp, 0, ci1);
      assertClauseModel(dp, 1, ci2);
      assertClauseModel(dp, 2, ci3);
      assertNull(dp.getClauseModel(3));
      assertNull(dp.getClauseModel(7));
   }

   private void assertClauseModel(DynamicUserDefinedPredicateFactory dp, int index, ClauseModel original) {
      ClauseModel actual = dp.getClauseModel(index);
      assertNotSame(original, actual);
      assertSame(original.getOriginal(), actual.getOriginal());
   }

   @Test
   public void testGetPredicate() {
      String[] data = {"a", "b", "c"};
      DynamicUserDefinedPredicateFactory dp = createDynamicPredicate();
      for (String d : data) {
         addLast(dp, d);
      }

      // test evaluate with atom as argument
      for (String d : data) {
         PTerm inputArg = atom(d);
         PTerm[] args = new PTerm[] {inputArg};
         Predicate e = dp.getPredicate(args);
         assertTrue(e.isRetryable());
         assertTrue(e.evaluate(args));
         assertSame(inputArg, args[0]);
         assertFalse(e.evaluate(args));
         assertSame(inputArg, args[0]);
      }

      // test evaluate with variable as argument
      PTerm inputArg = variable();
      PTerm[] args = new PTerm[] {inputArg};
      Predicate e = dp.getPredicate(args);
      assertTrue(e.isRetryable());
      for (String d : data) {
         assertTrue(e.evaluate(args));
         assertSame(TermType.ATOM, args[0].type());
         assertEquals(d, args[0].getName());
      }
      assertFalse(e.evaluate(args));
      assertSame(inputArg, args[0]);
      assertSame(inputArg, args[0].get());
   }

   private DynamicUserDefinedPredicateFactory createDynamicPredicate() {
      KnowledgeBase kb = TestUtils.createKnowledgeBase();
      PredicateKey key = new PredicateKey(TEST_PREDICATE_NAME, 1);
      DynamicUserDefinedPredicateFactory dp = new DynamicUserDefinedPredicateFactory(kb, key);
      assertEquals(key, dp.getPredicateKey());
      assertTrue(dp.isDynamic());
      return dp;
   }

   private void addFirst(DynamicUserDefinedPredicateFactory dp, String argumentSyntax) {
      ClauseModel ci = createClauseModel(argumentSyntax);
      dp.addFirst(ci);
   }

   private void addLast(DynamicUserDefinedPredicateFactory dp, String argumentSyntax) {
      ClauseModel ci = createClauseModel(argumentSyntax);
      dp.addLast(ci);
   }

   private ClauseModel createClauseModel(String argumentSyntax) {
      String inputSyntax = createStructureSyntax(argumentSyntax);
      return TestUtils.createClauseModel(inputSyntax + ".");
   }

   private void assertIterator(DynamicUserDefinedPredicateFactory dp, String... expectedOrder) {
      Iterator<ClauseModel> itr = dp.getImplications();
      assertIterator(itr, expectedOrder);
   }

   private void assertIterator(Iterator<ClauseModel> itr, String... expectedOrder) {
      for (String expected : expectedOrder) {
         assertTrue(itr.hasNext());
         ClauseModel ci = itr.next();
         String predicateSyntax = createStructureSyntax(expected);
         assertEquals(predicateSyntax, ci.getOriginal().toString());
      }
      assertFalse(itr.hasNext());
   }

   private String createStructureSyntax(String argumentSyntax) {
      return TEST_PREDICATE_NAME + "(" + argumentSyntax + ")";
   }
}