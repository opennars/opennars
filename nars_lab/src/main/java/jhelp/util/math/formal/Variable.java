package jhelp.util.math.formal;

/**
 * Represents a variable<br>
 * <br>
 * 
 * @author JHelp
 */
public class Variable
      extends Function
{
   /**
    * Simplify a variable.<br>
    * It checks if the variable is a predefined constants. If its a predefined constants, the variable is replace by its value.
    * 
    * @author JHelp
    */
   class VariableSimplifier
         implements FunctionSimplifier
   {
      /**
       * Do the simplification.<br>
       * It checks if the variable is a predefined constants. If its a predefined constants, the variable is replace by its
       * value. <br>
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
         // Here we test if the variable is a defined function
         // If its a defined constant we replace the variable by its value
         if(Function.references.isConstantDefine(Variable.this.name))
         {
            return new Constant(Function.references.obtainConstantValue(Variable.this.name));
         }

         final Function f = Function.references.constantFunction(Variable.this.name);
         if((f != null) && (f.functionIsEquals(Variable.this) == false))
         {
            return f.simplify();
         }

         return Variable.this;
      }
   }

   /** The variable simplifier instance */
   private VariableSimplifier variableSimplifier;

   /**
    * Variable name
    */
   final String               name;

   /**
    * Create a variable
    * 
    * @param name
    *           Variable name
    */
   public Variable(final String name)
   {
      this.name = name;
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
    * @see Function#compareToInternal(Function)
    */
   @Override
   protected int compareToInternal(final Function function)
   {
      final Variable variable = (Variable) function;

      final int comp = this.name.compareToIgnoreCase(variable.name);

      if(comp != 0)
      {
         return comp;
      }

      return this.name.compareTo(variable.name);
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
      if(this.functionIsEquals(variable))
      {
         return Constant.ONE;
      }
      else
      {
         return Constant.ZERO;
      }
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

      if(function instanceof Variable)
      {
         final Variable variable = (Variable) function;
         return variable.name.equals(this.name);
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
      return new Variable(this.name);
   }

   /**
    * Variable name
    * 
    * @return Variable name
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Indicates if function can see as real number, that is to say that the value of {@link #obtainRealValueNumber()} as as
    * meaning
    * 
    * @return {@code true} if the function can see as real number
    * @see Function#isRealValueNumber()
    */
   @Override
   public boolean isRealValueNumber()
   {
      return Function.references.isConstantDefine(this.name);
   }

   /**
    * Simplify the variable.<br>
    * It checks if the variable is a predefined constants. If its a predefined constants, the variable is replace by its value. <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Function simplified
    * @see Function#obtainFunctionSimplifier()
    */
   @Override
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.variableSimplifier == null)
      {
         this.variableSimplifier = new VariableSimplifier();
      }

      return this.variableSimplifier;
   }

   /**
    * Real value of function, if the function can be represents by a real number. Else {@link Double#NaN} is return
    * 
    * @return Variable value or {@link Double#NaN} if not define
    * @see Function#obtainRealValueNumber()
    */
   @Override
   public double obtainRealValueNumber()
   {
      if(Function.references.isConstantDefine(this.name))
      {
         return Function.references.obtainConstantValue(this.name);
      }
      else
      {
         return(0.0D / 0.0D);
      }
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
      if(this.functionIsEquals(variable))
      {
         return function;
      }
      else
      {
         return this;
      }
   }

   /**
    * String that represents the function
    * 
    * @return String representation
    * @see Function#toString()
    */
   @Override
   public String toString()
   {
      return this.name;
   }

   /**
    * Variable list contains in this function
    * 
    * @return Variable list contains in this function
    * @see Function#variableList()
    */
   @Override
   public VariableList variableList()
   {
      final VariableList list = new VariableList();
      list.add(this);
      return list;
   }
}