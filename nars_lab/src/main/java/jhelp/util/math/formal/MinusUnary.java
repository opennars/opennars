package jhelp.util.math.formal;

/**
 * Minus unary<br>
 * It take the opposite a => -a <br>
 * 
 * @author JHelp
 */
public class MinusUnary
      extends UnaryOperator
{
   /**
    * Minus unary simplifier
    * 
    * @author JHelp
    */
   class MinusUnarySimplifier
         implements FunctionSimplifier
   {
      /**
       * Simplification : -C1 -> C2
       * 
       * @param constant
       *           Constant argument : C1
       * @return Constant result : C2
       */
      private Function simplify(final Constant constant)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         if(constant.isNul() == true)
         {
            return Constant.ZERO;
         }

         if(constant.isMinusOne() == true)
         {
            return Constant.ONE;
         }

         if(constant.isOne() == true)
         {
            return Constant.MINUS_ONE;
         }

         return new Constant(-constant.obtainRealValueNumber());
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
       * <td>-&nbsp;(&nbsp;C1&nbsp;/&nbsp;X&nbsp;)<br>
       * </td>
       * <td>C2&nbsp;/&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>-&nbsp;(&nbsp;X&nbsp;/&nbsp;C1)<br>
       * </td>
       * <td>X&nbsp;/&nbsp;C2<br>
       * </td>
       * </tr>
       * <tr>
       * <td>-&nbsp;(&nbsp;X&nbsp;/&nbsp;Y&nbsp;)<br>
       * </td>
       * <td>-&nbsp;(&nbsp;X&nbsp;/&nbsp;Y&nbsp;)<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param division
       *           Division argument
       * @return Simplification
       */
      private Function simplify(final Division division)
      {
         final Function function1 = division.parameter1.simplify();
         final Function function2 = division.parameter2.simplify();

         if((function1 instanceof Constant) == true)
         {
            return this.simplifyDivision((Constant) function1, function2);
         }

         if((function2 instanceof Constant) == true)
         {
            return this.simplifyDivision(function1, (Constant) function2);
         }

         return this.simplifyDivision(function1, function2);
      }

      /**
       * Simplification : -X -> -X
       * 
       * @param function
       *           Function to simplify
       * @return Function result
       */
      private Function simplify(final Function function)
      {
         return new MinusUnary(function.simplify());
      }

      /**
       * Simplification : -(-X) -> X
       * 
       * @param minusUnary
       *           Minus unary argument
       * @return Simplification
       */
      private Function simplify(final MinusUnary minusUnary)
      {
         return minusUnary.parameter.simplify();
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
       * <td>-&nbsp;(&nbsp;C1&nbsp;*&nbsp;X&nbsp;)<br>
       * </td>
       * <td>C2&nbsp;*&nbsp;X<br>
       * </td>
       * </tr>
       * <tr>
       * <td>-&nbsp;(&nbsp;X&nbsp;*&nbsp;C1)<br>
       * </td>
       * <td>X&nbsp;*&nbsp;C2<br>
       * </td>
       * </tr>
       * <tr>
       * <td>-&nbsp;(&nbsp;X&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * <td>-&nbsp;(&nbsp;X&nbsp;*&nbsp;Y&nbsp;)<br>
       * </td>
       * </tr>
       * </table>
       * 
       * @param multiplication
       *           Multiplication to simplify
       * @return Simplification
       */
      private Function simplify(final Multiplication multiplication)
      {
         final Function function1 = multiplication.parameter1.simplify();
         final Function function2 = multiplication.parameter2.simplify();

         if((function1 instanceof Constant) == true)
         {
            return this.simplifyMultiplication((Constant) function1, function2);
         }

         if((function2 instanceof Constant) == true)
         {
            return this.simplifyMultiplication(function1, (Constant) function2);
         }

         return this.simplifyMultiplication(function1, function2);
      }

      /**
       * Simplification : -(X-Y) -> Y-X
       * 
       * @param subtraction
       *           Subtraction argument
       * @return Simplification
       */
      private Function simplify(final Subtraction subtraction)
      {
         return new Subtraction(subtraction.parameter2.simplify(), subtraction.parameter1.simplify());
      }

      /**
       * Simplification : -(C1/X) -> C2 / x
       * 
       * @param constant
       *           Constant argument C1
       * @param function
       *           Function argument X
       * @return Simplification
       */
      private Function simplifyDivision(final Constant constant, final Function function)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         if(constant.isNul() == true)
         {
            return Constant.ZERO;
         }

         return new Division(new Constant(-constant.obtainRealValueNumber()), function.simplify());
      }

      /**
       * Simplification : -(X/C1) -> X/C2
       * 
       * @param function
       *           Function argument X
       * @param constant
       *           Constant argument C1
       * @return Simplification
       */
      private Function simplifyDivision(final Function function, final Constant constant)
      {
         if((constant.isUndefined() == true) || (constant.isNul() == true))
         {
            return Constant.UNDEFINED;
         }

         if(constant.isOne() == true)
         {
            return new MinusUnary(function.simplify());
         }

         if(constant.isMinusOne() == true)
         {
            return function.simplify();
         }

         return new Division(function.simplify(), new Constant(-constant.obtainRealValueNumber()));
      }

      /**
       * Simplification : -(X/Y) -> -(X/Y)
       * 
       * @param function1
       *           Function argument X
       * @param function2
       *           Function argument Y
       * @return Simplification
       */
      private Function simplifyDivision(final Function function1, final Function function2)
      {
         return new MinusUnary(new Division(function1.simplify(), function2.simplify()));
      }

      /**
       * Simplification : -(C1*X) -> C2*X
       * 
       * @param constant
       *           Constant argument C1
       * @param function
       *           Function argument X
       * @return Simplification
       */
      private Function simplifyMultiplication(final Constant constant, final Function function)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         if(constant.isNul() == true)
         {
            return Constant.ZERO;
         }

         if(constant.isMinusOne() == true)
         {
            return function.simplify();
         }

         if(constant.isOne() == true)
         {
            return new MinusUnary(function.simplify());
         }

         return Function.createMultiplication(new Constant(-constant.obtainRealValueNumber()), function.simplify());
      }

      /**
       * Simplification : -(X*C1) -> C2*X
       * 
       * @param function
       *           Function argument C
       * @param constant
       *           Constant argument C1
       * @return Simplification
       */
      private Function simplifyMultiplication(final Function function, final Constant constant)
      {
         return this.simplifyMultiplication(constant, function);
      }

      /**
       * Simplification : -(X*Y) -> -(X*Y)
       * 
       * @param function1
       *           Function argument X
       * @param function2
       *           Function argument Y
       * @return Simplification
       */
      private Function simplifyMultiplication(final Function function1, final Function function2)
      {
         return new MinusUnary(Function.createMultiplication(function2.simplify(), function1.simplify()));
      }

      /**
       * Simplification of the minus unary <br>
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
         final Function function = MinusUnary.this.parameter.simplify();

         if((function instanceof Constant) == true)
         {
            return this.simplify((Constant) function);
         }

         if((function instanceof Division) == true)
         {
            return this.simplify((Division) function);
         }

         if((function instanceof MinusUnary) == true)
         {
            return this.simplify((MinusUnary) function);
         }

         if((function instanceof Multiplication) == true)
         {
            return this.simplify((Multiplication) function);
         }

         if((function instanceof Subtraction) == true)
         {
            return this.simplify((Subtraction) function);
         }

         return this.simplify(function);
      }
   }

   /** Minus unary simplifier */
   private MinusUnarySimplifier minusUnarySimplifier;

   /**
    * Constructs the minus unary
    * 
    * @param parameter
    *           Parameter
    */
   public MinusUnary(final Function parameter)
   {
      super("-", parameter);
   }

   /**
    * Indicates if a function is equals to this minus unary <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param function
    *           Function to compare
    * @return {@code true} if function equals
    * @see Function#functionIsEqualsMoreSimple(Function)
    */
   @Override
   protected boolean functionIsEqualsMoreSimple(final Function function)
   {
      if(function == null)
      {
         return false;
      }

      if(function instanceof MinusUnary)
      {
         return this.parameter.functionIsEqualsMoreSimple(((MinusUnary) function).parameter);
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
      return new MinusUnary(this.parameter.derive(variable));
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

      if(function instanceof MinusUnary)
      {
         final MinusUnary minusUnary = (MinusUnary) function;
         return this.parameter.functionIsEquals(minusUnary.parameter);
      }
      else
      {
         return false;
      }
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
      return new MinusUnary(this.parameter.getCopy());
   }

   /**
    * Minus unary simplifier <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Minus unary simplifier
    * @see Function#obtainFunctionSimplifier()
    */
   @Override
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.minusUnarySimplifier == null)
      {
         this.minusUnarySimplifier = new MinusUnarySimplifier();
      }

      return this.minusUnarySimplifier;
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
      return new MinusUnary(this.parameter.replace(variable, function));
   }
}