package org.projog.example;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.Atom;
import org.projog.core.term.PTerm;

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
         if (arg1.unify(new Atom(key)) && arg2.unify(new Atom(value))) {
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