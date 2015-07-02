package jhelp.util.math.formal;

/**
 * Sinus <br>
 * <br>
 * 
 * @author JHelp
 */
public class Sinus
      extends UnaryOperator
{
   /**
    * Sinus simplifier
    * 
    * @author JHelp
    */
   class SinusSimplifier
         implements FunctionSimplifier
   {
      /**
       * Simplification : sin(C1) -> C2
       * 
       * @param constant
       *           Constant argument C1
       * @return Simplification
       */
      private Function simplify(final Constant constant)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         return new Constant(Math.sin(constant.obtainRealValueNumber()));
      }

      /**
       * Simplification : sin(X) -> sin(X)
       * 
       * @param function
       *           Function argument X
       * @return Simplification
       */
      private Function simplify(final Function function)
      {
         return new Sinus(function.simplify());
      }

      /**
       * Simplification : sin(-X) -> -sin(X)
       * 
       * @param minusUnary
       *           Minus unary argument -X
       * @return Simplification
       */
      private Function simplify(final MinusUnary minusUnary)
      {
         return new MinusUnary(new Sinus(minusUnary.parameter.simplify()));
      }

      /**
       * Simplification of the sinus <br>
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
         if((Sinus.this.parameter instanceof Constant) == true)
         {
            return this.simplify((Constant) Sinus.this.parameter);
         }

         if((Sinus.this.parameter instanceof MinusUnary) == true)
         {
            return this.simplify((MinusUnary) Sinus.this.parameter);
         }

         return this.simplify(Sinus.this.parameter.simplify());
      }
   }

   /** Sinus simplifier */
   private SinusSimplifier sinusSimplifier;

   /**
    * Constructs the sinus
    * 
    * @param parameter
    *           Parameter
    */
   public Sinus(final Function parameter)
   {
      super("sin", parameter);
   }

   /**
    * Indicates if function equals to this sinus <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param function
    *           Function to test
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

      if(function instanceof Sinus)
      {
         return this.parameter.functionIsEqualsMoreSimple(((Sinus) function).parameter);
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
      final Function d = this.parameter.derive(variable);
      return Function.createMultiplication(d, new Cosinus(this.parameter));
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

      if(function instanceof Sinus)
      {
         final Sinus sinus = (Sinus) function;
         return this.parameter.functionIsEquals(sinus.parameter);
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
      return new Sinus(this.parameter.getCopy());
   }

   /**
    * Sinus simplifier <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Sinus simplifier
    * @see jhelp.util.math.formal.Function#obtainFunctionSimplifier()
    */
   @Override
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.sinusSimplifier == null)
      {
         this.sinusSimplifier = new SinusSimplifier();
      }

      return this.sinusSimplifier;
   }

   /**
    * Replace variable by function
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
      return new Sinus(this.parameter.replace(variable, function));
   }
}