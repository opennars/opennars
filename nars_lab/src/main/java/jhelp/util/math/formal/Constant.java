package jhelp.util.math.formal;

import jhelp.util.math.UtilMath;

/**
 * Represents a constant <br>
 * <br>
 * 
 * @author JHelp
 */
public class Constant
      extends Function
{
   /**
    * e
    */
   public static final Constant E         = new Constant(2.7182818284590451D);
   /**
    * -1
    */
   public static final Constant MINUS_ONE = new Constant(-1D);
   /**
    * 1
    */
   public static final Constant ONE       = new Constant(1.0D);
   /**
    * &pi;
    */
   public static final Constant PI        = new Constant(3.1415926535897931D);
   /**
    * 2
    */
   public static final Constant TWO       = new Constant(2.0D);
   /**
    * The constant is undefined due illegal operation like division by zero, take the logarithm of negative value, ...
    */
   public static final Constant UNDEFINED = new Constant((0.0D / 0.0D));
   /**
    * 0
    */
   public static final Constant ZERO      = new Constant(0.0D);

   /**
    * Give the -1<sup>n</sup> constant
    * 
    * @param n
    *           Power to apply to -1
    * @return The constant result
    */
   public static Constant MOINS_UN_PUISSANCE(int n)
   {
      if(n < 0)
      {
         n = -n;
      }

      if((n % 2) == 0)
      {
         return Constant.ONE;
      }

      return Constant.MINUS_ONE;
   }

   /**
    * Real value
    */
   private final double real;

   /**
    * Constructs the constant
    * 
    * @param real
    *           Value
    */
   public Constant(final double real)
   {
      this.real = real;
   }

   /**
    * Internal comparison <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param function
    *           Function sure be the instance of the function
    * @return Comparison
    * @see jhelp.util.math.formal.Function#compareToInternal(jhelp.util.math.formal.Function)
    */
   @Override
   protected int compareToInternal(final Function function)
   {
      final Constant constant = (Constant) function;

      if(UtilMath.equals(this.real, constant.real) == true)
      {
         return 0;
      }

      if(this.real < constant.real)
      {
         return -1;
      }

      return 1;
   }

   /**
    * Absolute value of constant
    * 
    * @return Absolute value of constant
    */
   public Constant absoluteValue()
   {
      if(this.isNul())
      {
         return Constant.ZERO;
      }

      if(this.isPositive())
      {
         return this;
      }

      if(this.isMinusOne())
      {
         return Constant.ONE;
      }

      return new Constant(-this.real);
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
      return Constant.ZERO;
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

      if(function instanceof Constant)
      {
         final Constant constant = (Constant) function;

         if(this.isUndefined())
         {
            return constant.isUndefined();
         }

         return UtilMath.equals(this.real, constant.real);
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
      return new Constant(this.real);
   }

   /**
    * Indicates if the constant is -1
    * 
    * @return {@code true} the constant is -1
    */
   public boolean isMinusOne()
   {
      return UtilMath.equals(this.real, -1D);
   }

   /**
    * Indicates if constants is < 0
    * 
    * @return {@code true} if constants is < 0
    */
   public boolean isNegative()
   {
      return this.real < 0.0D;
   }

   /**
    * Indicates if the constant is 0
    * 
    * @return {@code true} if the constant is 0
    */
   public boolean isNul()
   {
      return UtilMath.isNul(this.real);
   }

   /**
    * Indicates if the constant is 1
    * 
    * @return {@code true} if the constant is 1
    */
   public boolean isOne()
   {
      return UtilMath.equals(this.real, 1.0D);
   }

   /**
    * Indicates if constants is > 0
    * 
    * @return {@code true} if constants is > 0
    */
   public boolean isPositive()
   {
      return this.real > 0.0D;
   }

   /**
    * Indicates if function can see as real number, that is to say that the value of {@link #obtainRealValueNumber()} as as
    * meaning
    * 
    * @return {@code true}
    * @see jhelp.util.math.formal.Function#isRealValueNumber()
    */
   @Override
   public boolean isRealValueNumber()
   {
      return true;
   }

   /**
    * Indicates if the constant is undefined
    * 
    * @return {@code true} if the constant is undefined
    */
   public boolean isUndefined()
   {
      return Double.isNaN(this.real) || Double.isInfinite(this.real);
   }

   /**
    * Return the constant divide by Pi
    * 
    * @return Constant divide by Pi
    */
   public Constant multipleDePi()
   {
      return new Constant(this.real / 3.1415926535897931D);
   }

   /**
    * Real value of function, if the function can be represents by a real number. Else {@link Double#NaN} is return
    * 
    * @return Variable value or {@link Double#NaN} if not define
    * @see jhelp.util.math.formal.Function#obtainRealValueNumber()
    */
   @Override
   public double obtainRealValueNumber()
   {
      return this.real;
   }

   /**
    * Return the Neperian logarithm apply to the constant
    * 
    * @return Neperian logarithm apply to the constant
    */
   public Constant puissanceDeE()
   {
      return new Constant(Math.log(this.real));
   }

   /**
    * Real string representation
    * 
    * @return Real string representation
    */
   @Override
   public String realString()
   {
      return String.valueOf(this.real);
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
      return this;
   }

   /**
    * Constant sign
    * 
    * @return Constant sign
    */
   public Constant sign()
   {
      if(this.isNul())
      {
         return Constant.ZERO;
      }

      if(this.isPositive())
      {
         return Constant.ONE;
      }

      return Constant.MINUS_ONE;
   }

   /**
    * String that represents the function
    * 
    * @return String representation
    */
   @Override
   public String toString()
   {
      return String.valueOf(this.real);
   }

   /**
    * Variable list contains in this function
    * 
    * @return Variable list contains in this function
    * @see jhelp.util.math.formal.Function#variableList()
    */
   @Override
   public VariableList variableList()
   {
      return new VariableList();
   }
}
