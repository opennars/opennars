/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.gui.layout<br>
 * Class : BorderLayoutConstraints<br>
 * Date : 26 juin 2010<br>
 * By JHelp
 */
package jhelp.engine.gui.layout;

/**
 * Border layout constraints<br>
 * <br>
 * Last modification : 26 juin 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public enum BorderLayoutConstraints
      implements Constraints
{
   /** Bottom position */
   BOTTOM,
   /** Bottom left position */
   BOTTOM_LEFT,
   /** Bottom right position */
   BOTTOM_RIGHT,
   /** Center position */
   CENTER,
   /** Left position */
   LEFT,
   /** Right position */
   RIGHT,
   /** Top position */
   TOP,
   /** Top left position */
   TOP_LEFT,
   /** Top right position */
   TOP_RIGHT;
   /**
    * Indicates if constraints equals
    * 
    * @param constraints
    *           Constraints test
    * @return {@code true} if equals
    * @see Constraints#equals(Constraints)
    */
   @Override
   public boolean equals(final Constraints constraints)
   {
      return constraints == this;
   }
}