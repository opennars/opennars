package org.projog.core.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.createArgs;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.KB;
import org.projog.core.Predicate;
import org.projog.core.term.PTerm;

public class AbstractRetryablePredicateTest {
   private static final AbstractRetryablePredicate INSTANCE = new AbstractRetryablePredicate() {
   };

   @Test
   public void testSimpleImplementation() {
      assertTrue(INSTANCE.isRetryable());
      assertTrue(INSTANCE.couldReEvaluationSucceed());

      KB kb = TestUtils.createKnowledgeBase();
      INSTANCE.setKB(kb);
      assertSame(kb, INSTANCE.getKB());
   }

   @Test
   public void testWrongNumberOfArgumentsException() {
      for (int i = 0; i < 10; i++) {
         assertWrongNumberOfArgumentsException(i);
      }
   }

   private void assertWrongNumberOfArgumentsException(int numberOfArguments) {
      try {
         INSTANCE.getPredicate(createArgs(numberOfArguments));
         fail();
      } catch (IllegalArgumentException e) {
         String expectedMessage = "The predicate factory: class org.projog.core.function.AbstractRetryablePredicateTest$1 does next accept the number of arguments: " + numberOfArguments;
         assertEquals(expectedMessage, e.getMessage());
      }
   }

   @Test
   public void testNoArgs() {
      AbstractRetryablePredicate p = new AbstractRetryablePredicate() {
         @Override
         public Predicate getPredicate() {
            return this;
         }
      };
      assertSame(p, p.getPredicate(createArgs(0)));
   }

   @Test
   public void testOneArg() {
      AbstractRetryablePredicate p = new AbstractRetryablePredicate() {
         @Override
         public Predicate getPredicate(PTerm t1) {
            return this;
         }
      };
      assertSame(p, p.getPredicate(createArgs(1)));
   }

   @Test
   public void testTwoArgs() {
      AbstractRetryablePredicate p = new AbstractRetryablePredicate() {
         @Override
         public Predicate getPredicate(PTerm t1, PTerm t2) {
            return this;
         }
      };
      assertSame(p, p.getPredicate(createArgs(2)));
   }

   @Test
   public void testThreeArgs() {
      AbstractRetryablePredicate p = new AbstractRetryablePredicate() {
         @Override
         public Predicate getPredicate(PTerm t1, PTerm t2, PTerm t3) {
            return this;
         }
      };
      assertSame(p, p.getPredicate(createArgs(3)));
   }
}