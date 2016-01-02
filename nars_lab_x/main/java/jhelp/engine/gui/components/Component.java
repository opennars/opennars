/**
 * Project : JHelpEngine<br>
 * Package : jhelp.gui.components<br>
 * Class : Component<br>
 * Date : 28 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.components;

import jhelp.engine.Texture;
import jhelp.engine.util.ColorsUtil;

/**
 * Generic component<br>
 * <br>
 * Last modification : 28 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public abstract class Component
{
   /** Background */
   private Color     backGround;
   /** Component height */
   protected int     height;
   /** Internal ID */
   protected int     internalID;
   /** Indicates if need to be refresh as soon as possible */
   protected boolean needRefresh;
   /** Preferred height */
   protected int     preferredHeight;
   /** Preferred width */
   protected int     preferredWidth;
   /** Component width */
   protected int     width;
   /** X location */
   protected int     x;
   /** Y location */
   protected int     y;

   /**
    * Constructs Component
    */
   public Component()
   {
      this.backGround = ColorsUtil.TRANSPARENT;
      this.needRefresh = true;
      this.x = this.y = this.width = this.height = this.preferredHeight = this.preferredWidth = 0;
   }

   /**
    * Call when mouse click on component
    * 
    * @param x
    *           Mouse X
    * @param y
    *           Mouse Y
    * @param buttonLeft
    *           Indicates if left button clicked
    * @param buttonRight
    *           Indicates if right button clicked
    */
   protected abstract void mouseClick(int x, int y, boolean buttonLeft, boolean buttonRight);

   /**
    * Draw the component in texture
    * 
    * @param texture
    *           Texture where draw
    * @param x
    *           X position
    * @param y
    *           Y position
    */
   protected abstract void paintComponent(Texture texture, int x, int y);

   /**
    * Return backGround
    * 
    * @return backGround
    */
   public Color getBackGround()
   {
      return this.backGround;
   }

   /**
    * Compute component preferred size
    * 
    * @param dimension
    *           Dimension to fill. If {@code null} a new instance is created
    * @return Preferred size
    */
   public Dimension getPrefrerredSize(Dimension dimension)
   {
      if(dimension == null)
      {
         dimension = new Dimension();
      }

      dimension.width = this.preferredWidth;
      dimension.height = this.preferredHeight;

      return dimension;
   }

   /**
    * Return needRefresh
    * 
    * @return needRefresh
    */
   public boolean isNeedRefresh()
   {
      return this.needRefresh;
   }

   /**
    * Draw the component in texture
    * 
    * @param texture
    *           Texture where draw
    * @param x
    *           X position
    * @param y
    *           Y position
    */
   public final void paint(final Texture texture, final int x, final int y)
   {
      this.needRefresh = false;

      texture.fillRect(x, y, this.width, this.height, this.backGround, true);
      this.paintComponent(texture, x, y);
   }

   /**
    * Call when preferred size need refresh.<br>
    * By default does nothing, because suppose that preferred size don't move, if can move override this method
    */
   public void refreshPreferredSize()
   {
   }

   /**
    * Modify backGround
    * 
    * @param backGround
    *           New backGround value
    */
   public void setBackGround(final Color backGround)
   {
      if(backGround == null)
      {
         throw new NullPointerException("backGround musn't be null");
      }

      if(this.backGround.equals(backGround) == false)
      {
         this.backGround = backGround;

         this.needRefresh = true;
      }
   }

   /**
    * Change component bounds
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param width
    *           Width
    * @param height
    *           Height
    */
   public void setBounds(final int x, final int y, final int width, final int height)
   {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }
}