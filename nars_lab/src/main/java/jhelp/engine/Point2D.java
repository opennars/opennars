/**
 */
package jhelp.engine;

import com.jogamp.opengl.GL2;
import jhelp.engine.util.Math3D;

/**
 * 2D point <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Point2D
{
   /** X */
   private float x;
   /** Y */
   private float y;

   /**
    * Constructs Point2D (0, 0)
    */
   public Point2D()
   {
      this.x = this.y = 0f;
   }

   /**
    * Constructs Point2D
    * 
    * @param x
    *           X
    * @param y
    *           Y
    */
   public Point2D(final float x, final float y)
   {
      this.x = x;
      this.y = y;
   }

   /**
    * Constructs Point2D
    * 
    * @param point
    *           Point copied
    */
   public Point2D(final Point2D point)
   {
      this.x = point.x;
      this.y = point.y;
   }

   /**
    * Indicates if an object is equal to this point
    * 
    * @param obj
    *           Object compare
    * @return {@code true} on equality
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(final Object obj)
   {
      if(obj == null)
      {
         return false;
      }
      if(obj == this)
      {
         return true;
      }
      if((obj instanceof Point2D) == false)
      {
         return false;
      }
      return this.equals((Point2D) obj);
   }

   /**
    * Indicates if other point is equal to this point
    * 
    * @param point
    *           Point compare
    * @return {@code true} on equality
    */
   public boolean equals(final Point2D point)
   {
      return Math3D.equal(this.x, point.x) && Math3D.equal(this.y, point.y);
   }

   /**
    * X
    * 
    * @return X
    */
   public float getX()
   {
      return this.x;
   }

   /**
    * Y
    * 
    * @return Y
    */
   public float getY()
   {
      return this.y;
   }

   /**
    * Apply like UV in OpenGL
    * 
    * @param gl
    *           OpenGL context
    */
   public void glTexCoord2f(final GL2 gl)
   {
      gl.glTexCoord2f(this.x, this.y);
   }

   /**
    * Change the point
    * 
    * @param x
    *           New X
    * @param y
    *           New Y
    */
   public void set(final float x, final float y)
   {
      this.x = x;
      this.y = y;
   }

   /**
    * Copy a point
    * 
    * @param point
    *           Point to copy
    */
   public void set(final Point2D point)
   {
      this.x = point.x;
      this.y = point.y;
   }

   /**
    * String representation
    * 
    * @return String representation
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      final StringBuffer sb = new StringBuffer("Point2D : (");
      sb.append(this.x);
      sb.append(", ");
      sb.append(this.y);
      sb.append(")");
      return sb.toString();
   }

   /**
    * Translate
    * 
    * @param x
    *           X
    * @param y
    *           Y
    */
   public void translate(final float x, final float y)
   {
      this.x += x;
      this.y += y;
   }
}