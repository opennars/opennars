package org.projog.core.function.construct;

import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.*;

import java.util.ArrayList;

/* TEST
 %QUERY p(a,b,c) =.. X
 %ANSWER X=[p,a,b,c]

 %FALSE p(a,b,c) =.. [p,x,y,z]

 %FALSE p(a,b,c) =.. []
 
 %QUERY [a,b,c,d] =.. X
 %ANSWER X=[.,a,[b,c,d]]

 %QUERY [a,b,c,d] =.. [X|Y]
 %ANSWER 
 % X=.
 % Y=[a,[b,c,d]]
 %ANSWER

 %QUERY X =.. [a,b,c,d]
 %ANSWER X=a(b, c, d)

 %QUERY X =.. [a,[b,c],d]
 %ANSWER X=a([b,c], d)

 %QUERY a+b =.. X
 %ANSWER X=[+,a,b]

 %QUERY a+b =.. [+, X, Y]
 %ANSWER
 % X=a
 % Y=b
 %ANSWER
 
 %QUERY a =.. [a]
 %ERROR Expected first argument to be a variable or a predicate but got a ATOM with value: a

 %QUERY a+b =.. '+ X Y'
 %ERROR Expected second argument to be a variable or a list but got a ATOM with value: + X Y

 %QUERY X =.. Y
 %ERROR Both arguments are variables: X and: Y
 */
/**
 * <code>X=..L</code> - "univ".
 * <p>
 * The <code>X=..L</code> predicate (pronounced "univ") provides a way to obtain the arguments of a structure as a list
 * or construct a structure from a list of arguments.
 * </p>
 */
public final class Univ extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2) {
      PrologOperator argType1 = arg1.type();
      PrologOperator argType2 = arg2.type();
      boolean isFirstArgumentVariable = argType1.isVariable();
      boolean isFirstArgumentPredicate = argType1.isStructure();
      boolean isSecondArgumentVariable = argType2.isVariable();
      boolean isSecondArgumentList = isList(argType2);

      if (!isFirstArgumentPredicate && !isFirstArgumentVariable) {
         throw new ProjogException("Expected first argument to be a variable or a predicate but got a " + argType1 + " with value: " + arg1);
      } else if (!isSecondArgumentList && !isSecondArgumentVariable) {
         throw new ProjogException("Expected second argument to be a variable or a list but got a " + argType2 + " with value: " + arg2);
      } else if (isFirstArgumentVariable && isSecondArgumentVariable) {
         throw new ProjogException("Both arguments are variables: " + arg1 + " and: " + arg2);
      } else if (isFirstArgumentPredicate) {
         PTerm predicateAsList = toList(arg1);
         return predicateAsList.unify(arg2);
      } else {
         PTerm listAsPredicate = toPredicate(arg2);
         return arg1.unify(listAsPredicate);
      }
   }

   private boolean isList(PrologOperator tt) {
      return tt == PrologOperator.LIST || tt == PrologOperator.EMPTY_LIST;
   }

   private PTerm toPredicate(PTerm t) {
      if (t.term(0).type() != PrologOperator.ATOM) {
         throw new ProjogException("First argument is not an atom in list: " + t);
      }
      String predicateName = t.term(0).getName();
      ArrayList<PTerm> predicateArgs = new ArrayList<>();
      PTerm arg = t.term(1);
      while (arg.type() == PrologOperator.LIST) {
         predicateArgs.add(arg.term(0));
         arg = arg.term(1);
      }
      if (arg.type() != PrologOperator.EMPTY_LIST) {
         predicateArgs.add(arg);
      }
      return PStruct.make(predicateName, predicateArgs.toArray(new PTerm[predicateArgs.size()]));
   }

   private PTerm toList(PTerm t) {
      String predicateName = t.getName();
      int numArgs = t.length();
      PTerm[] listArgs = new PTerm[numArgs + 1];
      listArgs[0] = new PAtom(predicateName);
      for (int i = 0; i < numArgs; i++) {
         listArgs[i + 1] = t.term(i);
      }
      return ListFactory.createList(listArgs);
   }
}