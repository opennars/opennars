/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine<br>
 * Class : TextureGirdUV<br>
 * Date : 1 juin 2009<br>
 * By JHelp
 */
package jhelp.engine;

import java.util.ArrayList;

/**
 * Texture with "grid" based on Object UV <br>
 * <br>
 * Last modification : 1 juin 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class TextureGirdUV
      extends Texture
{
   /**
    * Shape description <br>
    * <br>
    * Last modification : 2 déc. 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   static class Shape
   {
      /** Color to fill */
      public int     color;
      /** Shape */
      public Polygon polygon;

      /**
       * Constructs Shape
       */
      public Shape()
      {
      }
   }

   /** Background color */
   private Color            backgroundColor;
   /** Border color */
   private Color            borderColor;
   /** Shape list */
   private ArrayList<Shape> shapes;

   /**
    * Constructs TextureGirdUV
    * 
    * @param name
    *           Texture name
    * @param width
    *           Width
    * @param height
    *           Height
    */
   public TextureGirdUV(final String name, final int width, final int height)
   {
      super(name, width, height, 0xFFFFFFFF);

      this.shapes = new ArrayList<Shape>();

      this.backgroundColor = Color.WHITE;
      this.borderColor = Color.BLACK;
   }

   /**
    * Shape color
    * 
    * @param shape
    *           Shape index
    * @return Shape color
    */
   public int colorOnShape(final int shape)
   {
      return this.shapes.get(shape).color;
   }

   /**
    * Create grid from mesh
    * 
    * @param mesh
    *           Mesh to "extract" grid
    */
   public void createGird(final Mesh mesh)
   {
      this.shapes = mesh.obtainUVshapes(this.width, this.height);

      this.refreshShapes();
   }

   /**
    * Create grid from object
    * 
    * @param object3D
    *           Object to "extract" grid
    */
   public void createGird(final Object3D object3D)
   {
      this.createGird(object3D.mesh);
   }

   /**
    * Return backgroundColor
    * 
    * @return backgroundColor
    */
   public Color getBackgroundColor()
   {
      return this.backgroundColor;
   }

   /**
    * Return borderColor
    * 
    * @return borderColor
    */
   public Color getBorderColor()
   {
      return this.borderColor;
   }

   /**
    * Number of shape
    * 
    * @return Number of shape
    */
   public int getNumberOfShape()
   {
      return this.shapes.size();
   }

   /**
    * Obtain a shape for a position
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @return Shape index under the position or -1
    */
   public int obtainShape(final int x, final int y)
   {
      final int nb = this.shapes.size();
      for(int i = 0; i < nb; i++)
      {
         if(this.shapes.get(i).polygon.contains(x, y) == true)
         {
            return i;
         }
      }

      return -1;
   }

   /**
    * Refresh shapes drawing
    */
   public void refreshShapes()
   {
      this.fillRect(0, 0, this.width, this.height, this.backgroundColor, false);

      for(final Shape shape : this.shapes)
      {
         this.draw(shape.polygon, this.borderColor, false, 1);
         this.fill(shape.polygon, new Color(shape.color, true), true);
      }

      this.flush();
   }

   /**
    * Modify backgroundColor
    * 
    * @param backgroundColor
    *           New backgroundColor value
    */
   public void setBackgroundColor(final Color backgroundColor)
   {
      this.backgroundColor = backgroundColor;
      this.refreshShapes();
   }

   /**
    * Modify borderColor
    * 
    * @param borderColor
    *           New borderColor value
    */
   public void setBorderColor(final Color borderColor)
   {
      this.borderColor = borderColor;
      this.refreshShapes();
   }

   /**
    * Change shape color
    * 
    * @param shape
    *           Shape index
    * @param color
    *           Color to apply
    */
   public void setColorOnShape(final int shape, final int color)
   {
      this.shapes.get(shape).color = color;
      this.refreshShapes();
   }
}