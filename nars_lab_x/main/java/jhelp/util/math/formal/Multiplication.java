package jhelp.util.math.formal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 * Multiplication <br>
 * <br>
 * 
 * @author JHelp
 */
public class Multiplication
      extends BinaryOperator
{
   /**
    * Multiplication simplifier
    * 
    * @author JHelp
    */
   class MultiplicationSimplifier
         implements FunctionSimplifier
   {
      /**
       * Simplification : C1*C2 -> C3
       * 
       * @param constant1
       *           Constant argument C1
       * @param constant2
       *           Constant argument C2
       * @return Simplification C3
       */
      private Function simplify(final Constant constant1, final Constant constant2)
      {
         if((constant1.isUndefined() == true) || (constant2.isUndefined() == true))
         {
            return Constant.UNDEFINED;
         }

         return new Constant(constant1.obtainRealValueNumber() * constant2.obtainRealValueNumber());
      }

      /**
       * Simplification : C1*X -> C1*X
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
            return Constant.ZERO;
         }

         if(constant.isOne() == true)
         {
            return function.simplify();
         }

         if(constant.isMinusOne() == true)
         {
            return new MinusUnary(function.simplify());
         }

         return new Multiplication(constant, function.simplify());
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
       * <td>C1&nbsp;*&nbsp;(&nbsp;C2&nbsp;*&nbsp;X&nbsp;)<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>C1&nbsp;*&nbsp;(&nbsp;X&nbsp;*C2&nbsp;)<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>C1&nbsp;*&nbsp;(&nbsp;X&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * <td>C1&nbsp;*&nbsp;(&nbsp;X&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param constant
       *           Constant argument C1
       * @param multiplication
       *           Multiplication argument
       * @return Simplification
       */
      private Function simplify(final Constant constant, final Multiplication multiplication)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         if(constant.isNul() == true)
         {
            return Constant.ZERO;
         }

         if(constant.isOne() == true)
         {
            return multiplication.simplify();
         }

         if(constant.isMinusOne() == true)
         {
            return new MinusUnary(multiplication.simplify());
         }

         if((multiplication.parameter1 instanceof Constant) == true)
         {
            final Constant constant2 = (Constant) multiplication.parameter1;

            if(constant2.isUndefined() == true)
            {
               return Constant.UNDEFINED;
            }

            if(constant2.isNul() == true)
            {
               return Constant.ZERO;
            }

            return new Multiplication(new Constant(constant.obtainRealValueNumber() * constant2.obtainRealValueNumber()), multiplication.parameter2.simplify());
         }

         if((multiplication.parameter2 instanceof Constant) == true)
         {
            final Constant constant2 = (Constant) multiplication.parameter2;

            if(constant2.isUndefined() == true)
            {
               return Constant.UNDEFINED;
            }

            if(constant2.isNul() == true)
            {
               return Constant.ZERO;
            }

            return new Multiplication(new Constant(constant.obtainRealValueNumber() * constant2.obtainRealValueNumber()), multiplication.parameter1.simplify());
         }

         return new Multiplication(constant, multiplication.simplify());
      }

      /**
       * Simplification : (X/Y)*(Z/A) -> (X*Z)/(Y*A)
       * 
       * @param division1
       *           Division argument X/Y
       * @param division2
       *           Division argument Z/A
       * @return Simplification
       */
      private Function simplify(final Division division1, final Division division2)
      {
         return new Division(Function.createMultiplication(division1.parameter1.simplify(), division2.parameter1.simplify()), Function.createMultiplication(division1.parameter2.simplify(), division2.parameter2.simplify()));
      }

      /**
       * Simplification : (X/Y)*Z -> (X*Z)/Y
       * 
       * @param division
       *           Division argument X/Y
       * @param function
       *           Function argument Z
       * @return Simplification
       */
      private Function simplify(final Division division, final Function function)
      {
         return new Division(Function.createMultiplication(division.parameter1.simplify(), function.simplify()), division.parameter2.simplify());
      }

      /**
       * Simplification : exp(X)*exp(Y) -> exp(X+Y)
       * 
       * @param exponential1
       *           Exponential argument exp(X)
       * @param exponential2
       *           Exponential argument exp(Y)
       * @return Simplification
       */
      private Function simplify(final Exponential exponential1, final Exponential exponential2)
      {
         return new Exponential(Function.createAddition(exponential2.parameter.simplify(), exponential1.parameter.simplify()));
      }

      /**
       * Simplification : X*C1 -> C1*X
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
            return Constant.ZERO;
         }

         if(constant.isOne() == true)
         {
            return function.simplify();
         }

         if(constant.isMinusOne() == true)
         {
            return new MinusUnary(function.simplify());
         }

         return new Multiplication(constant, function.simplify());
      }

      /**
       * Simplification : X*(Y/Z) -> (X*Z)/Z
       * 
       * @param function
       *           function argument X
       * @param division
       *           Division argument Y/Z
       * @return Simplification
       */
      private Function simplify(final Function function, final Division division)
      {
         return new Division(Function.createMultiplication(function.simplify(), division.parameter1.simplify()), division.parameter2.simplify());
      }

      /**
       * Simplification : X*Y -> Y*X
       * 
       * @param function1
       *           Function argument X
       * @param function2
       *           Function argument Y
       * @return Simplification
       */
      private Function simplify(final Function function1, final Function function2)
      {
         final Function[] array = Multiplication.extractMultiplicationParameters(Multiplication.this);
         final Function[] compress = Multiplication.compressConstant(array);

         if(array != compress)
         {
            return Function.createMultiplication(compress);
         }

         return Function.createMultiplication(function2.simplify(), function1.simplify());
      }

      /**
       * Simplification X*(-Y) -> -(X*Y)
       * 
       * @param function
       *           Function argument X
       * @param minusUnary
       *           Minus unary argument -Y
       * @return Simplification
       */
      private Function simplify(final Function function, final MinusUnary minusUnary)
      {
         return new MinusUnary(Function.createMultiplication(function.simplify(), minusUnary.parameter.simplify()));
      }

      /**
       * Simplification : (-X)*Y -> -(X*Y)
       * 
       * @param minusUnary
       *           Minus argument -X
       * @param function
       *           Function argument Y
       * @return Simplification
       */
      private Function simplify(final MinusUnary minusUnary, final Function function)
      {
         return new MinusUnary(Function.createMultiplication(function.simplify(), minusUnary.parameter.simplify()));
      }

      /**
       * Simplification (-X)*(-Y) -> X*Y
       * 
       * @param minusUnary1
       *           Minus unary argument -X
       * @param minusUnary2
       *           Minus unary argument -Y
       * @return Simplification
       */
      private Function simplify(final MinusUnary minusUnary1, final MinusUnary minusUnary2)
      {
         return Function.createMultiplication(minusUnary2.parameter.simplify(), minusUnary1.parameter.simplify());
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
       * <td>(&nbsp;C1&nbsp;*&nbsp;X&nbsp;)&nbsp;*&nbsp;C2<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>(&nbsp;X&nbsp;*&nbsp;C1&nbsp;)&nbsp;*&nbsp;C2<br>
       * </td>
       * <td>C3&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>C1&nbsp;*&nbsp;(&nbsp;X&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * <td>C1&nbsp;*&nbsp;(&nbsp;X&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param multiplication
       *           Multiplication argument
       * @param constant
       *           Constant argument C1
       * @return Simplification
       */
      private Function simplify(final Multiplication multiplication, final Constant constant)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         if(constant.isNul() == true)
         {
            return Constant.ZERO;
         }

         if(constant.isOne() == true)
         {
            return multiplication.simplify();
         }

         if(constant.isMinusOne() == true)
         {
            return new MinusUnary(multiplication.simplify());
         }

         if((multiplication.parameter1 instanceof Constant) == true)
         {
            final Constant constant2 = (Constant) multiplication.parameter1;

            if(constant2.isUndefined() == true)
            {
               return Constant.UNDEFINED;
            }

            if(constant2.isNul() == true)
            {
               return Constant.ZERO;
            }

            return new Multiplication(new Constant(constant.obtainRealValueNumber() * constant2.obtainRealValueNumber()), multiplication.parameter2.simplify());
         }

         if((multiplication.parameter2 instanceof Constant) == true)
         {
            final Constant constant2 = (Constant) multiplication.parameter2;

            if(constant2.isUndefined() == true)
            {
               return Constant.UNDEFINED;
            }

            if(constant2.isNul() == true)
            {
               return Constant.ZERO;
            }

            return new Multiplication(new Constant(constant.obtainRealValueNumber() * constant2.obtainRealValueNumber()), multiplication.parameter1.simplify());
         }

         return new Multiplication(constant, multiplication.simplify());
      }

      /**
       * Simplification of the multiplication <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @return Simplification
       * @see jhelp.util.math.formal.FunctionSimplifier#simplify()
       */
      @Override
      public Function simplify()
      {
         final Function function1 = Multiplication.this.parameter1.simplify();
         final Function function2 = Multiplication.this.parameter2.simplify();

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

            if((function2 instanceof Multiplication) == true)
            {
               return this.simplify((Constant) function1, (Multiplication) function2);
            }

            return this.simplify((Constant) function1, function2);
         }

         if((function2 instanceof Constant) == true)
         {
            if((function1 instanceof Multiplication) == true)
            {
               return this.simplify((Multiplication) function1, (Constant) function2);
            }

            return this.simplify(function1, (Constant) function2);
         }

         if((function1 instanceof Division) == true)
         {
            if((function2 instanceof Division) == true)
            {
               return this.simplify((Division) function1, (Division) function2);
            }

            return this.simplify((Division) function1, function2);
         }

         if(((function1 instanceof Exponential) == true) && ((function2 instanceof Exponential) == true))
         {
            return this.simplify((Exponential) function1, (Exponential) function2);
         }

         if((function2 instanceof Division) == true)
         {
            return this.simplify(function1, (Division) function2);
         }

         return this.simplify(function1, function2);
      }
   }

   /**
    * Compress constant of multiplication list
    * 
    * @param array
    *           Array to compress
    * @return Compressed array
    */
   @SuppressWarnings("unchecked")
   static Function[] compressConstant(final Function[] array)
   {
      if(array.length <= 1)
      {
         return array;
      }

      Arrays.sort(array, Function.COMPARATOR);

      if(((array[0] instanceof Constant) == false) || ((array[1] instanceof Constant) == false))
      {
         return array;
      }

      final int length = array.length;
      int index = 2;

      double value = ((Constant) array[0]).obtainRealValueNumber() * ((Constant) array[1]).obtainRealValueNumber();

      while((index < length) && ((array[index] instanceof Constant) == true))
      {
         value *= ((Constant) array[index]).obtainRealValueNumber();

         index++;
      }

      if(index >= length)
      {
         return new Function[]
         {
            new Constant(value)
         };
      }

      final Function[] result = new Function[(length - index) + 1];

      result[0] = new Constant(value);
      System.arraycopy(array, index, result, 1, length - index);

      return result;
   }

   /**
    * Extract real multiplication parameters
    * 
    * @param multiplication
    *           Multiplication where extract
    * @return Real multiplication parameters
    */
   static Function[] extractMultiplicationParameters(Multiplication multiplication)
   {
      final ArrayList<Function> list = new ArrayList<Function>();

      final Stack<Multiplication> stack = new Stack<Multiplication>();
      stack.push(multiplication);

      while(stack.isEmpty() == false)
      {
         multiplication = stack.pop();

         if(multiplication.parameter1 instanceof Multiplication)
         {
            stack.push((Multiplication) multiplication.parameter1);
         }
         else
         {
            list.add(multiplication.parameter1);
         }

         if(multiplication.parameter2 instanceof Multiplication)
         {
            stack.push((Multiplication) multiplication.parameter2);
         }
         else
         {
            list.add(multiplication.parameter2);
         }
      }

      return list.toArray(new Function[list.size()]);
   }

   /**
    * Extract all multiplication parameters
    * 
    * @param function
    *           Function where extract parameters
    * @return Extracted parameters
    */
   static Function[] extractMultiplications(final Function function)
   {
      if(function instanceof Multiplication)
      {
         return Multiplication.extractMultiplicationParameters((Multiplication) function);
      }

      return new Function[]
      {
         function
      };
   }

   /** Multiplication simplifier */
   private MultiplicationSimplifier multiplicationSimplifier;

   /**
    * Constructs the multiplication
    * 
    * @param parameter1
    *           First parameter
    * @param parameter2
    *           Second parameter
    */
   public Multiplication(final Function parameter1, final Function parameter2)
   {
      super("*", parameter1, parameter2);
   }

   /**
    * Indicates if function is equals, the equality test is more simple than {@link #functionIsEquals(Function)} its use
    * internally for {@link Function#simplifyMaximum()} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param function
    *           Function to compare with
    * @return {@code true} if equals
    * @see Function#functionIsEqualsMoreSimple(Function)
    */
   @Override
   protected boolean functionIsEqualsMoreSimple(final Function function)
   {
      if(function == null)
      {
         return false;
      }

      if(function instanceof Multiplication)
      {
         final Multiplication multiplication = (Multiplication) function;

         if(this.parameter1.functionIsEqualsMoreSimple(multiplication.parameter1) == true)
         {
            return this.parameter2.functionIsEqualsMoreSimple(multiplication.parameter2);
         }

         if(this.parameter1.functionIsEqualsMoreSimple(multiplication.parameter2) == true)
         {
            return this.parameter2.functionIsEqualsMoreSimple(multiplication.parameter1);
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
      final Function d1 = this.parameter1.derive(variable);
      final Function d2 = this.parameter2.derive(variable);
      return Function.createAddition(Function.createMultiplication(d1, this.parameter2), Function.createMultiplication(this.parameter1, d2));
   }

   /**
    * Indicates if a function is equals to this function
    * 
    * @param function
    *           Function tested
    * @return {@code true} if there sure equals. {@code false} dosen't mean not equals, but not sure about equality
    * @see Function#functionIsEquals(Function)
    */
   @SuppressWarnings("unchecked")
   @Override
   public boolean functionIsEquals(final Function function)
   {
      if(function == null)
      {
         return false;
      }

      if(function instanceof Multiplication)
      {
         final Multiplication multiplication = (Multiplication) function;

         final Function[] thisParameters = Multiplication.extractMultiplicationParameters(this);
         final Function[] otherParameters = Multiplication.extractMultiplicationParameters(multiplication);

         Arrays.sort(thisParameters, Function.COMPARATOR);
         Arrays.sort(otherParameters, Function.COMPARATOR);

         return Arrays.equals(thisParameters, otherParameters);
      }
      return false;
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
      return Function.createMultiplication(this.parameter1.getCopy(), this.parameter2.getCopy());
   }

   /**
    * Multiplication simplifier <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Multiplication simplifier
    * @see Function#obtainFunctionSimplifier()
    */
   @Override
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.multiplicationSimplifier == null)
      {
         this.multiplicationSimplifier = new MultiplicationSimplifier();
      }

      return this.multiplicationSimplifier;
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
      return Function.createMultiplication(this.parameter1.replace(variable, function), this.parameter2.replace(variable, function));
   }
}