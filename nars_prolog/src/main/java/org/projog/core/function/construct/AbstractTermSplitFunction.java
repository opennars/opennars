package org.projog.core.function.construct;

import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.ListFactory;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermType;

/**
 * Extended by {@code Predicate}s that compares a term to a list of individual characters or digits.
 * 
 * @see AtomChars
 * @see NumberChars
 */
abstract class AbstractTermSplitFunction extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2) {
      if (arg1.type().isVariable()) {
         return evaluateWithVariableFirstArgument(arg1, arg2);
      } else {
         return evaluateWithConcreteFirstArgument(arg1, arg2);
      }
   }

   /**
    * Converts {@code arg2} from a list to an atom and attempts to unify it with {@code arg1}.
    * <p>
    * Example of a prolog query that would cause this method to be used:
    * 
    * <pre>
    * ?- number_chars(X, [1,4,2]).
    * X = 142
    * </pre>
    * 
    * @param arg1 a {@code Variable}
    * @param arg2 a {@code List}
    * @return {@code true} if was able to unify
    */
   private boolean evaluateWithVariableFirstArgument(PTerm arg1, PTerm arg2) {
      if (isNotList(arg2)) {
         throw new ProjogException("As the first argument: " + arg1 + " is a variable the second argument needs to be a list but was: " + arg2 + " of type: " + arg2.type());
      }
      StringBuffer sb = new StringBuffer();
      appendListElementsToString(sb, arg2);
      PTerm t = toTerm(sb.toString());
      return arg1.unify(t);
   }

   /**
    * Converts {@code arg1} to a list and attempts to unify it with {@code arg2}.
    * <p>
    * Example of a prolog query that would cause this method to be used:
    * 
    * <pre>
    * ?- atom_chars(apple, X).
    * X = [a,p,p,l,e]
    * </pre>
    * 
    * @param arg1 a {@code Atom} or {@code Numeric}
    * @param arg2 in order to unify, this argument must represent a {@code Atom} or {@code List}
    * @return {@code true} if was able to unify
    */
   private boolean evaluateWithConcreteFirstArgument(PTerm arg1, PTerm arg2) {
      char[] chars = arg1.getName().toCharArray();
      int numChars = chars.length;
      PTerm[] listElements = new PTerm[numChars];
      for (int i = 0; i < numChars; i++) {
         listElements[i] = toTerm(Character.toString(chars[i]));
      }
      PTerm l = ListFactory.createList(listElements);
      return arg2.unify(l);
   }

   private boolean isNotList(PTerm t) {
      TermType tt = t.type();
      return tt != TermType.LIST && tt != TermType.EMPTY_LIST;
   }

   private void appendListElementsToString(StringBuffer sb, PTerm t) {
      if (t.type() == TermType.LIST) {
         appendListElementsToString(sb, t.arg(0));
         appendListElementsToString(sb, t.arg(1));
      } else if (t.type() != TermType.EMPTY_LIST) {
         sb.append(t.toString());
      }
   }

   protected abstract PTerm toTerm(String s);
}