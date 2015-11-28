/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.graphics<br>
 * Class : DrawOnTexture<br>
 * Date : 23 juin 2009<br>
 * By JHelp
 */
package jhelp.engine.graphics;

import jhelp.engine.Texture;

/**
 * Layer on texture<br>
 * <br>
 * Last modification : 23 juin 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
abstract class DrawOnTexture
{
   /**
    * Draw the layer
    * 
    * @param texture
    *           Texture where draw
    */
   public abstract void draw(Texture texture);

   /**
    * Indicates if this draw change the texture so that any action before is useless
    * 
    * @return {@code true} if this draw change the texture so that any action before is useless
    */
   public boolean isAbsorber()
   {
      return false;
   }
}