package org.projog.example;

import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.PAtom;
import org.projog.core.term.PTerm;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class RetryablePredicateExample extends AbstractRetryablePredicate {
   private Iterator<Map.Entry<Object, Object>> systemProperties;

   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2) {
      if (systemProperties == null) {
         systemProperties = System.getProperties().entrySet().iterator();
      }
      while (systemProperties.hasNext()) {
         arg1.backtrack();
         arg2.backtrack();
         Entry<Object, Object> entry = systemProperties.next();
         String key = (String) entry.getKey();
         String value = (String) entry.getValue();
         if (arg1.unify(new PAtom(key)) && arg2.unify(new PAtom(value))) {
            return true;
         }
      }
      return false;
   }

   @Override
   public RetryablePredicateExample getPredicate(PTerm arg1, PTerm arg2) {
      return new RetryablePredicateExample();
   }
}