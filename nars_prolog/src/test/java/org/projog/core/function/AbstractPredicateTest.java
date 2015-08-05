package org.projog.core.function;

import org.junit.Test;
import org.projog.core.term.PTerm;

import static org.junit.Assert.*;
import static org.projog.TestUtils.createArgs;

public class AbstractPredicateTest {
   // a non-abstract implementation of AbstractPredicate (so we can create and test it) 
   static class DummyPredicate extends AbstractPredicate {
      @Override
      public boolean isRetryable() {
         return false;
      }

      @Override
      public boolean couldReEvaluationSucceed() {
         return false;
      }
   }

   @Test
   public void testWrongNumberOfArgumentsException() {
      for (int i = 0; i < 10; i++) {
         assertWrongNumberOfArgumentsException(i);
      }
   }

   private void assertWrongNumberOfArgumentsException(int numberOfArguments) {
      try {
         new DummyPredicate().evaluate(createArgs(numberOfArguments));
         fail();
      } catch (IllegalArgumentException e) {
         String expectedMessage = "The predicate: class org.projog.core.function.AbstractPredicateTest$DummyPredicate does next accept the number of arguments: " + numberOfArguments;
         assertEquals(expectedMessage, e.getMessage());
      }
   }

   @Test
   public void testNoArgs() {
      AbstractPredicate p = new DummyPredicate() {
         @Override
         public boolean evaluate() {
            return true;
         }
      };
      assertTrue(p.evaluate(createArgs(0)));
   }

   @Test
   public void testOneArg() {
      AbstractPredicate p = new DummyPredicate() {
         @Override
         public boolean evaluate(PTerm t1) {
            return true;
         }
      };
      assertTrue(p.evaluate(createArgs(1)));
   }

   @Test
   public void testTwoArgs() {
      AbstractPredicate p = new DummyPredicate() {
         @Override
         public boolean evaluate(PTerm t1, PTerm t2) {
            return true;
         }
      };
      assertTrue(p.evaluate(createArgs(2)));
   }

   @Test
   public void testThreeArgs() {
      AbstractPredicate p = new DummyPredicate() {
         @Override
         public boolean evaluate(PTerm t1, PTerm t2, PTerm t3) {
            return true;
         }
      };
      assertTrue(p.evaluate(createArgs(3)));
   }
}
