package jhelp.util.math.formal;

/**
 * Represents tangent function <br>
 * <br>
 * 
 * @author JHelp
 */
public class Tangent
      extends UnaryOperator
{
   /**
    * Simplifier for tangent
    * 
    * @author JHelp
    */
   class TangentSimplifier
         implements FunctionSimplifier
   {
      /**
       * tan(C1) => C2
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

         return new Constant(Math.tan(constant.obtainRealValueNumber()));
      }

      /**
       * Simplify the tangent
       * 
       * @param function
       *           Function parameter
       * @return Simplified function
       */
      private Function simplify(final Function function)
      {
         return new Tangent(function.simplify());
      }

      /**
       * Simplify the tangent <br>
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
         if((Tangent.this.parameter instanceof Constant) == true)
         {
            return this.simplify((Constant) Tangent.this.parameter);
         }

         return this.simplify(Tangent.this.parameter.simplify());
      }
   }

   /** Tangent simplifier */
   private TangentSimplifier tangentSimplifier;

   /**
    * Constructs the tangent function
    * 
    * @param parameter
    *           Function to apply tangent on it
    */
   public Tangent(final Function parameter)
   {
      super("tan", parameter);
   }

   /**
    * Compare quickly with an other function <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param function
    *           Function to compare with
    * @return {@code true} if sure equals, {@code false} not sure about equality
    * @see jhelp.util.math.formal.Function#functionIsEqualsMoreSimple(jhelp.util.math.formal.Function)
    */
   @Override
   protected boolean functionIsEqualsMoreSimple(final Function function)
   {
      if(function == null)
      {
         return false;
      }

      if(function instanceof Tangent)
      {
         return this.parameter.functionIsEqualsMoreSimple(((Tangent) function).parameter);
      }

      return false;
   }

   /**
    * Derive the function
    * 
    * @param variable
    *           Variable for derive
    * @return Derived function
    * @see jhelp.util.math.formal.Function#derive(jhelp.util.math.formal.Variable)
    */
   @Override
   public Function derive(final Variable variable)
   {
      final Function d = this.parameter.derive(variable);
      return Function.createMultiplication(d, Function.createAddition(Constant.ONE, Function.createMultiplication(this, this)));
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

      if(function instanceof Tangent)
      {
         final Tangent tangent = (Tangent) function;
         return this.parameter.functionIsEquals(tangent.parameter);
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
      return new Tangent(this.parameter.getCopy());
   }

   /**
    * Tangent simplifier <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Tangent simplifier
    * @see jhelp.util.math.formal.Function#obtainFunctionSimplifier()
    */
   @Override
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.tangentSimplifier == null)
      {
         this.tangentSimplifier = new TangentSimplifier();
      }

      return this.tangentSimplifier;
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
      return new Tangent(this.parameter.replace(variable, function));
   }
}