package jhelp.util.math.formal;

/**
 * Constants references.<br>
 * A reference associate a symbol to a real value as PI, e, ... <br>
 * The symbol will be replace by its value when simplification occurs <br>
 * <br>
 * 
 * @author JHelp
 */
public interface ConstantsReferences
{
   /**
    * Function representation of the constant
    * 
    * @param s
    *           Symbol search
    * @return Function representation or {@code null} if no symbol match
    */
   public abstract Function constantFunction(String s);

   /**
    * Define a constant
    * 
    * @param s
    *           Symbol associate
    * @param d
    *           Constant value
    */
   public abstract void defineConstant(String s, double d);

   /**
    * Indicates is a symbol is a define constant
    * 
    * @param s
    *           Symbol tested
    * @return {@code true} if the symbol defined
    */
   public abstract boolean isConstantDefine(String s);

   /**
    * Real value of constant
    * 
    * @param s
    *           Symbol search
    * @return Constant value or {@link Double#NaN} if not define
    */
   public abstract double obtainConstantValue(String s);
}