package org.projog.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.projog.TestUtils.atom;
import static org.projog.TestUtils.structure;
import static org.projog.TestUtils.variable;

import java.util.List;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.event.ProjogEventsObservable;
import org.projog.core.function.bool.True;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermFormatter;

public class KnowledgeBaseUtilsTest {
   private final KnowledgeBase kb = TestUtils.createKnowledgeBase();

   @Test
   public void testConjunctionPredicateName() {
      assertEquals(",", KnowledgeBaseUtils.CONJUNCTION_PREDICATE_NAME);
   }

   @Test
   public void testImplicationPredicateName() {
      assertEquals(":-", KnowledgeBaseUtils.IMPLICATION_PREDICATE_NAME);
   }

   @Test
   public void testQuestionPredicateName() {
      assertEquals("?-", KnowledgeBaseUtils.QUESTION_PREDICATE_NAME);
   }

   @Test
   public void testGetPredicateKeysByName() {
      String predicateName = "testGetPredicateKeysByName";

      assertTrue(KnowledgeBaseUtils.getPredicateKeysByName(kb, predicateName).isEmpty());

      PredicateKey input[] = {new PredicateKey(predicateName, 0), new PredicateKey(predicateName, 1), new PredicateKey(predicateName, 2), new PredicateKey(predicateName, 3)};

      for (PredicateKey key : input) {
         kb.createOrReturnUserDefinedPredicate(key);
         // add entries with a different name to the name we are calling getPredicateKeysByName with
         // to check that the method isn't just returning ALL keys
         PredicateKey keyWithDifferentName = new PredicateKey(predicateName + "X", key.getNumArgs());
         kb.createOrReturnUserDefinedPredicate(keyWithDifferentName);
      }

      List<PredicateKey> output = KnowledgeBaseUtils.getPredicateKeysByName(kb, predicateName);
      assertEquals(input.length, output.size());
      for (PredicateKey key : input) {
         assertTrue(output.contains(key));
      }
   }

   @Test
   public void testGetPredicate() {
      assertGetPredicate(atom("true"), True.class);
      assertGetPredicate(atom("does_not_exist"), UnknownPredicate.class);
   }

   private void assertGetPredicate(PTerm input, Class<?> expected) {
      Predicate e = KnowledgeBaseUtils.getPredicate(kb, input);
      assertSame(expected, e.getClass());
   }

   @Test
   public void testIsQuestionOrDirectiveFunctionCall() {
      assertTrue(KnowledgeBaseUtils.isQuestionOrDirectiveFunctionCall(structure("?-", atom())));
      assertTrue(KnowledgeBaseUtils.isQuestionOrDirectiveFunctionCall(structure("?-", structure("=", atom(), atom()))));
      assertTrue(KnowledgeBaseUtils.isQuestionOrDirectiveFunctionCall(structure(":-", atom())));
      assertTrue(KnowledgeBaseUtils.isQuestionOrDirectiveFunctionCall(structure(":-", structure("=", atom(), atom()))));

      assertFalse(KnowledgeBaseUtils.isQuestionOrDirectiveFunctionCall(atom("?-")));
      assertFalse(KnowledgeBaseUtils.isQuestionOrDirectiveFunctionCall(structure("?-", atom(), atom())));
      assertFalse(KnowledgeBaseUtils.isQuestionOrDirectiveFunctionCall(atom(":-")));
      assertFalse(KnowledgeBaseUtils.isQuestionOrDirectiveFunctionCall(structure(":-", atom(), atom())));
      assertFalse(KnowledgeBaseUtils.isQuestionOrDirectiveFunctionCall(structure(">=", atom())));
   }

   @Test
   public void testIsDynamicFunctionCall() {
      assertTrue(KnowledgeBaseUtils.isDynamicFunctionCall(structure("dynamic", atom())));
      assertTrue(KnowledgeBaseUtils.isDynamicFunctionCall(structure("dynamic", structure("=", atom(), atom()))));

      assertFalse(KnowledgeBaseUtils.isDynamicFunctionCall(atom("dynamic")));
      assertFalse(KnowledgeBaseUtils.isDynamicFunctionCall(structure("dynamic", atom(), atom())));
      assertFalse(KnowledgeBaseUtils.isDynamicFunctionCall(structure(":-", atom())));
   }

   @Test
   public void testIsConjuction() {
      assertFalse(KnowledgeBaseUtils.isConjunction(TestUtils.parseSentence("true.")));
      assertTrue(KnowledgeBaseUtils.isConjunction(TestUtils.parseSentence("true, true.")));
      assertTrue(KnowledgeBaseUtils.isConjunction(TestUtils.parseSentence("true, true, true.")));
      assertTrue(KnowledgeBaseUtils.isConjunction(TestUtils.parseSentence("repeat(3), X<1, write(V), nl, true, !, fail.")));
   }

   @Test
   public void testIsSingleAnswer_NonRetryablePredicate() {
      // test single term representing a predicate that is not repeatable
      assertTrue(KnowledgeBaseUtils.isSingleAnswer(kb, atom("true")));
   }

   @Test
   public void testIsSingleAnswer_RetryablePredicate() {
      // test single term representing a predicate that *is* repeatable
      assertFalse(KnowledgeBaseUtils.isSingleAnswer(kb, atom("repeat")));
   }

   @Test
   public void testIsSingleAnswer_NonRetryableConjuction() {
      // test conjunction of terms that are all not repeatable
      PTerm conjuctionOfNonRepeatableTerms = TestUtils.parseSentence("write(X), nl, true, X<1.");
      assertTrue(KnowledgeBaseUtils.isSingleAnswer(kb, conjuctionOfNonRepeatableTerms));
   }

   @Test
   public void testIsSingleAnswer_RetryableConjuction() {
      // test conjunction of terms where one is repeatable
      PTerm conjuctionIncludingRepeatableTerm = TestUtils.parseSentence("write(X), nl, repeat, true, X<1.");
      assertFalse(KnowledgeBaseUtils.isSingleAnswer(kb, conjuctionIncludingRepeatableTerm));
   }

   @Test
   public void testIsSingleAnswer_Disjunction() {
      // test disjunction
      // (Note that the disjunction used in the test *would* only give a single answer 
      // but KnowledgeBaseUtils.isSingleAnswer is not currently smart enough to spot this)
      PTerm disjunctionOfTerms = TestUtils.parseSentence("true ; fail.");
      assertFalse(KnowledgeBaseUtils.isSingleAnswer(kb, disjunctionOfTerms));
   }

   @Test
   public void testIsSingleAnswer_Variable() {
      assertFalse(KnowledgeBaseUtils.isSingleAnswer(kb, variable()));
   }

   @Test
   public void testToArrayOfConjunctions() {
      PTerm t = TestUtils.parseSentence("a, b(1,2,3), c.");
      PTerm[] conjunctions = KnowledgeBaseUtils.toArrayOfConjunctions(t);
      assertEquals(3, conjunctions.length);
      assertSame(t.arg(0).arg(0), conjunctions[0]);
      assertSame(t.arg(0).arg(1), conjunctions[1]);
      assertSame(t.arg(1), conjunctions[2]);
   }

   @Test
   public void testToArrayOfConjunctionsNoneConjunctionArgument() {
      PTerm t = TestUtils.parseSentence("a(b(1,2,3), c).");
      PTerm[] conjunctions = KnowledgeBaseUtils.toArrayOfConjunctions(t);
      assertEquals(1, conjunctions.length);
      assertSame(t, conjunctions[0]);
   }

   @Test
   public void testGetProjogEventsObservable() {
      KnowledgeBase kb1 = TestUtils.createKnowledgeBase();
      KnowledgeBase kb2 = TestUtils.createKnowledgeBase();
      ProjogEventsObservable o1 = KnowledgeBaseUtils.getProjogEventsObservable(kb1);
      ProjogEventsObservable o2 = KnowledgeBaseUtils.getProjogEventsObservable(kb2);
      assertNotNull(o1);
      assertNotNull(o2);
      assertNotSame(o1, o2);
      assertSame(o1, KnowledgeBaseUtils.getProjogEventsObservable(kb1));
      assertSame(o2, KnowledgeBaseUtils.getProjogEventsObservable(kb2));
   }

   @Test
   public void testGetProjogProperties() {
      KnowledgeBase kb1 = TestUtils.createKnowledgeBase();
      KnowledgeBase kb2 = TestUtils.createKnowledgeBase();
      ProjogProperties o1 = KnowledgeBaseUtils.getProjogProperties(kb1);
      ProjogProperties o2 = KnowledgeBaseUtils.getProjogProperties(kb2);
      assertNotNull(o1);
      assertNotNull(o2);
      assertNotSame(o1, o2);
      assertSame(o1, KnowledgeBaseUtils.getProjogProperties(kb1));
      assertSame(o2, KnowledgeBaseUtils.getProjogProperties(kb2));
   }

   @Test
   public void testGetOperands() {
      KnowledgeBase kb1 = TestUtils.createKnowledgeBase();
      KnowledgeBase kb2 = TestUtils.createKnowledgeBase();
      Operands o1 = KnowledgeBaseUtils.getOperands(kb1);
      Operands o2 = KnowledgeBaseUtils.getOperands(kb2);
      assertNotNull(o1);
      assertNotNull(o2);
      assertNotSame(o1, o2);
      assertSame(o1, KnowledgeBaseUtils.getOperands(kb1));
      assertSame(o2, KnowledgeBaseUtils.getOperands(kb2));
   }

   @Test
   public void testGetTermFormatter() {
      KnowledgeBase kb1 = TestUtils.createKnowledgeBase();
      KnowledgeBase kb2 = TestUtils.createKnowledgeBase();
      TermFormatter o1 = KnowledgeBaseUtils.getTermFormatter(kb1);
      TermFormatter o2 = KnowledgeBaseUtils.getTermFormatter(kb2);
      assertNotNull(o1);
      assertNotNull(o2);
      assertNotSame(o1, o2);
      assertSame(o1, KnowledgeBaseUtils.getTermFormatter(kb1));
      assertSame(o2, KnowledgeBaseUtils.getTermFormatter(kb2));
   }

   @Test
   public void testGetSpyPoints() {
      KnowledgeBase kb1 = TestUtils.createKnowledgeBase();
      KnowledgeBase kb2 = TestUtils.createKnowledgeBase();
      SpyPoints o1 = KnowledgeBaseUtils.getSpyPoints(kb1);
      SpyPoints o2 = KnowledgeBaseUtils.getSpyPoints(kb2);
      assertNotNull(o1);
      assertNotNull(o2);
      assertNotSame(o1, o2);
      assertSame(o1, KnowledgeBaseUtils.getSpyPoints(kb1));
      assertSame(o2, KnowledgeBaseUtils.getSpyPoints(kb2));
   }

   @Test
   public void testGetFileHandles() {
      KnowledgeBase kb1 = TestUtils.createKnowledgeBase();
      KnowledgeBase kb2 = TestUtils.createKnowledgeBase();
      FileHandles o1 = KnowledgeBaseUtils.getFileHandles(kb1);
      FileHandles o2 = KnowledgeBaseUtils.getFileHandles(kb2);
      assertNotNull(o1);
      assertNotNull(o2);
      assertNotSame(o1, o2);
      assertSame(o1, KnowledgeBaseUtils.getFileHandles(kb1));
      assertSame(o2, KnowledgeBaseUtils.getFileHandles(kb2));
   }

   @Test
   public void testCalculatables() {
      KnowledgeBase kb1 = TestUtils.createKnowledgeBase();
      KnowledgeBase kb2 = TestUtils.createKnowledgeBase();
      Calculatables o1 = KnowledgeBaseUtils.getCalculatables(kb1);
      Calculatables o2 = KnowledgeBaseUtils.getCalculatables(kb2);
      assertNotNull(o1);
      assertNotNull(o2);
      assertNotSame(o1, o2);
      assertSame(o1, KnowledgeBaseUtils.getCalculatables(kb1));
      assertSame(o2, KnowledgeBaseUtils.getCalculatables(kb2));
   }
}