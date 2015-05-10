package org.projog.core.udp.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.integerNumber;
import static org.projog.TestUtils.parseSentence;
import static org.projog.TestUtils.variable;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.CutException;
import org.projog.core.KnowledgeBase;
import org.projog.core.term.Atom;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermType;
import org.projog.core.term.TermUtils;
import org.projog.core.term.Variable;
import org.projog.core.udp.ClauseModel;

public class ClauseActionFactoryTest {
   private final KnowledgeBase kb = TestUtils.createKnowledgeBase();

   /** @see AlwaysMatchedClauseAction */
   @Test
   public void testAlwaysMatchedClauseAction() {
      testAlwaysMatchedClauseAction("test.");
      testAlwaysMatchedClauseAction("test(_).");
      testAlwaysMatchedClauseAction("test(X).");
      testAlwaysMatchedClauseAction("test(X, Y, _).");
   }

   private void testAlwaysMatchedClauseAction(String sentence) {
      ClauseAction ca = getClauseAction(sentence);
      assertEquals(AlwaysMatchedClauseAction.class, ca.getClass());
      assertFalse(ca.couldReEvaluationSucceed());
      assertTrue(ca.getFree().evaluate((PTerm[]) null));
   }

   /** @see ImmutableArgumentsClauseAction */
   @Test
   public void testImmutableArgumentsClauseAction() {
      ClauseAction ca = getClauseAction("test(a, 1, 1.2, p(a, b, c), []).");
      assertEquals(ImmutableArgumentsClauseAction.class, ca.getClass());
      testNonRetryableClauseActionSuccess(ca, "test(a, 1, 1.2, p(a, b, c), [])", "test(a, 1, 1.2, p(a, b, c), [])");
      testNonRetryableClauseActionSuccess(ca, "test(X, 1, 1.2, p(X, b, c), [])", "test(a, 1, 1.2, p(a, b, c), [])");
      testNonRetryableClauseActionFailure(ca, "test(a, 1, 1.22, p(a, b, c), [])");
   }

   /** @see MutableArgumentsClauseAction */
   @Test
   public void testMutableArgumentsClauseAction() {
      ClauseAction ca = getClauseAction("test(a, X).");
      assertEquals(MutableArgumentsClauseAction.class, ca.getClass());
      testNonRetryableClauseActionSuccess(ca, "test(a, b)", "test(a, b)");
      testNonRetryableClauseActionSuccess(ca, "test(X, b)", "test(a, b)");
      testNonRetryableClauseActionSuccess(ca, "test(X, X)", "test(a, a)");
      testNonRetryableClauseActionSuccess(ca, "test(X, Y)", "test(a, Y)");
      testNonRetryableClauseActionFailure(ca, "test(b, a)");

      ca = getClauseAction("test(X, X).");
      assertEquals(MutableArgumentsClauseAction.class, ca.getClass());
      testNonRetryableClauseActionSuccess(ca, "test(a, a)", "test(a, a)");
      testNonRetryableClauseActionSuccess(ca, "test(a, Y)", "test(a, a)");
      testNonRetryableClauseActionFailure(ca, "test(a, b)");

      ca = getClauseAction("test(a, c(x(a,X,Y)), c(d(e([a,c,X])))).");
      assertEquals(MutableArgumentsClauseAction.class, ca.getClass());
      testNonRetryableClauseActionSuccess(ca, "test(a, c(x(a,X,y)), c(d(e([a,c,x]))))", "test(a, c(x(a, x, y)), c(d(e(.(a, .(c, .(x, [])))))))");
      testNonRetryableClauseActionFailure(ca, "test(a, c(x(a,x,y)), c(d(e([a,c,y]))))");
   }

   /** @see MultiFunctionSingleResultClauseAction */
   @Test
   public void testMultiFunctionSingleResultClauseAction1() {
      ClauseAction ca = getClauseAction("test(X, Y) :- true, N is X+1, N<5, N=Y.");
      assertEquals(MultiFunctionSingleResultClauseAction.class, ca.getClass());
      assertFalse(ca.couldReEvaluationSucceed());

      PTerm x = integerNumber(2);
      Variable y = variable("Y");
      assertTrue(ca.getFree().evaluate(new PTerm[] {x, y}));
      assertEquals(TermType.INTEGER, y.type());
      assertEquals(3, ((IntegerNumber) y.get()).getLong());

      assertFalse(ca.getFree().evaluate(new PTerm[] {integerNumber(4), y}));
   }

   @Test
   public void testMultiFunctionSingleResultClauseAction2() {
      ClauseAction ca = getClauseAction("test(X, Y) :- Y is X mod 2, Y=1.");
      assertEquals(MultiFunctionSingleResultClauseAction.class, ca.getClass());
      testNonRetryableClauseActionSuccess(ca, "test(53, X)", "test(53, 1)");
      testNonRetryableClauseActionFailure(ca, "test(52, X)");
   }

   /** @see CutClauseAction */
   @Test
   public void testCutClauseAction() {
      ClauseAction ca = getClauseAction("test(a,b,Z) :- !.");
      assertEquals(CutClauseAction.class, ca.getClass());
      CutClauseAction cca = (CutClauseAction) ca;
      boolean success = testCutClauseAction(cca, "a,b,c");
      assertTrue(success);
      success = testCutClauseAction(cca, "a,b,c");
      assertTrue(success);
      success = testCutClauseAction(cca, "d,b,c");
      assertFalse(success);
   }

   private boolean testCutClauseAction(CutClauseAction original, String prologArgumentSyntax) {
      CutClauseAction freeCopy = original.getFree();
      PTerm[] queryArgs = getArgs(prologArgumentSyntax);
      try {
         if (!freeCopy.evaluate(queryArgs)) {
            return false;
         }
      } catch (Throwable t) {
         fail("Caught on first attempt at evaluating: " + t);
      }
      try {
         freeCopy.evaluate(queryArgs);
         fail("No CutException thrown");
      } catch (CutException e) {
         // this it what we expect
      } catch (Throwable t) {
         fail("Caught on second attempt at evaluating: " + t);
      }
      return true;
   }

   /** @see SingleFunctionSingleResultClauseAction */
   @Test
   public void testSingleFunctionSingleResultClauseAction() {
      ClauseAction ca = getClauseAction("test(Y) :- Y is 3+2.");
      assertEquals(SingleFunctionSingleResultClauseAction.class, ca.getClass());
      testNonRetryableClauseActionSuccess(ca, "test(5)", "test(5)");
      testNonRetryableClauseActionSuccess(ca, "test(X)", "test(5)");
      testNonRetryableClauseActionFailure(ca, "test(6)");
   }

   /** @see SingleFunctionMultiResultClauseAction */
   @Test
   public void testSingleFunctionMultiResultClauseAction() {
      int iterations = 3;
      ClauseAction ca = getClauseAction("test :- repeat(" + iterations + ").");
      assertEquals(SingleFunctionMultiResultClauseAction.class, ca.getClass());
      assertTrue(ca.couldReEvaluationSucceed());
      for (int i = 0; i < iterations; i++) {
         assertTrue(ca.evaluate(TermUtils.EMPTY_ARRAY));
      }
      assertFalse(ca.evaluate(TermUtils.EMPTY_ARRAY));
   }

   /** @see SingleFunctionMultiResultClauseAction */
   @Test
   public void testSingleFunctionMultiResultClauseActionVariableAntecedant() {
      ClauseAction ca = getClauseAction("true(X) :- X.");
      assertEquals(SingleFunctionMultiResultClauseAction.class, ca.getClass());
      assertTrue(ca.couldReEvaluationSucceed());

      assertTrue(ca.evaluate(new PTerm[] {new Atom("true")}));
      assertFalse(ca.couldReEvaluationSucceed());

      ca = ca.getFree();
      assertFalse(ca.evaluate(new PTerm[] {new Atom("fail")}));

      ca = ca.getFree();
      assertTrue(ca.evaluate(new PTerm[] {new Atom("repeat")}));
      assertTrue(ca.couldReEvaluationSucceed());
   }

   // helper methods
   private void testNonRetryableClauseActionSuccess(ClauseAction ca, String query, String output) {
      assertFalse(ca.couldReEvaluationSucceed());
      PTerm t = parseSentence(query + ".");
      assertTrue(ca.getFree().evaluate(t.getArgs()));
      assertEquals(output, t.toString());
   }

   private void testNonRetryableClauseActionFailure(ClauseAction ca, String query) {
      assertFalse(ca.couldReEvaluationSucceed());
      PTerm t = parseSentence(query + ".");
      assertFalse(ca.getFree().evaluate(t.getArgs()));
   }

   private ClauseAction getClauseAction(String prologSentenceSyntax) {
      ClauseModel ci = TestUtils.createClauseModel(prologSentenceSyntax);
      return ClauseActionFactory.getClauseAction(kb, ci);
   }

   private PTerm[] getArgs(String prologArgumentSyntax) {
      PTerm t = TestUtils.parseSentence("p(" + prologArgumentSyntax + ").");
      return t.getArgs();
   }
}