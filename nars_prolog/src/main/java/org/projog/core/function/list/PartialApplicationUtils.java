package org.projog.core.function.list;

import org.projog.core.KB;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;
import org.projog.core.term.PTerm;
import org.projog.core.term.PrologOperator;

import static org.projog.core.term.TermUtils.backtrack;

// Moved methods to separate class so can be used by both MapList and SubList. If useful then move to TermUtils.
class PartialApplicationUtils {
   static boolean isAtomOrStructure(PTerm arg) {
      PrologOperator type = arg.type();
      return type == PrologOperator.STRUCTURE || type == PrologOperator.ATOM;
   }

   static boolean isList(PTerm arg) {
      PrologOperator type = arg.type();
      return type == PrologOperator.EMPTY_LIST || type == PrologOperator.LIST;
   }

   static PredicateFactory getPredicateFactory(KB kb, PTerm partiallyAppliedFunction) {
      return getPredicateFactory(kb, partiallyAppliedFunction, 1);
   }

   static PredicateFactory getPredicateFactory(KB kb, PTerm partiallyAppliedFunction, int numberOfExtraArguments) {
      int numArgs = partiallyAppliedFunction.length() + numberOfExtraArguments;
      PredicateKey key = new PredicateKey(partiallyAppliedFunction.getName(), numArgs);
      return kb.getPredicateFactory(key);
   }

   static PTerm[] createArguments(PTerm partiallyAppliedFunction, PTerm... extraArguments) {
      int originalNumArgs = partiallyAppliedFunction.length();
      PTerm[] result = new PTerm[originalNumArgs + extraArguments.length];

      for (int i = 0; i < originalNumArgs; i++) {
         result[i] = partiallyAppliedFunction.term(i).get();
      }

      for (int i = 0; i < extraArguments.length; i++) {
         result[originalNumArgs + i] = extraArguments[i].get();
      }

      return result;
   }

   static boolean apply(PredicateFactory pf, PTerm[] args) {
      Predicate p = pf.getPredicate(args);
      if (p.evaluate(args)) {
         return true;
      } else {
         backtrack(args);
         return false;
      }
   }
}
