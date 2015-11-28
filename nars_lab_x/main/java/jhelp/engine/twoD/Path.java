/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.twoD<br>
 * Class : Path<br>
 * Date : 17 janv. 2009<br>
 * By JHelp
 */
package jhelp.engine.twoD;

import jhelp.engine.Point2D;
import jhelp.engine.Point3D;
import jhelp.engine.Texture;
import jhelp.engine.io.ConstantsXML;
import jhelp.engine.util.Math3D;
import jhelp.xml.MarkupXML;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * A path <br>
 * <br>
 * Last modification : 21 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Path
{
   /**
    * Path element <br>
    * <br>
    * Last modification : 23 mai 2009<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   public class Couple
   {
      /** Element index */
      public int         index;
      /** Element description */
      public PathElement pathElement;

      /**
       * Constructs Couple
       * 
       * @param pathElement
       *           Element description
       * @param index
       *           Element index
       */
      Couple(final PathElement pathElement, final int index)
      {
         this.pathElement = pathElement;
         this.index = index;
      }
   }

   /**
    * Start/select/end information to know where start/end an element when path is transformed on line array. select to know who
    * is the select one <br>
    * <br>
    * Last modification : 23 mai 2009<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   public class Triplet
   {
      /** End */
      public int end;
      /** Select */
      public int select;
      /** Start */
      public int start;

      /**
       * Constructs Triplet
       * 
       * @param start
       *           Start
       * @param select
       *           Select
       * @param end
       *           End
       */
      Triplet(final int start, final int select, final int end)
      {
         this.start = start;
         this.select = select;
         this.end = end;
      }
   }

   /** Path elements */
   private final ArrayList<PathElement> path;

   /** Path size */
   private float                        size;

   /**
    * Constructs Path<br>
    */
   public Path()
   {
      this.path = new ArrayList<PathElement>();
      this.size = 0;
   }

   /**
    * Add path element
    * 
    * @param pathType
    *           Path type
    * @param points
    *           Control points
    */
   public void addPathElement(final PathType pathType, final Point2D... points)
   {
      if(pathType == null)
      {
         throw new NullPointerException("pathType musn't be null");
      }
      if(points == null)
      {
         throw new NullPointerException("points musn't be null");
      }
      switch(pathType)
      {
         case CUBIC:
            if(points.length != 4)
            {
               throw new IllegalArgumentException("Cubic must have exactly 4 points");
            }
         break;
         case LINE:
            if(points.length != 2)
            {
               throw new IllegalArgumentException("Line must have exactly 2 points");
            }
         break;
         case QUAD:
            if(points.length != 3)
            {
               throw new IllegalArgumentException("Quadric must have exactly 3 points");
            }
         break;
      }
      final Point3D[] points3D = new Point3D[points.length];
      final float step = 1f / (points.length - 1f);
      float value = 0f;
      for(int i = 0; i < points.length; i++, value += step)
      {
         points3D[i] = new Point3D(points[i], value);
      }
      final PathElement pathElement = new PathElement(pathType, points3D);
      this.path.add(pathElement);
   }

   /**
    * Append a cubic.<br>
    * A cubic have two controls points.
    * 
    * @param startPoint
    *           Start point
    * @param start
    *           Start value
    * @param controlPoint1
    *           First control point
    * @param control1
    *           First control value
    * @param controlPoint2
    *           Second control point
    * @param control2
    *           Second control value
    * @param endPoint
    *           End point
    * @param end
    *           End value
    */
   public void appendCubic(final Point2D startPoint, final float start, final Point2D controlPoint1, final float control1, final Point2D controlPoint2, final float control2, final Point2D endPoint, final float end)
   {
      this.path.add(new PathElement(PathType.CUBIC, new Point3D(startPoint, start), new Point3D(controlPoint1, control1), new Point3D(controlPoint2, control2), new Point3D(endPoint, end)));
      this.size = -1;
   }

   /**
    * Append a cubic.<br>
    * A cubic have two controls points.
    * 
    * @param startPoint
    *           Start point
    * @param controlPoint1
    *           First control point
    * @param controlPoint2
    *           Second control point
    * @param endPoint
    *           End point
    */
   public void appendCubic(final Point2D startPoint, final Point2D controlPoint1, final Point2D controlPoint2, final Point2D endPoint)
   {
      this.appendCubic(startPoint, 0, controlPoint1, 1f / 3f, controlPoint2, 2f / 3f, endPoint, 1);
   }

   /**
    * Append line to the path
    * 
    * @param startPoint
    *           Start point
    * @param start
    *           Start value
    * @param endPoint
    *           End point
    * @param end
    *           End value
    */
   public void appendLine(final Point2D startPoint, final float start, final Point2D endPoint, final float end)
   {
      this.path.add(new PathElement(PathType.LINE, new Point3D(startPoint, start), new Point3D(endPoint, end)));
      this.size = -1;
   }

   /**
    * Append a line to the path
    * 
    * @param startPoint
    *           Start point
    * @param endPoint
    *           End point
    */
   public void appendLine(final Point2D startPoint, final Point2D endPoint)
   {
      this.appendLine(startPoint, 0, endPoint, 1);
   }

   /**
    * Append a quadric.<br>
    * A quadric have one control point.
    * 
    * @param startPoint
    *           Start point
    * @param start
    *           Start value
    * @param controlPoint
    *           Control point
    * @param control
    *           Control value
    * @param endPoint
    *           End point
    * @param end
    *           End value
    */
   public void appendQuad(final Point2D startPoint, final float start, final Point2D controlPoint, final float control, final Point2D endPoint, final float end)
   {
      this.path.add(new PathElement(PathType.QUAD, new Point3D(startPoint, start), new Point3D(controlPoint, control), new Point3D(endPoint, end)));
      this.size = -1;
   }

   /**
    * Append a quadric.<br>
    * A quadric have one control point.
    * 
    * @param startPoint
    *           Start point
    * @param controlPoint
    *           Control point
    * @param endPoint
    *           End point
    */
   public void appendQuad(final Point2D startPoint, final Point2D controlPoint, final Point2D endPoint)
   {
      this.appendQuad(startPoint, 0, controlPoint, 0.5f, endPoint, 1);
   }

   /**
    * Compute rectangle around the path
    * 
    * @return Rectangle around the path
    */
   public Rectangle2D computeBorder()
   {
      float minX = Float.MAX_VALUE;
      float minY = Float.MAX_VALUE;
      float maxX = -Float.MAX_VALUE;
      float maxY = -Float.MAX_VALUE;

      for(final PathElement pathElement : this.path)
      {
         for(final Point3D point : pathElement.points)
         {
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
         }
      }

      if((minX > maxX) || (minY > maxY))
      {
         return null;
      }

      return new Rectangle2D.Float(minX, minY, maxX - minX, maxY - minY);
   }

   /**
    * Compute the path on set of lines.<br>
    * Precision is use to approximate quadric and cubic elements.<br>
    * Bigger is the precision, more the path is smooth, but it takes more time to compute and more lines are computed, so more
    * memory is used.<br>
    * A value that make smooth without slow application and don't take lot off memory, could be in [16, 32].<br>
    * Just for information, the number of lines generate by these method is
    * <code>NB_LINES + precision * (NB_QUADRICS + NB_CUBICS)</code><br>
    * Where : <li><b>NB_LINES</b> : Numbers of line element</li> <li><b>NB_QUADRICS</b> : Numbers of quadric element</li> <li>
    * <b>NB_CUBICS</b> : Numbers of cubic element</li> <br>
    * The precision can't be less than 2, if you give a lesser value, then it goes automatic to 2.
    * 
    * @param precision
    *           Precision value
    * @return Set of lines to approximate the path
    */
   public ArrayList<Line2D> computePath(int precision)
   {
      if(precision < 2)
      {
         precision = 2;
      }

      // Initialization
      final ArrayList<Line2D> arrayListLines = new ArrayList<Line2D>();
      Line2D line2D;
      Point2D point1, point2;
      float x1, x2, x3, x4, y1, y2, y3, y4, value1, value2, value3, value4;
      double[] x, y, val;
      int index;
      Point3D[] points;

      // For each element
      for(final PathElement pathElement : this.path)
      {
         // Get element points
         points = pathElement.points;
         switch(pathElement.pathType)
         {
         // If the element is a line, add just this line
            case LINE:
               point1 = new Point2D(points[0].getX(), points[0].getY());
               value1 = points[0].getZ();
               point2 = new Point2D(points[1].getX(), points[1].getY());
               value2 = points[1].getZ();
               line2D = new Line2D(point1, point2, value1, value2);
               line2D.pathElement = pathElement;
               arrayListLines.add(line2D);
            break;

            // If the element is quadric, interpolate it
            case QUAD:
               // Get values
               x1 = points[0].getX();
               y1 = points[0].getY();
               value1 = points[0].getZ();
               x2 = points[1].getX();
               y2 = points[1].getY();
               value2 = points[1].getZ();
               x3 = points[2].getX();
               y3 = points[2].getY();
               value3 = points[2].getZ();
               // Interpolate values
               x = Texture.PQuadriques(x1, x2, x3, precision);
               y = Texture.PQuadriques(y1, y2, y3, precision);
               val = Texture.PQuadriques(value1, value2, value3, precision);
               // Add interpolated lines
               for(index = 1; index < precision; index++)
               {
                  point1 = new Point2D((float) x[index - 1], (float) y[index - 1]);
                  value1 = (float) val[index - 1];
                  point2 = new Point2D((float) x[index], (float) y[index]);
                  value2 = (float) val[index];
                  line2D = new Line2D(point1, point2, value1, value2);
                  line2D.pathElement = pathElement;
                  arrayListLines.add(line2D);
               }
            break;

            // If the element is cubic, interpolate it
            case CUBIC:
               // Get values
               x1 = points[0].getX();
               y1 = points[0].getY();
               value1 = points[0].getZ();
               x2 = points[1].getX();
               y2 = points[1].getY();
               value2 = points[1].getZ();
               x3 = points[2].getX();
               y3 = points[2].getY();
               value3 = points[2].getZ();
               x4 = points[3].getX();
               y4 = points[3].getY();
               value4 = points[3].getZ();
               // Interpolate values
               x = Texture.PCubiques(x1, x2, x3, x4, precision);
               y = Texture.PCubiques(y1, y2, y3, y4, precision);
               val = Texture.PCubiques(value1, value2, value3, value4, precision);
               // Add interpolated lines
               for(index = 1; index < precision; index++)
               {
                  point1 = new Point2D((float) x[index - 1], (float) y[index - 1]);
                  value1 = (float) val[index - 1];
                  point2 = new Point2D((float) x[index], (float) y[index]);
                  value2 = (float) val[index];
                  line2D = new Line2D(point1, point2, value1, value2);
                  line2D.pathElement = pathElement;
                  arrayListLines.add(line2D);
               }
            break;
         }
      }

      return arrayListLines;
   }

   /**
    * Compute omogenus version of the path
    * 
    * @param precision
    *           Precision to use
    * @param start
    *           Start value associated at path start (Can be use for interpolated u, v, ...)
    * @param end
    *           End value associated at path end (Can be use for interpolated u, v, ...)
    * @return Line list computed, each line have interpolation value computed
    */
   public ArrayList<Line2D> computePathOmogenous(final int precision, final float start, final float end)
   {
      final ArrayList<Line2D> line2ds = this.computePath(precision);

      float size = 0;
      for(final Line2D line2d : line2ds)
      {
         line2d.additonal = Math3D.distance(line2d.pointStart, line2d.pointEnd);
         size += line2d.additonal;
      }

      if(Math3D.nul(size) == true)
      {
         return line2ds;
      }

      float value = start;
      final float diff = end - start;
      for(final Line2D line2d : line2ds)
      {
         line2d.start = value;
         value += (line2d.additonal * diff) / size;
         line2d.end = value;
      }

      return line2ds;
   }

   /**
    * Try to compute the path size<br>
    * TODO make a better algorithm
    * 
    * @return Path size
    */
   public float computePathSize()
   {
      // If the size is already compute, no need to compute again
      if(this.size >= 0f)
      {
         return this.size;
      }

      // Start computing
      this.size = 0;

      // For each element, add its size
      Point3D old = null;
      for(final PathElement pathElement : this.path)
      {
         // An element's size is suppose be the sum of size between each points
         // compose the element
         final Point3D[] points = pathElement.points;
         final int length = points.length;
         if(old != null)
         {
            this.size += Math.sqrt(Math3D.square(old.getX() - points[0].getX()) + Math3D.square(old.getY() - points[0].getY()));
         }
         for(int i = 1; i < length; i++)
         {
            this.size += Math.sqrt(Math3D.square(points[i - 1].getX() - points[i].getX()) + Math3D.square(points[i - 1].getY() - points[i].getY()));
         }
         old = points[length - 1];
      }

      return this.size;
   }

   /**
    * Compute start/select/end information of a path element
    * 
    * @param couple
    *           Path element
    * @return start/select/end information
    */
   public Triplet computeTriplet(final Couple couple)
   {
      if(couple == null)
      {
         return null;
      }
      final int indexElement = this.path.indexOf(couple.pathElement);
      if(indexElement < 0)
      {
         return null;
      }
      int start = 0;
      for(int index = 0; index < indexElement; index++)
      {
         start += this.path.get(index).points.length;
      }
      return new Triplet(start, start + couple.index, (start + couple.pathElement.points.length) - 1);
   }

   /**
    * Number of elements
    * 
    * @return Number of elements
    */
   public int countPathElement()
   {
      return this.path.size();
   }

   /**
    * Insert a path element
    * 
    * @param index
    *           Insertion index
    * @param pathType
    *           Path type
    * @param points
    *           Control points
    */
   public void insertPathElement(final int index, final PathType pathType, final Point2D... points)
   {
      if(pathType == null)
      {
         throw new NullPointerException("pathType musn't be null");
      }
      if(points == null)
      {
         throw new NullPointerException("points musn't be null");
      }
      switch(pathType)
      {
         case CUBIC:
            if(points.length != 4)
            {
               throw new IllegalArgumentException("Cubic must have exactly 4 points");
            }
         break;
         case LINE:
            if(points.length != 2)
            {
               throw new IllegalArgumentException("Line must have exactly 2 points");
            }
         break;
         case QUAD:
            if(points.length != 3)
            {
               throw new IllegalArgumentException("Quadric must have exactly 3 points");
            }
         break;
      }
      final Point3D[] points3D = new Point3D[points.length];
      final float step = 1f / (points.length - 1f);
      float value = 0f;
      for(int i = 0; i < points.length; i++, value += step)
      {
         points3D[i] = new Point3D(points[i], value);
      }
      final PathElement pathElement = new PathElement(pathType, points3D);
      this.path.add(index, pathElement);
   }

   /**
    * Try to "linearize" values to the path.<br>
    * The aim is to have homogeneous values.<br>
    * TODO make a better algorithm
    * 
    * @param start
    *           Value at the start of the path
    * @param end
    *           Value at the end of the path
    */
   public void linearize(final float start, final float end)
   {
      this.size = this.computePathSize();
      float value = start;
      Point3D actualPoint;
      Point3D oldPoint = null;
      float dist;
      for(final PathElement pathElement : this.path)
      {
         final Point3D[] points = pathElement.points;
         final int length = points.length;
         for(int i = 0; i < length; i++)
         {
            actualPoint = points[i];
            if(oldPoint != null)
            {
               dist = (float) Math.sqrt(Math3D.square(actualPoint.getX() - oldPoint.getX()) + Math3D.square(actualPoint.getY() - oldPoint.getY()));
               value += ((end - start) * dist) / this.size;
            }
            actualPoint.set(actualPoint.getX(), actualPoint.getY(), value);
            oldPoint = actualPoint;
         }
      }
   }

   /**
    * Read parameters form XML
    * 
    * @param markupXML
    *           Markup to parse
    * @throws Exception
    *            On reading or parsing issue
    */
   public void loadFromXML(final MarkupXML markupXML) throws Exception
   {
      this.path.clear();
      this.size = 0;
      PathElement pathElement;
      for(final MarkupXML markup : markupXML.obtainChildren(ConstantsXML.MARKUP_PATH_ELEMENT))
      {
         pathElement = new PathElement();
         pathElement.loadFromXML(markup);
         this.path.add(pathElement);
      }
   }

   /**
    * Change a path element
    * 
    * @param index
    *           Element index
    * @param pathType
    *           New type
    * @param points
    *           New control points
    */
   public void modifyPathElement(final int index, final PathType pathType, final Point2D... points)
   {
      if(pathType == null)
      {
         throw new NullPointerException("pathType musn't be null");
      }
      if(points == null)
      {
         throw new NullPointerException("points musn't be null");
      }
      switch(pathType)
      {
         case CUBIC:
            if(points.length != 4)
            {
               throw new IllegalArgumentException("Cubic must have exactly 4 points");
            }
         break;
         case LINE:
            if(points.length != 2)
            {
               throw new IllegalArgumentException("Line must have exactly 2 points");
            }
         break;
         case QUAD:
            if(points.length != 3)
            {
               throw new IllegalArgumentException("Quadric must have exactly 3 points");
            }
         break;
      }
      this.path.get(index).pathType = pathType;
      this.path.get(index).points = new Point3D[points.length];
      final float step = 1f / (points.length - 1f);
      float value = 0f;
      for(int i = 0; i < points.length; i++, value += step)
      {
         this.path.get(index).points[i] = new Point3D(points[i], value);
      }
   }

   /**
    * Change a path element
    * 
    * @param pathElement
    *           Element to modify
    * @param pathType
    *           New path type
    * @param points
    *           Control points
    */
   public void modifyPathElement(final PathElement pathElement, final PathType pathType, final Point2D... points)
   {
      if(pathElement == null)
      {
         throw new NullPointerException("pathElement musn't be null");
      }
      if(pathType == null)
      {
         throw new NullPointerException("pathType musn't be null");
      }
      if(points == null)
      {
         throw new NullPointerException("points musn't be null");
      }
      switch(pathType)
      {
         case CUBIC:
            if(points.length != 4)
            {
               throw new IllegalArgumentException("Cubic must have exactly 4 points");
            }
         break;
         case LINE:
            if(points.length != 2)
            {
               throw new IllegalArgumentException("Line must have exactly 2 points");
            }
         break;
         case QUAD:
            if(points.length != 3)
            {
               throw new IllegalArgumentException("Quadric must have exactly 3 points");
            }
         break;
      }
      pathElement.pathType = pathType;
      pathElement.points = new Point3D[points.length];
      final float step = 1f / (points.length - 1f);
      float value = 0f;
      for(int i = 0; i < points.length; i++, value += step)
      {
         pathElement.points[i] = new Point3D(points[i], value);
      }
   }

   /**
    * All control points list
    * 
    * @return All control points list
    */
   public ArrayList<Point2D> obtainControlPoints()
   {
      final ArrayList<Point2D> arrayList = new ArrayList<Point2D>();
      for(final PathElement pathElement : this.path)
      {
         for(final Point3D point : pathElement.points)
         {
            arrayList.add(new Point2D(point.x, point.y));
         }
      }
      return arrayList;
   }

   /**
    * List of Element "near" a given position
    * 
    * @param x
    *           X position
    * @param y
    *           Y position
    * @param precision
    *           What near mean. Its the distance maximum form position
    * @return List of elements founds
    */
   public ArrayList<Couple> obtainCoupleNear(final float x, final float y, float precision)
   {
      if(precision < 0)
      {
         precision = 0;
      }
      final ArrayList<Couple> arrayList = new ArrayList<Couple>();
      Point3D point3D;
      for(final PathElement pathElement : this.path)
      {
         for(int index = 0; index < pathElement.points.length; index++)
         {
            point3D = pathElement.points[index];
            if((Math.abs(x - point3D.x) <= precision) && (Math.abs(y - point3D.y) <= precision))
            {
               arrayList.add(new Couple(pathElement, index));
            }
         }
      }
      return arrayList;
   }

   /**
    * Path element
    * 
    * @param index
    *           Element index
    * @return Path element
    */
   public PathElement obtainPathElement(final int index)
   {
      return this.path.get(index);
   }

   /**
    * Indicates if position is over a control point
    * 
    * @param x
    *           Position X
    * @param y
    *           Position Y
    * @param precision
    *           Maximum distance to consider
    * @return {@code true} if position is over a control point
    */
   public boolean overControlPoint(final float x, final float y, float precision)
   {
      if(precision < 0)
      {
         precision = 0;
      }
      Point3D point3D;
      for(final PathElement pathElement : this.path)
      {
         for(final Point3D point : pathElement.points)
         {
            point3D = point;
            if((Math.abs(x - point3D.x) <= precision) && (Math.abs(y - point3D.y) <= precision))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Remove element
    * 
    * @param index
    *           Index of element
    */
   public void removeElement(final int index)
   {
      this.path.remove(index);
   }

   /**
    * Remove element
    * 
    * @param pathElement
    *           Element to remove
    */
   public void removeElement(final PathElement pathElement)
   {
      this.path.remove(pathElement);
   }

   /**
    * XML version
    * 
    * @return XML version
    */
   public MarkupXML saveToXML()
   {
      final MarkupXML markupXML = new MarkupXML(ConstantsXML.MARKUP_PATH);
      for(final PathElement pathElement : this.path)
      {
         markupXML.addChild(pathElement.saveToXML());
      }
      return markupXML;
   }
}