package org.projog.core.udp.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.projog.TestUtils.createClauseModel;
import static org.projog.TestUtils.createKnowledgeBase;
import static org.projog.TestUtils.parseTerm;
import static org.projog.TestUtils.write;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.projog.core.KnowledgeBase;
import org.projog.core.term.PTerm;
import org.projog.core.udp.ClauseModel;
import org.projog.core.udp.TailRecursivePredicateMetaData;

public class InterpretedTailRecursivePredicateFactoryTest {
   private final InterpretedTailRecursivePredicateFactory FACTORY = createFactory("prefix([],Ys).", "prefix([X|Xs],[X|Ys]) :- prefix(Xs,Ys).");

   @Test
   public void testSingleResultQuery() {
      PTerm arg1 = parseTerm("[a]");
      PTerm arg2 = parseTerm("[a,b,c]");
      InterpretedTailRecursivePredicate singleResultPredicate = FACTORY.getPredicate(arg1, arg2);

      assertFalse(singleResultPredicate.isRetryable());
      assertFalse(singleResultPredicate.couldReEvaluationSucceed());
      assertTrue(singleResultPredicate.evaluate(arg1, arg2));
   }

   @Test
   public void testMultiResultQuery() {
      PTerm arg1 = parseTerm("X");
      PTerm arg2 = parseTerm("[a,b,c]");
      InterpretedTailRecursivePredicate multiResultPredicate = FACTORY.getPredicate(arg1, arg2);

      assertTrue(multiResultPredicate.isRetryable());
      assertTrue(multiResultPredicate.couldReEvaluationSucceed());
      assertTrue(multiResultPredicate.evaluate(arg1, arg2));
      assertEquals("[]", write(arg1));
      assertTrue(multiResultPredicate.evaluate(arg1, arg2));
      assertEquals("[a]", write(arg1));
      assertTrue(multiResultPredicate.evaluate(arg1, arg2));
      assertEquals("[a,b]", write(arg1));
      assertTrue(multiResultPredicate.evaluate(arg1, arg2));
      assertEquals("[a,b,c]", write(arg1));
      assertFalse(multiResultPredicate.evaluate(arg1, arg2));
   }

   private InterpretedTailRecursivePredicateFactory createFactory(String firstClauseSyntax, String secondClauseSyntax) {
      KnowledgeBase kb = createKnowledgeBase();
      List<ClauseModel> clauses = createClauseModels(firstClauseSyntax, secondClauseSyntax);
      TailRecursivePredicateMetaData metaData = TailRecursivePredicateMetaData.create(kb, clauses);
      return new InterpretedTailRecursivePredicateFactory(kb, metaData);
   }

   private List<ClauseModel> createClauseModels(String firstClauseSyntax, String secondClauseSyntax) {
      List<ClauseModel> clauses = new ArrayList<>();
      clauses.add(createClauseModel(firstClauseSyntax));
      clauses.add(createClauseModel(secondClauseSyntax));
      return clauses;
   }
}