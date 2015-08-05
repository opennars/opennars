package jhelp.util.math.formal;

import jhelp.util.text.UtilText;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Represents a mathematical function in formal format <br>
 * <br>
 * 
 * @author JHelp
 */
public abstract class Function
      implements Comparable<Function>
{
   /**
    * Simplifier of function by default.<br>
    * It does nothing, it just return the function itself
    * 
    * @author JHelp
    */
   class DefaultSimplifier
         implements FunctionSimplifier
   {
      /**
       * Simplify the function <br>
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
         return Function.this;
      }
   }

   /**
    * Actual constant reference
    */
   protected static ConstantsReferences     references = ConstantsReferencesDefault.getReferentielConstanteCourant();

   /** Comparator of 2 functions */
   public static final Comparator<Function> COMPARATOR = new Comparator<Function>()
                                                       {
                                                          /**
                                                           * Compare 2 functions
                                                           * 
                                                           * @param function1
                                                           *           First function
                                                           * @param function2
                                                           *           Second function
                                                           * @return Comparison result
                                                           */
                                                          @Override
                                                          public int compare(final Function function1, final Function function2)
                                                          {
                                                             return function1.compareTo(function2);
                                                          }
                                                       };

   /**
    * Add parentheses to respect the operator priority
    * 
    * @param string
    *           String to add parentheses
    * @return String with parentheses
    */
   private static String addParentheses(final String string)
   {
      final int nb = string.length();
      int oper = -1;
      int mark = -1;
      int p = 0;

      // Search the operator max priority index
      for(int i = nb - 1; i >= 0; i--)
      {
         switch(string.charAt(i))
         {
            case ',':
            case '.':
            default:
            break;
            case '+':
               if((p == 0) && (oper <= 0))
               {
                  mark = i;
                  oper = 0;
               }
            break;
            case '-':
               if((p == 0) && (oper <= 1))
               {
                  mark = i;
                  oper = 1;
               }
            break;
            case '*':
               if((p == 0) && (oper <= 2))
               {
                  mark = i;
                  oper = 2;
               }
            break;
            case '/':
               if((p == 0) && (oper <= 3))
               {
                  mark = i;
                  oper = 3;
               }
            break;
            case ')':
               p++;
            break;
            case '(':
               p--;
            break;
         }
      }

      if(mark < 1)
      {
         return string;
      }

      // Search second parameter
      int max = mark + 1;
      boolean b = true;
      p = 0;
      while((max < nb) && b)
      {
         final char car = string.charAt(max);
         if((p == 0) && Function.isBinaryOperator(car))
         {
            b = false;
         }
         if(car == '(')
         {
            p++;
         }
         if(car == ')')
         {
            p--;
         }
         if(b)
         {
            max++;
         }
      }

      // Search first parameter
      int min = mark - 1;
      b = true;
      p = 0;
      while((min > 0) && b)
      {
         final char car = string.charAt(min);
         if((p == 0) && Function.isBinaryOperator(car))
         {
            b = false;
         }
         if(car == '(')
         {
            p--;
         }
         if(car == ')')
         {
            p++;
         }
         if(b)
         {
            min--;
         }
      }

      // Add parentheses
      final StringBuffer sb = new StringBuffer(nb + 2);
      if(min == 0)
      {
         min = -1;
      }
      if(min > 0)
      {
         sb.append(string.substring(0, min + 1));
      }
      sb.append('(');
      sb.append(string.substring(min + 1, max));
      sb.append(')');
      if(max < nb)
      {
         sb.append(string.substring(max));
      }

      // Look if need add more parentheses
      return Function.addParentheses(sb.toString());
   }

   /**
    * Indicates if a character is binary operator
    * 
    * @param car
    *           Tested character
    * @return {@code true} if character is binary operator
    */
   private static boolean isBinaryOperator(final char car)
   {
      return (car == '+') || (car == '-') || (car == '*') || (car == '/');
   }

   /**
    * Give the "parameter" of the string
    * 
    * @param string
    *           String to parse
    * @return Extracted "parameter"
    */
   protected static String getArgument(final String string)
   {
      if(!string.startsWith("("))
      {
         return string;
      }

      final int nb = string.length();
      final StringBuffer sb = new StringBuffer(nb);
      int p = 1;
      for(int i = 1; (i < nb) && (p > 0); i++)
      {
         final char character = string.charAt(i);
         switch(character)
         {
            case '(':
               p++;
               sb.append(character);
            break;
            case ')':
               if(--p > 0)
               {
                  sb.append(character);
               }
            break;
            default:
               sb.append(character);
            break;
         }
      }

      return sb.toString().intern();
   }

   /**
    * Indicates if several functions are equals each others
    * 
    * @param function
    *           Function reference
    * @param functions
    *           Functions tests to be equals to the references
    * @return {@code true} if all functions are equals to the reference
    */
   public static final boolean allEquals(final Function function, final Function... functions)
   {
      if(function == null)
      {
         throw new NullPointerException("function musn't be null");
      }

      if(functions == null)
      {
         throw new NullPointerException("functions musn't be null");
      }

      for(final Function func : functions)
      {
         if(function.equals(func) == false)
         {
            return false;
         }
      }

      return true;
   }

   /**
    * Create an addition of several functions
    * 
    * @param functions
    *           Functions list
    * @return Created function
    */
   public static Function createAddition(final Function... functions)
   {
      if(functions == null)
      {
         return Constant.ZERO;
      }

      Arrays.sort(functions, Function.COMPARATOR);

      final int length = functions.length;

      switch(length)
      {
         case 0:
            return Constant.ZERO;

         case 1:
            return functions[0];

         case 2:
            return new Addition(functions[0], functions[1]);

         case 3:
            return new Addition(functions[0], new Addition(functions[1], functions[2]));

         case 4:
            return new Addition(new Addition(functions[0], functions[1]), new Addition(functions[2], functions[3]));
      }

      return new Addition(Function.createAddition(Arrays.copyOfRange(functions, 0, length / 2)), Function.createAddition(Arrays.copyOfRange(functions, length / 2, length)));
   }

   /**
    * Create an multiplication of several functions
    * 
    * @param functions
    *           Functions list
    * @return Created function
    */
   public static Function createMultiplication(final Function... functions)
   {
      if(functions == null)
      {
         return Constant.ZERO;
      }

      Arrays.sort(functions, Function.COMPARATOR);

      final int length = functions.length;

      switch(length)
      {
         case 0:
            return Constant.ZERO;

         case 1:
            return functions[0];

         case 2:
            return new Multiplication(functions[0], functions[1]);

         case 3:
            return new Multiplication(functions[0], new Multiplication(functions[1], functions[2]));

         case 4:
            return new Multiplication(new Multiplication(functions[0], functions[1]), new Multiplication(functions[2], functions[3]));
      }

      return new Multiplication(Function.createMultiplication(Arrays.copyOfRange(functions, 0, length / 2)), Function.createMultiplication(Arrays.copyOfRange(functions, length / 2, length)));
   }

   /**
    * Parse string to function.<br>
    * This function is case sensitive<br>
    * Reserved word/symbol :
    * <table border>
    * <tr>
    * <th>Symbol</th>
    * <th>Explanations</th>
    * </tr>
    * <tr>
    * <td>+</td>
    * <td>Addition</td>
    * </tr>
    * <tr>
    * <td>-</td>
    * <td>Subtraction<br>
    * It also represents the unary minus (Take the opposite sign)</td>
    * </tr>
    * <tr>
    * <td>*</td>
    * <td>Multiplication</td>
    * </tr>
    * <tr>
    * <td>/</td>
    * <td>Division</td>
    * </tr>
    * <tr>
    * <td>cos</td>
    * <td>Cosinus</td>
    * </tr>
    * <tr>
    * <td>exp</td>
    * <td>Exponential</td>
    * </tr>
    * <tr>
    * <td>ln</td>
    * <td>Logarithm Neperian</td>
    * </tr>
    * <tr>
    * <td>sin</td>
    * <td>Sinus</td>
    * </tr>
    * <tr>
    * <td>tan</td>
    * <td>Tangent</td>
    * </tr>
    * </table>
    * Real number are treat as constants. The decimal separator is the dot (.) symbol ex: 3.21 <br>
    * Other symbols/words are treats as variable
    * 
    * @param function
    *           String to parse
    * @return Function parsed
    */
   public static Function parse(String function)
   {
      // For null we return 0
      if(function == null)
      {
         return Constant.ZERO;
      }

      // Remove all white characters
      function = UtilText.removeWhiteCharacters(function);

      // Empty string return 0
      if(function.length() < 1)
      {
         return Constant.ZERO;
      }

      // Start by - => add 0 before
      if(function.startsWith("-") == true)
      {
         function = "0" + function;
      }
      // Test if can be treat as real constant
      try
      {
         final double d = Double.parseDouble(function);
         // If the case, we return the constant
         return new Constant(d);
      }
      catch(final Exception exception)
      {
         // Else we extract the parameter after add the need parentheses to
         // respect the priority
         function = Function.getArgument(Function.addParentheses(function));
      }

      // Try to consider as unary operator
      final UnaryOperator unairy = UnaryOperator.parserOperateurUnaire(function);
      if(unairy != null)
      {
         return unairy;
      }

      // Try to consider as binary operator
      final BinaryOperator binairy = BinaryOperator.parseBinaryOperator(function);
      if(binairy != null)
      {
         return binairy;
      }

      // If all fails, this is a variable
      return new Variable(function);
   }

   /**
    * Modify the constants reference <br>
    * This reference says what variable name can be replace by constant, example : PI replace by
    * 3.1415926535897932384626433832795
    * 
    * @param references
    *           New reference
    */
   public static void setConstantsReferences(final ConstantsReferences references)
   {
      if(references != null)
      {
         Function.references = references;
      }
   }

   /** The default function simplifier */
   private DefaultSimplifier defaultSimplifier;

   /**
    * Function empty
    */
   public Function()
   {
   }

   /**
    * Internal comparison
    * 
    * @param function
    *           Function sure be the instance of the function
    * @return Comparison
    */
   protected abstract int compareToInternal(Function function);

   /**
    * Indicates if function is equals, the equality test is more simple than {@link #functionIsEquals(Function)} its use
    * internally for {@link Function#simplifyMaximum()}
    * 
    * @param function
    *           Function to compare with
    * @return {@code true} if equals
    * @see Function#functionIsEqualsMoreSimple(Function)
    */
   protected boolean functionIsEqualsMoreSimple(final Function function)
   {
      return this.functionIsEquals(function);
   }

   /**
    * Compare with an other function <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param function
    *           Function to compare
    * @return Comparison
    * @see Comparable#compareTo(Object)
    */
   @Override
   public int compareTo(final Function function)
   {
      if(this.functionIsEquals(function) == true)
      {
         return 0;
      }

      if(this.getClass().equals(function.getClass()) == true)
      {
         return this.compareToInternal(function);
      }

      if((this instanceof Constant) == true)
      {
         return -1;
      }

      if((this instanceof Variable) == true)
      {
         if((function instanceof Constant) == true)
         {
            return 1;
         }
         else
         {
            return -1;
         }
      }

      if((this instanceof MinusUnary) == true)
      {
         if(((function instanceof Constant) == true) || ((function instanceof Variable) == true))
         {
            return 1;
         }
         else
         {
            return -1;
         }
      }

      if(((function instanceof Constant) == true) || ((function instanceof Variable) == true) || ((function instanceof MinusUnary) == true))
      {
         return 1;
      }

      return this.getClass().getName().compareTo(function.getClass().getName());
   }

   /**
    * Derive the function
    * 
    * @param variable
    *           Variable for derive
    * @return Derived
    */
   public final Function derive(final String variable)
   {
      return this.derive(new Variable(variable));
   }

   /**
    * Derive with several variable
    * 
    * @param list
    *           Variable list
    * @return Derive
    */
   public final Function derive(final String... list)
   {
      final VariableList li = new VariableList();
      li.add(list);
      return this.derive(li);
   }

   /**
    * Derive the function
    * 
    * @param variable
    *           Variable for derive
    * @return Derived
    */
   public abstract Function derive(Variable variable);

   /**
    * Derive with several variable
    * 
    * @param list
    *           Variable list
    * @return Derive
    */
   public final Function derive(final VariableList list)
   {
      final int nb = list.numberOfVariables();
      Function derive = Constant.ZERO;
      for(int i = 0; i < nb; i++)
      {
         derive = new Addition(derive, this.derive(list.get(i)));
      }
      return derive;
   }

   /**
    * Indicates if an object is equals to the function
    * 
    * @param object
    *           Tested object
    * @return {@code true} if equals
    * @see Object#equals(Object)
    */
   @Override
   public final boolean equals(final Object object)
   {
      if(object == null)
      {
         return false;
      }

      if(object instanceof Function)
      {
         final Function f = (Function) object;

         if(this.functionIsEqualsMoreSimple(f) == true)
         {
            return true;
         }

         return this.functionIsEquals(f);
      }

      return false;
   }

   /**
    * Indicates if a function is equals to this function
    * 
    * @param function
    *           Function tested
    * @return {@code true} if there sure equals. {@code false} dosen't mean not equals, but not sure about equality
    */
   public abstract boolean functionIsEquals(Function function);

   /**
    * Copy the function
    * 
    * @return Copy
    */
   public abstract Function getCopy();

   /**
    * Indicates if function can see as real number, that is to say that the value of {@link #obtainRealValueNumber()} as as
    * meaning
    * 
    * @return {@code true} if the function can see as real number
    */
   public abstract boolean isRealValueNumber();

   /**
    * Obtain the simplifier of the function.<br>
    * Override this function to provide a simplifier that is not the default one
    * 
    * @return Simplifier link to the function
    */
   public FunctionSimplifier obtainFunctionSimplifier()
   {
      if(this.defaultSimplifier == null)
      {
         this.defaultSimplifier = new DefaultSimplifier();
      }

      return this.defaultSimplifier;
   }

   /**
    * Real value of function, if the function can be represents by a real number. Else {@link Double#NaN} is return
    * 
    * @return Variable value or {@link Double#NaN} if not define
    */
   public abstract double obtainRealValueNumber();

   /**
    * Real string representation
    * 
    * @return Real string representation
    */
   public String realString()
   {
      return this.toString();
   }

   /**
    * Replace variable by constant
    * 
    * @param variable
    *           Variable to replace
    * @param constant
    *           Constant for replace
    * @return Result function
    */
   public final Function replace(final String variable, final double constant)
   {
      return this.replace(new Variable(variable), new Constant(constant));
   }

   /**
    * Replace variable by function
    * 
    * @param variable
    *           Variable to replace
    * @param function
    *           Function for replace
    * @return Result function
    */
   public final Function replace(final String variable, final Function function)
   {
      return this.replace(new Variable(variable), function);
   }

   /**
    * Replace variable by constant
    * 
    * @param variable
    *           Variable to replace
    * @param constant
    *           Constant for replace
    * @return Result function
    */
   public final Function replace(final Variable variable, final double constant)
   {
      return this.replace(variable, new Constant(constant));
   }

   /**
    * Replace variable by function
    * 
    * @param variable
    *           Variable to replace
    * @param function
    *           Function for replace
    * @return Result function
    */
   public abstract Function replace(Variable variable, Function function);

   /**
    * Simplify the function
    * 
    * @return Simplified function
    */
   public final Function simplify()
   {
      final FunctionSimplifier functionSimplifier = this.obtainFunctionSimplifier();

      if(functionSimplifier == null)
      {
         return this;
      }

      return functionSimplifier.simplify();
   }

   /**
    * Simplify at maximum the function
    * 
    * @return The most simple version of the function
    */
   public final Function simplifyMaximum()
   {
      final ArrayList<Function> alreadySeen = new ArrayList<Function>();

      alreadySeen.add(this);

      Function f = this.simplify();

      boolean ok = true;

      while(ok == true)
      {
         for(final Function ff : alreadySeen)
         {
            if(ff.functionIsEqualsMoreSimple(f))
            {
               ok = false;
               break;
            }
         }

         if(ok == true)
         {
            alreadySeen.add(f);
            f = f.simplify();
         }
      }

      alreadySeen.clear();

      return f;
   }

   /**
    * Simplify at maximum the function on printing each steps
    * 
    * @param printStream
    *           Where print the steps
    * @return The most simple version of the function
    */
   public final Function simplifyMaximum(final PrintStream printStream)
   {
      final ArrayList<Function> alreadySeen = new ArrayList<Function>();

      printStream.println(this.toString());
      printStream.print("\t -> ");

      alreadySeen.add(this);
      Function f = this.simplify();

      printStream.println(f.toString());

      boolean ok = true;

      while(ok == true)
      {
         for(final Function ff : alreadySeen)
         {
            if(ff.functionIsEqualsMoreSimple(f))
            {
               ok = false;
               break;
            }
         }

         if(ok == true)
         {
            alreadySeen.add(f);
            f = f.simplify();
            printStream.print("\t -> ");
            printStream.println(f.toString());
         }
      }

      alreadySeen.clear();

      return f;
   }

   /**
    * String that represents the function
    * 
    * @return String representation
    * @see Object#toString()
    */
   @Override
   public abstract String toString();

   /**
    * Total derive
    * 
    * @return Total derive
    */
   public final Function totalDerive()
   {
      return this.derive(this.variableList());
   }

   /**
    * Variable list contains in this function
    * 
    * @return Variable list contains in this function
    */
   public abstract VariableList variableList();
}