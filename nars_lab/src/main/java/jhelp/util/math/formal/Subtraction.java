package jhelp.util.math.formal;

import java.util.Arrays;

/**
 * Subtraction <br>
 * <br>
 * 
 * @author JHelp
 */
public class Subtraction
      extends BinaryOperator
{
   /**
    * Subtraction simplifier
    * 
    * @author JHelp
    */
   class SubtractionSimplifier
         implements FunctionSimplifier
   {
      /**
       * Simplification :
       * <table border=1>
       * <tr>
       * <th>Function<br>
       * </th>
       * <th>Simplification<br>
       * </th>
       * </tr>
       * <tr>
       * <td>(&nbsp;X&nbsp;+&nbsp;Y&nbsp;)&nbsp;-&nbsp;X<br>
       * </td>
       * <td>Y<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;X&nbsp;+&nbsp;Y&nbsp;)&nbsp;-&nbsp;Y<br>
       * </td>
       * <td>X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;X&nbsp;+&nbsp;Y&nbsp;)&nbsp;-&nbsp;Z<br>
       * </td>
       * <td>(&nbsp;X&nbsp;-&nbsp;Z&nbsp;)&nbsp;+&nbsp;Y<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param addition
       *           Addition argument
       * @param function
       *           Function argument
       * @return Simplification
       */
      private Function simplify(final Addition addition, final Function function)
      {
         if(addition.parameter1.equals(function) == true)
         {
            return addition.parameter2.simplify();
         }

         if(addition.parameter2.equals(function) == true)
         {
            return addition.parameter1.simplify();
         }

         return Function.createAddition(new Subtraction(addition.parameter1.simplify(), function.simplify()), addition.parameter2.simplify());
      }

      /**
       * Simplification : C1-C2 -> C3
       * 
       * @param constant1
       *           Constant argument C1
       * @param constant2
       *           Constant Argument C2
       * @return Simplification
       */
      private Function simplify(final Constant constant1, final Constant constant2)
      {
         if((constant1.isUndefined() == true) || (constant2.isUndefined() == true))
         {
            return Constant.UNDEFINED;
         }

         return new Constant(constant1.obtainRealValueNumber() - constant2.obtainRealValueNumber());
      }

      /**
       * Simplification : C1-X -> C1-X
       * 
       * @param constant
       *           Constant argument C1
       * @param function
       *           Function argument X
       * @return Simplification
       */
      private Function simplify(final Constant constant, final Function function)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         if(constant.isNul() == true)
         {
            return new MinusUnary(function.simplify());
         }

         return new Subtraction(constant, function.simplify());
      }

      /**
       * Simplification :
       * <table border=1>
       * <tr>
       * <th>Function<br>
       * </th>
       * <th>Simplification<br>
       * </th>
       * </tr>
       * <tr>
       * <td>X&nbsp;-&nbsp;(&nbsp;X&nbsp;+&nbsp;Y&nbsp;)<br>
       * </td>
       * <td>-Y<br>
       * </td>
       * </tr>
       * <tr>
       * <td>X&nbsp;-&nbsp;(&nbsp;Y&nbsp;+&nbsp;X&nbsp;)<br>
       * </td>
       * <td>-Y<br>
       * </td>
       * </tr>
       * <tr>
       * <td>X&nbsp;-&nbsp;(&nbsp;Y&nbsp;+&nbsp;Z&nbsp;)<br>
       * </td>
       * <td>(&nbsp;X&nbsp;-&nbsp;Y&nbsp;)&nbsp;-&nbsp;Z<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param function
       *           Function argument X
       * @param addition
       *           Addition argument
       * @return Simplification
       */
      private Function simplify(final Function function, final Addition addition)
      {
         if(addition.parameter1.equals(function) == true)
         {
            return new MinusUnary(addition.parameter2.simplify());
         }

         if(addition.parameter2.equals(function) == true)
         {
            return new MinusUnary(addition.parameter1.simplify());
         }

         return new Subtraction(new Subtraction(function.simplify(), addition.parameter1.simplify()), addition.parameter2.simplify());
      }

      /**
       * Simplification : X-C1 -> C2+X
       * 
       * @param function
       *           Function argument X
       * @param constant
       *           Constant argument C1
       * @return Simplification
       */
      private Function simplify(final Function function, final Constant constant)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         if(constant.isNul() == true)
         {
            function.simplify();
         }

         return Function.createAddition(new Constant(-constant.obtainRealValueNumber()), function.simplify());
      }

      /**
       * Simplification&nbsp;:<br>
       * (X1+X2+...+Xn)-(Y1+Y2+..+Ym)&nbsp;=&gt;&nbsp;(X1-Y1)+...+(Xo-Yo)+R<br>
       * <table border=1>
       * <tr>
       * <th>Condition<br>
       * </th>
       * <th>o<br>
       * </th>
       * <th>R<br>
       * </th>
       * </tr>
       * <tr>
       * <td>n&nbsp;==&nbsp;m<br>
       * </td>
       * <td>n<br>
       * </td>
       * <td>0<br>
       * </td>
       * </tr>
       * <tr>
       * <td>n&nbsp;&gt;&nbsp;m<br>
       * </td>
       * <td>m<br>
       * </td>
       * <td>X(o+1)+...+Xn<br>
       * </td>
       * </tr>
       * <tr>
       * <td>n&nbsp;&lt;&nbsp;m<br>
       * </td>
       * <td>n<br>
       * </td>
       * <td>-(Y(o+1)+...+Ym)<br>
       * </td>
       * </tr>
       * </table>
       * <br>
       * <b>&nbsp;OR&nbsp;</b><br>
       * <br>
       * <br>
       * <table border=1>
       * <tr>
       * <th>Function<br>
       * </th>
       * <th>Simplification<br>
       * </th>
       * </tr>
       * <tr>
       * <td>(&nbsp;X&nbsp;+&nbsp;Y&nbsp;)&nbsp;-&nbsp;X<br>
       * </td>
       * <td>Y<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;X&nbsp;+&nbsp;Y&nbsp;)&nbsp;-&nbsp;Y<br>
       * </td>
       * <td>X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;X&nbsp;+&nbsp;Y&nbsp;)&nbsp;-&nbsp;Z<br>
       * </td>
       * <td>(&nbsp;X&nbsp;-&nbsp;Z&nbsp;)&nbsp;+&nbsp;Y<br>
       * </td>
       * </tr>
       * <tr>
       * <td>X&nbsp;&nbsp;-&nbsp;(&nbsp;X&nbsp;+&nbsp;Y&nbsp;)<br>
       * </td>
       * <td>-&nbsp;Y<br>
       * </td>
       * </tr>
       * <tr>
       * <td>X&nbsp;-&nbsp;(&nbsp;Y&nbsp;+&nbsp;X&nbsp;)<br>
       * </td>
       * <td>-&nbsp;Y<br>
       * </td>
       * </tr>
       * <tr>
       * <td>X&nbsp;-&nbsp;(&nbsp;Y&nbsp;+&nbsp;Z&nbsp;)<br>
       * </td>
       * <td>(&nbsp;X&nbsp;-&nbsp;Y&nbsp;)&nbsp;-&nbsp;Z<br>
       * </td>
       * </tr>
       * <tr>
       * <td>X&nbsp;-&nbsp;Y<br>
       * </td>
       * <td>X&nbsp;-&nbsp;Y<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param function1
       *           First function argument
       * @param function2
       *           Second function argument
       * @return Simplification
       */
      private Function simplify(final Function function1, final Function function2)
      {
         final Function[] additions1 = Addition.extractAdditions(function1);
         final Function[] additions2 = Addition.extractAdditions(function2);

         if((additions1.length > 1) || (additions2.length > 1))
         {
            // (X1+X2+...+Xn)-(Y1+Y2+..+Ym) => (X1-Y1)+...+(Xo-Yo)+R
            // if n==m => o=n | R=0
            // if n>m => o=m | R=X(o+1)+...+Xn
            // if n<m => o=n | R=-(Y(o+1)+...+Ym)
            final int n = additions1.length;
            final int m = additions2.length;
            final int o = Math.min(n, m);

            Function f = new Subtraction(additions1[0].simplify(), additions2[0].simplify());

            for(int i = 1; i < o; i++)
            {
               f = Function.createAddition(f.simplify(), new Subtraction(additions1[i].simplify(), additions2[i].simplify()));
            }

            if(n == m)
            {
               return f.simplify();
            }

            if(n > m)
            {
               return Function.createAddition(f.simplify(), Function.createAddition(Arrays.copyOfRange(additions1, o, n)).simplify());
            }

            return new Subtraction(f.simplify(), Function.createAddition(Arrays.copyOfRange(additions2, o, m)).simplify());
         }

         final Function function = this.simplifyMultipleMultiplication(function1, function2);

         if(function != null)
         {
            return function;
         }

         if((function1 instanceof Addition) == true)
         {
            return this.simplify((Addition) function1, function2);
         }

         if((function2 instanceof Addition) == true)
         {
            return this.simplify(function1, (Addition) function2);
         }

         return new Subtraction(function1.simplify(), function2.simplify());
      }

      /**
       * Simplification : X-(-Y) -> X+Y
       * 
       * @param function
       *           Function argument X
       * @param minusUnary
       *           Minus unary argument -Y
       * @return Simplification
       */
      private Function simplify(final Function function, final MinusUnary minusUnary)
      {
         return Function.createAddition(minusUnary.parameter.simplify(), function.simplify());
      }

      /**
       * Simplification :
       * <table border=1>
       * <tr>
       * <th>Function<br>
       * </th>
       * <th>Simplification<br>
       * </th>
       * </tr>
       * <tr>
       * <td>X&nbsp;-&nbsp;(&nbsp;X&nbsp;*&nbsp;C1&nbsp;)<br>
       * </td>
       * <td>C2&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>X&nbsp;-&nbsp;(&nbsp;C1&nbsp;*&nbsp;X&nbsp;)<br>
       * </td>
       * <td>C2&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>X&nbsp;-&nbsp;(&nbsp;Y&nbsp;*&nbsp;Z&nbsp;)<br>
       * </td>
       * <td>X&nbsp;-&nbsp;(&nbsp;Y&nbsp;*&nbsp;Z&nbsp;)<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param function
       *           Function argument X
       * @param multiplication
       *           Multiplication argument
       * @return Simplification
       */
      private Function simplify(final Function function, final Multiplication multiplication)
      {
         if((function.equals(multiplication.parameter1) == true) && ((multiplication.parameter2 instanceof Constant) == true))
         {
            final Constant constant = (Constant) multiplication.parameter2;

            return Function.createMultiplication(new Constant(1 - constant.obtainRealValueNumber()), function.simplify());
         }

         if((function.equals(multiplication.parameter2) == true) && ((multiplication.parameter1 instanceof Constant) == true))
         {
            final Constant constant = (Constant) multiplication.parameter1;

            return Function.createMultiplication(new Constant(1 - constant.obtainRealValueNumber()), function.simplify());
         }

         return new Subtraction(function.simplify(), multiplication.simplify());
      }

      /**
       * Simplification : X-(Y-Z) -> X+(Z-Y)
       * 
       * @param function
       *           Function argument X
       * @param subtraction
       *           Subtraction argument Y-Z
       * @return Simplification
       */
      private Function simplify(final Function function, final Subtraction subtraction)
      {
         return Function.createAddition(function.simplify(), new Subtraction(subtraction.parameter2.simplify(), subtraction.parameter1.simplify()));
      }

      /**
       * Simplification : ln(X)-ln(Y) -> ln(X/Y)
       * 
       * @param logarithm1
       *           Logarithm argument ln(X)
       * @param logarithm2
       *           Logarithm argument ln(Y)
       * @return Simplification
       */
      private Function simplify(final Logarithm logarithm1, final Logarithm logarithm2)
      {
         return new Logarithm(new Division(logarithm1.parameter.simplify(), logarithm2.parameter.simplify()));
      }

      /**
       * Simplification : (-X)-Y -> -(X+Y)
       * 
       * @param minusUnary
       *           Minus unary argument -X
       * @param function
       *           Function argument Y
       * @return Simplification
       */
      private Function simplify(final MinusUnary minusUnary, final Function function)
      {
         return new MinusUnary(Function.createAddition(minusUnary.parameter.simplify(), function.simplify()));
      }

      /**
       * Simplification : (-X)-(-Y) -> Y-X
       * 
       * @param minusUnary1
       *           Minus unary argument -X
       * @param minusUnary2
       *           Minus unary argument -Y
       * @return Simplification
       */
      private Function simplify(final MinusUnary minusUnary1, final MinusUnary minusUnary2)
      {
         return new Subtraction(minusUnary2.parameter.simplify(), minusUnary1.parameter.simplify());
      }

      /**
       * Simplification :
       * <table border=1>
       * <tr>
       * <th>Function<br>
       * </th>
       * <th>Simplification<br>
       * </th>
       * </tr>
       * <tr>
       * <td>(&nbsp;X&nbsp;*&nbsp;C1)&nbsp;-&nbsp;X<br>
       * </td>
       * <td>C2&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;X<br>
       * </td>
       * <td>C2&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;X&nbsp;*&nbsp;Y&nbsp;)&nbsp;-&nbsp;Z<br>
       * </td>
       * <td>(&nbsp;X&nbsp;*&nbsp;Y&nbsp;)&nbsp;-&nbsp;Z<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param multiplication
       *           Multiplication argument
       * @param function
       *           Function argument
       * @return Simplification
       */
      private Function simplify(final Multiplication multiplication, final Function function)
      {
         if((function.equals(multiplication.parameter1) == true) && ((multiplication.parameter2 instanceof Constant) == true))
         {
            final Constant constant = (Constant) multiplication.parameter2;

            return Function.createMultiplication(new Constant(constant.obtainRealValueNumber() - 1), function.simplify());
         }

         if((function.equals(multiplication.parameter2) == true) && ((multiplication.parameter1 instanceof Constant) == true))
         {
            final Constant constant = (Constant) multiplication.parameter1;

            return Function.createMultiplication(new Constant(constant.obtainRealValueNumber() - 1), function.simplify());
         }

         return new Subtraction(multiplication.simplify(), function.simplify());
      }

      /**
       * Simplification :
       * <table border=1>
       * <tr>
       * <th>Function<br>
       * </th>
       * <th>Simplification<br>
       * </th>
       * </tr>
       * <tr>
       * <td>(C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;(&nbsp;C2&nbsp;*&nbsp;X&nbsp;)<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;(&nbsp;X&nbsp;*&nbsp;C2&nbsp;)<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;X&nbsp;*&nbsp;C1&nbsp;)&nbsp;-&nbsp;(&nbsp;C2&nbsp;*&nbsp;X&nbsp;)<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;X&nbsp;*&nbsp;C1&nbsp;)&nbsp;-&nbsp;(&nbsp;X&nbsp;*&nbsp;C2&nbsp;)<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;&nbsp;X<br>
       * </td>
       * <td>C2&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <br>
       * <tr>
       * <td>(&nbsp;X&nbsp;*&nbsp;C1&nbsp;)&nbsp;-&nbsp;&nbsp;X<br>
       * </td>
       * <td>C2&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>X&nbsp;-&nbsp;(C1&nbsp;*&nbsp;X&nbsp;)<br>
       * </td>
       * <td>C2&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>X&nbsp;-&nbsp;(&nbsp;X&nbsp;*&nbsp;C1&nbsp;)&nbsp;<br>
       * </td>
       * <td>C2&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;cos(X)&nbsp;*&nbsp;cos(Y)&nbsp;)&nbsp;-&nbsp;(&nbsp;sin(X)&nbsp;*&nbsp;sin(Y)&nbsp;)<br>
       * </td>
       * <td>cos(X&nbsp;+&nbsp;Y)<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;cos(X)&nbsp;*&nbsp;sin(Y)&nbsp;)&nbsp;-&nbsp;(&nbsp;sin(X)&nbsp;*&nbsp;cos(Y)&nbsp;)<br>
       * </td>
       * <td>sin(X&nbsp;-&nbsp;Y)<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;sin(Y)&nbsp;*&nbsp;cos(X)&nbsp;&nbsp;)&nbsp;-&nbsp;(&nbsp;sin(X)&nbsp;*&nbsp;cos(Y)&nbsp;)<br>
       * </td>
       * <td>sin(X&nbsp;-&nbsp;Y)<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;cos(X)&nbsp;*&nbsp;sin(Y)&nbsp;)&nbsp;-&nbsp;(&nbsp;&nbsp;cos(Y)&nbsp;*&nbsp;sin(X)&nbsp;)<br>
       * </td>
       * <td>sin(X&nbsp;-&nbsp;Y)<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;&nbsp;sin(Y)*cos(X)&nbsp;)&nbsp;-&nbsp;(&nbsp;&nbsp;cos(Y)&nbsp;*&nbsp;sin(X)&nbsp;)<br>
       * </td>
       * <td>sin(X&nbsp;-&nbsp;Y)<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;sin(X)&nbsp;*&nbsp;sin(Y)&nbsp;)&nbsp;-&nbsp;(&nbsp;cos(X)&nbsp;*&nbsp;cos(Y)&nbsp;)<br>
       * </td>
       * <td>-cos(X&nbsp;+&nbsp;Y)<br>
       * </td>
       * </tr>
       * <tr>
       * <td>
       * (X1&nbsp;*&nbsp;...&nbsp;*&nbsp;Xn&nbsp;*&nbsp;C1&nbsp;*&nbsp;Y1&nbsp;*&nbsp;...&nbsp;*&nbsp;Ym)&nbsp;-&nbsp;(X1&nbsp
       * ;*&nbsp;...&nbsp;*&nbsp;Xn&nbsp;*&nbsp;C2&nbsp;*&nbsp;Y1&nbsp;*&nbsp;...&nbsp;*&nbsp;Ym)&nbsp;<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X1&nbsp;*&nbsp;...&nbsp;*&nbsp;Xn&nbsp;*&nbsp;Y1&nbsp;*&nbsp;...&nbsp;*&nbsp;Ym<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(X*Y)&nbsp;-&nbsp;(Z*A)<br>
       * </td>
       * <td>(X*Y)&nbsp;-&nbsp;(Z*A)<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param multiplication1
       *           First multiplication argument
       * @param multiplication2
       *           Second Multiplication argument
       * @return Simplification
       */
      private Function simplify(final Multiplication multiplication1, final Multiplication multiplication2)
      {
         if((multiplication1.parameter1 instanceof Constant) == true)
         {
            if((multiplication2.parameter1 instanceof Constant) == true)
            {
               return this.simplifySubtractionOfMultiplications((Constant) multiplication1.parameter1, multiplication1.parameter2, (Constant) multiplication2.parameter1, multiplication2.parameter2);
            }

            if((multiplication2.parameter2 instanceof Constant) == true)
            {
               return this.simplifySubtractionOfMultiplications((Constant) multiplication1.parameter1, multiplication1.parameter2, multiplication2.parameter1, (Constant) multiplication2.parameter2);
            }

            if(multiplication1.parameter2.equals(multiplication2) == true)
            {
               return Function.createMultiplication(new Constant(((Constant) multiplication1.parameter1).obtainRealValueNumber() - 1), multiplication2.parameter1.simplify(), multiplication2.parameter2.simplify());
            }
         }

         if((multiplication1.parameter2 instanceof Constant) == true)
         {
            if((multiplication2.parameter1 instanceof Constant) == true)
            {
               return this.simplifySubtractionOfMultiplications(multiplication1.parameter1, (Constant) multiplication1.parameter2, (Constant) multiplication2.parameter1, multiplication2.parameter2);
            }

            if((multiplication2.parameter2 instanceof Constant) == true)
            {
               return this.simplifySubtractionOfMultiplications(multiplication1.parameter1, (Constant) multiplication1.parameter2, multiplication2.parameter1, (Constant) multiplication2.parameter2);
            }

            if(multiplication1.parameter1.equals(multiplication2) == true)
            {
               return Function.createMultiplication(new Constant(((Constant) multiplication1.parameter2).obtainRealValueNumber() - 1), multiplication2.parameter1.simplify(), multiplication2.parameter2.simplify());
            }
         }

         if((multiplication2.parameter1 instanceof Constant) == true)
         {
            if(multiplication2.parameter2.equals(multiplication1) == true)
            {
               return Function.createMultiplication(new Constant(1 - ((Constant) multiplication2.parameter1).obtainRealValueNumber()), multiplication1.parameter1.simplify(), multiplication1.parameter2.simplify());
            }
         }

         if((multiplication2.parameter2 instanceof Constant) == true)
         {
            if(multiplication2.parameter1.equals(multiplication1) == true)
            {
               return Function.createMultiplication(new Constant(1 - ((Constant) multiplication2.parameter2).obtainRealValueNumber()), multiplication1.parameter1.simplify(), multiplication1.parameter2.simplify());
            }
         }

         if((multiplication1.parameter1 instanceof Cosinus) == true)
         {
            if(((multiplication1.parameter2 instanceof Cosinus) == true) && ((multiplication2.parameter1 instanceof Sinus) == true) && ((multiplication2.parameter2 instanceof Sinus) == true))
            {
               return this.simplifySubtractionOfMultiplications((Cosinus) multiplication1.parameter1, (Cosinus) multiplication1.parameter2, (Sinus) multiplication2.parameter1, (Sinus) multiplication2.parameter2);
            }

            if(((multiplication1.parameter2 instanceof Sinus) == true) && ((multiplication2.parameter1 instanceof Cosinus) == true) && ((multiplication2.parameter2 instanceof Sinus) == true))
            {
               return this.simplifySubtractionOfMultiplications((Cosinus) multiplication1.parameter1, (Sinus) multiplication1.parameter2, (Cosinus) multiplication2.parameter1, (Sinus) multiplication2.parameter2);
            }

            if(((multiplication1.parameter2 instanceof Sinus) == true) && ((multiplication2.parameter1 instanceof Sinus) == true) && ((multiplication2.parameter2 instanceof Cosinus) == true))
            {
               return this.simplifySubtractionOfMultiplications((Cosinus) multiplication1.parameter1, (Sinus) multiplication1.parameter2, (Sinus) multiplication2.parameter1, (Cosinus) multiplication2.parameter2);
            }
         }

         if((multiplication1.parameter1 instanceof Sinus) == true)
         {
            if(((multiplication1.parameter2 instanceof Cosinus) == true) && ((multiplication2.parameter1 instanceof Cosinus) == true) && ((multiplication2.parameter2 instanceof Sinus) == true))
            {
               return this.simplifySubtractionOfMultiplications((Sinus) multiplication1.parameter1, (Cosinus) multiplication1.parameter2, (Cosinus) multiplication2.parameter1, (Sinus) multiplication2.parameter2);
            }

            if(((multiplication1.parameter2 instanceof Sinus) == true) && ((multiplication2.parameter1 instanceof Cosinus) == true) && ((multiplication2.parameter2 instanceof Cosinus) == true))
            {
               return this.simplifySubtractionOfMultiplications((Sinus) multiplication1.parameter1, (Sinus) multiplication1.parameter2, (Cosinus) multiplication2.parameter1, (Cosinus) multiplication2.parameter2);
            }

            if(((multiplication1.parameter2 instanceof Cosinus) == true) && ((multiplication2.parameter1 instanceof Sinus) == true) && ((multiplication2.parameter2 instanceof Cosinus) == true))
            {
               return this.simplifySubtractionOfMultiplications((Sinus) multiplication1.parameter1, (Cosinus) multiplication1.parameter2, (Sinus) multiplication2.parameter1, (Cosinus) multiplication2.parameter2);
            }
         }

         final Function function = this.simplifyMultipleMultiplication(multiplication1, multiplication2);

         if(function != null)
         {
            return function;
         }

         return this.simplifySubtractionOfMultiplications(multiplication1.parameter1, multiplication1.parameter2, multiplication2.parameter1, multiplication2.parameter2);
      }

      /**
       * Simplification : (X-Y)-Z -> (X-Z)-Y
       * 
       * @param subtraction
       *           Subtraction argument X-Y
       * @param function
       *           Function argument Z
       * @return Simplification
       */
      private Function simplify(final Subtraction subtraction, final Function function)
      {
         return new Subtraction(new Subtraction(subtraction.parameter1.simplify(), function.simplify()), subtraction.parameter2.simplify());
      }

      /**
       * Simplification : (X-Y)-(Z-A) -> (X-Z)+(A-Y)
       * 
       * @param subtraction1
       *           Subtraction argument X-Y
       * @param subtraction2
       *           Subtraction argument Z-A
       * @return Simplification
       */
      private Function simplify(final Subtraction subtraction1, final Subtraction subtraction2)
      {
         return Function.createAddition(new Subtraction(subtraction1.parameter1.simplify(), subtraction2.parameter1.simplify()), new Subtraction(subtraction2.parameter2.simplify(), subtraction1.parameter2.simplify()));
      }

      /**
       * Simplification : (X1 * ... * Xn * C1 * Y1 * ... * Ym) - (X1 * ... * Xn * C2 * Y1 * ... * Ym) -> C3 * X1 * ... * Xn * Y1
       * * ... * Ym
       * 
       * @param function1
       *           First function argument
       * @param function2
       *           Second function argument
       * @return Simplification or {@code null} if arguments not match to the simplification
       */
      private Function simplifyMultipleMultiplication(final Function function1, final Function function2)
      {
         Function[] multiplications1 = Multiplication.extractMultiplications(function1);
         Function[] multiplications2 = Multiplication.extractMultiplications(function2);

         if((multiplications1.length > 1) || (multiplications2.length > 1))
         {
            multiplications1 = Multiplication.compressConstant(multiplications1);
            multiplications2 = Multiplication.compressConstant(multiplications2);

            if((multiplications1.length > 1) || (multiplications2.length > 1))
            {
               double value;
               Function f1;
               Function f2;

               if(multiplications1[0] instanceof Constant)
               {
                  value = ((Constant) multiplications1[0]).obtainRealValueNumber();

                  f1 = Function.createMultiplication(Arrays.copyOfRange(multiplications1, 1, multiplications1.length));
               }
               else
               {
                  value = 1;

                  f1 = Function.createMultiplication(multiplications1);
               }

               if(multiplications2[0] instanceof Constant)
               {
                  value -= ((Constant) multiplications2[0]).obtainRealValueNumber();

                  f2 = Function.createMultiplication(Arrays.copyOfRange(multiplications2, 1, multiplications2.length));
               }
               else
               {
                  value -= 1;

                  f2 = Function.createMultiplication(multiplications2);
               }

               if(f1.equals(f2) == true)
               {
                  // (C1 * X) - (C2 * X) => C3 * X // Where C3 = C1-C2
                  // (C1 * X) - (X * C2) => C3 * X // Where C3 = C1-C2
                  // (X * C1) - (C2 * X) => C3 * X // Where C3 = C1-C2
                  // (X * C1) - (X * C2) => C3 * X // Where C3 = C1-C2
                  // (C1 * X) - X => C2 * X // Where C2 = C1-1
                  // (X * C1) - X => C2 * X // Where C2 = C1-1
                  // X - (C1 * X) => C2 * X // Where C2 = 1-C1
                  // X - (X * C1) => C2 * X // Where C2 = 1-C1
                  return Function.createMultiplication(new Constant(value), f1.simplify());
               }
            }
         }

         return null;
      }

      /**
       * Simplification :
       * <table border=1>
       * <tr>
       * <th>Function<br>
       * </th>
       * <th>Simplification<br>
       * </th>
       * </tr>
       * <tr>
       * <td>(C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;(&nbsp;C2&nbsp;*&nbsp;X&nbsp;)<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;(&nbsp;C2&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * <td>(C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;(&nbsp;C2&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param constant1
       *           Constant argument C1
       * @param function1
       *           Function argument X
       * @param constant2
       *           Constant argument C2
       * @param function2
       *           Function argument X or Y
       * @return Simplification
       */
      private Function simplifySubtractionOfMultiplications(final Constant constant1, final Function function1, final Constant constant2, final Function function2)
      {
         if((constant1.isUndefined() == true) || (constant2.isUndefined() == true))
         {
            return Constant.UNDEFINED;
         }

         if(function1.equals(function2) == true)
         {
            return new Multiplication(new Constant(constant1.obtainRealValueNumber() - constant2.obtainRealValueNumber()), function1.simplify());
         }

         if(constant1.isNul() == true)
         {
            return new Multiplication(new Constant(-constant2.obtainRealValueNumber()), function2.simplify());
         }

         if(constant2.isNul() == true)
         {
            return new Multiplication(constant1, function1.simplify());
         }

         return Function.createAddition(new Multiplication(new Constant(-constant2.obtainRealValueNumber()), function2.simplify()), new Multiplication(constant1, function1.simplify()));
      }

      /**
       * Simplification :
       * <table border=1>
       * <tr>
       * <th>Function<br>
       * </th>
       * <th>Simplification<br>
       * </th>
       * </tr>
       * <tr>
       * <td>(C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;(&nbsp;X&nbsp;*&nbsp;C2&nbsp;)<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;(&nbsp;Y&nbsp;*&nbsp;C2&nbsp;)<br>
       * </td>
       * <td>(C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;(&nbsp;C2&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param constant1
       *           Constant argument C1
       * @param function1
       *           Function argument X
       * @param function2
       *           Function argument X or Y
       * @param constant2
       *           Constant argument C2
       * @return Simplification
       */
      private Function simplifySubtractionOfMultiplications(final Constant constant1, final Function function1, final Function function2, final Constant constant2)
      {
         if((constant1.isUndefined() == true) || (constant2.isUndefined() == true))
         {
            return Constant.UNDEFINED;
         }

         if(function1.equals(function2) == true)
         {
            return new Multiplication(new Constant(constant1.obtainRealValueNumber() - constant2.obtainRealValueNumber()), function1.simplify());
         }

         if(constant1.isNul() == true)
         {
            return new Multiplication(new Constant(-constant2.obtainRealValueNumber()), function2.simplify());
         }

         if(constant2.isNul() == true)
         {
            return new Multiplication(constant1, function1.simplify());
         }

         return Function.createAddition(new Multiplication(new Constant(-constant2.obtainRealValueNumber()), function2.simplify()), new Multiplication(constant1, function1.simplify()));
      }

      /**
       * (cos(f1)*cos(f2))-(sin(f1)*sin(f2)) => cos(f1+f2)
       * 
       * @param cosinus1
       *           cos(f1)
       * @param cosinus2
       *           cos(f2)
       * @param sinus1
       *           sin(f1)
       * @param sinus2
       *           sin(f2)
       * @return cos(f1+f2)
       */
      private Function simplifySubtractionOfMultiplications(final Cosinus cosinus1, final Cosinus cosinus2, final Sinus sinus1, final Sinus sinus2)
      {
         if(((cosinus1.parameter.equals(sinus1.parameter) == true) && (cosinus2.parameter.equals(sinus2.parameter) == true))
               || ((cosinus1.parameter.equals(sinus2.parameter) == true) && (cosinus2.parameter.equals(sinus1.parameter) == true)))
         {
            return new Cosinus(Function.createAddition(cosinus1.parameter.simplify(), cosinus2.parameter.simplify()));
         }

         return new Subtraction(Function.createMultiplication(cosinus1.simplify(), cosinus2.simplify()), Function.createMultiplication(sinus1.simplify(), sinus2.simplify()));
      }

      /**
       * ((cos(f1)*sin(f2))-(cos(f2)*sin(f1)) => sin(f1-f2)
       * 
       * @param cosinus1
       *           cos(f1)
       * @param sinus1
       *           sin(f2)
       * @param cosinus2
       *           cos(f2)
       * @param sinus2
       *           sin(f1)
       * @return sin(f1-f2)
       */
      private Function simplifySubtractionOfMultiplications(final Cosinus cosinus1, final Sinus sinus1, final Cosinus cosinus2, final Sinus sinus2)
      {
         if((cosinus1.parameter.equals(sinus2.parameter) == true) && (sinus1.parameter.equals(cosinus2.parameter) == true))
         {
            return new Sinus(new Subtraction(sinus1.parameter.simplify(), cosinus1.parameter.simplify()));
         }

         return new Subtraction(Function.createMultiplication(cosinus1.simplify(), cosinus2.simplify()), Function.createMultiplication(sinus1.simplify(), sinus2.simplify()));
      }

      /**
       * ((cos(f1)*sin(f2))-(sin(f1)*cos(f2)) => sin(f1-f2)
       * 
       * @param cosinus1
       *           cos(f1)
       * @param sinus1
       *           sin(f2)
       * @param sinus2
       *           sin(f1)
       * @param cosinus2
       *           cos(f2)
       * @return sin(f1-f2)
       */
      private Function simplifySubtractionOfMultiplications(final Cosinus cosinus1, final Sinus sinus1, final Sinus sinus2, final Cosinus cosinus2)
      {
         if((cosinus1.parameter.equals(sinus2.parameter) == true) && (sinus1.parameter.equals(cosinus2.parameter) == true))
         {
            return new Sinus(new Subtraction(sinus1.parameter.simplify(), cosinus1.parameter.simplify()));
         }

         return new Subtraction(Function.createMultiplication(cosinus1.simplify(), cosinus2.simplify()), Function.createMultiplication(sinus1.simplify(), sinus2.simplify()));
      }

      /**
       * Simplification :
       * <table border=1>
       * <tr>
       * <th>Function<br>
       * </th>
       * <th>Simplification<br>
       * </th>
       * </tr>
       * <tr>
       * <td>(X&nbsp;*&nbsp;C1&nbsp;)&nbsp;-&nbsp;(C2&nbsp;*&nbsp;X&nbsp;)<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(X&nbsp;*&nbsp;C1&nbsp;)&nbsp;-&nbsp;(C2&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * <td>(C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;(&nbsp;C2&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param function1
       *           Function argument X
       * @param constant1
       *           Constant argument C1
       * @param constant2
       *           Constant argument C2
       * @param function2
       *           Function argument X or Y
       * @return Simplification
       */
      private Function simplifySubtractionOfMultiplications(final Function function1, final Constant constant1, final Constant constant2, final Function function2)
      {
         if((constant1.isUndefined() == true) || (constant2.isUndefined() == true))
         {
            return Constant.UNDEFINED;
         }

         if(function1.equals(function2) == true)
         {
            return new Multiplication(new Constant(constant1.obtainRealValueNumber() - constant2.obtainRealValueNumber()), function1.simplify());
         }

         if(constant1.isNul() == true)
         {
            return new Multiplication(new Constant(-constant2.obtainRealValueNumber()), function2.simplify());
         }

         if(constant2.isNul() == true)
         {
            return new Multiplication(constant1, function1.simplify());
         }

         return Function.createAddition(new Multiplication(new Constant(-constant2.obtainRealValueNumber()), function2.simplify()), new Multiplication(constant1, function1.simplify()));
      }

      /**
       * Simplification :
       * <table border=1>
       * <tr>
       * <th>Function<br>
       * </th>
       * <th>Simplification<br>
       * </th>
       * </tr>
       * <tr>
       * <td>(X&nbsp;*&nbsp;C1&nbsp;)&nbsp;-&nbsp;(&nbsp;X&nbsp;*&nbsp;C2&nbsp;)<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(X&nbsp;*&nbsp;C1&nbsp;)&nbsp;-&nbsp;(&nbsp;Y&nbsp;*&nbsp;C2&nbsp;)<br>
       * </td>
       * <td>(C1&nbsp;*&nbsp;X&nbsp;)&nbsp;-&nbsp;(&nbsp;C2&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param function1
       *           Function argument X
       * @param constant1
       *           Constant argument C1
       * @param function2
       *           Function argument X or Y
       * @param constant2
       *           Constant argument C2
       * @return Simplification
       */
      private Function simplifySubtractionOfMultiplications(final Function function1, final Constant constant1, final Function function2, final Constant constant2)
      {
         if((constant1.isUndefined() == true) || (constant2.isUndefined() == true))
         {
            return Constant.UNDEFINED;
         }

         if(function1.equals(function2) == true)
         {
            return new Multiplication(new Constant(constant1.obtainRealValueNumber() - constant2.obtainRealValueNumber()), function1.simplify());
         }

         if(constant1.isNul() == true)
         {
            return new Multiplication(new Constant(-constant2.obtainRealValueNumber()), function2.simplify());
         }

         if(constant2.isNul() == true)
         {
            return new Multiplication(constant1, function1.simplify());
         }

         return Function.createAddition(new Multiplication(new Constant(-constant2.obtainRealValueNumber()), function2.simplify()), new Multiplication(constant1, function1.simplify()));
      }

      /**
       * (f1*f2)-(f3*f4) => (f1*f2)-(f3*f4)
       * 
       * @param function1
       *           f1
       * @param function2
       *           f2
       * @param function3
       *           f3
       * @param function4
       *           f4
       * @return (f1*f2)-(f3*f4)
       */
      private Function simplifySubtractionOfMultiplications(final Function function1, final Function function2, final Function function3, final Function function4)
      {
         return new Subtraction(Function.createMultiplication(function1.simplify(), function2.simplify()), Function.createMultiplication(function3.simplify(), function4.simplify()));
      }

      /**
       * (sin(f1)*cos(f2))-(cos(f1)*sin(f2)) => sin(f1-f2)
       * 
       * @param sinus1
       *           sin(f1)
       * @param cosinus1
       *           cos(f2)
       * @param cosinus2
       *           cos(f1)
       * @param sinus2
       *           sin(f2)
       * @return sin(f1-f2)
       */
      private Function simplifySubtractionOfMultiplications(final Sinus sinus1, final Cosinus cosinus1, final Cosinus cosinus2, final Sinus sinus2)
      {
         if((cosinus1.parameter.equals(sinus2.parameter) == true) && (sinus1.parameter.equals(cosinus2.parameter) == true))
         {
            return new Sinus(new Subtraction(sinus1.parameter.simplify(), cosinus1.parameter.simplify()));
         }

         return new Subtraction(Function.createMultiplication(cosinus1.simplify(), cosinus2.simplify()), Function.createMultiplication(sinus1.simplify(), sinus2.simplify()));
      }

      /**
       * (sin(f1)*cos(f2))-(sin(f2)*cos(f1)) => sin(f1-f2)
       * 
       * @param sinus1
       *           sin(f1)
       * @param cosinus1
       *           cos(f2)
       * @param sinus2
       *           sin(f2)
       * @param cosinus2
       *           cos(f1)
       * @return sin(f1-f2)
       */
      private Function simplifySubtractionOfMultiplications(final Sinus sinus1, final Cosinus cosinus1, final Sinus sinus2, final Cosinus cosinus2)
      {
         if((cosinus1.parameter.equals(sinus2.parameter) == true) && (sinus1.parameter.equals(cosinus2.parameter) == true))
         {
            return new Sinus(new Subtraction(sinus1.parameter.simplify(), cosinus1.parameter.simplify()));
         }

         return new Subtraction(Function.createMultiplication(cosinus1.simplify(), cosinus2.simplify()), Function.createMultiplication(sinus1.simplify(), sinus2.simplify()));
      }

      /**
       * (sin(f1)*sin(f2))-(cos(f1)*cos(f2)) => -cos(f1+f2)
       * 
       * @param sinus1
       *           sin(f1)
       * @param sinus2
       *           sin(f2)
       * @param cosinus1
       *           cos(f1)
       * @param cosinus2
       *           cos(f2)
       * @return -cos(f1+f2)
       */
      private Function simplifySubtractionOfMultiplications(final Sinus sinus1, final Sinus sinus2, final Cosinus cosinus1, final Cosinus cosinus2)
      {
         if(((cosinus1.parameter.equals(sinus1.parameter) == true) && (cosinus2.parameter.equals(sinus2.parameter) == true))
               || ((cosinus1.parameter.equals(sinus2.parameter) == true) && (cosinus2.parameter.equals(sinus1.parameter) == true)))
         {
            return new MinusUnary(new Cosinus(Function.createAddition(cosinus1.parameter.simplify(), cosinus2.parameter.simplify())));
         }

         return new Subtraction(Function.createMultiplication(cosinus1.simplify(), cosinus2.simplify()), Function.createMultiplication(sinus1.simplify(), sinus2.simplify()));
      }

      /**
       * Simplify the subtraction <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @return Simplified function
       * @see jhelp.util.math.formal.FunctionSimplifier#simplify()
       */
      @Override
      public Function simplify()
      {
         final Function function1 = Subtraction.this.parameter1.simplify();
         final Function function2 = Subtraction.this.parameter2.simplify();

         if(function1.equals(function2) == true)
         {
            return Constant.ZERO;
         }

         if((function1 instanceof MinusUnary) == true)
         {
            if((function2 instanceof MinusUnary) == true)
            {
               return this.simplify((MinusUnary) function1, (MinusUnary) function2);
            }

            return this.simplify((MinusUnary) function1, function2);
         }

         if((function2 instanceof MinusUnary) == true)
         {
            return this.simplify(function1, (MinusUnary) function2);
         }

         if((function1 instanceof Constant) == true)
         {
            if((function2 instanceof Constant) == true)
            {
               return this.simplify((Constant) function1, (Constant) function2);
            }

            return this.simplify((Constant) function1, function2);
         }

         if((function2 instanceof Constant) == true)
         {
            return this.simplify(function1, (Constant) function2);
         }

         if((function1 instanceof Subtraction) == true)
         {
            if((function2 instanceof Subtraction) == true)
            {
               return this.simplify((Subtraction) function1, (Subtraction) function2);
            }

            return this.simplify((Subtraction) function1, function2);
         }

         if((function2 instanceof Subtraction) == true)
         {
            return this.simplify(function1, (Subtraction) function2);
         }

         if((function1 instanceof Multiplication) == true)
         {
            if((function2 instanceof Multiplication) == true)
            {
               return this.simplify((Multiplication) function1, (Multiplication) function2);
            }

            return this.simplify((Multiplication) function1, function2);
         }

         if((function2 instanceof Multiplication) == true)
         {
            return this.simplify(function1, (Multiplication) function2);
         }

         if((function1 instanceof Logarithm) == true)
         {
            if((function2 instanceof Logarithm) == true)
            {
               return this.simplify((Logarithm) function1, (Logarithm) function2);
            }
         }

         return this.simplify(function1, function2);
      }
   }

   /** Simplifier linked */
   private SubtractionSimplifier subtractionSimplifier;

   /**
    * Constructs the Subtraction
    * 
    * @param parameter1
    *           First parameter
    * @param parameter2
    *           Second parameter
    */
   public Subtraction(final Function parameter1, final Function parameter2)
   {
      super("-", parameter1, parameter2);
   }

   /**
    * Quickly compare with an other function <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param function
    *           Function to compare with
    * @return {@code true} if equals, {@code false} not sure about equality
    * @see Function#functionIsEqualsMoreSimple(Function)
    */
   @Override
   protected boolean functionIsEqualsMoreSimple(final Function function)
   {
      if(function == null)
      {
         return false;
      }

      if(function instanceof Subtraction)
      {
         final Subtraction subtraction = (Subtraction) function;

         if(this.parameter1.functionIsEqualsMoreSimple(subtraction.parameter1) == true)
         {
            return this.parameter2.functionIsEqualsMoreSimple(subtraction.parameter2);
         }
      }
      return false;
   }

   /**
    * Derive the function
    * 
    * @param variable
    *           Variable for derive
    * @return Derived
    * @see Function#derive(Variable)
    */
   @Override
   public Function derive(final Variable variable)
   {
      return new Subtraction(this.parameter1.derive(variable), this.parameter2.derive(variable));
   }

   /**
    * Indicates if a function is equals to this function
    * 
    * @param function
    *           Function tested
    * @return {@code true} if there sure equals. {@code false} dosen't mean not equals, but not sure about equality
    * @see Function#functionIsEquals(Function)
    */
   @Override
   public boolean functionIsEquals(final Function function)
   {
      if(function == null)
      {
         return false;
      }

      if(function instanceof Subtraction)
      {
         final Subtraction subtraction = (Subtraction) function;
         if(this.parameter1.functionIsEquals(subtraction.parameter1))
         {
            if(this.parameter2.functionIsEquals(subtraction.parameter2) == true)
            {
               return true;
            }
         }
      }

      return function.functionIsEquals(new Addition(this.parameter1, new MinusUnary(this.parameter2)));
   }

   /**
    * Copy the function
    * 
    * @return Copy
    * @see Function#getCopy()
    */
   @Override
   public Function getCopy()
   {
      return new Subtraction(this.parameter1.getCopy(), this.parameter2.getCopy());
   }

   /**
    * Simplifier attach to this subtraction <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Simplifier attach to this subtraction
    * @see Function#obtainFunctionSimplifier()
    */
   @Override
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.subtractionSimplifier == null)
      {
         this.subtractionSimplifier = new SubtractionSimplifier();
      }

      return this.subtractionSimplifier;
   }

   /**
    * Replace variable by function
    * 
    * @param variable
    *           Variable to replace
    * @param function
    *           Function for replace
    * @return Result function
    * @see Function#replace(Variable, Function)
    */
   @Override
   public Function replace(final Variable variable, final Function function)
   {
      return new Subtraction(this.parameter1.replace(variable, function), this.parameter2.replace(variable, function));
   }
}