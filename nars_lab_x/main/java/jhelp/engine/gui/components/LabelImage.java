/**
 * Project : JHelpEngine<br>
 * Package : jhelp.gui.components<br>
 * Class : LabelImage<br>
 * Date : 29 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.components;

import jhelp.engine.Texture;

import java.awt.image.BufferedImage;

/**
 * Label with image<br>
 * <br>
 * Last modification : 29 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class LabelImage
      extends Component
{
   /** Image carry */
   private final BufferedImage bufferedImage;

   /**
    * Constructs LabelImage
    * 
    * @param bufferedImage
    *           Image to carry
    */
   public LabelImage(final BufferedImage bufferedImage)
   {
      if(bufferedImage == null)
      {
         throw new NullPointerException("bufferedImage musn't be null");
      }

      this.bufferedImage = bufferedImage;
      this.preferredWidth = this.bufferedImage.getWidth();
      this.preferredHeight = this.bufferedImage.getHeight();
   }

   /**
    * Call when mouse click
    * 
    * @param x
    *           Mouse X
    * @param y
    *           Mouse Y
    * @param buttonLeft
    *           Indicates if left button down
    * @param buttonRight
    *           Indicates if right button down
    * @see Component#mouseClick(int, int, boolean, boolean)
    */
   @Override
   protected void mouseClick(final int x, final int y, final boolean buttonLeft, final boolean buttonRight)
   {
   }

   /**
    * Draw the label
    * 
    * @param texture
    *           Texture where draw
    * @param x
    *           X
    * @param y
    *           Y
    * @see Component#paintComponent(jhelp.engine.Texture, int, int)
    */
   @Override
   protected void paintComponent(final Texture texture, final int x, final int y)
   {
      texture.drawImage(x, y, this.bufferedImage);
   }
}