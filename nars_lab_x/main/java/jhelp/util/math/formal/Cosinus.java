package jhelp.util.math.formal;

/**
 * Cosinus function <br>
 * <br>
 * 
 * @author JHelp
 */
public class Cosinus
      extends UnaryOperator
{
   /**
    * Simplifier for cosinus function
    * 
    * @author JHelp
    */
   class CosinusSimplifier
         implements FunctionSimplifier
   {
      /**
       * Simplify cos(C)
       * 
       * @param constant
       *           Constant C
       * @return Simplified function
       */
      private Function simplify(final Constant constant)
      {
         if(constant.isUndefined() == true)
         {
            return Constant.UNDEFINED;
         }

         return new Constant(Math.cos(constant.obtainRealValueNumber()));
      }

      /**
       * Simplify cos(f)
       * 
       * @param function
       *           Function f
       * @return Simplified function
       */
      private Function simplify(final Function function)
      {
         return new Cosinus(function.simplify());
      }

      /**
       * Simplify cos(-f) -> cos(f)
       * 
       * @param minusUnary
       *           Minus unary parameter
       * @return Simplified function
       */
      private Function simplify(final MinusUnary minusUnary)
      {
         return new Cosinus(minusUnary.getParameter().simplify());
      }

      /**
       * Simplify the function <br>
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
         final Function function = Cosinus.this.parameter.simplify();

         if((function instanceof Constant) == true)
         {
            return this.simplify((Constant) function);
         }

         if((function instanceof MinusUnary) == true)
         {
            return this.simplify((MinusUnary) function);
         }

         return this.simplify(function);
      }
   }

   /** Simpilifier for cosinus */
   private CosinusSimplifier cosinusSimplifier;

   /**
    * Constructs the function
    * 
    * @param parameter
    *           Parameter
    */
   public Cosinus(final Function parameter)
   {
      super("cos", parameter);
   }

   /**
    * Indicates if a function is equals to this function <br>
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

      if(function instanceof Cosinus)
      {
         return this.parameter.functionIsEqualsMoreSimple(((Cosinus) function).parameter);
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
      return new MinusUnary(Function.createMultiplication(d, new Sinus(this.parameter)));
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

      if(function instanceof Cosinus)
      {
         final Cosinus cosinus = (Cosinus) function;
         return this.parameter.functionIsEquals(cosinus.parameter);
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
      return new Cosinus(this.parameter.getCopy());
   }

   /**
    * The cosinus simplifier <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return The cosinus simplifier
    * @see jhelp.util.math.formal.Function#obtainFunctionSimplifier()
    */
   @Override
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.cosinusSimplifier == null)
      {
         this.cosinusSimplifier = new CosinusSimplifier();
      }

      return this.cosinusSimplifier;
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
      return new Cosinus(this.parameter.replace(variable, function));
   }
}