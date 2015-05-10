package org.projog.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class UnknownPredicateTest {
   @Test
   public void testUnknownPredicate() {
      UnknownPredicate e = UnknownPredicate.UNKNOWN_PREDICATE;
      assertSame(e, e.getPredicate());
      assertFalse(e.evaluate());
      assertFalse(e.isRetryable());
   }
}