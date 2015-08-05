package org.projog.core.function.construct;

import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.PAtom;
import org.projog.core.term.PStruct;
import org.projog.core.term.PTerm;

import static org.projog.core.term.TermUtils.*;

/* TEST
 %QUERY functor(f(a,b,c(Z)),F,N)
 %ANSWER
 % Z=UNINSTANTIATED VARIABLE
 % F=f
 % N=3
 %ANSWER

 %QUERY functor(a+b,F,N)
 %ANSWER
 % F=+
 % N=2
 %ANSWER

 %QUERY functor([a,b,c],F,N)
 %ANSWER
 % F=.
 % N=2
 %ANSWER

 %QUERY functor(atom,F,N)
 %ANSWER
 % F=atom
 % N=0
 %ANSWER

 %FALSE functor([a,b,c],'.',3)
 %FALSE functor([a,b,c],a,Z)

 %QUERY functor( X, sentence, 2)
 %ANSWER X = sentence(_, _)

 copy(Old, New) :- functor(Old, F, N), functor(New, F, N).

 %QUERY copy(sentence(a,b), X)
 %ANSWER X = sentence(_, _)
 */
/**
 * <code>functor(T,F,N)</code>
 * <p>
 * Predicate <code>functor(T,F,N)</code> means "<code>T</code> is a structure with name (functor) <code>F</code> and
 * <code>N</code> number of arguments".
 * </p>
 */
public final class Functor extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm t, PTerm f, PTerm n) {
      switch (t.type()) {
         case ATOM:
            return f.unify(t) && n.unify(new IntegerNumber(0));
         case STRUCTURE:
         case LIST:
         case EMPTY_LIST:
            return f.unify(new PAtom(t.getName())) && n.unify(new IntegerNumber(t.length()));
         case NAMED_VARIABLE:
            int numArgs = toInt(n);
            PTerm[] a = new PTerm[numArgs];
            for (int i = 0; i < numArgs; i++) {
               a[i] = createAnonymousVariable();
            }
            String functorName = getAtomName(f);
            return t.unify(PStruct.make(functorName, a));
         default:
            throw new ProjogException("Invalid type for first argument of Functor command: " + t.type());
      }
   }
}