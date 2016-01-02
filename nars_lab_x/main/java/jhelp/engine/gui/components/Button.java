/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.gui.components<br>
 * Class : Button<br>
 * Date : 5 aoet 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.components;

import jhelp.engine.Texture;
import jhelp.engine.gui.events.ButtonClickListener;
import jhelp.engine.util.ColorsUtil;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * A button <br>
 * <br>
 * Last modification : 5 aoet 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Button
      extends Component
{
   /** Indicates if the button is clicked */
   private boolean                              clicked;
   /** Text font */
   private Font                                 font;
   /** Text color */
   private Color                                foreGround;
   /** Listeners of button click */
   private final ArrayList<ButtonClickListener> listeners;
   /** Button text */
   private String                               text;

   /**
    * Constructs Button
    * 
    * @param text
    *           Button text
    */
   public Button(final String text)
   {
      if(text == null)
      {
         throw new NullPointerException("text musn't be null");
      }

      this.text = text;
      this.foreGround = Color.BLACK;
      this.font = new Font("Arial", Font.BOLD, 20);
      this.computePreferredSize();
      this.clicked = false;

      this.listeners = new ArrayList<ButtonClickListener>(10);
   }

   /**
    * Compute preferred size
    */
   private void computePreferredSize()
   {
      Rectangle2D rectangle2D = this.font.getStringBounds(this.text, Texture.CONTEXT);

      this.preferredWidth = (int) rectangle2D.getWidth() + 10;
      this.preferredHeight = (int) rectangle2D.getHeight() + 10;

      rectangle2D = null;
   }

   /**
    * Call when mouse click on button
    * 
    * @param x
    *           Mouse X
    * @param y
    *           Mouse Y
    * @param buttonLeft
    *           Indicates if button left is down
    * @param buttonRight
    *           Indicates if button right is down
    * @see jhelp.engine.gui.components.Component#mouseClick(int, int, boolean, boolean)
    */
   @Override
   protected void mouseClick(final int x, final int y, final boolean buttonLeft, final boolean buttonRight)
   {
      this.clicked = true;
      this.needRefresh = true;

      for(final ButtonClickListener buttonClickListener : this.listeners)
      {
         buttonClickListener.buttonClick(this);
      }
   }

   /**
    * Draw the button on texture
    * 
    * @param texture
    *           Texture where paint
    * @param x
    *           X location
    * @param y
    *           Y loactaion
    * @see jhelp.engine.gui.components.Component#paintComponent(jhelp.engine.Texture, int, int)
    */
   @Override
   protected void paintComponent(final Texture texture, final int x, final int y)
   {
      texture.fillString(x + 5, y + 5, this.text, this.foreGround, this.font, true);

      if(this.clicked == true)
      {
         texture.drawLine(x, y, x + this.width, y, ColorsUtil.SHADOW_DARK, true);
         texture.drawLine(x, y + 1, x, y + this.height, ColorsUtil.SHADOW_DARK, true);

         texture.drawLine(x + 1, y + 1, (x + this.width) - 1, y + 1, ColorsUtil.SHADOW, true);
         texture.drawLine(x + 1, y + 2, x + 1, (y + this.height) - 1, ColorsUtil.SHADOW, true);

         texture.drawLine(x + 1, y + this.height, x + this.width, y + this.height, ColorsUtil.LIGHT_BRIGHT, true);
         texture.drawLine(x + this.width, y + 1, x + this.width, y + this.height, ColorsUtil.LIGHT_BRIGHT, true);

         texture.drawLine(x + 2, (y + this.height) - 1, (x + this.width) - 1, (y + this.height) - 1, ColorsUtil.LIGHT, true);
         texture.drawLine((x + this.width) - 1, y + 2, (x + this.width) - 1, (y + this.height) - 1, ColorsUtil.LIGHT, true);

         this.clicked = false;
         this.needRefresh = true;
      }
      else
      {
         texture.drawLine(x, y, x + this.width, y, ColorsUtil.LIGHT_BRIGHT, true);
         texture.drawLine(x, y + 1, x, y + this.height, ColorsUtil.LIGHT_BRIGHT, true);

         texture.drawLine(x + 1, y + 1, (x + this.width) - 1, y + 1, ColorsUtil.LIGHT, true);
         texture.drawLine(x + 1, y + 2, x + 1, (y + this.height) - 1, ColorsUtil.LIGHT, true);

         texture.drawLine(x + 1, y + this.height, x + this.width, y + this.height, ColorsUtil.SHADOW, true);
         texture.drawLine(x + this.width, y + 1, x + this.width, y + this.height, ColorsUtil.SHADOW, true);

         texture.drawLine(x + 2, (y + this.height) - 1, (x + this.width) - 1, (y + this.height) - 1, ColorsUtil.SHADOW_DARK, true);
         texture.drawLine((x + this.width) - 1, y + 2, (x + this.width) - 1, (y + this.height) - 1, ColorsUtil.SHADOW_DARK, true);
      }
   }

   /**
    * Add button click listener
    * 
    * @param buttonClickListener
    *           Listener to add
    */
   public void addButtonClickListener(final ButtonClickListener buttonClickListener)
   {
      this.listeners.add(buttonClickListener);
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
    * Remove button click listener
    * 
    * @param buttonClickListener
    *           Listener to remove
    */
   public void removeButtonClickListener(final ButtonClickListener buttonClickListener)
   {
      this.listeners.remove(buttonClickListener);
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