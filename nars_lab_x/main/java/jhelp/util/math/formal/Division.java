package jhelp.util.math.formal;

/**
 * Division <br>
 * <br>
 * 
 * @author JHelp
 */
public class Division
      extends BinaryOperator
{
   /**
    * Division simplifier
    * 
    * @author JHelp
    */
   class DivisionSimplifier
         implements FunctionSimplifier
   {
      /**
       * Simplify C1/C2 -> C3
       * 
       * @param constant1
       *           Constant C1
       * @param constant2
       *           Constant C2
       * @return Constant result C3
       */
      private Function simplify(final Constant constant1, final Constant constant2)
      {
         if((constant1.isUndefined() == true) || (constant2.isUndefined() == true) || (constant2.isNul() == true))
         {
            return Constant.UNDEFINED;
         }

         if(constant1.isNul() == true)
         {
            return Constant.ZERO;
         }

         return new Constant(constant1.obtainRealValueNumber() / constant2.obtainRealValueNumber());
      }

      /**
       * Simplify C/f
       * 
       * @param constant
       *           Constant C
       * @param function
       *           Function f
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
            return Constant.ZERO;
         }

         return new Division(constant, function.simplify());
      }

      /**
       * Simplify
       * <ul>
       * <li>C1/(C2*f) -> C3/f</li>
       * <li>C1/(f*C2) -> C3/f</li>
       * <li>C1/(f1*f2)</li>
       * </ul>
       * 
       * @param constant
       *           Constant C1
       * @param multiplication
       *           Multiplication (C2*f, f*C2 or f1*f2)
       * @return Simplified function
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

         final Function function1 = multiplication.parameter1.simplify();
         final Function function2 = multiplication.parameter2.simplify();

         if((function1 instanceof Constant) == true)
         {
            return this.simplifyDivisionOfConstantMultiplication(constant, (Constant) function1, function2);
         }

         if((function2 instanceof Constant) == true)
         {
            return this.simplifyDivisionOfConstantMultiplication(constant, function1, (Constant) function2);
         }

         return this.simplifyDivisionOfConstantMultiplication(constant, function1, function2);
      }

      /**
       * Simplify cos(f)/sin(f) -> 1/tan(f)
       * 
       * @param cosinus
       *           Cosinus function
       * @param sinus
       *           Sinus function
       * @return Simplified function
       */
      private Function simplify(final Cosinus cosinus, final Sinus sinus)
      {
         if(cosinus.parameter.equals(sinus.parameter) == true)
         {
            return new Division(Constant.ONE, new Tangent(cosinus.parameter.simplify()));
         }

         return new Division(cosinus.simplify(), sinus.simplify());
      }

      /**
       * Simplify (f1/f2)/(f3/f4) -> (f1*f4)/(f2*f3)
       * 
       * @param division1
       *           First division f1/f2
       * @param division2
       *           Second division f3/f4
       * @return Result (f1*f4)/(f2*f3)
       */
      private Function simplify(final Division division1, final Division division2)
      {
         return new Division(Function.createMultiplication(division1.parameter1.simplify(), division2.parameter2.simplify()), Function.createMultiplication(division1.parameter2.simplify(), division2.parameter1.simplify()));
      }

      /**
       * Simplify (f1/f2)/f3 -> f1/(f2*f3)
       * 
       * @param division
       *           Division f1/f2
       * @param function
       *           Function f3
       * @return Result f1/(f2*f3)
       */
      private Function simplify(final Division division, final Function function)
      {
         return new Division(division.parameter1.simplify(), Function.createMultiplication(division.parameter2.simplify(), function.simplify()));
      }

      /**
       * Simplify e<sup>f1</sup>/e<sup>f2</sup> -> e<sup>f1-f2</sup>
       * 
       * @param exponential1
       *           First exponential e<sup>f1</sup>
       * @param exponential2
       *           Second exponential e<sup>f2</sup>
       * @return Result e<sup>f1-f2</sup>
       */
      private Function simplify(final Exponential exponential1, final Exponential exponential2)
      {
         return new Exponential(new Subtraction(exponential1.parameter.simplify(), exponential2.parameter.simplify()));
      }

      /**
       * Simplify f/C1 -> C2*f
       * 
       * @param function
       *           Function f
       * @param constant
       *           Constant C1
       * @return Result C2*f
       */
      private Function simplify(final Function function, final Constant constant)
      {
         if((constant.isUndefined() == true) || (constant.isNul() == true))
         {
            return Constant.UNDEFINED;
         }

         if(constant.isOne() == true)
         {
            return function.simplify();
         }

         if(constant.isMinusOne() == true)
         {
            return new MinusUnary(function.simplify());
         }

         return Function.createMultiplication(new Constant(1.0 / constant.obtainRealValueNumber()), function.simplify());
      }

      /**
       * f1/(f2/f3) -> (f1*f3)/f2
       * 
       * @param function
       *           f1
       * @param division
       *           f2/f3
       * @return (f1*f3)/f2
       */
      private Function simplify(final Function function, final Division division)
      {
         return new Division(Function.createMultiplication(function.simplify(), division.parameter2.simplify()), division.parameter1.simplify());
      }

      /**
       * Simplify f1/f2
       * 
       * @param numerator
       *           f1
       * @param denominator
       *           f2
       * @return Simplified function
       */
      private Function simplify(final Function numerator, final Function denominator)
      {
         return new Division(numerator.simplify(), denominator.simplify());
      }

      /**
       * f1/(-f2) -> -(f1/f2)
       * 
       * @param function
       *           f1
       * @param minusUnary
       *           -f2
       * @return -(f1/f2)
       */
      private Function simplify(final Function function, final MinusUnary minusUnary)
      {
         return new MinusUnary(new Division(function.simplify(), minusUnary.parameter.simplify()));
      }

      /**
       * (-f1)/f2 -> -(f1/f2)
       * 
       * @param minusUnary
       *           -f1
       * @param function
       *           f2
       * @return -(f1/f2)
       */
      private Function simplify(final MinusUnary minusUnary, final Function function)
      {
         return new MinusUnary(new Division(minusUnary.parameter.simplify(), function.simplify()));
      }

      /**
       * (-f1)/-(f2) -> f1/f2
       * 
       * @param minusUnaryNumerator
       *           -f1
       * @param minusUnaryDenominator
       *           -f2
       * @return f1*f2
       */
      private Function simplify(final MinusUnary minusUnaryNumerator, final MinusUnary minusUnaryDenominator)
      {
         return new Division(minusUnaryNumerator.parameter.simplify(), minusUnaryDenominator.parameter.simplify());
      }

      /**
       * (f1*f2)/C1 -> C2*f1*f2
       * 
       * @param multiplication
       *           f1*f2
       * @param constant
       *           C1
       * @return C2*f1*f2
       */
      private Function simplify(final Multiplication multiplication, final Constant constant)
      {
         if((constant.isUndefined() == true) || (constant.isNul() == true))
         {
            return Constant.UNDEFINED;
         }

         final Function function1 = multiplication.parameter1.simplify();
         final Function function2 = multiplication.parameter2.simplify();

         if((function1 instanceof Constant) == true)
         {
            return this.simplifyDivisionOfMultiplicationConstant((Constant) function1, function2, constant);
         }

         if((function2 instanceof Constant) == true)
         {
            return this.simplifyDivisionOfMultiplicationConstant(function1, (Constant) function2, constant);
         }

         return this.simplifyDivisionOfMultiplicationConstant(function1, function2, constant);
      }

      /**
       * sin(f)/cos(f) -> tan(f)
       * 
       * @param sinus
       *           sin(f)
       * @param cosinus
       *           cos(f)
       * @return Simplified function
       */
      private Function simplify(final Sinus sinus, final Cosinus cosinus)
      {
         if(sinus.parameter.equals(cosinus.parameter) == true)
         {
            return new Tangent(cosinus.parameter.simplify());
         }

         return new Division(sinus.simplify(), cosinus.simplify());
      }

      /**
       * C1/(C2*f) -> C3/f
       * 
       * @param constant
       *           C1
       * @param parameterConstant
       *           C2
       * @param parameter
       *           f
       * @return C3/f
       */
      private Function simplifyDivisionOfConstantMultiplication(final Constant constant, final Constant parameterConstant, final Function parameter)
      {
         if((parameterConstant.isUndefined() == true) || (parameterConstant.isNul() == true) || (constant.isUndefined() == true))
         {
            return Constant.UNDEFINED;
         }

         return new Division(new Constant(constant.obtainRealValueNumber() / parameterConstant.obtainRealValueNumber()), parameter.simplify());
      }

      /**
       * C1/(f*C2) -> C3/f
       * 
       * @param constant
       *           C1
       * @param parameter
       *           f
       * @param parameterConstant
       *           C2
       * @return C3/f
       */
      private Function simplifyDivisionOfConstantMultiplication(final Constant constant, final Function parameter, final Constant parameterConstant)
      {
         if((parameterConstant.isUndefined() == true) || (parameterConstant.isNul() == true) || (constant.isUndefined() == true))
         {
            return Constant.UNDEFINED;
         }

         return new Division(new Constant(constant.obtainRealValueNumber() / parameterConstant.obtainRealValueNumber()), parameter.simplify());
      }

      /**
       * Simplify C/(f1*f2)
       * 
       * @param constant
       *           C
       * @param parameter1
       *           f1
       * @param parameter2
       *           f2
       * @return Simplified function
       */
      private Function simplifyDivisionOfConstantMultiplication(final Constant constant, final Function parameter1, final Function parameter2)
      {
         return new Division(constant, Function.createMultiplication(parameter2.simplify(), parameter1.simplify()));
      }

      /**
       * (C1*f)/C2 -> C3*f
       * 
       * @param parameterConstant
       *           C1
       * @param parameter
       *           f
       * @param constant
       *           C2
       * @return C3*f
       */
      private Function simplifyDivisionOfMultiplicationConstant(final Constant parameterConstant, final Function parameter, final Constant constant)
      {
         if((parameterConstant.isUndefined() == true) || (constant.isUndefined() == true) || (constant.isNul() == true))
         {
            return Constant.UNDEFINED;
         }

         return new Multiplication(new Constant(parameterConstant.obtainRealValueNumber() / constant.obtainRealValueNumber()), parameter);
      }

      /**
       * (f*C1)/C2 -> C3*f
       * 
       * @param parameter
       *           f
       * @param parameterConstant
       *           C1
       * @param constant
       *           C2
       * @return C3*f
       */
      private Function simplifyDivisionOfMultiplicationConstant(final Function parameter, final Constant parameterConstant, final Constant constant)
      {
         if((parameterConstant.isUndefined() == true) || (constant.isUndefined() == true) || (constant.isNul() == true))
         {
            return Constant.UNDEFINED;
         }

         return new Multiplication(new Constant(parameterConstant.obtainRealValueNumber() / constant.obtainRealValueNumber()), parameter.simplify());
      }

      /**
       * Simplify (f1*f2)/C
       * 
       * @param parameter1
       *           f1
       * @param parameter2
       *           f2
       * @param constant
       *           C
       * @return simplified function
       */
      private Function simplifyDivisionOfMultiplicationConstant(final Function parameter1, final Function parameter2, final Constant constant)
      {
         return new Division(Function.createMultiplication(parameter2.simplify(), parameter1.simplify()), constant);
      }

      /**
       * Simplify the division <br>
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
         final Function numerator = Division.this.parameter1.simplify();
         final Function denominator = Division.this.parameter2.simplify();

         if(numerator.equals(denominator) == true)
         {
            return Constant.ONE;
         }

         if((numerator instanceof MinusUnary) == true)
         {
            if((denominator instanceof MinusUnary) == true)
            {
               return this.simplify((MinusUnary) numerator, (MinusUnary) denominator);
            }

            return this.simplify((MinusUnary) numerator, denominator);
         }

         if((denominator instanceof MinusUnary) == true)
         {
            return this.simplify(numerator, (MinusUnary) denominator);
         }

         if((numerator instanceof Constant) == true)
         {
            if((denominator instanceof Constant) == true)
            {
               return this.simplify((Constant) numerator, (Constant) denominator);
            }

            if((denominator instanceof Multiplication) == true)
            {
               return this.simplify((Constant) numerator, (Multiplication) denominator);
            }

            return this.simplify((Constant) numerator, denominator);
         }

         if((denominator instanceof Constant) == true)
         {
            return this.simplify(numerator, (Constant) denominator);
         }

         if(((numerator instanceof Cosinus) == true) && ((denominator instanceof Sinus) == true))
         {
            return this.simplify((Cosinus) numerator, (Sinus) denominator);
         }

         if((numerator instanceof Division) == true)
         {
            if((denominator instanceof Division) == true)
            {
               return this.simplify((Division) numerator, (Division) denominator);
            }

            return this.simplify((Division) numerator, denominator);
         }

         if(((numerator instanceof Exponential) == true) && ((denominator instanceof Exponential) == true))
         {
            return this.simplify((Exponential) numerator, (Exponential) denominator);
         }

         if(((numerator instanceof Multiplication) == true) && ((denominator instanceof Constant) == true))
         {
            return this.simplify((Multiplication) numerator, (Constant) denominator);
         }

         if(((numerator instanceof Sinus) == true) && ((denominator instanceof Cosinus) == true))
         {
            return this.simplify((Sinus) numerator, (Cosinus) denominator);
         }

         if((denominator instanceof Division) == true)
         {
            return this.simplify(numerator, (Division) denominator);
         }

         return this.simplify(numerator, denominator);
      }
   }

   /** Division simplifier */
   private DivisionSimplifier divisionSimplifier;

   /**
    * Constructs a division
    * 
    * @param parameter1
    *           First parameter
    * @param parameter2
    *           Second parameter
    */
   public Division(final Function parameter1, final Function parameter2)
   {
      super("/", parameter1, parameter2);
   }

   /**
    * Indicates if a function is equals to this division <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param function
    *           Function to compare
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

      if(function instanceof Division)
      {
         final Division division = (Division) function;

         if(this.parameter1.functionIsEqualsMoreSimple(division.parameter1) == true)
         {
            return this.parameter2.functionIsEqualsMoreSimple(division.parameter2);
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
      final Function d1 = this.parameter1.derive(variable);
      final Function d2 = this.parameter2.derive(variable);
      return new Division(new Subtraction(Function.createMultiplication(d1, this.parameter2), Function.createMultiplication(this.parameter1, d2)), Function.createMultiplication(this.parameter2, this.parameter2));
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

      if(function instanceof Division)
      {
         final Division division = (Division) function;
         return this.parameter1.functionIsEquals(division.parameter1) && this.parameter2.functionIsEquals(division.parameter2);
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
    * @see jhelp.util.math.formal.Function#getCopy()
    */
   @Override
   public Function getCopy()
   {
      return new Division(this.parameter1.getCopy(), this.parameter2.getCopy());
   }

   /**
    * Division simplifier <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Division simplifier
    * @see jhelp.util.math.formal.Function#obtainFunctionSimplifier()
    */
   @Override
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.divisionSimplifier == null)
      {
         this.divisionSimplifier = new DivisionSimplifier();
      }

      return this.divisionSimplifier;
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
      return new Division(this.parameter1.replace(variable, function), this.parameter2.replace(variable, function));
   }
}