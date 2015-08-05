/**
 */
package jhelp.engine;

import com.jogamp.opengl.GL2;
import jhelp.engine.util.Math3D;
import jhelp.math.Vec3f;
import jhelp.math.Vec4f;

/**
 * 3D point, can be used like vector <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Point3D
{
   /**
    * Middle between two points
    * 
    * @param point1
    *           First point
    * @param point2
    *           Second point
    * @return Middle
    */
   public static Point3D getCenter(final Point3D point1, final Point3D point2)
   {
      return new Point3D((point1.x + point2.x) / 2f, (point1.y + point2.y) / 2f, (point1.z + point2.z) / 2f);
   }

   /**
    * Distance between two points
    * 
    * @param x1
    *           X1
    * @param y1
    *           Y1
    * @param z1
    *           Z1
    * @param x2
    *           X2
    * @param y2
    *           Y2
    * @param z2
    *           Z2
    * @return Distance
    */
   public static float getDistance(final float x1, final float y1, final float z1, final float x2, final float y2, final float z2)
   {
      return Point3D.getLength(x2 - x1, y2 - y1, z2 - z1);
   }

   /**
    * Distance between two point
    * 
    * @param point1
    *           Point 1
    * @param x2
    *           X2
    * @param y2
    *           Y2
    * @param z2
    *           Z2
    * @return Distance
    */
   public static float getDistance(final Point3D point1, final float x2, final float y2, final float z2)
   {
      return Point3D.getDistance(point1.x, point1.y, point1.z, x2, y2, z2);
   }

   /**
    * Distance between two point
    * 
    * @param point1
    *           Point 1
    * @param point2
    *           Point 2
    * @return Distance
    */
   public static float getDistance(final Point3D point1, final Point3D point2)
   {
      return Point3D.getDistance(point1, point2.x, point2.y, point2.z);
   }

   /**
    * Distance square between two points
    * 
    * @param x1
    *           X1
    * @param y1
    *           Y1
    * @param z1
    *           Z1
    * @param x2
    *           X2
    * @param y2
    *           Y2
    * @param z2
    *           Z2
    * @return Distance square
    */
   public static float getDistanceSquare(final float x1, final float y1, final float z1, final float x2, final float y2, final float z2)
   {
      return Point3D.getLengthSquare(x2 - x1, y2 - y1, z2 - z1);
   }

   /**
    * Distance square between two point
    * 
    * @param point1
    *           Point 1
    * @param x2
    *           X2
    * @param y2
    *           Y2
    * @param z2
    *           Z2
    * @return Distance square
    */
   public static float getDistanceSquare(final Point3D point1, final float x2, final float y2, final float z2)
   {
      return Point3D.getDistanceSquare(point1.x, point1.y, point1.z, x2, y2, z2);
   }

   /**
    * Distance square between two point
    * 
    * @param point1
    *           Point 1
    * @param point2
    *           Point 2
    * @return Distance square
    */
   public static float getDistanceSquare(final Point3D point1, final Point3D point2)
   {
      return Point3D.getDistanceSquare(point1, point2.x, point2.y, point2.z);
   }

   /**
    * Vector length
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    * @return Length
    */
   public static float getLength(final float x, final float y, final float z)
   {
      return (float) Math.sqrt((x * x) + (y * y) + (z * z));
   }

   /**
    * Vector length square
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    * @return Length square
    */
   public static float getLengthSquare(final float x, final float y, final float z)
   {
      return (x * x) + (y * y) + (z * z);
   }

   /** X */
   public float x;

   /** Y */
   public float y;

   /** Z */
   public float z;

   /**
    * Constructs Point3D (0, 0, 0)
    */
   public Point3D()
   {
      this.x = this.y = this.z = 0f;
   }

   /**
    * Constructs Point3D
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    */
   public Point3D(final float x, final float y, final float z)
   {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   /**
    * Constructs Point3D
    * 
    * @param point
    *           Point for X, Y values
    * @param z
    *           Z
    */
   public Point3D(final Point2D point, final float z)
   {
      this.x = point.getX();
      this.y = point.getY();
      this.z = z;
   }

   /**
    * Constructs Point3D copy to an other
    * 
    * @param point
    *           Point to copy
    */
   public Point3D(final Point3D point)
   {
      this.x = point.x;
      this.y = point.y;
      this.z = point.z;
   }

   /**
    * Constructs Point3D
    * 
    * @param vec3f
    *           Base vector
    */
   public Point3D(final Vec3f vec3f)
   {
      this.x = vec3f.x();
      this.y = vec3f.y();
      this.z = vec3f.z();
   }

   /**
    * Constructs Point3D
    * 
    * @param vec4f
    *           JOGL vector
    */
   public Point3D(final Vec4f vec4f)
   {
      final float w = vec4f.w();
      //
      this.x = vec4f.x() / w;
      this.y = vec4f.y() / w;
      this.z = vec4f.z() / w;
   }

   /**
    * Add vector or translate a point
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    * @return Result vector or translated point
    */
   public Point3D add(final float x, final float y, final float z)
   {
      return new Point3D(this.x + x, this.y + y, this.z + z);
   }

   /**
    * Add vector or translate a point
    * 
    * @param vector
    *           Vector to add
    * @return Result vector or translated point
    */
   public Point3D add(final Point3D vector)
   {
      return new Point3D(this.x + vector.x, this.y + vector.y, this.z + vector.z);
   }

   /**
    * Translate in opposite way
    * 
    * @param point
    *           Translation vector
    */
   public void antiTranslate(final Point3D point)
   {
      this.x -= point.x;
      this.y -= point.y;
      this.z -= point.z;
   }

   /**
    * Make the dot product between this vector and an other
    * 
    * @param vector
    *           Vector we do the dot product
    * @return Dot product
    */
   public float dotProduct(final Point3D vector)
   {
      return (this.x * vector.x) + (this.y * vector.y) + (this.z * vector.z);
   }

   /**
    * Indicates if an object is equal to this point
    * 
    * @param obj
    *           Object to compare
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
      if((obj instanceof Point3D) == false)
      {
         return false;
      }
      return this.equals((Point3D) obj);
   }

   /**
    * Indicates if an other point is equal to this point
    * 
    * @param point
    *           Point compare
    * @return {@code true} on equality
    */
   public boolean equals(final Point3D point)
   {
      return Math3D.equal(this.x, point.x) && Math3D.equal(this.y, point.y) && Math3D.equal(this.z, point.z);
   }

   /**
    * Multiply the vector by a factor
    * 
    * @param factor
    *           Multiply factor
    * @return Result vector
    */
   public Point3D factor(final float factor)
   {
      return new Point3D(this.x * factor, this.y * factor, this.z * factor);
   }

   /**
    * Opposite vector
    * 
    * @return Opposite vector
    */
   public Point3D getOptsite()
   {
      return new Point3D(-this.x, -this.y, -this.z);
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
    * Z
    * 
    * @return Z
    */
   public float getZ()
   {
      return this.z;
   }

   /**
    * Apply like a normal in OpenGL
    * 
    * @param gl
    *           OpenGL context
    */
   public void glNormal3f(final GL2 gl)
   {
      gl.glNormal3f(this.x, this.y, this.z);
   }

   /**
    * Apply like a point in OpenGL
    * 
    * @param gl
    *           OpenGL context
    */
   public void glVertex3f(final GL2 gl)
   {
      gl.glVertex3f(this.x, this.y, this.z);
   }

   /**
    * Vector length
    * 
    * @return Vector length
    */
   public float length()
   {
      return (float) Math.sqrt(this.dotProduct(this));
   }

   /**
    * Normalize this vector
    */
   public void normalize()
   {
      final float length = this.length();
      if(Math3D.nul(length) == false)
      {
         this.x /= length;
         this.y /= length;
         this.z /= length;
      }
   }

   /**
    * Make dot product between this vector and an other
    * 
    * @param vector
    *           Vector we do the product
    * @return The product
    */
   public Point3D product(final Point3D vector)
   {
      return new Point3D(//
            (this.y * vector.z) - (this.z * vector.y), //
            (this.x * vector.z) - (this.z * vector.x), //
            (this.x * vector.y) - (this.y * vector.x));
   }

   /**
    * Modify the point
    * 
    * @param x
    *           New X
    * @param y
    *           New Y
    * @param z
    *           New Z
    */
   public void set(final float x, final float y, final float z)
   {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   /**
    * Copy a point
    * 
    * @param point
    *           Point copied
    */
   public void set(final Point3D point)
   {
      this.x = point.x;
      this.y = point.y;
      this.z = point.z;
   }

   /**
    * Copy JOGL vector
    * 
    * @param vec3f
    *           Copied vector
    */
   public void set(final Vec3f vec3f)
   {
      this.x = vec3f.x();
      this.y = vec3f.y();
      this.z = vec3f.z();
   }

   /**
    * Copy JOGL vector
    * 
    * @param vec4f
    *           Copied vector
    */
   public void set(final Vec4f vec4f)
   {
      final float w = vec4f.w();
      //
      this.x = vec4f.x() / w;
      this.y = vec4f.y() / w;
      this.z = vec4f.z() / w;
   }

   /**
    * Subtraction two vector or two points
    * 
    * @param vector
    *           Vector or point to substract
    * @return Vector result
    */
   public Point3D substract(final Point3D vector)
   {
      return new Point3D(this.x - vector.x, this.y - vector.y, this.z - vector.z);
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
      final StringBuffer sb = new StringBuffer("Point3D : (");
      sb.append(this.x);
      sb.append(", ");
      sb.append(this.y);
      sb.append(", ");
      sb.append(this.z);
      sb.append(")");
      return sb.toString();
   }

   /**
    * To JOGL vector
    * 
    * @return JOGL vector
    */
   public Vec3f toVect3f()
   {
      return new Vec3f(this.x, this.y, this.z);
   }

   /**
    * To JOGL vector
    * 
    * @return JOGL vector
    */
   public Vec4f toVect4f()
   {
      return new Vec4f(this.x, this.y, this.z, 1f);
   }

   /**
    * Translate
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    */
   public void translate(final float x, final float y, final float z)
   {
      this.x += x;
      this.y += y;
      this.z += z;
   }

   /**
    * Translate
    * 
    * @param point
    *           Translation vector
    */
   public void translate(final Point3D point)
   {
      this.x += point.x;
      this.y += point.y;
      this.z += point.z;
   }
}