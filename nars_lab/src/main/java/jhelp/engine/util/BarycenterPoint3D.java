/**
 */
package jhelp.engine.util;

import jhelp.engine.Point3D;

/**
 * Barycenter of a set center of 3D points <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class BarycenterPoint3D
{
   /** Barycenter for X */
   private Barycenter barycenterX;
   /** Barycenter for Y */
   private Barycenter barycenterY;
   /** Barycenter for Z */
   private Barycenter barycenterZ;

   /**
    * Constructs BarycenterPoint3D
    */
   public BarycenterPoint3D()
   {
      this.barycenterX = new Barycenter();
      this.barycenterY = new Barycenter();
      this.barycenterZ = new Barycenter();
   }

   /**
    * Add point to the set
    * 
    * @param point
    *           Point to add
    */
   public void add(Point3D point)
   {
      this.add(point.getX(), point.getY(), point.getZ());
   }

   /**
    * Add point to the set
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    */
   public void add(double x, double y, double z)
   {
      this.barycenterX.add(x);
      this.barycenterY.add(y);
      this.barycenterZ.add(z);
   }

   /**
    * Indicates if the set of points is empty
    * 
    * @return {@code true} if the set of points is empty
    */
   public boolean isEmpty()
   {
      return this.barycenterX.isEmpty();
   }

   /**
    * The barycenter.<br>
    * Return {@code null} if the set of points is empty
    * 
    * @return The barycenter
    */
   public Point3D getBarycenter()
   {
      if(this.isEmpty())
      {
         return null;
      }

      return new Point3D((float) this.barycenterX.getBarycenter(), (float) this.barycenterY.getBarycenter(), (float) this.barycenterZ.getBarycenter());
   }
}