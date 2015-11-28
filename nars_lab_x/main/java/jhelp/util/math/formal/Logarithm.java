package jhelp.util.math.formal;

/**
 * Logarithm Neperian <br>
 * <br>
 * 
 * @author JHelp
 */
public class Logarithm
      extends UnaryOperator
{
   /**
    * Logarithm function simplifier
    * 
    * @author JHelp
    */
   class LogarithmSimplifier
         implements FunctionSimplifier
   {
      /**
       * Simplification : ln(C1) -> C2
       * 
       * @param constant
       *           Constant argument : C1
       * @return Constant result : C2
       */
      private Function simplify(final Constant constant)
      {
         if((constant.isUndefined() == true) || (constant.isNul() == true) || (constant.isNegative() == true))
         {
            return Constant.UNDEFINED;
         }

         return new Constant(Math.log(constant.obtainRealValueNumber()));
      }

      /**
       * Simplification : ln(exp(X)) -> X
       * 
       * @param exponential
       *           Exponential argument : exp(X)
       * @return Result : X
       */
      private Function simplify(final Exponential exponential)
      {
         return exponential.parameter.simplify();
      }

      /**
       * Simplification : ln(X) -> ln(X)
       * 
       * @param function
       *           Function argument : X
       * @return Result : ln(X)
       */
      private Function simplify(final Function function)
      {
         return new Logarithm(function.simplify());
      }

      /**
       * Simplify the logarithm function <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @return More "simple" function
       * @see jhelp.util.math.formal.FunctionSimplifier#simplify()
       */
      @Override
      public Function simplify()
      {
         final Function function = Logarithm.this.parameter.simplify();

         if((function instanceof Constant) == true)
         {
            return this.simplify((Constant) function);
         }

         if((function instanceof Exponential) == true)
         {
            return this.simplify((Exponential) function);
         }

         return this.simplify(function);
      }
   }

   /** Logarithm simplifier */
   private LogarithmSimplifier logarithmSimplifier;

   /**
    * Constructs the logarithm
    * 
    * @param parameter
    *           Parameter
    */
   public Logarithm(final Function parameter)
   {
      super("ln", parameter);
   }

   /**
    * Indicates if a function is equals to this logarithm function <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param function
    *           Tested function
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

      if(function instanceof Logarithm)
      {
         return this.parameter.functionIsEqualsMoreSimple(((Logarithm) function).parameter);
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
      final Function d = this.parameter.derive(variable);
      return new Division(d, this.parameter);
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

      if(function instanceof Logarithm)
      {
         final Logarithm logarithm = (Logarithm) function;
         return this.parameter.functionIsEquals(logarithm.parameter);
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
      return new Logarithm(this.parameter.getCopy());
   }

   /**
    * Obtain the logarithm simplifier <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Logarithm simplifier
    * @see Function#obtainFunctionSimplifier()
    */
   @Override
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.logarithmSimplifier == null)
      {
         this.logarithmSimplifier = new LogarithmSimplifier();
      }

      return this.logarithmSimplifier;
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
      return new Logarithm(this.parameter.replace(variable, function));
   }
}