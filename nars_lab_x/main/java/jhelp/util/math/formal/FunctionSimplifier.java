package jhelp.util.math.formal;

/**
 * Function simplifier
 * 
 * @author JHelp
 */
public interface FunctionSimplifier
{
   /**
    * Call when simplify the function
    * 
    * @return More "simple" function
    */
   public Function simplify();
}