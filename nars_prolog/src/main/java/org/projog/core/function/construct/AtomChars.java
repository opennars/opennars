package org.projog.core.function.construct;

import org.projog.core.term.PAtom;

/* TEST
 %QUERY atom_chars(X,[a,p,p,l,e])
 %ANSWER X = apple

 %QUERY atom_chars(apple,X)
 %ANSWER X = [a,p,p,l,e]

 %TRUE atom_chars(apple,[a,p,p,l,e])

 %FALSE atom_chars(apple,[a,p,l,l,e])

 %FALSE atom_chars(apple,[a,p,p,l,e,s])

 %QUERY atom_chars(apple,[X,Y,Y,Z,e])
 %ANSWER 
 % X = a
 % Y = p
 % Z = l
 %ANSWER

 %FALSE atom_chars(apple,[X,Y,Z,Z,e])
 
 %QUERY atom_chars(X,'apple')
 %ERROR As the first argument: X is a variable the second argument needs to be a list but was: apple of type: ATOM
 */
/**
 * <code>atom_chars(A,L)</code> - compares an atom to a list of characters.
 * <p>
 * <code>atom_chars(A,L)</code> compares the name of an atom <code>A</code> with the list of characters <code>L</code>.
 * </p>
 */
public final class AtomChars extends AbstractTermSplitFunction {
   @Override
   protected PAtom toTerm(String s) {
      return new PAtom(s);
   }
}