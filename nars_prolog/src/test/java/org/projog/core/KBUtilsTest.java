package org.projog.core;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.event.ProjogEventsObservable;
import org.projog.core.function.bool.True;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermFormatter;

import java.util.List;

import static org.junit.Assert.*;
import static org.projog.TestUtils.*;

public class KBUtilsTest {
   private final KB kb = TestUtils.createKnowledgeBase();

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
         kb.getDefined(key);
         // add entries with a different name to the name we are calling getPredicateKeysByName with
         // to check that the method isn't just returning ALL keys
         PredicateKey keyWithDifferentName = new PredicateKey(predicateName + "X", key.getNumArgs());
         kb.getDefined(keyWithDifferentName);
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
      assertSame(t.term(0).term(0), conjunctions[0]);
      assertSame(t.term(0).term(1), conjunctions[1]);
      assertSame(t.term(1), conjunctions[2]);
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
      KB kb1 = TestUtils.createKnowledgeBase();
      KB kb2 = TestUtils.createKnowledgeBase();
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
      KB kb1 = TestUtils.createKnowledgeBase();
      KB kb2 = TestUtils.createKnowledgeBase();
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
      KB kb1 = TestUtils.createKnowledgeBase();
      KB kb2 = TestUtils.createKnowledgeBase();
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
      KB kb1 = TestUtils.createKnowledgeBase();
      KB kb2 = TestUtils.createKnowledgeBase();
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
      KB kb1 = TestUtils.createKnowledgeBase();
      KB kb2 = TestUtils.createKnowledgeBase();
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
      KB kb1 = TestUtils.createKnowledgeBase();
      KB kb2 = TestUtils.createKnowledgeBase();
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
      KB kb1 = TestUtils.createKnowledgeBase();
      KB kb2 = TestUtils.createKnowledgeBase();
      Calculatables o1 = KnowledgeBaseUtils.getCalculatables(kb1);
      Calculatables o2 = KnowledgeBaseUtils.getCalculatables(kb2);
      assertNotNull(o1);
      assertNotNull(o2);
      assertNotSame(o1, o2);
      assertSame(o1, KnowledgeBaseUtils.getCalculatables(kb1));
      assertSame(o2, KnowledgeBaseUtils.getCalculatables(kb2));
   }
}