package org.projog.core.function;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.KB;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class AbstractSingletonPredicateTest {
   @Test
   public void testSimpleImplementation() {
      PredicateFactory pf = new AbstractSingletonPredicate() {
      };

      Predicate p = pf.getPredicate((PTerm[]) null);
      assertFalse(p.isRetryable());
      assertFalse(p.couldReEvaluationSucceed());
      assertSame(pf, p);

      KB kb = TestUtils.createKnowledgeBase();
      pf.setKB(kb);
      assertSame(kb, ((AbstractSingletonPredicate) pf).getKB());
   }

   /**
    * Check {@code AbstractSingletonPredicate#setKnowledgeBase(KnowledgeBase)} invokes
    * {@code AbstractSingletonPredicate#init()} after setting the knowledge base.
    */
   @Test
   public void testInit() {
      class TestPredicate extends AbstractSingletonPredicate {
         KB x;

         @Override
         protected void init() {
            x = getKB();
         }
      };
      TestPredicate pf = new TestPredicate();
      KB kb = TestUtils.createKnowledgeBase();
      pf.setKB(kb);
      assertSame(kb, pf.x);
      assertSame(kb, ((AbstractSingletonPredicate) pf).getKB());
   }
}