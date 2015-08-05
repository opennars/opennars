package org.projog.core.udp;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.term.PTerm;

import static org.junit.Assert.assertEquals;

public class ClauseModelTest {
   @Test
   public void testSingleTerm() {
      assertClauseModel("a.", "a", "true");
   }

   @Test
   public void testSimpleImplication() {
      assertClauseModel("a :- true.", "a", "true");
   }

   @Test
   public void testConjunctionImplication() {
      assertClauseModel("a :- b, c, d.", "a", ",(,(b, c), d)");
   }

   @Test
   public void testDefinteClauseGrammer() {
      assertClauseModel("a --> b, c.", "a(A2, A0)", ",(b(A2, A1), c(A1, A0))");
   }

   private void assertClauseModel(String inputSyntax, String consequentSyntax, String antecedantSyntax) {
      ClauseModel ci = TestUtils.createClauseModel(inputSyntax);
      assertToString(consequentSyntax, ci.getConsequent());
      assertToString(antecedantSyntax, ci.getAntecedant());
   }

   private void assertToString(String syntax, PTerm t1) {
      assertEquals(syntax, t1.toString());
   }
}