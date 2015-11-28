/**
 * Project : JHelpEngine<br>
 * Package : jhelp.gui.layout<br>
 * Class : VerticalLyoutConstraints<br>
 * Date : 29 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.layout;

/**
 * Constraints for horizontal layout <br>
 * <br>
 * Last modification : 29 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class HorizontalLayoutConstraints
      implements Constraints
{
   /** Next ID */
   private static int NEXT_CODE = 0;

   /**
    * Give a horizontal layout constraints
    * 
    * @return Horizontal layout constraints
    */
   public static HorizontalLayoutConstraints obtainHorizontalLayoutConstraints()
   {
      return new HorizontalLayoutConstraints();
   }

   /** Constraints ID */
   private final int code;

   /**
    * Constructs HorizontalLayoutConstraints
    */
   private HorizontalLayoutConstraints()
   {
      this.code = HorizontalLayoutConstraints.NEXT_CODE++;
   }

   /**
    * Indicates if a constraints is equals
    * 
    * @param constraints
    *           Constraints test
    * @return {@code true} if equals
    * @see Constraints#equals(Constraints)
    */
   @Override
   public boolean equals(final Constraints constraints)
   {
      return ((HorizontalLayoutConstraints) constraints).code == this.code;
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