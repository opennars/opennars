/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.twoD<br>
 * Class : BorderIterator<br>
 * Date : 9 juin 2009<br>
 * By JHelp
 */
package jhelp.engine.twoD;

import jhelp.engine.Font3D;
import jhelp.engine.util.Math3D;

import java.awt.geom.PathIterator;
import java.util.ArrayList;

/**
 * Iterator around a path border.<br>
 * <br>
 * Last modification : 9 juin 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class BorderIterator
      implements PathIterator
{
   /**
    * Path element <br>
    * <br>
    * Last modification : 2 déc. 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   private class PathElement
   {
      /**
       * Element type : {@link PathIterator#SEG_CLOSE}, {@link PathIterator#SEG_LINETO} or {@link PathIterator#SEG_MOVETO}
       */
      public int    type;
      /** X */
      public double x;
      /** Y */
      public double y;

      /**
       * Constructs PathElement
       */
      public PathElement()
      {
      }
   }

   /** Array of elements */
   private ArrayList<PathElement> arrayList;
   /** Actual index read */
   private int                    index;
   /** Path length */
   private double                 length;
   /** Maximum X */
   private double                 maxX;
   /** Maximum Y */
   private double                 maxY;
   /** Minimum X */
   private double                 minX;
   /** Minimum Y */
   private double                 minY;
   /**
    * Winding rule : {@link PathIterator#WIND_EVEN_ODD} or {@link PathIterator#WIND_NON_ZERO}
    */
   private final int              windingRule;

   /**
    * Constructs BorderIterator
    * 
    * @param shape
    *           Shape to "walk" on its border
    * @param flatness
    *           The maximum distance that the line segments used to approximate the curved segments are allowed to deviate from
    *           any point on the original curve
    */
   public BorderIterator(final Shape shape, final float flatness)
   {
      this.arrayList = new ArrayList<PathElement>();
      this.index = 0;

      PathElement pathElement = null;
      double[] temp = new double[6];
      final PathIterator pathIterator = shape.getPathIterator(Font3D.affineTransform, flatness);
      this.windingRule = pathIterator.getWindingRule();
      boolean first = true;
      this.length = 0;

      double dx = 0, dy = 0, cx = 0, cy = 0, fx = 0, fy;

      while(pathIterator.isDone() == false)
      {
         pathElement = new PathElement();

         pathElement.type = pathIterator.currentSegment(temp);
         pathElement.x = temp[0];
         pathElement.y = temp[1];

         switch(pathElement.type)
         {
            case PathIterator.SEG_MOVETO:
               dx = cx = pathElement.x;
               dy = cy = pathElement.y;
            break;
            case PathIterator.SEG_LINETO:
               fx = pathElement.x;
               fy = pathElement.y;

               this.length += Math.sqrt(Math3D.square(cx - fx) + Math3D.square(cy - fy));

               cx = fx;
               cy = fy;
            break;
            case PathIterator.SEG_CLOSE:
               this.length += Math.sqrt(Math3D.square(cx - dx) + Math3D.square(cy - dy));

               cx = dx;
               cy = dy;
            break;
         }

         if(first == true)
         {
            this.minX = this.maxX = pathElement.x;
            this.minY = this.maxY = pathElement.y;
         }
         else
         {
            this.minX = Math.min(this.minX, pathElement.x);
            this.maxX = Math.max(this.maxX, pathElement.x);

            this.minY = Math.min(this.minY, pathElement.y);
            this.maxY = Math.max(this.maxY, pathElement.y);
         }
         first = false;

         this.arrayList.add(pathElement);

         pathIterator.next();
      }

      temp = null;
   }

   /**
    * Read current path element
    * 
    * @param coords
    *           Coordinates to fill
    * @return Path type : {@link PathIterator#SEG_CLOSE}, {@link PathIterator#SEG_LINETO} or {@link PathIterator#SEG_MOVETO}
    * @see PathIterator#currentSegment(double[])
    */
   @Override
   public int currentSegment(final double[] coords)
   {
      final PathElement pathElement = this.arrayList.get(this.index);

      coords[0] = pathElement.x;
      coords[1] = pathElement.y;

      return pathElement.type;
   }

   /**
    * Read the current path element
    * 
    * @param coords
    *           Coordinate to fill
    * @return Path element type : {@link PathIterator#SEG_CLOSE}, {@link PathIterator#SEG_LINETO} or
    *         {@link PathIterator#SEG_MOVETO}
    * @see PathIterator#currentSegment(float[])
    */
   @Override
   public int currentSegment(final float[] coords)
   {
      final PathElement pathElement = this.arrayList.get(this.index);

      coords[0] = (float) pathElement.x;
      coords[1] = (float) pathElement.y;

      return pathElement.type;
   }

   /**
    * Destroy the iterator
    */
   public void destroy()
   {
      this.arrayList.clear();
      this.arrayList = null;
   }

   /**
    * Path length
    * 
    * @return Path length
    */
   public double getLength()
   {
      return this.length;
   }

   /**
    * Return maxX
    * 
    * @return maxX
    */
   public double getMaxX()
   {
      return this.maxX;
   }

   /**
    * Return maxY
    * 
    * @return maxY
    */
   public double getMaxY()
   {
      return this.maxY;
   }

   /**
    * Return minX
    * 
    * @return minX
    */
   public double getMinX()
   {
      return this.minX;
   }

   /**
    * Return minY
    * 
    * @return minY
    */
   public double getMinY()
   {
      return this.minY;
   }

   /**
    * * Returns the winding rule for determining the interior of the path.
    * 
    * @return {@link PathIterator#WIND_EVEN_ODD} or {@link PathIterator#WIND_NON_ZERO}
    * @see PathIterator#getWindingRule()
    */
   @Override
   public int getWindingRule()
   {
      return this.windingRule;
   }

   /**
    * Indicates if we are at the end of the path
    * 
    * @return {@code true} if we are at the end of the path
    * @see PathIterator#isDone()
    */
   @Override
   public boolean isDone()
   {
      return this.index >= this.arrayList.size();
   }

   /**
    * Got to next path element
    * 
    * @see PathIterator#next()
    */
   @Override
   public void next()
   {
      this.index++;
   }

   /**
    * Number of {@link PathIterator#SEG_CLOSE} in the description
    * 
    * @return Number of {@link PathIterator#SEG_CLOSE}
    */
   public int numberOfClose()
   {
      int number = 0;

      for(final PathElement pathElement : this.arrayList)
      {
         if(pathElement.type == PathIterator.SEG_CLOSE)
         {
            number++;
         }
      }

      return number;
   }

   /**
    * Number of elements inside the iterator
    * 
    * @return Number of elements inside the iterator
    */
   public int numberOfElements()
   {
      return this.arrayList.size();
   }

   /**
    * Number of {@link PathIterator#SEG_LINETO} in the description
    * 
    * @return Number of {@link PathIterator#SEG_LINETO}
    */
   public int numberOfLineTo()
   {
      int number = 0;

      for(final PathElement pathElement : this.arrayList)
      {
         if(pathElement.type == PathIterator.SEG_LINETO)
         {
            number++;
         }
      }

      return number;
   }

   /**
    * Number of {@link PathIterator#SEG_MOVETO} in the description
    * 
    * @return Number of {@link PathIterator#SEG_MOVETO}
    */
   public int numberOfMoveTo()
   {
      int number = 0;

      for(final PathElement pathElement : this.arrayList)
      {
         if(pathElement.type == PathIterator.SEG_MOVETO)
         {
            number++;
         }
      }

      return number;
   }

   /**
    * Restart the iterator to the first path element
    */
   public void reset()
   {
      this.index = 0;
   }
}