/**
 * Project : JHelpEngine<br>
 * Package : jhelp.gui.layout<br>
 * Class : Constraints<br>
 * Date : 29 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.layout;

/**
 * Layout constraints<br>
 * <br>
 * Last modification : 29 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public interface Constraints
{
   /**
    * Indicates if equals to other constraints
    * 
    * @param constraints
    *           Constraints tested
    * @return {@code true} if equals to other constraints
    */
   public boolean equals(Constraints constraints);

   /**
    * Hash code
    * 
    * @return Hash code
    */
   @Override
   public int hashCode();
}