/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.twoD<br>
 * Class : Line2D<br>
 * Date : 17 janv. 2009<br>
 * By JHelp
 */
package jhelp.engine.twoD;

import jhelp.engine.Point2D;

/**
 * A 2D line <br>
 * <br>
 * Last modification : 21 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Line2D
{
   /** Additional information */
   public float       additonal;
   /** End value */
   public float       end;
   /** Path element linked */
   public PathElement pathElement;
   /** End point */
   public Point2D     pointEnd;
   /** Start point */
   public Point2D     pointStart;
   /** Start value */
   public float       start;

   /**
    * Constructs Line2D
    * 
    * @param pointStart
    *           Start Point
    * @param pointEnd
    *           End point
    * @param start
    *           Start value
    * @param end
    *           End value
    */
   public Line2D(final Point2D pointStart, final Point2D pointEnd, final float start, final float end)
   {
      this.pointStart = pointStart;
      this.pointEnd = pointEnd;
      this.start = start;
      this.end = end;
   }
}