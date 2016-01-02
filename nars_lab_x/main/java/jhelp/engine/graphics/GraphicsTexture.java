/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.graphics<br>
 * Class : GraphicsTexture<br>
 * Date : 23 juin 2009<br>
 * By JHelp
 */
package jhelp.engine.graphics;

import jhelp.engine.Texture;

import java.util.ArrayList;

/**
 * Graphics on texture with layer<br>
 * <br>
 * Last modification : 23 juin 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class GraphicsTexture
{
   /** Current dynamic line */
   private LineOnTexture                  dynamicLine;
   /** Current dynamic oval */
   private OvalOnTexture                  dynamicOval;
   /** Current dynamic rectangle */
   private RectangleOnTexture             dynamicRectangle;
   /** Layers */
   private final ArrayList<DrawOnTexture> layers;
   /** Texture where draw */
   private Texture                        texture;

   /**
    * Constructs GraphicsTexture
    * 
    * @param texture
    *           Texture where draw
    */
   public GraphicsTexture(final Texture texture)
   {
      if(texture == null)
      {
         throw new NullPointerException("texture musn't be null");
      }

      this.texture = texture;
      this.layers = new ArrayList<DrawOnTexture>();
   }

   /**
    * Remove all dynamics objects
    */
   private void resetDynamics()
   {
      this.dynamicLine = null;
      this.dynamicRectangle = null;
      this.dynamicOval = null;
   }

   /**
    * Aplly layers on texture
    */
   public void apply()
   {
      for(final DrawOnTexture drawOnTexture : this.layers)
      {
         drawOnTexture.draw(this.texture);
      }

      this.texture.flush();
   }

   /**
    * Clear the texture
    * 
    * @param color
    *           Color to use for clear
    */
   public void clear(final Color color)
   {
      this.resetDynamics();
      this.layers.add(new ClearTexture(color));
   }

   /**
    * Compress the graphics on removing useless texture
    */
   public void compress()
   {
      this.resetDynamics();
      final int size = this.layers.size();

      for(int i = size - 1; i >= 0; i--)
      {
         if(this.layers.get(i).isAbsorber() == true)
         {
            for(int r = i - 1; r >= 0; r--)
            {
               this.layers.remove(r);
            }

            break;
         }
      }
   }

   /**
    * Draw a line
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
    * @param mix
    *           Indicates if we mix alpha ({@code true}) or override ( {@code false})
    */
   public void drawLine(final int x1, final int y1, final int x2, final int y2, final Color color, final boolean mix)
   {
      this.layers.add(new LineOnTexture(x1, y1, x2, y2, color, mix));
   }

   /**
    * Draw an oval
    * 
    * @param x1
    *           First corner X
    * @param y1
    *           First corner Y
    * @param x2
    *           Second/opposite corner X
    * @param y2
    *           Second/opposite corner Y
    * @param color
    *           Color on oval
    * @param fill
    *           Indicates if have to fill the oval
    * @param mix
    *           Indicates if mix colors or just replace
    */
   public void drawOval(final int x1, final int y1, final int x2, final int y2, final Color color, final boolean fill, final boolean mix)
   {
      this.layers.add(new OvalOnTexture(x1, y1, x2, y2, color, fill, mix));
   }

   /**
    * Draw a rectangle
    * 
    * @param x1
    *           First corner X
    * @param y1
    *           First corner Y
    * @param x2
    *           Second/opposite corner X
    * @param y2
    *           Second/opposite corner Y
    * @param color
    *           Color on oval
    * @param fill
    *           Indicates if have to fill the oval
    * @param mix
    *           Indicates if mix colors or just replace
    */
   public void drawRectangle(final int x1, final int y1, final int x2, final int y2, final Color color, final boolean fill, final boolean mix)
   {
      this.layers.add(new RectangleOnTexture(x1, y1, x2, y2, color, fill, mix));
   }

   /**
    * Terminate the dynamic line
    */
   public void endDynamicLine()
   {
      this.dynamicLine = null;
   }

   /**
    * Terminate dynamic line on given point
    * 
    * @param x
    *           End position X
    * @param y
    *           End position Y
    */
   public void endDynamicLine(final int x, final int y)
   {
      if(this.dynamicLine != null)
      {
         this.dynamicLine.changeLastPoint(x, y);
      }

      this.dynamicLine = null;
   }

   /**
    * Terminate the dynamic oval
    */
   public void endDynamicOval()
   {
      this.dynamicOval = null;
   }

   /**
    * Terminate dynamic oval on given point
    * 
    * @param x
    *           End position X
    * @param y
    *           End position Y
    */
   public void endDynamicOval(final int x, final int y)
   {
      if(this.dynamicOval != null)
      {
         this.dynamicOval.changeLastPoint(x, y);
      }

      this.dynamicOval = null;
   }

   /**
    * Terminate the dynamic rectangle
    */
   public void endDynamicRectangle()
   {
      this.dynamicRectangle = null;
   }

   /**
    * Terminate dynamic rectangle on given point
    * 
    * @param x
    *           End position X
    * @param y
    *           End position Y
    */
   public void endDynamicRectangle(final int x, final int y)
   {
      if(this.dynamicRectangle != null)
      {
         this.dynamicRectangle.changeLastPoint(x, y);
      }

      this.dynamicRectangle = null;
   }

   /**
    * Indicates if dynamic line started
    * 
    * @return {@code true} if dynamic line started
    */
   public boolean isDynamicLineStarted()
   {
      return this.dynamicLine != null;
   }

   /**
    * Indicates if dynamic oval started
    * 
    * @return {@code true} if dynamic line started
    */
   public boolean isDynamicOvalStarted()
   {
      return this.dynamicOval != null;
   }

   /**
    * Indicates if dynamic rectangle started
    * 
    * @return {@code true} if dynamic line started
    */
   public boolean isDynamicRectangleStarted()
   {
      return this.dynamicRectangle != null;
   }

   /**
    * Change dynamic line last point
    * 
    * @param x
    *           New last point X
    * @param y
    *           New last point Y
    */
   public void modifyDynamicLine(final int x, final int y)
   {
      if(this.dynamicLine != null)
      {
         this.dynamicLine.changeLastPoint(x, y);
      }
   }

   /**
    * Change dynamic oval last point
    * 
    * @param x
    *           New last point X
    * @param y
    *           New last point Y
    */
   public void modifyDynamicOval(final int x, final int y)
   {
      if(this.dynamicOval != null)
      {
         this.dynamicOval.changeLastPoint(x, y);
      }
   }

   /**
    * Change dynamic rectangle last point
    * 
    * @param x
    *           New last point X
    * @param y
    *           New last point Y
    */
   public void modifyDynamicRectangle(final int x, final int y)
   {
      if(this.dynamicRectangle != null)
      {
         this.dynamicRectangle.changeLastPoint(x, y);
      }
   }

   /**
    * Remove dynamic line
    */
   public void removeDynamicLine()
   {
      if(this.dynamicLine != null)
      {
         this.layers.remove(this.dynamicLine);
      }

      this.dynamicLine = null;
   }

   /**
    * Remove dynamic oval
    */
   public void removeDynamicOval()
   {
      if(this.dynamicOval != null)
      {
         this.layers.remove(this.dynamicOval);
      }

      this.dynamicOval = null;
   }

   /**
    * Remove dynamic rectangle
    */
   public void removeDynamicRectangle()
   {
      if(this.dynamicRectangle != null)
      {
         this.layers.remove(this.dynamicRectangle);
      }

      this.dynamicRectangle = null;
   }

   /**
    * Remove last layer
    */
   public void removeLast()
   {
      if(this.layers.size() > 0)
      {
         final DrawOnTexture drawOnTexture = this.layers.remove(this.layers.size() - 1);

         if(drawOnTexture == this.dynamicLine)
         {
            this.dynamicLine = null;
         }
         else if(drawOnTexture == this.dynamicRectangle)
         {
            this.dynamicRectangle = null;
         }
         else if(drawOnTexture == this.dynamicOval)
         {
            this.dynamicOval = null;
         }
      }
   }

   /**
    * Change the texture
    * 
    * @param texture
    *           New texture where draw
    */
   public void setTexture(final Texture texture)
   {
      if(texture == null)
      {
         throw new NullPointerException("texture musn't be null");
      }

      this.texture = texture;
   }

   /**
    * Start dynamic line
    * 
    * @param x
    *           Start X
    * @param y
    *           Start Y
    * @param color
    *           Color to use
    * @param mix
    *           Indicates if mix or replace
    */
   public void startDynamicLine(final int x, final int y, final Color color, final boolean mix)
   {
      this.dynamicLine = new LineOnTexture(x, y, x, y, color, mix);
      this.layers.add(this.dynamicLine);
   }

   /**
    * Start dynamic oval
    * 
    * @param x
    *           Start X
    * @param y
    *           Start Y
    * @param color
    *           Color to use
    * @param fill
    *           Indicates if should fill
    * @param mix
    *           Indicates if mix or replace
    */
   public void startDynamicOval(final int x, final int y, final Color color, final boolean fill, final boolean mix)
   {
      this.dynamicOval = new OvalOnTexture(x, y, x, y, color, fill, mix);
      this.layers.add(this.dynamicOval);
   }

   /**
    * Start dynamic rectangle
    * 
    * @param x
    *           Start X
    * @param y
    *           Start Y
    * @param color
    *           Color to use
    * @param fill
    *           Indicates if should fill
    * @param mix
    *           Indicates if mix or replace
    */
   public void startDynamicRectangle(final int x, final int y, final Color color, final boolean fill, final boolean mix)
   {
      this.dynamicRectangle = new RectangleOnTexture(x, y, x, y, color, fill, mix);
      this.layers.add(this.dynamicRectangle);
   }
}