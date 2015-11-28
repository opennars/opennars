package jhelp.util.math.formal;

import java.util.Hashtable;

/**
 * Default constant reference <br>
 * <br>
 * 
 * @author JHelp
 */
public class ConstantsReferencesDefault
      implements ConstantsReferences
{
   /**
    * Symbols name
    */
   private static final String SYMBOLS_NAMES[]  =
                                                {
         "PI", "E"
                                                };
   /**
    * Actual number of symbol define
    */
   private static final int    SYMBOLS_NUMBER   = ConstantsReferencesDefault.SYMBOLS_NAMES.length;
   /**
    * Value of symbols
    */
   private static final double SYMBOLS_VALUES[] =
                                                {
         Math.PI, Math.E
                                                };

   /**
    * Create default constant reference
    * 
    * @return Default constant reference
    */
   public static ConstantsReferences getReferentielConstanteCourant()
   {
      final ConstantsReferencesDefault rcd = new ConstantsReferencesDefault();
      for(int i = 0; i < ConstantsReferencesDefault.SYMBOLS_NUMBER; i++)
      {
         rcd.defineConstant(ConstantsReferencesDefault.SYMBOLS_NAMES[i], ConstantsReferencesDefault.SYMBOLS_VALUES[i]);
      }
      return rcd;
   }

   /**
    * Table of associate symbol<->value
    */
   private final Hashtable<String, Double> table;

   /**
    * Constructs an empty constant reference
    */
   public ConstantsReferencesDefault()
   {
      this.table = new Hashtable<String, Double>();
   }

   /**
    * Constant on function representation
    * 
    * @param constant
    *           Symbol
    * @return Function representation
    * @see ConstantsReferences#constantFunction(String)
    */
   @Override
   public Function constantFunction(final String constant)
   {
      final Double d = this.table.get(constant);
      if(d != null)
      {
         return new Constant(d.doubleValue());
      }
      else
      {
         return new Variable(constant);
      }
   }

   /**
    * Define a constant
    * 
    * @param nom
    *           Constant name
    * @param valeur
    *           Constant value
    * @see ConstantsReferences#defineConstant(String, double)
    */
   @Override
   public void defineConstant(final String nom, final double valeur)
   {
      this.table.put(nom, new Double(valeur));
   }

   /**
    * Indicate if constants is define
    * 
    * @param constant
    *           Symbol tested
    * @return {@code true} if constants is definee
    * @see ConstantsReferences#isConstantDefine(String)
    */
   @Override
   public boolean isConstantDefine(final String constant)
   {
      return this.table.containsKey(constant);
   }

   /**
    * Constant value
    * 
    * @param constant
    *           Symbol search
    * @return Constant value
    * @see ConstantsReferences#obtainConstantValue(String)
    */
   @Override
   public double obtainConstantValue(final String constant)
   {
      final Double d = this.table.get(constant);
      if(d != null)
      {
         return d.doubleValue();
      }
      else
      {
         return(0.0D / 0.0D);
      }
   }
}