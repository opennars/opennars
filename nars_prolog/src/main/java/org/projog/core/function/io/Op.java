package org.projog.core.function.io;

import org.projog.core.Operands;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

import static org.projog.core.KnowledgeBaseUtils.getOperands;
import static org.projog.core.term.TermUtils.getAtomName;
import static org.projog.core.term.TermUtils.toInt;

/* TEST
 '~'(X,Y) :- X>Y-4, X<Y+4.

 %TRUE op(1000,xfx,'~')

 %TRUE 4 ~ 7
 %TRUE 7 ~ 7
 %TRUE 10 ~ 7
 %FALSE 11 ~ 7
 %FALSE 3 ~ 7

 % Example of invalid arguments
 %QUERY op(X,xfx,'><')
 %ERROR Expected Numeric but got: NAMED_VARIABLE with value: X

 %QUERY op(1000,Y,'><')
 %ERROR Expected an atom but got: NAMED_VARIABLE with value: Y

 %QUERY op(1000,xfx,Z)
 %ERROR Expected an atom but got: NAMED_VARIABLE with value: Z

 %QUERY op(1000,zfz,'><')
 %ERROR Cannot add operand with associativity of: zfz as the only values allowed are: [xfx, xfy, yfx, fx, fy, xf, yf]
 
 % Create some prefix and postfix operators for the later examples below.

 %TRUE op(550, fy, 'fyExample')
 %TRUE op(650, fx, 'fxExample')
 %TRUE op(600, yf, 'yfExample')
 %TRUE op(500, xf, 'xfExample')

 % Example of nested prefix operators.

 %QUERY X = fxExample fyExample fyExample a, write_canonical(X), nl
 %OUTPUT
 % fxExample(fyExample(fyExample(a)))
 % 
 %OUTPUT
 %ANSWER X=fxExample fyExample fyExample a

 % Example of a postfix operator.

 %QUERY X = 123 yfExample, write_canonical(X), nl
 %OUTPUT
 % yfExample(123)
 % 
 %OUTPUT
 %ANSWER X=123 yfExample

 % Example of nested postfix operators.

 %QUERY X = a xfExample yfExample yfExample, write_canonical(X), nl
 %OUTPUT
 % yfExample(yfExample(xfExample(a)))
 % 
 %OUTPUT
 %ANSWER X=a xfExample yfExample yfExample

 % Example of combining post and prefix operators where the postfix operator has the higher precedence.

 %QUERY X = fyExample a yfExample, write_canonical(X), nl
 %OUTPUT
 % yfExample(fyExample(a))
 % 
 %OUTPUT
 %ANSWER X=fyExample a yfExample

 % Example of combining post and prefix operators where the prefix operator has the higher precedence.

 %TRUE op(700, fy, 'fyExampleB')

 %QUERY X = fyExampleB a yfExample, write_canonical(X), nl
 %OUTPUT
 % fyExampleB(yfExample(a))
 % 
 %OUTPUT
 %ANSWER X=fyExampleB a yfExample

 % Examples of how an "x" in an associativity (i.e. "fx" or "xf") means that the argument can contain operators of only a lower level of priority than the operator represented by "f".
 
 %QUERY X = a xfExample xfExample
 %ERROR Invalid postfix: xfExample 500 and term: xfExample(a) 500 Line: X = a xfExample xfExample.

 %QUERY X = fxExample fxExample a
 %ERROR Invalid prefix: fxExample level: 650 greater than current level: 649 Line: X = fxExample fxExample a.
 */
/**
 * <code>op(X,Y,Z)</code>
 * <p>
 * Allows functors (names of predicates) to be defined as "operators". The use of operators allows syntax to be easier
 * to write and read. <code>Z</code> is the atom that we want to be an operator, <code>X</code> is the precedence class
 * (an integer), and <code>Y</code> the associativity specifier. e.g. <code>op(1200,xfx,':-')</code>
 * </p>
 */
public final class Op extends AbstractSingletonPredicate {
   private Operands operands;

   @Override
   protected void init() {
      operands = getOperands(getKB());
   }

   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2, PTerm arg3) {
      int precedence = toInt(arg1);
      String associativity = getAtomName(arg2);
      String name = getAtomName(arg3);
      operands.addOperand(name, associativity, precedence);
      return true;
   }
}