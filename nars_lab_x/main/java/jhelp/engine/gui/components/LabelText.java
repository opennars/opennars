/**
 * Project : JHelpEngine<br>
 * Package : jhelp.gui.components<br>
 * Class : LabelText<br>
 * Date : 29 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.components;

import jhelp.engine.Texture;

import java.awt.geom.Rectangle2D;

/**
 * Label with text <br>
 * <br>
 * Last modification : 29 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class LabelText
      extends Component
{
   /** Text font */
   private Font   font;
   /** Text color */
   private Color  foreGround;
   /** Text */
   private String text;

   /**
    * Constructs LabelText
    */
   public LabelText()
   {
      this("");
   }

   /**
    * Constructs LabelText
    * 
    * @param text
    *           Text
    */
   public LabelText(final String text)
   {
      if(text == null)
      {
         throw new NullPointerException("text musn't be null");
      }

      this.text = text;
      this.foreGround = Color.BLACK;
      this.font = new Font("Courier", Font.BOLD, 20);
      this.computePreferredSize();
   }

   /**
    * Compute preferred size
    */
   private void computePreferredSize()
   {
      Rectangle2D rectangle2D = this.font.getStringBounds(this.text, Texture.CONTEXT);

      this.preferredWidth = (int) rectangle2D.getWidth();
      this.preferredHeight = (int) rectangle2D.getHeight();

      rectangle2D = null;
   }

   /**
    * Call when mouse click
    * 
    * @param x
    *           Mouse X
    * @param y
    *           Mouse Y
    * @param buttonLeft
    *           Indicates if mouse left button down
    * @param buttonRight
    *           Indicates if mouse right button down
    * @see Component#mouseClick(int, int, boolean, boolean)
    */
   @Override
   protected void mouseClick(final int x, final int y, final boolean buttonLeft, final boolean buttonRight)
   {
   }

   /**
    * Paint the label
    * 
    * @param texture
    *           Texture where paint
    * @param x
    *           X
    * @param y
    *           Y
    * @see Component#paintComponent(jhelp.engine.Texture, int, int)
    */
   @Override
   protected void paintComponent(final Texture texture, final int x, final int y)
   {
      texture.fillString(x, y, this.text, this.foreGround, this.font, true);
   }

   /**
    * Return font
    * 
    * @return font
    */
   public Font getFont()
   {
      return this.font;
   }

   /**
    * Return foreGround
    * 
    * @return foreGround
    */
   public Color getForeGround()
   {
      return this.foreGround;
   }

   /**
    * Return text
    * 
    * @return text
    */
   public String getText()
   {
      return this.text;
   }

   /**
    * Modify font
    * 
    * @param font
    *           New font value
    */
   public void setFont(final Font font)
   {
      if(font == null)
      {
         throw new NullPointerException("font musn't be null");
      }

      if(this.font.equals(font) == false)
      {
         this.font = font;

         this.computePreferredSize();

         this.needRefresh = true;
      }
   }

   /**
    * Modify foreGround
    * 
    * @param foreGround
    *           New foreGround value
    */
   public void setForeGround(final Color foreGround)
   {
      if(foreGround == null)
      {
         throw new NullPointerException("foreGround musn't be null");
      }

      if(this.foreGround.equals(foreGround) == false)
      {
         this.foreGround = foreGround;

         this.needRefresh = true;
      }
   }

   /**
    * Modify text
    * 
    * @param text
    *           New text value
    */
   public void setText(final String text)
   {
      if(text == null)
      {
         throw new NullPointerException("text musn't be null");
      }

      if(this.text.equals(text) == false)
      {
         this.text = text;

         this.computePreferredSize();

         this.needRefresh = true;
      }
   }
}