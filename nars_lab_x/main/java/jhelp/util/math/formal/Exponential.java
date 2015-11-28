package jhelp.util.math.formal;

/**
 * Exponential function <br>
 * <br>
 * 
 * @author JHelp
 */
public class Exponential
      extends UnaryOperator
{
   /**
    * Simplifier of exponential operator
    * 
    * @author JHelp
    */
   class ExponentialSimplifier
         implements FunctionSimplifier
   {
      /**
       * exp(C1) => C2
       * 
       * @param constant
       *           C1
       * @return C2
       */
      private Function simplify(final Constant constant)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         return new Constant(Math.exp(constant.obtainRealValueNumber()));
      }

      /**
       * Simplify the exponential
       * 
       * @param function
       *           Function inside exponential
       * @return Simplified function
       */
      private Function simplify(final Function function)
      {
         return new Exponential(function.simplify());
      }

      /**
       * exp(ln(f)) => f
       * 
       * @param logarithm
       *           ln(f)
       * @return f
       */
      private Function simplify(final Logarithm logarithm)
      {
         return logarithm.parameter.simplify();
      }

      /**
       * Simplify the exponential <br>
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
         final Function function = Exponential.this.parameter.simplify();

         if((function instanceof Constant) == true)
         {
            return this.simplify((Constant) function);
         }

         if((function instanceof Logarithm) == true)
         {
            return this.simplify((Logarithm) function);
         }

         return this.simplify(function);
      }
   }

   /** Exponential simplifier */
   private ExponentialSimplifier exponentialSimplifier;

   /**
    * Constructs exponential
    * 
    * @param f
    *           Parameter
    */
   public Exponential(final Function f)
   {
      super("exp", f);
   }

   /**
    * Compare quickly if function is equals to this exponential <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param function
    *           Function to compare
    * @return {@code true} for equals, {@code false} not sure about equality
    * @see Function#functionIsEqualsMoreSimple(Function)
    */
   @Override
   protected boolean functionIsEqualsMoreSimple(final Function function)
   {
      if(function == null)
      {
         return false;
      }

      if(function instanceof Exponential)
      {
         return this.parameter.functionIsEqualsMoreSimple(((Exponential) function).parameter);
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
      return Function.createMultiplication(d, this);
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

      if(function instanceof Exponential)
      {
         final Exponential exponential = (Exponential) function;
         return this.parameter.functionIsEquals(exponential.parameter);
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
      return new Exponential(this.parameter.getCopy());
   }

   /**
    * The exponential simplifier <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Associated simplifier
    * @see Function#obtainFunctionSimplifier()
    */
   @Override
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.exponentialSimplifier == null)
      {
         this.exponentialSimplifier = new ExponentialSimplifier();
      }

      return this.exponentialSimplifier;
   }

   /**
    * Replace variable list by function
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
      return new Exponential(this.parameter.replace(variable, function));
   }
}