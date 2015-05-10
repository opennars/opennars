package org.projog.core.udp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.KnowledgeBase;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;
import org.projog.core.term.PTerm;
import org.projog.core.udp.compiler.CompiledPredicate;
import org.projog.core.udp.compiler.CompiledTailRecursivePredicate;
import org.projog.core.udp.interpreter.InterpretedTailRecursivePredicateFactory;
import org.projog.core.udp.interpreter.InterpretedUserDefinedPredicate;

/**
 * Tests {@link StaticUserDefinedPredicateFactory}.
 * <p>
 * NOTE: For these tests to work you need to have "projogGeneratedClasses" in the classpath as that will be the output
 * directory for bytecode generated at runtime.
 * 
 * @see org.projog.TestUtils#COMPILATION_ENABLED_PROPERTIES
 */
public class StaticUserDefinedPredicateFactoryTest {
   private static final KnowledgeBase COMPILATION_ENABLED_KB = TestUtils.createKnowledgeBase(TestUtils.COMPILATION_ENABLED_PROPERTIES);
   private static final KnowledgeBase COMPILATION_DISABLED_KB = TestUtils.createKnowledgeBase(TestUtils.COMPILATION_DISABLED_PROPERTIES);
   private static final String[] RECURSIVE_PREDICATE_SYNTAX = {"concatenate([],L,L).", "concatenate([X|L1],L2,[X|L3]) :- concatenate(L1,L2,L3)."};
   private static final String[] NON_RECURSIVE_PREDICATE_SYNTAX = {"p(X,Y,Z) :- repeat(3), X<Y.", "p(X,Y,Z) :- X is Y+Z.", "p(X,Y,Z) :- X=a."};

   @Test
   public void testTrue() {
      PredicateFactory pf = getActualPredicateFactory(toTerms("p."));
      assertSame(SingleRuleAlwaysTruePredicate.class, pf.getClass());
      Predicate p = pf.getPredicate();
      assertTrue(p.evaluate());
      assertFalse(p.isRetryable());
   }

   @Test
   public void testRepeatSetAmount() {
      PTerm[] clauses = toTerms("p.", "p.", "p.");
      int expectedSuccessfulEvaluations = clauses.length;
      PredicateFactory pf = getActualPredicateFactory(clauses);
      assertSame(MultipleRulesAlwaysTruePredicate.class, pf.getClass());
      Predicate p = pf.getPredicate();
      assertTrue(p.isRetryable());
      for (int i = 0; i < expectedSuccessfulEvaluations; i++) {
         assertTrue(p.couldReEvaluationSucceed());
         assertTrue(p.evaluate());
      }
      assertFalse(p.couldReEvaluationSucceed());
      assertFalse(p.evaluate());
   }

   @Test
   public void testSingleRuleWithSingleImmutableArgumentPredicate() {
      PTerm clause = TestUtils.parseTerm("p(a)");
      PredicateFactory pf = getActualPredicateFactory(clause);
      assertSame(SingleRuleWithSingleImmutableArgumentPredicate.class, pf.getClass());
      SingleRuleWithSingleImmutableArgumentPredicate sr = (SingleRuleWithSingleImmutableArgumentPredicate) pf;
      assertStrictEquality(clause.arg(0), sr.data);
   }

   @Test
   public void testMultipleRulesWithSingleImmutableArgumentPredicate() {
      PTerm[] clauses = toTerms("p(a).", "p(b).", "p(c).");
      PredicateFactory pf = getActualPredicateFactory(clauses);
      assertSame(MultipleRulesWithSingleImmutableArgumentPredicate.class, pf.getClass());
      MultipleRulesWithSingleImmutableArgumentPredicate mr = (MultipleRulesWithSingleImmutableArgumentPredicate) pf;
      assertEquals(clauses.length, mr.data.length);
      for (int i = 0; i < clauses.length; i++) {
         assertStrictEquality(clauses[i].arg(0), mr.data[i]);
      }
   }

   @Test
   public void testSingleRuleWithMultipleImmutableArgumentsPredicate() {
      PTerm clause = TestUtils.parseTerm("p(a,b,c).");
      PredicateFactory pf = getActualPredicateFactory(clause);
      assertSame(SingleRuleWithMultipleImmutableArgumentsPredicate.class, pf.getClass());
      SingleRuleWithMultipleImmutableArgumentsPredicate sr = (SingleRuleWithMultipleImmutableArgumentsPredicate) pf;
      assertEquals(clause.args(), sr.data.length);
      for (int i = 0; i < clause.args(); i++) {
         assertStrictEquality(clause.arg(i), sr.data[i]);
      }
   }

   @Test
   public void testMultipleRulesWithMultipleImmutableArgumentsPredicate() {
      PTerm[] clauses = toTerms("p(a,b,c).", "p(1,2,3).", "p(x,y,z).");
      PredicateFactory pf = getActualPredicateFactory(clauses);
      assertSame(MultipleRulesWithMultipleImmutableArgumentsPredicate.class, pf.getClass());
      MultipleRulesWithMultipleImmutableArgumentsPredicate mr = (MultipleRulesWithMultipleImmutableArgumentsPredicate) pf;
      assertEquals(clauses.length, mr.data.length);
      for (int c = 0; c < clauses.length; c++) {
         assertEquals(clauses[c].args(), mr.data[c].length);
         for (int a = 0; a < clauses[c].args(); a++) {
            assertStrictEquality(clauses[c].arg(a), mr.data[c][a]);
         }
      }
   }

   @Test
   public void testInterpretedTailRecursivePredicateFactory() {
      PredicateFactory pf = getActualPredicateFactory(toTerms(RECURSIVE_PREDICATE_SYNTAX));
      assertSame(InterpretedTailRecursivePredicateFactory.class, pf.getClass());
   }

   @Test
   public void testCompiledTailRecursivePredicate() {
      PredicateFactory pf = getActualPredicateFactory(COMPILATION_ENABLED_KB, toTerms(RECURSIVE_PREDICATE_SYNTAX));
      assertTrue(pf instanceof CompiledPredicate);
      assertTrue(pf instanceof CompiledTailRecursivePredicate);
   }

   @Test
   public void testInterpretedUserDefinedPredicate() {
      PredicateFactory pf = getActualPredicateFactory(toTerms(NON_RECURSIVE_PREDICATE_SYNTAX));
      assertSame(InterpretedUserDefinedPredicate.class, pf.getPredicate().getClass());
   }

   @Test
   public void testCompiledPredicate() {
      PredicateFactory pf = getActualPredicateFactory(COMPILATION_ENABLED_KB, toTerms(NON_RECURSIVE_PREDICATE_SYNTAX));
      assertTrue(pf instanceof CompiledPredicate);
      assertFalse(pf instanceof CompiledTailRecursivePredicate);
   }

   @Test
   public void testConjunctionContainingVariables() {
      PTerm[] clauses = toTerms("and(X,Y) :- X, Y.");
      PredicateFactory pf = getActualPredicateFactory(clauses);
      assertSame(InterpretedUserDefinedPredicate.class, pf.getPredicate().getClass());
   }

   @Test
   public void testVariableAntecedant() {
      PTerm[] clauses = toTerms("true(X) :- X.");
      PredicateFactory pf = getActualPredicateFactory(clauses);
      assertSame(InterpretedUserDefinedPredicate.class, pf.getPredicate().getClass());
   }

   private PredicateFactory getActualPredicateFactory(PTerm... clauses) {
      return getActualPredicateFactory(COMPILATION_DISABLED_KB, clauses);
   }

   private PredicateFactory getActualPredicateFactory(KnowledgeBase kb, PTerm... clauses) {
      StaticUserDefinedPredicateFactory f = null;
      for (PTerm clause : clauses) {
         if (f == null) {
            PredicateKey key = PredicateKey.createForTerm(clause);
            f = new StaticUserDefinedPredicateFactory(key);
            f.setKnowledgeBase(kb);
         }
         ClauseModel clauseModel = ClauseModel.createClauseModel(clause);
         f.addLast(clauseModel);
      }
      return f.getActualPredicateFactory();
   }

   private PTerm[] toTerms(String... clausesSyntax) {
      PTerm[] clauses = new PTerm[clausesSyntax.length];
      for (int i = 0; i < clauses.length; i++) {
         clauses[i] = TestUtils.parseSentence(clausesSyntax[i]);
      }
      return clauses;
   }

   private void assertStrictEquality(PTerm t1, PTerm t2) {
      assertTrue("Term: " + t1 + " is not strictly equal to term: " + t2, t1.strictEquals(t2));
   }
}