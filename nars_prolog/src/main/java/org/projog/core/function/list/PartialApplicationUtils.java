package org.projog.core.function.list;

import static org.projog.core.term.TermUtils.backtrack;

import org.projog.core.KnowledgeBase;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermType;

// Moved methods to separate class so can be used by both MapList and SubList. If useful then move to TermUtils.
class PartialApplicationUtils {
   static boolean isAtomOrStructure(PTerm arg) {
      TermType type = arg.type();
      return type == TermType.STRUCTURE || type == TermType.ATOM;
   }

   static boolean isList(PTerm arg) {
      TermType type = arg.type();
      return type == TermType.EMPTY_LIST || type == TermType.LIST;
   }

   static PredicateFactory getPredicateFactory(KnowledgeBase kb, PTerm partiallyAppliedFunction) {
      return getPredicateFactory(kb, partiallyAppliedFunction, 1);
   }

   static PredicateFactory getPredicateFactory(KnowledgeBase kb, PTerm partiallyAppliedFunction, int numberOfExtraArguments) {
      int numArgs = partiallyAppliedFunction.args() + numberOfExtraArguments;
      PredicateKey key = new PredicateKey(partiallyAppliedFunction.getName(), numArgs);
      return kb.getPredicateFactory(key);
   }

   static PTerm[] createArguments(PTerm partiallyAppliedFunction, PTerm... extraArguments) {
      int originalNumArgs = partiallyAppliedFunction.args();
      PTerm[] result = new PTerm[originalNumArgs + extraArguments.length];

      for (int i = 0; i < originalNumArgs; i++) {
         result[i] = partiallyAppliedFunction.arg(i).get();
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
