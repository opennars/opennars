/**
 */
package jhelp.util.math.formal;

/**
 * Example of use of formal compute <br>
 * <br>
 * 
 * @author JHelp
 */
public class UseExample
{
   /**
    * Parse the string and show each step of simplification
    * 
    * @param string
    *           String to parse
    * @return Simplified function
    */
   static Function simplifyTests(final String string)
   {
      System.out.println("Treat of : " + string);
      Function function = Function.parse(string);
      System.out.println("\tResult function : " + function.toString());
      System.out.println("\tSimplify :");
      function = function.simplifyMaximum(System.out);
      System.out.println("\tResult :" + function.toString());
      System.out.println();
      return function;
   }

   /**
    * Launch the example
    * 
    * @param args
    *           Unused
    */
   public static void main(final String[] args)
   {
      Function function = UseExample.simplifyTests("x+y+z+a+b+c+d+e+f-x-y-z-a-b-c-d-e-f");
      function = UseExample.simplifyTests("x-a+x-a");
      function = UseExample.simplifyTests("cos(x)*sin(y)");
      System.out.println("\tCompute derive in x :");
      Function derive = function.derive("x").simplifyMaximum(System.out);
      System.out.println(function + " --dx--> " + derive);
      System.out.println("\tCompute derive in  y :");
      derive = function.derive("y").simplifyMaximum(System.out);
      System.out.println(function + " --dy--> " + derive);
      System.out.println("\tCompute total derive  :");
      derive = function.totalDerive().simplifyMaximum(System.out);
      System.out.println(function + " --/\\--> " + derive);
      System.out.println();
      System.out.println("Replace x by z - y in " + derive.toString() + " : ");
      derive = derive.replace("x", Function.parse("z-y"));
      System.out.println("\t" + derive.toString());
      System.out.println("\tSimplify : ");
      derive.simplifyMaximum(System.out);
      function = UseExample.simplifyTests("cos(vis)*cos(vis)+sin(vis)*sin(vis)");
      function = UseExample.simplifyTests("sin(rabit)*sin(rabit)+cos(rabit)*cos(rabit)");
      function = UseExample.simplifyTests("PI*zebra/E");
      function = function.replace("zebra", 1);
      function = function.simplifyMaximum(System.out);
      function = UseExample.simplifyTests("bear+zebra");
      function = function.replace("zebra", new Variable("bear"));
      function = function.simplifyMaximum(System.out);
      function = UseExample.simplifyTests("-bear+zebra");
      function = function.replace("zebra", new Variable("bear"));
      function = function.simplifyMaximum(System.out);

      System.out.println("------------------------------------------------------------------");
      UseExample.printInformations(Function.parse("3*x+exp(y*ln(x))"));
      System.out.println("------------------------------------------------------------------");
      UseExample.printInformations(Function.parse("3*x+exp(ln(y)+ln(x))"));
      System.out.println("------------------------------------------------------------------");
      UseExample.printInformations(Function.parse("ln(x)+exp(y)+sin(z)/cos(z*ln(x))"));

   }

   /**
    * Print informations about a function
    * 
    * @param function
    *           Function to print
    */
   public static void printInformations(Function function)
   {
      System.out.println((new StringBuilder("Function : ")).append(function).toString());
      System.out.println("Simplify : ");
      function = function.simplifyMaximum(System.out);
      final Function deriveX = function.derive(new Variable("x"));
      System.out.println((new StringBuilder("Derive in x : ")).append(deriveX).toString());
      System.out.println("Simplify : ");
      deriveX.simplifyMaximum(System.out);
      final Function deriveY = function.derive(new Variable("y"));
      System.out.println((new StringBuilder("Derive in y : ")).append(deriveY).toString());
      System.out.println("Simplify : ");
      deriveY.simplifyMaximum(System.out);
      final Function deriveZ = function.derive(new Variable("z"));
      System.out.println((new StringBuilder("Derive in z : ")).append(deriveZ).toString());
      System.out.println("Simplify : ");
      deriveZ.simplifyMaximum(System.out);
      final Function e = Function.parse("E");
      Function f = function.replace(new Variable("x"), e);
      System.out.println((new StringBuilder("x=E : ")).append(f).toString());
      System.out.println("Simplify : ");
      f.simplifyMaximum(System.out);
      f = function.replace(new Variable("x"), new Constant(0.1d));
      System.out.println((new StringBuilder("x=0.1 : ")).append(f).toString());
      System.out.println("Simplify : ");
      f.simplifyMaximum(System.out);
   }
}