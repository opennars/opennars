package org.projog.core.term;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.projog.TestUtils.createKnowledgeBase;
import static org.projog.TestUtils.parseSentence;
import static org.projog.core.KnowledgeBaseUtils.getTermFormatter;

public class TermFormatterTest {
   @Test
   public void testTermToString() {
      String inputSyntax = "?- X = -1 + 1.684 , p(1, 7.3, [_,[]|c])";
      PTerm inputTerm = parseSentence(inputSyntax + ".");

      TermFormatter tf = createFormatter();
      assertEquals(inputSyntax, tf.toString(inputTerm));
   }

   private TermFormatter createFormatter() {
      return getTermFormatter(createKnowledgeBase());
   }
}
