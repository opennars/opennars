package jhelp.util.math.formal;

import jhelp.util.Utilities;

/**
 * Operator with 2 parameters <br>
 * <br>
 * 
 * @author JHelp
 */
public abstract class BinaryOperator
      extends Function
{
   /**
    * Symbols list of operator concern.<br>
    * Operators are sort by priority, the less priority to the most
    */
   private static final String OPERATORS[] =
                                           {
         "+", "-", "*", "/"
                                           };

   /**
    * Try parse a string to a binary operator.<br>
    * {@code null} is return if the string is not a binary operator
    * 
    * @param string
    *           String to parse
    * @return Binary operator or {@code null}
    */
   protected static BinaryOperator parseBinaryOperator(final String string)
   {
      String firstParameter = Function.getArgument(string);
      int operatorIndex = firstParameter.length() + 2;

      if(firstParameter.length() >= string.length())
      {
         final int nbOfCharacter = string.length();
         operatorIndex = -1;
         int parenthesisDeepCount = 0;

         for(int i = 0; (i < nbOfCharacter) && (operatorIndex < 0); i++)
         {
            final char car = string.charAt(i);
            switch(car)
            {
               case '(':
                  parenthesisDeepCount++;
               break;
               case ')':
                  parenthesisDeepCount--;
               break;
               default:
                  if((parenthesisDeepCount == 0) && (Utilities.indexOf(BinaryOperator.OPERATORS, String.valueOf(car)) >= 0))
                  {
                     operatorIndex = i;
                  }
               break;
            }
         }

         if(operatorIndex < 0)
         {
            return null;
         }

         firstParameter = string.substring(0, operatorIndex);
      }

      int index = -1;
      for(int i = 0; i < BinaryOperator.OPERATORS.length; i++)
      {
         if(string.startsWith(BinaryOperator.OPERATORS[i], operatorIndex))
         {
            index = i;

            break;
         }
      }

      if(index >= 0)
      {
         final String secondParameter = Function.getArgument(string.substring(operatorIndex + BinaryOperator.OPERATORS[index].length()));
         final Function func1 = Function.parse(firstParameter);
         final Function func2 = Function.parse(secondParameter);
         switch(index)
         {
            case 0: // Addition
               return new Addition(func1, func2);
            case 1: // Substraction
               return new Subtraction(func1, func2);
            case 2: // Multiplication
               return new Multiplication(func1, func2);
            case 3: // Division
               return new Division(func1, func2);
         }
      }

      return null;
   }

   /**
    * Operator string representation
    */
   private final String     operatorStringRepresentation;

   /**
    * First parameter
    */
   protected final Function parameter1;
   /**
    * Second parameter
    */
   protected final Function parameter2;

   /**
    * Constructs a binary operator
    * 
    * @param operatorStringRepresentation
    *           String represents the operator
    * @param parameter1
    *           First parameter
    * @param parameter2
    *           Second parameter
    */
   public BinaryOperator(final String operatorStringRepresentation, final Function parameter1, final Function parameter2)
   {
      this.operatorStringRepresentation = operatorStringRepresentation;
      this.parameter1 = parameter1;
      this.parameter2 = parameter2;
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
      final BinaryOperator binaryOperator = (BinaryOperator) function;

      final int comp = this.parameter1.compareTo(binaryOperator.parameter1);

      if(comp != 0)
      {
         return comp;
      }

      return this.parameter2.compareTo(binaryOperator.parameter2);
   }

   /**
    * First parameter in binary operation
    * 
    * @return First parameter in binary operation
    */
   public Function getParameter1()
   {
      return this.parameter1;
   }

   /**
    * Second parameter in binary operation
    * 
    * @return Second parameter in binary operation
    */
   public Function getParameter2()
   {
      return this.parameter2;
   }

   /**
    * Indicates if function can see as real number, that is to say that the value of {@link #obtainRealValueNumber()} as as
    * meaning
    * 
    * @return {@code false}
    * @see Function#isRealValueNumber()
    */
   @Override
   public boolean isRealValueNumber()
   {
      return false;
   }

   /**
    * Real value of function, if the function can be represents by a real number. Else {@link Double#NaN} is return
    * 
    * @return {@link Double#NaN}
    * @see Function#obtainRealValueNumber()
    */
   @Override
   public double obtainRealValueNumber()
   {
      return(0.0D / 0.0D);
   }

   /**
    * String representation of the function
    * 
    * @return String representation of the function
    * @see Function#toString()
    */
   @Override
   public String toString()
   {
      final StringBuffer string1 = new StringBuffer(this.parameter1.toString());
      if(!(this.parameter1 instanceof Constant) && !(this.parameter1 instanceof Variable) && !(this.parameter1 instanceof UnaryOperator))
      {
         string1.insert(0, '(');
         string1.append(')');
      }
      else
      {
         string1.append(' ');
      }

      final StringBuffer string2 = new StringBuffer(this.parameter2.toString());
      if(!(this.parameter2 instanceof Constant) && !(this.parameter2 instanceof Variable) && !(this.parameter2 instanceof UnaryOperator))
      {
         string2.insert(0, '(');
         string2.append(')');
      }
      else
      {
         string2.insert(0, ' ');
      }

      string1.append(this.operatorStringRepresentation);
      string1.append(string2);

      return string1.toString();
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
      final VariableList liste = this.parameter1.variableList();
      liste.add(this.parameter2.variableList());
      return liste;
   }
}