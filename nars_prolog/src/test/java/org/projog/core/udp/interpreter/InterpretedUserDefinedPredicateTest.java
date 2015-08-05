package org.projog.core.udp.interpreter;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.KB;
import org.projog.core.PredicateKey;
import org.projog.core.SpyPoints;
import org.projog.core.term.PTerm;
import org.projog.core.term.PVar;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.projog.TestUtils.atom;
import static org.projog.TestUtils.variable;
import static org.projog.core.KnowledgeBaseUtils.getSpyPoints;

public class InterpretedUserDefinedPredicateTest {
   private final KB kb = TestUtils.createKnowledgeBase();
   private final PredicateKey key = PredicateKey.createForTerm(atom("test"));
   private final SpyPoints.SpyPoint spyPoint = getSpyPoints(kb).getSpyPoint(key);
   private final DummyClauseAction singleResultA = new DummyClauseAction(atom("a"));
   private final DummyClauseAction singleResultB = new DummyClauseAction(atom("b"));
   private final DummyClauseAction singleResultC = new DummyClauseAction(atom("c"));
   private final DummyClauseAction multiResultXYZ = new DummyClauseAction(atom("x"), atom("y"), atom("z"));

   @Test
   public void testEmpty() {
      InterpretedUserDefinedPredicate p = getInterpretedUserDefinedPredicate();
      assertTrue(p.isRetryable());
      assertFalse(p.evaluate());
   }

   @Test
   public void testSingleClauseActionMatchingImmutableInputArg() {
      assertEvaluatesOnce(atom("a"), singleResultA);
      assertDoesNotEvaluate(atom("b"), singleResultA);
   }

   @Test
   public void testImmutableInputArgMatchingMoreThanOnce() {
      ClauseAction[] rows = {singleResultA, singleResultA, singleResultA};
      assertEvaluates(atom("a"), rows.length, rows);
   }

   @Test
   public void testManyClauseActionsMatchingImmutableInputArg() {
      ClauseAction[] rows = {singleResultA, singleResultB, singleResultC};
      assertEvaluatesOnce(atom("a"), rows);
      assertEvaluatesOnce(atom("b"), rows);
      assertEvaluatesOnce(atom("c"), rows);
      assertDoesNotEvaluate(atom("d"), rows);
   }

   @Test
   public void testSingleRetryableClauseActionsMatchingImmutableInputArg() {
      assertEvaluatesOnce(atom("x"), multiResultXYZ);
      assertEvaluatesOnce(atom("y"), multiResultXYZ);
      assertEvaluatesOnce(atom("z"), multiResultXYZ);
      assertDoesNotEvaluate(atom("d"), multiResultXYZ);
   }

   @Test
   public void testMixtureOfClauseActionsMatchingImmutableInputArg() {
      ClauseAction[] rows = {singleResultA, singleResultB, multiResultXYZ, singleResultC};
      assertEvaluatesOnce(atom("a"), rows);
      assertEvaluatesOnce(atom("b"), rows);
      assertEvaluatesOnce(atom("c"), rows);
      assertEvaluatesOnce(atom("x"), multiResultXYZ);
      assertEvaluatesOnce(atom("y"), multiResultXYZ);
      assertEvaluatesOnce(atom("z"), multiResultXYZ);
      assertDoesNotEvaluate(atom("d"), rows);
   }

   private void assertEvaluatesOnce(PTerm inputArg, ClauseAction... rows) {
      assertEvaluates(inputArg, 1, rows);
   }

   private void assertEvaluates(PTerm inputArg, int timesMatched, ClauseAction... rows) {
      InterpretedUserDefinedPredicate p = getInterpretedUserDefinedPredicate(rows);
      PTerm[] queryArgs = {inputArg};
      for (int i = 0; i < timesMatched; i++) {
         assertTrue(p.evaluate(queryArgs));
      }
      assertFalse(p.evaluate(queryArgs));
   }

   private void assertDoesNotEvaluate(PTerm inputArg, ClauseAction... rows) {
      InterpretedUserDefinedPredicate p = getInterpretedUserDefinedPredicate(rows);
      PTerm[] queryArgs = {inputArg};
      assertFalse(p.evaluate(queryArgs));
   }

   @Test
   public void testSingleClauseActionMatchingVariable() {
      assertEvaluateWithVariableInputArgument(singleResultA);
   }

   @Test
   public void testManyClauseActionsMatchingVariable() {
      assertEvaluateWithVariableInputArgument(singleResultA, singleResultB, singleResultC);
   }

   @Test
   public void testSingleRetryableClauseActionsMatchingVariable() {
      assertEvaluateWithVariableInputArgument(multiResultXYZ);
   }

   @Test
   public void testMixtureOfClauseActionsMatchingVariable() {
      assertEvaluateWithVariableInputArgument(singleResultA, singleResultB, multiResultXYZ, singleResultC);
   }

   private void assertEvaluateWithVariableInputArgument(DummyClauseAction... rows) {
      InterpretedUserDefinedPredicate p = getInterpretedUserDefinedPredicate(rows);
      PVar v = variable("X");
      PTerm[] queryArgs = {v};
      for (DummyClauseAction row : rows) {
         for (PTerm t : row.terms) {
            assertTrue(p.evaluate(queryArgs));
            assertSame(t, v.get());
         }
      }
      assertFalse(p.evaluate(queryArgs));
      assertSame(v, v.get());
   }

   private InterpretedUserDefinedPredicate getInterpretedUserDefinedPredicate(ClauseAction... rows) {
      List<ClauseAction> list = new ArrayList<>();
      for (ClauseAction row : rows) {
         list.add(row);
      }
      return new InterpretedUserDefinedPredicate(key, spyPoint, list.iterator());
   }

   private static class DummyClauseAction implements ClauseAction {
      final PTerm[] terms;
      int ctr;

      DummyClauseAction(PTerm... terms) {
         this.terms = terms;
      }

      @Override
      public ClauseAction getFree() {
         return new DummyClauseAction(terms);
      }

      @Override
      public boolean evaluate(PTerm[] queryArgs) {
         while (true) {
            if (ctr == terms.length) {
               return false;
            }
            if (ctr > 0) {
               queryArgs[0].backtrack();
            }
            PTerm t = terms[ctr++];
            if (t.unify(queryArgs[0])) {
               return true;
            }
         }
      }

      @Override
      public boolean couldReEvaluationSucceed() {
         return terms.length > 0;
      }
   };
}