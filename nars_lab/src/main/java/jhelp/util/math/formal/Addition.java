package jhelp.util.math.formal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 * Represents an addition <br>
 * <br>
 * 
 * @author JHelp
 */
public class Addition
      extends BinaryOperator
{
   /**
    * Addition simplifier
    * 
    * @author JHelp
    */
   class AdditionSimplifier
         implements FunctionSimplifier
   {
      /**
       * Try to "compress" the addition.<br>
       * f1+C1+C2+f2+C3 => C4+f1+f2 (by example)
       * 
       * @return Compressed function or {@code null} if compression not happen
       */
      private Function compress()
      {
         final Function[] array = Addition.extractAdditionParameters(Addition.this);
         final Function[] compress = Addition.compressConstant(array);

         if(array != compress)
         {
            return Function.createAddition(compress);
         }

         return null;
      }

      /**
       * (f1 + f2) + f3 => f2 + (f1 + f3)
       * 
       * @param addition
       *           (f1 + f2)
       * @param function
       *           f3
       * @return f2 + (f1 + f3)
       */
      private Function simplify(final Addition addition, final Function function)
      {
         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return new Addition(addition.getParameter2().simplify(), new Addition(addition.getParameter1().simplify(), function.simplify()));
      }

      /**
       * C1 + C2 => C3
       * 
       * @param constant1
       *           C1
       * @param constant2
       *           C2
       * @return C3
       */
      private Function simplify(final Constant constant1, final Constant constant2)
      {
         if((constant1.isUndefined() == true) || (constant2.isUndefined() == true))
         {
            return Constant.UNDEFINED;
         }

         return new Constant(constant1.obtainRealValueNumber() + constant2.obtainRealValueNumber());
      }

      /**
       * Simplify C + f
       * 
       * @param constant
       *           C
       * @param function
       *           f
       * @return Simplified function
       */
      private Function simplify(final Constant constant, final Function function)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         if(constant.isNul() == true)
         {
            return function.simplify();
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return new Addition(constant, function.simplify());
      }

      /**
       * f1 + (f2 + f3) => f3 + ( f2 + f1 )
       * 
       * @param function
       *           f1
       * @param addition
       *           f2 + f3
       * @return f3 + ( f2 + f1 )
       */
      private Function simplify(final Function function, final Addition addition)
      {
         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return new Addition(addition.getParameter2().simplify(), new Addition(addition.getParameter1().simplify(), function.simplify()));
      }

      /**
       * f + C => C + f
       * 
       * @param function
       *           f
       * @param constant
       *           C
       * @return C + f
       */
      private Function simplify(final Function function, final Constant constant)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         if(constant.isNul() == true)
         {
            return function.simplify();
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return new Addition(constant, function.simplify());
      }

      /**
       * Tries to simplify the addition of 2 functions
       * 
       * @param function1
       *           Function 1
       * @param function2
       *           Function 2
       * @return Simplified function
       */
      private Function simplify(final Function function1, final Function function2)
      {
         if(function1.equals(function2) == true)
         {
            return new Multiplication(Constant.TWO, function1.simplify());
         }

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
                  value += ((Constant) multiplications2[0]).obtainRealValueNumber();

                  f2 = Function.createMultiplication(Arrays.copyOfRange(multiplications2, 1, multiplications2.length));
               }
               else
               {
                  value += 1;

                  f2 = Function.createMultiplication(multiplications2);
               }

               if(f1.equals(f2) == true)
               {
                  // (C1 * X) + (C2 * X) => C3 * X // Where C3 = C1+C2
                  // (C1 * X) + (X * C2) => C3 * X // Where C3 = C1+C2
                  // (X * C1) + (C2 * X) => C3 * X // Where C3 = C1+C2
                  // (X * C1) + (X * C2) => C3 * X // Where C3 = C1+C2
                  // (C1 * X) + X => C2 * X // Where C2 = C1+1
                  // (X * C1) + X => C2 * X // Where C2 = C1+1
                  // X + (C1 * X) => C2 * X // Where C2 = C1+1
                  // X + (X * C1) => C2 * X // Where C2 = C1+1
                  return Function.createMultiplication(new Constant(value), f1.simplify());
               }
            }
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         if((function1 instanceof Subtraction) == true)
         {
            if((function2 instanceof Subtraction) == true)
            {
               return this.simplify((Subtraction) function1, (Subtraction) function2);
            }

            return this.simplify((Subtraction) function1, function2);
         }

         if((function1 instanceof Addition) == true)
         {
            return this.simplify((Addition) function1, function2);
         }

         if((function1 instanceof Multiplication) == true)
         {
            if((function2 instanceof Multiplication) == true)
            {
               return this.simplify((Multiplication) function1, (Multiplication) function2);
            }

            return this.simplify((Multiplication) function1, function2);
         }

         if((function2 instanceof Addition) == true)
         {
            return this.simplify(function1, (Addition) function2);
         }

         if((function2 instanceof Subtraction) == true)
         {
            return this.simplify(function1, (Subtraction) function2);
         }

         if((function2 instanceof Multiplication) == true)
         {
            return this.simplify(function1, (Multiplication) function2);
         }

         return Function.createAddition(function2.simplify(), function1.simplify());
      }

      /**
       * f1 + (- f2) => f1 - f2
       * 
       * @param function
       *           f1
       * @param minusUnary
       *           -f2
       * @return f1 - f2
       */
      private Function simplify(final Function function, final MinusUnary minusUnary)
      {
         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return new Subtraction(function.simplify(), minusUnary.getParameter().simplify());
      }

      /**
       * Try to simplify f1 + (f2 * f3)
       * 
       * @param function
       *           f1
       * @param multiplication
       *           f2 * f3
       * @return Simplified function
       */
      private Function simplify(final Function function, final Multiplication multiplication)
      {
         return this.simplifyAdditonOfMultiplicationAndFunction(multiplication.parameter1, multiplication.parameter2, function);
      }

      /**
       * f1 + (f2 - f3) => (f2 + f1) - f3
       * 
       * @param function
       *           f1
       * @param subtraction
       *           f2 - f3
       * @return (f2 + f1) - f3
       */
      private Function simplify(final Function function, final Subtraction subtraction)
      {
         if(subtraction.parameter2.equals(function) == true)
         {
            return subtraction.parameter1.simplify();
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return new Subtraction(Function.createAddition(subtraction.parameter1.simplify(), function.simplify()), subtraction.parameter2.simplify());
      }

      /**
       * ln(f1) + ln(f2) => ln(f1 * f2)
       * 
       * @param logarithm1
       *           ln(f1)
       * @param logarithm2
       *           ln(f12)
       * @return ln(f1 * f2)
       */
      private Function simplify(final Logarithm logarithm1, final Logarithm logarithm2)
      {
         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return new Logarithm(Function.createMultiplication(logarithm1.getParameter().simplify(), logarithm2.getParameter().simplify()));
      }

      /**
       * (- f1) + f2 => f2 - f1
       * 
       * @param minusUnary
       *           -f1
       * @param function
       *           f2
       * @return f2 - f1
       */
      private Function simplify(final MinusUnary minusUnary, final Function function)
      {
         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return new Subtraction(function.simplify(), minusUnary.getParameter().simplify());
      }

      /**
       * (- f1) + (- f2) => - (f1 + f2)
       * 
       * @param minusUnary1
       *           -f1
       * @param minusUnary2
       *           -f2
       * @return - (f1 + f2)
       */
      private Function simplify(final MinusUnary minusUnary1, final MinusUnary minusUnary2)
      {
         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return new MinusUnary(new Addition(minusUnary1.getParameter().simplify(), minusUnary2.getParameter().simplify()));
      }

      /**
       * Try to simplify (f1 * f2) + f3
       * 
       * @param multiplication
       *           f1 * f2
       * @param function
       *           f3
       * @return Simplified function
       */
      private Function simplify(final Multiplication multiplication, final Function function)
      {
         return this.simplifyAdditonOfMultiplicationAndFunction(multiplication.parameter1, multiplication.parameter2, function);
      }

      /**
       * Try to simplify (f1 * f2) + (f3 * f4)
       * 
       * @param multiplication1
       *           f1 * f2
       * @param multiplication2
       *           f3 * f4
       * @return Simplified function
       */
      private Function simplify(final Multiplication multiplication1, final Multiplication multiplication2)
      {
         final Function function1 = multiplication1.parameter1.simplify();
         final Function function2 = multiplication1.parameter2.simplify();
         final Function function3 = multiplication2.parameter1.simplify();
         final Function function4 = multiplication2.parameter2.simplify();

         if((function1 instanceof Cosinus) == true)
         {
            if(((function2 instanceof Cosinus) == true) && ((function3 instanceof Sinus) == true) && ((function4 instanceof Sinus) == true))
            {
               return this.simplifyAdditionOfMultiplcations((Cosinus) function1, (Cosinus) function2, (Sinus) function3, (Sinus) function4);
            }

            if(((function2 instanceof Sinus) == true) && ((function3 instanceof Cosinus) == true) && ((function4 instanceof Sinus) == true))
            {
               return this.simplifyAdditionOfMultiplcations((Cosinus) function1, (Sinus) function2, (Cosinus) function3, (Sinus) function4);
            }

            if(((function2 instanceof Sinus) == true) && ((function3 instanceof Sinus) == true) && ((function4 instanceof Cosinus) == true))
            {
               return this.simplifyAdditionOfMultiplcations((Cosinus) function1, (Sinus) function2, (Sinus) function3, (Cosinus) function4);
            }
         }

         if((function1 instanceof Sinus) == true)
         {
            if(((function2 instanceof Cosinus) == true) && ((function3 instanceof Sinus) == true) && ((function4 instanceof Cosinus) == true))
            {
               return this.simplifyAdditionOfMultiplcations((Sinus) function1, (Cosinus) function2, (Sinus) function3, (Cosinus) function4);
            }

            if(((function2 instanceof Cosinus) == true) && ((function3 instanceof Cosinus) == true) && ((function4 instanceof Sinus) == true))
            {
               return this.simplifyAdditionOfMultiplcations((Sinus) function1, (Cosinus) function2, (Cosinus) function3, (Sinus) function4);
            }

            if(((function2 instanceof Sinus) == true) && ((function3 instanceof Cosinus) == true) && ((function4 instanceof Cosinus) == true))
            {
               return this.simplifyAdditionOfMultiplcations((Sinus) function1, (Sinus) function2, (Cosinus) function3, (Cosinus) function4);
            }
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return this.simplifyAdditionOfMultiplcations(function1, function2, function3, function4);
      }

      /**
       * (f1 - f2) + f3 => (f3 - f2) + f1
       * 
       * @param subtraction
       *           f1 - f2
       * @param function
       *           f3
       * @return (f3 - f2) + f1
       */
      private Function simplify(final Subtraction subtraction, final Function function)
      {
         if(subtraction.parameter2.equals(function) == true)
         {
            return subtraction.parameter1.simplify();
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return new Subtraction(Function.createAddition(function.simplify(), subtraction.parameter1.simplify()), subtraction.parameter2.simplify());
      }

      /**
       * (f1 - f2) + (f3 - f4) => (f1 + f3) - (f2 + f4)
       * 
       * @param subtraction1
       *           f1 - f2
       * @param subtraction2
       *           f3 - f4
       * @return (f1 + f3) - (f2 + f4)
       */
      private Function simplify(final Subtraction subtraction1, final Subtraction subtraction2)
      {
         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return new Subtraction(Function.createAddition(subtraction1.parameter1.simplify(), subtraction2.parameter1.simplify()).simplify(), Function.createAddition(subtraction1.parameter2.simplify(),
               subtraction2.parameter2.simplify()).simplify());
      }

      /**
       * Tries to simplify (cos(f1) * cos(f2)) + (sin(f3) * sin(f4))
       * 
       * @param cosinus1
       *           cos(f1)
       * @param cosinus2
       *           cos(f2)
       * @param sinus1
       *           sin(f3)
       * @param sinus2
       *           sin(f4)
       * @return Simplified function
       */
      private Function simplifyAdditionOfMultiplcations(final Cosinus cosinus1, final Cosinus cosinus2, final Sinus sinus1, final Sinus sinus2)
      {
         if(((cosinus1.parameter.equals(sinus1.parameter) == true) && (cosinus2.parameter.equals(sinus2.parameter) == true))
               || ((cosinus1.parameter.equals(sinus2.parameter) == true) && (cosinus2.parameter.equals(sinus1.parameter) == true)))
         {
            return new Cosinus(new Subtraction(cosinus1.parameter.simplify(), cosinus2.parameter.simplify()));
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return Function.createAddition(Function.createMultiplication(sinus2.simplify(), sinus1.simplify()), Function.createMultiplication(cosinus2.simplify(), cosinus1.simplify()));
      }

      /**
       * Tries to simplify (cos(f1) * sin(f2)) + (c(f3) * sin(f4))
       * 
       * @param cosinus1
       *           cos(f1)
       * @param sinus1
       *           sin(f2)
       * @param cosinus2
       *           cos(f3)
       * @param sinus2
       *           sin(f4)
       * @return Simplified function
       */
      private Function simplifyAdditionOfMultiplcations(final Cosinus cosinus1, final Sinus sinus1, final Cosinus cosinus2, final Sinus sinus2)
      {
         if((cosinus1.parameter.equals(sinus2.parameter) == true) && (sinus1.parameter.equals(cosinus2.parameter) == true))
         {
            return new Sinus(Function.createAddition(cosinus1.parameter.simplify(), sinus1.parameter.simplify()));
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return Function.createAddition(Function.createMultiplication(sinus2.parameter.simplify(), cosinus2.parameter.simplify()), Function.createMultiplication(sinus1.parameter.simplify(), cosinus1.parameter.simplify()));
      }

      /**
       * Tries to simplify (cos(f1) * sin(f2)) + (sin(f3) * cos(f4))
       * 
       * @param cosinus1
       *           cos(f1)
       * @param sinus1
       *           sin(f2)
       * @param sinus2
       *           sin(f3)
       * @param cosinus2
       *           cos(f4)
       * @return Simplified function
       */
      private Function simplifyAdditionOfMultiplcations(final Cosinus cosinus1, final Sinus sinus1, final Sinus sinus2, final Cosinus cosinus2)
      {
         if((cosinus1.parameter.equals(sinus2.parameter) == true) && (sinus1.parameter.equals(cosinus2.parameter) == true))
         {
            return new Sinus(Function.createAddition(cosinus1.parameter.simplify(), sinus1.parameter.simplify()));
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return Function.createAddition(Function.createMultiplication(sinus2.parameter.simplify(), cosinus2.parameter.simplify()), Function.createMultiplication(sinus1.parameter.simplify(), cosinus1.parameter.simplify()));
      }

      /**
       * Tries to simplify (f1 * f2) + (f3 * f4)
       * 
       * @param function1
       *           f1
       * @param function2
       *           f2
       * @param function3
       *           f3
       * @param function4
       *           f4
       * @return Simplified function
       */
      private Function simplifyAdditionOfMultiplcations(final Function function1, final Function function2, final Function function3, final Function function4)
      {
         if(function1.equals(function3) == true)
         {
            return Function.createMultiplication(Function.createAddition(function2.simplify(), function4.simplify()), function1.simplify());
         }

         if(function1.equals(function4) == true)
         {
            return Function.createMultiplication(Function.createAddition(function2.simplify(), function3.simplify()), function1.simplify());
         }

         if(function2.equals(function3) == true)
         {
            return Function.createMultiplication(Function.createAddition(function1.simplify(), function4.simplify()), function2.simplify());
         }

         if(function2.equals(function4) == true)
         {
            return Function.createMultiplication(Function.createAddition(function1.simplify(), function3.simplify()), function2.simplify());
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return Function.createAddition(Function.createMultiplication(function4.simplify(), function3.simplify()), Function.createMultiplication(function2.simplify(), function1.simplify()));
      }

      /**
       * Tries to simplify (sin(f1) * cos(f2)) + (cos(f3) * sin(f4))
       * 
       * @param sinus1
       *           sin(f1)
       * @param cosinus1
       *           cos(f2)
       * @param cosinus2
       *           cos(f3)
       * @param sinus2
       *           sin(f4)
       * @return Simplified function
       */
      private Function simplifyAdditionOfMultiplcations(final Sinus sinus1, final Cosinus cosinus1, final Cosinus cosinus2, final Sinus sinus2)
      {
         if((cosinus1.parameter.equals(sinus2.parameter) == true) && (sinus1.parameter.equals(cosinus2.parameter) == true))
         {
            return new Sinus(Function.createAddition(cosinus1.parameter.simplify(), sinus1.parameter.simplify()));
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return Function.createAddition(Function.createMultiplication(sinus2.parameter.simplify(), cosinus2.parameter.simplify()), Function.createMultiplication(sinus1.parameter.simplify(), cosinus1.parameter.simplify()));
      }

      /**
       * Tries to simplify (sin(f1) * cos(f2)) + (sin(f3) * cos(f4))
       * 
       * @param sinus1
       *           sin(f1)
       * @param cosinus1
       *           cos(f2)
       * @param sinus2
       *           sin(f3)
       * @param cosinus2
       *           cos(f4)
       * @return Simplified function
       */
      private Function simplifyAdditionOfMultiplcations(final Sinus sinus1, final Cosinus cosinus1, final Sinus sinus2, final Cosinus cosinus2)
      {
         if((cosinus1.parameter.equals(sinus2.parameter) == true) && (sinus1.parameter.equals(cosinus2.parameter) == true))
         {
            return new Sinus(Function.createAddition(cosinus1.parameter.simplify(), sinus1.parameter.simplify()));
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return Function.createAddition(Function.createMultiplication(sinus2.parameter.simplify(), cosinus2.parameter.simplify()), Function.createMultiplication(sinus1.parameter.simplify(), cosinus1.parameter.simplify()));
      }

      /**
       * Tries to simplify (sin(f1) * sin(f2)) + (cos(f3) * cos(f4))
       * 
       * @param sinus1
       *           sin(f1)
       * @param sinus2
       *           sin(f2)
       * @param cosinus1
       *           cos(f3)
       * @param cosinus2
       *           cos(f4)
       * @return Simplified function
       */
      private Function simplifyAdditionOfMultiplcations(final Sinus sinus1, final Sinus sinus2, final Cosinus cosinus1, final Cosinus cosinus2)
      {
         if(((cosinus1.parameter.equals(sinus1.parameter) == true) && (cosinus2.parameter.equals(sinus2.parameter) == true))
               || ((cosinus1.parameter.equals(sinus2.parameter) == true) && (cosinus2.parameter.equals(sinus1.parameter) == true)))
         {
            return new Cosinus(new Subtraction(cosinus1.parameter.simplify(), cosinus2.parameter.simplify()));
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return Function.createAddition(Function.createMultiplication(sinus2.simplify(), sinus1.simplify()), Function.createMultiplication(cosinus2.simplify(), cosinus1.simplify()));
      }

      /**
       * Tries to simplify (f1 * f2) + f3
       * 
       * @param parameter1
       *           f1
       * @param parameter2
       *           f2
       * @param function
       *           f3
       * @return Simplified function
       */
      private Function simplifyAdditonOfMultiplicationAndFunction(final Function parameter1, final Function parameter2, final Function function)
      {
         if(parameter1.equals(function) == true)
         {
            return Function.createMultiplication(Function.createAddition(Constant.ONE, parameter2.simplify()), function.simplify());
         }

         if(parameter2.equals(function) == true)
         {
            return Function.createMultiplication(Function.createAddition(Constant.ONE, parameter1.simplify()), function.simplify());
         }

         final Function compress = this.compress();
         if(compress != null)
         {
            return compress;
         }

         return Function.createAddition(function.simplify(), Function.createMultiplication(parameter1.simplify(), parameter2.simplify()));
      }

      /**
       * Simplify the addition <br>
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
         final Function function1 = Addition.this.parameter1.simplify();
         final Function function2 = Addition.this.parameter2.simplify();

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

         if(((function1 instanceof Logarithm) == true) && ((function2 instanceof Logarithm) == true))
         {
            return this.simplify((Logarithm) function1, (Logarithm) function2);
         }

         return this.simplify(function1, function2);
      }
   }

   /**
    * Compress constant of addition list
    * 
    * @param array
    *           Array to compress
    * @return Compressed array
    */
   static Function[] compressConstant(final Function[] array)
   {
      Arrays.sort(array, Function.COMPARATOR);

      if(((array[0] instanceof Constant) == false) || ((array[1] instanceof Constant) == false))
      {
         return array;
      }

      final int length = array.length;
      int index = 2;

      double value = ((Constant) array[0]).obtainRealValueNumber() + ((Constant) array[1]).obtainRealValueNumber();

      while((index < length) && ((array[index] instanceof Constant) == true))
      {
         value += ((Constant) array[index]).obtainRealValueNumber();

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
    * Extract real addition parameters
    * 
    * @param addition
    *           Addition where extract
    * @return Real addition parameters
    */
   static Function[] extractAdditionParameters(Addition addition)
   {
      final ArrayList<Function> list = new ArrayList<Function>();

      final Stack<Addition> stack = new Stack<Addition>();
      stack.push(addition);

      while(stack.isEmpty() == false)
      {
         addition = stack.pop();

         if(addition.parameter1 instanceof Addition)
         {
            stack.push((Addition) addition.parameter1);
         }
         else
         {
            list.add(addition.parameter1);
         }

         if(addition.parameter2 instanceof Addition)
         {
            stack.push((Addition) addition.parameter2);
         }
         else
         {
            list.add(addition.parameter2);
         }
      }

      return list.toArray(new Function[list.size()]);
   }

   /**
    * Extract addition parameters from function
    * 
    * @param function
    *           Function where extract
    * @return Extracted parameters
    */
   static Function[] extractAdditions(final Function function)
   {
      if(function instanceof Addition)
      {
         return Addition.extractAdditionParameters((Addition) function);
      }

      return new Function[]
      {
         function
      };
   }

   /** Simplifier linked to the addition */
   private AdditionSimplifier additionSimplifier;

   /**
    * Constructs the addition
    * 
    * @param parameter1
    *           First parameter
    * @param parameter2
    *           Second parameter
    */
   public Addition(final Function parameter1, final Function parameter2)
   {
      super("+", parameter1, parameter2);
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
    * @see jhelp.util.math.formal.Function#functionIsEqualsMoreSimple(jhelp.util.math.formal.Function)
    */
   @Override
   protected boolean functionIsEqualsMoreSimple(final Function function)
   {
      if(function == null)
      {
         return false;
      }

      if(function instanceof Addition)
      {
         final Addition addition = (Addition) function;

         if(this.parameter1.functionIsEqualsMoreSimple(addition.parameter1) == true)
         {
            return this.parameter2.functionIsEqualsMoreSimple(addition.parameter2);
         }

         if(this.parameter1.functionIsEqualsMoreSimple(addition.parameter2) == true)
         {
            return this.parameter2.functionIsEqualsMoreSimple(addition.parameter1);
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
    * @see jhelp.util.math.formal.Function#derive(jhelp.util.math.formal.Variable)
    */
   @Override
   public Function derive(final Variable variable)
   {
      return Function.createAddition(this.parameter1.derive(variable), this.parameter2.derive(variable));
   }

   /**
    * Indicates if a function is equals to this function
    * 
    * @param function
    *           Function tested
    * @return {@code true} if there sure equals. {@code false} dosen't mean not equals, but not sure about equality
    * @see jhelp.util.math.formal.Function#functionIsEquals(jhelp.util.math.formal.Function)
    */
   @Override
   public boolean functionIsEquals(final Function function)
   {
      if(function == null)
      {
         return false;
      }

      if(function instanceof Addition)
      {
         final Addition addition = (Addition) function;

         final Function[] thisParameters = Addition.extractAdditionParameters(this);
         final Function[] otherParameters = Addition.extractAdditionParameters(addition);

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
    * @see jhelp.util.math.formal.Function#getCopy()
    */
   @Override
   public Function getCopy()
   {
      return Function.createAddition(this.parameter1.getCopy(), this.parameter2.getCopy());
   }

   /**
    * The addition simplifier
    * 
    * @return The addition simplifier
    */
   @Override
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.additionSimplifier == null)
      {
         this.additionSimplifier = new AdditionSimplifier();
      }

      return this.additionSimplifier;
   }

   /**
    * Replace variable list by function
    * 
    * @param variable
    *           Variable to replace
    * @param function
    *           Function for replace
    * @return Result function
    * @see jhelp.util.math.formal.Function#replace(jhelp.util.math.formal.Variable, jhelp.util.math.formal.Function)
    */
   @Override
   public Function replace(final Variable variable, final Function function)
   {
      return Function.createAddition(this.parameter1.replace(variable, function), this.parameter2.replace(variable, function));
   }
}