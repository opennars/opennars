package org.projog.example;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PAtom;
import org.projog.core.term.PTerm;

import static org.projog.core.term.TermUtils.getAtomName;

public class SingletonPredicateExample extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm t1, PTerm t2) {
      PAtom t1ToUpperCase = new PAtom(getAtomName(t1).toUpperCase());
      return t2.unify(t1ToUpperCase);
   }
}