package org.projog.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.projog.core.PredicateKey;
import org.projog.core.term.Atom;
import org.projog.core.term.PTerm;

/**
 * Simply tests get methods of {@link ProjogStackTraceElement} (as that is the only functionality the class provides).
 * <p>
 * For a more thorough test, including how it is used by {@link Projog#getStackTrace(Throwable)}, see
 * {@link ProjogTest#testIOExceptionWhileEvaluatingQueries()}.
 */
public class ProjogStackTraceElementTest {
   @Test
   public void test() {
      final PredicateKey key = new PredicateKey("test", 1);
      final int clauseIdx = 9;
      final PTerm term = new Atom("test");
      final ProjogStackTraceElement e = new ProjogStackTraceElement(key, clauseIdx, term);
      assertSame(key, e.getPredicateKey());
      assertSame(term, e.getTerm());
      assertEquals(clauseIdx, e.getClauseIdx());
   }
}