package jhelp.util.math.formal;

/**
 * Unary operator <br>
 * <br>
 * 
 * @author JHelp
 */
public abstract class UnaryOperator
      extends Function
{
   /**
    * Symbols of unary operators
    */
   private final static String OPERATORS[] =
                                           {
         "-", "exp", "ln", "cos", "sin", "tan"
                                           };

   /**
    * Try to parse a string to a unary operator.<br>
    * If fails {@code null} is return
    * 
    * @param string
    *           String to parse
    * @return Unary operator or {@code null}
    */
   protected static UnaryOperator parserOperateurUnaire(final String string)
   {
      int indice = -1;
      for(int i = 0; (i < UnaryOperator.OPERATORS.length) && (indice < 0); i++)
      {
         if(string.startsWith(UnaryOperator.OPERATORS[i]))
         {
            indice = i;
         }
      }
      if(indice >= 0)
      {
         int p = 0;
         for(int i = UnaryOperator.OPERATORS[indice].length(); i < (string.length() - 1); i++)
         {
            switch(string.charAt(i))
            {
               default:
               break;
               case '(':
                  p++;
               break;
               case ')':
                  if(--p < 1)
                  {
                     return null;
                  }
               break;
            }
         }
         final Function f = Function.parse(Function.getArgument(string.substring(UnaryOperator.OPERATORS[indice].length())));
         switch(indice)
         {
            case 0: // Unary minus
               return new MinusUnary(f);
            case 1: // Exponential
               return new Exponential(f);
            case 2: // Logarithm
               return new Logarithm(f);
            case 3: // Cosinus
               return new Cosinus(f);
            case 4: // Sinus
               return new Sinus(f);
            case 5: // Tangent
               return new Tangent(f);
         }
      }
      return null;
   }

   /**
    * Operator symbol
    */
   private final String     operator;
   /**
    * Operator parameter
    */
   protected final Function parameter;

   /**
    * Constructs the operator
    * 
    * @param operator
    *           Operator symbol
    * @param parameter
    *           Parameter
    */
   public UnaryOperator(final String operator, final Function parameter)
   {
      this.operator = operator;
      this.parameter = parameter;
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
      final UnaryOperator unaryOperator = (UnaryOperator) function;

      return this.parameter.compareTo(unaryOperator.parameter);
   }

   /**
    * The parameter
    * 
    * @return The parameter
    */
   public Function getParameter()
   {
      return this.parameter;
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
      return false;
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
      return(0.0D / 0.0D);
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
      final StringBuffer sb = new StringBuffer(12);
      sb.append(this.operator);
      sb.append('(');
      sb.append(this.parameter.toString());
      sb.append(')');
      return sb.toString();
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
      return this.parameter.variableList();
   }
}