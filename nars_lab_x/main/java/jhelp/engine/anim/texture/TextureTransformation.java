package jhelp.engine.anim.texture;

import jhelp.engine.Texture;
import jhelp.engine.util.Math3D;

/**
 * Describe a transformation apply to a texture
 * 
 * @author JHelp
 */
public class TextureTransformation
{
   /** Contrast change */
   private float   contrast;
   /** Indicates if colors should be inverted */
   private boolean invertColor;
   /** Shift X on texture */
   private int     shiftX;
   /** Shift Y on texture */
   private int     shiftY;

   /**
    * Create a new instance of TextureTransformation
    */
   public TextureTransformation()
   {
      this.shiftX = 0;
      this.shiftY = 0;
      this.contrast = 1;
      this.invertColor = false;
   }

   /**
    * Apply the transformation to given texture
    * 
    * @param texture
    *           Texture to modify
    */
   public void apply(final Texture texture)
   {
      if((this.shiftX != 0) || (this.shiftY != 0))
      {
         texture.shift(this.shiftX, this.shiftY);
      }

      if(Math3D.equal(this.contrast, 1) == false)
      {
         texture.contrast(this.contrast);
      }

      if(this.invertColor == true)
      {
         texture.invert();
      }

      texture.flush();
   }

   /**
    * Actual contrast value
    * 
    * @return Actual contrast value
    */
   public float getContrast()
   {
      return this.contrast;
   }

   /**
    * Actual shiftX value
    * 
    * @return Actual shiftX value
    */
   public int getShiftX()
   {
      return this.shiftX;
   }

   /**
    * Actual shiftY value
    * 
    * @return Actual shiftY value
    */
   public int getShiftY()
   {
      return this.shiftY;
   }

   /**
    * Actual invertColor value
    * 
    * @return Actual invertColor value
    */
   public boolean isInvertColor()
   {
      return this.invertColor;
   }

   /**
    * Change contrast
    * 
    * @param contrast
    *           New contrast value
    */
   public void setContrast(final float contrast)
   {
      this.contrast = contrast;
   }

   /**
    * Change invertColor
    * 
    * @param invertColor
    *           New invertColor value
    */
   public void setInvertColor(final boolean invertColor)
   {
      this.invertColor = invertColor;
   }

   /**
    * Do a shift on texture
    * 
    * @param shiftX
    *           Shift X
    * @param shiftY
    *           Shift Y
    */
   public void setShift(final int shiftX, final int shiftY)
   {
      this.shiftX = shiftX;
      this.shiftY = shiftY;
   }
}