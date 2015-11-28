/**
 * Project : JHelpEngine<br>
 * Package : jhelp.gui.layout<br>
 * Class : VerticalLyoutConstraints<br>
 * Date : 29 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.layout;

/**
 * Vertical layout constraints<br>
 * <br>
 * Last modification : 29 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public final class VerticalLayoutConstraints
      implements Constraints
{
   /** Next code */
   private static int NEXT_CODE = 0;

   /**
    * Create new Vertical layout constraints
    * 
    * @return Vertical layout constraints
    */
   public static VerticalLayoutConstraints obtainVerticalLayoutConstraints()
   {
      return new VerticalLayoutConstraints();
   }

   /** Layout code */
   private final int code;

   /**
    * Constructs VerticalLyoutConstraints
    */
   private VerticalLayoutConstraints()
   {
      this.code = VerticalLayoutConstraints.NEXT_CODE++;
   }

   /**
    * Compare with an other constraints
    * 
    * @param constraints
    *           Constraints to compare
    * @return {@code true} if equals
    * @see Constraints#equals(Constraints)
    */
   @Override
   public boolean equals(final Constraints constraints)
   {
      return ((VerticalLayoutConstraints) constraints).code == this.code;
   }

   /**
    * Hash code
    * 
    * @return Hash code
    * @see Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return this.code;
   }
}