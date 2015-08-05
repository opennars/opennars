package org.projog.core.function.construct;

import org.projog.core.KB;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PAtom;
import org.projog.core.term.PTerm;
import org.projog.core.term.PrologOperator;

import static org.projog.core.term.PrologOperator.ATOM;
import static org.projog.core.term.TermUtils.getAtomName;

/* TEST
 % Examples of when all three terms are atoms:
 %TRUE atom_concat(abc, def, abcdef)
 %TRUE atom_concat(a, bcdef, abcdef)
 %TRUE atom_concat(abcde, f, abcdef)
 %TRUE atom_concat(abcdef, '', abcdef)
 %TRUE atom_concat('', abcdef, abcdef)
 %TRUE atom_concat('', '', '')
 %FALSE atom_concat(ab, def, abcdef)
 %FALSE atom_concat(abc, ef, abcdef)
 
 % Examples of when first term is a variable:
 %QUERY atom_concat(abc, X, abcdef)
 %ANSWER X=def
 %QUERY atom_concat(abcde, X, abcdef)
 %ANSWER X=f
 %QUERY atom_concat(a, X, abcdef)
 %ANSWER X=bcdef
 %QUERY atom_concat('', X, abcdef)
 %ANSWER X=abcdef
 %QUERY atom_concat(abcdef, X, abcdef)
 %ANSWER X=
 
 % Examples of when second term is a variable:
 %QUERY atom_concat(X, def, abcdef)
 %ANSWER X=abc
 %QUERY atom_concat(X, f, abcdef)
 %ANSWER X=abcde
 %QUERY atom_concat(X, bcdef, abcdef)
 %ANSWER X=a
 %QUERY atom_concat(X, abcdef, abcdef)
 %ANSWER X=
 %QUERY atom_concat(X, '', abcdef)
 %ANSWER X=abcdef
 
 % Examples of when third term is a variable:
 %QUERY atom_concat(abc, def, X)
 %ANSWER X=abcdef
 %QUERY atom_concat(a, bcdef, X)
 %ANSWER X=abcdef
 %QUERY atom_concat(abcde, f, X)
 %ANSWER X=abcdef
 %QUERY atom_concat(abcdef, '', X)
 %ANSWER X=abcdef
 %QUERY atom_concat('', abcdef, X)
 %ANSWER X=abcdef
 %QUERY atom_concat('', '', X)
 %ANSWER X=

 % Examples of when first and second terms are variables:
 %QUERY atom_concat(X, Y, abcdef)
 %ANSWER
 % X=
 % Y=abcdef
 %ANSWER
 %ANSWER
 % X=a
 % Y=bcdef
 %ANSWER
 %ANSWER
 % X=ab
 % Y=cdef
 %ANSWER
 %ANSWER
 % X=abc
 % Y=def
 %ANSWER
 %ANSWER
 % X=abcd
 % Y=ef
 %ANSWER
 %ANSWER
 % X=abcde
 % Y=f
 %ANSWER
 %ANSWER
 % X=abcdef
 % Y=
 %ANSWER
 %QUERY atom_concat(X, Y, a)
 %ANSWER
 % X=
 % Y=a
 %ANSWER
 %ANSWER
 % X=a
 % Y=
 %ANSWER
 %QUERY atom_concat(X, Y, '')
 %ANSWER
 % X=
 % Y=
 %ANSWER

 % Examples when combination of term types cause failure:
 %QUERY atom_concat(X, Y, Z)
 %ERROR Expected an atom but got: NAMED_VARIABLE with value: Z
 %QUERY atom_concat('', Y, Z)
 %ERROR Expected an atom but got: NAMED_VARIABLE with value: Z
 %QUERY atom_concat(X, '', Z)
 %ERROR Expected an atom but got: NAMED_VARIABLE with value: Z
 %FALSE atom_concat(a, b, c)
 %FALSE atom_concat(a, '', '')
 %FALSE atom_concat('', b, '')
 %FALSE atom_concat('', '', c)
 */
/**
 * <code>atom_concat(X, Y, Z)</code> - concatenates atom names.
 * <p>
 * <code>atom_concat(X, Y, Z)</code> succeeds if the name of atom <code>Z</code> matches the concatenation of the names
 * of atoms <code>X<code> and <code>Y</code>.
 * </p>
 */
public final class AtomConcat implements PredicateFactory {
   private final Singleton singleton = new Singleton();

   @Override
   public Predicate getPredicate(PTerm... args) {
      return getPredicate(args[0], args[1], args[2]);
   }

   public Predicate getPredicate(PTerm prefix, PTerm suffix, PTerm combined) {
      if (prefix.type().isVariable() && suffix.type().isVariable()) {
         return new Retryable(getAtomName(combined));
      } else {
         return singleton;
      }
   }

   @Override
   public void setKB(KB kb) {
      singleton.setKB(kb);
   }

   private static class Singleton extends AbstractSingletonPredicate {
      @Override
      public boolean evaluate(PTerm arg1, PTerm arg2, PTerm arg3) {
         assertAtomOrVariable(arg1);
         assertAtomOrVariable(arg2);
         assertAtomOrVariable(arg3);

         final boolean isArg1Atom = isAtom(arg1);
         final boolean isArg2Atom = isAtom(arg2);
         if (isArg1Atom && isArg2Atom) {
            final PAtom concat = new PAtom(arg1.getName() + arg2.getName());
            return arg3.unify(concat);
         } else {
            final String atomName = getAtomName(arg3);
            if (isArg1Atom) {
               String prefix = arg1.getName();
               return (atomName.startsWith(prefix) && arg2.unify(new PAtom(atomName.substring(prefix.length()))));
            } else if (isArg2Atom) {
               String suffix = arg2.getName();
               return (atomName.endsWith(suffix) && arg1.unify(new PAtom(atomName.substring(0, (atomName.length() - suffix.length())))));
            } else {
               throw new ProjogException("If third argument is not an atom then both first and second arguments must be: " + arg1 + " " + arg2 + " " + arg3);
            }
         }
      }

      private void assertAtomOrVariable(PTerm t) {
         final PrologOperator type = t.type();
         if (type != PrologOperator.ATOM && !type.isVariable()) {
            throw new ProjogException("Expected an atom or variable but got: " + type + " with value: " + t);
         }
      }

      private boolean isAtom(PTerm t) {
         return t.type() == ATOM;
      }
   }

   private static class Retryable implements Predicate {
      final String combined;
      int ctr;

      Retryable(String combined) {
         this.combined = combined;
      }

      @Override
      public boolean evaluate(PTerm... args) {
         return evaluate(args[0], args[1], args[2]);
      }

      private boolean evaluate(PTerm arg1, PTerm arg2, PTerm arg3) {
         while (couldReEvaluationSucceed()) {
            arg1.backtrack();
            arg2.backtrack();

            PAtom prefix = new PAtom(combined.substring(0, ctr));
            PAtom suffix = new PAtom(combined.substring(ctr));
            ctr++;

            return arg1.unify(prefix) && arg2.unify(suffix);
         }
         return false;
      }

      @Override
      public boolean isRetryable() {
         return true;
      }

      @Override
      public boolean couldReEvaluationSucceed() {
         return ctr <= combined.length();
      }
   }
}