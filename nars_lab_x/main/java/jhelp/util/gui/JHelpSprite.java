package jhelp.util.gui;


/**
 * A sprite, in an other word an image can be show/hide/translate easily over an other image.<br>
 * To create a sripe use {@link JHelpImage#createSprite(int, int, int, int)}
 * 
 * @author JHelp
 */
public final class JHelpSprite
{
   /** Back image before show the sprite */
   private JHelpImage back;
   /** Sprite height */
   private final int  height;
   /** Image draw on the sprite */
   private JHelpImage image;
   /** Parent that contains the sprite */
   private JHelpImage parent;
   /** Sprite index in the parent */
   private int        spriteIndex;
   /** Indicates actual visibility */
   private boolean    visible;

   /** Sprite width */
   private final int  width;

   /** Sprite X */
   private int        x;
   /** Sprite Y */
   private int        y;

   /**
    * Create a new instance of JHelpSprite
    * 
    * @param x
    *           Start X
    * @param y
    *           Start Y
    * @param width
    *           Sprite width
    * @param height
    *           Sprite height
    * @param parent
    *           Image parent
    * @param spriteIndex
    *           Sprite index
    */
   JHelpSprite(final int x, final int y, final int width, final int height, final JHelpImage parent, final int spriteIndex)
   {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.parent = parent;

      this.image = new JHelpImage(width, height);
      this.back = new JHelpImage(width, height);

      this.visible = false;
      this.spriteIndex = spriteIndex;
   }

   /**
    * Create a new instance of JHelpSprite
    * 
    * @param x
    *           X start position
    * @param y
    *           Y start position
    * @param source
    *           Image source
    * @param parent
    *           Image parent
    * @param spriteIndex
    *           Sprite index
    */
   JHelpSprite(final int x, final int y, final JHelpImage source, final JHelpImage parent, final int spriteIndex)
   {
      this.x = x;
      this.y = y;
      this.width = source.getWidth();
      this.height = source.getHeight();
      this.parent = parent;

      this.image = source;
      this.back = new JHelpImage(this.width, this.height);

      this.visible = false;
      this.spriteIndex = spriteIndex;
   }

   /**
    * Change internally the sprite visibility
    * 
    * @param visible
    *           New visible state
    * @param forced
    *           Indicates if the change is forced
    */
   void changeVisible(final boolean visible, final boolean forced)
   {
      if(visible == true)
      {
         this.image.refresh();
      }

      if(this.visible != visible)
      {
         if(this.visible == true)
         {
            this.parent.drawImageOver(this.x, this.y, this.back, 0, 0, this.width, this.height);
         }
         else
         {
            this.back.drawImageOver(0, 0, this.parent, this.x, this.y, this.width, this.height);
            this.parent.drawImageInternal(this.x, this.y, this.image, 0, 0, this.width, this.height, true);
         }

         this.visible = visible;
      }
      else if((forced == true) && (this.visible == true))
      {
         this.parent.drawImageOver(this.x, this.y, this.back, 0, 0, this.width, this.height);
         this.parent.drawImageInternal(this.x, this.y, this.image, 0, 0, this.width, this.height, true);
      }
   }

   /**
    * Sprite index
    * 
    * @return Sprite index
    */
   int getSpriteIndex()
   {
      return this.spriteIndex;
   }

   /**
    * Change parent image
    * 
    * @param parent
    *           New parent image
    */
   void setParent(final JHelpImage parent)
   {
      this.parent = parent;
   }

   /**
    * Change sprite index
    * 
    * @param spriteIndex
    *           New sprite index
    */
   void setSpriteIndex(final int spriteIndex)
   {
      this.spriteIndex = spriteIndex;
   }

   /**
    * Call by garbage collector when want free some memory <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @throws Throwable
    *            On issue
    * @see Object#finalize()
    */
   @Override
   protected void finalize() throws Throwable
   {
      this.back = null;
      this.image = null;
      this.parent = null;

      super.finalize();
   }

   /**
    * Sprite height
    * 
    * @return Sprite height
    */
   public int getHeight()
   {
      return this.height;
   }

   /**
    * Image for draw on the sprite
    * 
    * @return Image for draw on the sprite
    */
   public JHelpImage getImage()
   {
      return this.image;
   }

   /**
    * The sprite parent. In other words, the {@link JHelpImage} where the sprite is attach to
    * 
    * @return The sprite parent
    */
   public JHelpImage getParent()
   {
      return this.parent;
   }

   /**
    * Sprite width
    * 
    * @return Sprite width
    */
   public int getWidth()
   {
      return this.width;
   }

   /**
    * X position
    * 
    * @return X position
    */
   public int getX()
   {
      return this.x;
   }

   /**
    * Y position
    * 
    * @return Y position
    */
   public int getY()
   {
      return this.y;
   }

   /**
    * Indicates if sprite is visible
    * 
    * @return {@code true} if sprite is visible
    */
   public boolean isVisible()
   {
      return this.visible;
   }

   /**
    * Change sprite position
    * 
    * @param x
    *           New X
    * @param y
    *           New Y
    */
   public void setPosition(final int x, final int y)
   {
      if((this.x != x) || (this.y != y))
      {
         final boolean visible = this.visible;

         if(visible == true)
         {
            this.setVisible(false);
         }

         this.x = x;
         this.y = y;

         if(visible == true)
         {
            this.setVisible(true);
         }
      }
   }

   /**
    * Change sprite visibility
    * 
    * @param visible
    *           New visible state
    */
   public void setVisible(final boolean visible)
   {
      this.parent.changeSpriteVisibiliy(this.spriteIndex, visible, false);
   }

   /**
    * Translate the sprite
    * 
    * @param vx
    *           X of translation vector
    * @param vy
    *           Y of translation vector
    */
   public void translate(final int vx, final int vy)
   {
      this.setPosition(vx + this.x, vy + this.y);
   }
}