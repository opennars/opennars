/**
 * Project : JHelpEngine<br>
 * Package : jhelp.gui.layout<br>
 * Class : Layout<br>
 * Date : 29 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.layout;

/**
 * Layout components<br>
 * <br>
 * Last modification : 29 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public abstract class Layout
{
   /**
    * Constructs Layout
    */
   public Layout()
   {
   }

   /**
    * Compute preferred size
    * 
    * @param width
    *           Space width
    * @param height
    *           Space height
    * @param elements
    *           Elements to layout
    * @return Preferred size
    */
   public abstract Dimension computePrefferedSize(int width, int height, LayoutElement... elements);

   /**
    * Layout components
    * 
    * @param width
    *           Space with
    * @param height
    *           Space height
    * @param elements
    *           Element to layout
    */
   public abstract void layout(int width, int height, LayoutElement... elements);
}