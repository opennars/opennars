package jhelp.engine.graphics;

import jhelp.engine.Texture;

/**
 * Order to clear a texture
 * 
 * @author JHelp
 */
class ClearTexture
      extends DrawOnTexture
{
   /** Color used to clear */
   private final Color color;

   /**
    * Create a new instance of ClearTexture
    * 
    * @param color
    *           Color used to clear
    */
   public ClearTexture(final Color color)
   {
      this.color = color;
   }

   /**
    * Clear the given texture <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param texture
    *           Texture to clear
    * @see jhelp.engine.graphics.DrawOnTexture#draw(jhelp.engine.Texture)
    */
   @Override
   public void draw(final Texture texture)
   {
      texture.clear(this.color);
   }

   /**
    * Indicates that this drawing modify completely the texture and can't be reversed <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return {@code true}
    * @see jhelp.engine.graphics.DrawOnTexture#isAbsorber()
    */
   @Override
   public boolean isAbsorber()
   {
      return true;
   }
}