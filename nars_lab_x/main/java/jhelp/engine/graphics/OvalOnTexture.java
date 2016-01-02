/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.graphics<br>
 * Class : LineOnTexture<br>
 * Date : 23 juin 2009<br>
 * By JHelp
 */
package jhelp.engine.graphics;

import jhelp.engine.Texture;

import java.awt.geom.Ellipse2D;

/**
 * Rectangle on texture<br>
 * <br>
 * Last modification : 23 juin 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
class OvalOnTexture
      extends DrawOnTexture
{
   /** Line color */
   private final Color   color;
   /** Indicates if oval should be fill */
   private final boolean fill;
   /** Indicates if we mix with alpha ({@code true}) or override ( {@code false}) */
   private final boolean mix;
   /** First point's X */
   private final int     x1;
   /** Second point's X */
   private int           x2;
   /** First point's Y */
   private final int     y1;
   /** Second point's Y */
   private int           y2;

   /**
    * Constructs RectangleOnTexture
    * 
    * @param x1
    *           First point's X
    * @param y1
    *           First point's Y
    * @param x2
    *           Second point's X
    * @param y2
    *           Second point's Y
    * @param color
    *           Line color
    * @param fill
    *           Indicate if have to fill the oval
    * @param mix
    *           Indicates if we mix with alpha ({@code true}) or override ({@code false})
    */
   OvalOnTexture(final int x1, final int y1, final int x2, final int y2, final Color color, final boolean fill, final boolean mix)
   {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
      this.color = color;
      this.fill = fill;
      this.mix = mix;
   }

   /**
    * Change oval last point
    * 
    * @param x
    *           Last X
    * @param y
    *           Last Y
    */
   public void changeLastPoint(final int x, final int y)
   {
      this.x2 = x;
      this.y2 = y;
   }

   /**
    * Draw rectangle on texture
    * 
    * @param texture
    *           Texture where draw
    * @see DrawOnTexture#draw(jhelp.engine.Texture)
    */
   @Override
   public void draw(final Texture texture)
   {

      if(this.fill == true)
      {
         texture.fillOval(Math.min(this.x1, this.x2), Math.min(this.y1, this.y2), Math.abs(this.x1 - this.x2) + 1, Math.abs(this.y1 - this.y2) + 1, this.color,
               this.mix);
      }
      else
      {
         texture.draw(new Ellipse2D.Double(Math.min(this.x1, this.x2), Math.min(this.y1, this.y2), Math.abs(this.x1 - this.x2) + 1,
               Math.abs(this.y1 - this.y2) + 1), this.color, this.mix, 5);
      }
   }
}