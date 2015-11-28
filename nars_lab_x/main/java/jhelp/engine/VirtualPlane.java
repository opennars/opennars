/**
 */
package jhelp.engine;

import com.jogamp.opengl.GL2;
import jhelp.engine.util.BufferUtils;
import jhelp.engine.util.Math3D;

/**
 * Virtual pane. <br>
 * Could use for clipping<br>
 * Could be use to know witch side of a plane is a point.<br>
 * The plane equation is : aX+bY+cZ+d=0; <br>
 * <br>
 * 
 * @author JHelp
 */
public class VirtualPlane
{
   /**
    * Side of point form the plane <br>
    * <br>
    * 
    * @author JHelp
    */
   public enum Side
   {
      /** Back of the plane */
      BACK,
      /** Front of the plane */
      FRONT,
      /** On the plane */
      ON
   }

   /** Next plane ID */
   private static int  PLANE = GL2.GL_CLIP_PLANE0;
   /** <i>a</i> on the equation : aX+bY+cZ+d=0 */
   private final float a;
   /** <i>b</i> on the equation : aX+bY+cZ+d=0 */
   private final float b;
   /** <i>c</i> on the equation : aX+bY+cZ+d=0 */
   private final float c;
   /** <i>d</i> on the equation : aX+bY+cZ+d=0 */
   private final float d;
   /** Normal of the plane */
   private Point3D     normal;
   /** Plane ID */
   private int         plane;

   /**
    * Constructs the plane with the equation : aX+bY+cZ+d=0
    * 
    * @param a
    *           <i>a</i> on the equation : aX+bY+cZ+d=0
    * @param b
    *           <i>b</i> on the equation : aX+bY+cZ+d=0
    * @param c
    *           <i>c</i> on the equation : aX+bY+cZ+d=0
    * @param d
    *           <i>d</i> on the equation : aX+bY+cZ+d=0
    */
   public VirtualPlane(final float a, final float b, final float c, final float d)
   {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
      this.plane = -1;
   }

   /**
    * Constructs plane with normal vector and one point of the plane
    * 
    * @param normal
    *           Normal vector of the plane
    * @param point
    *           Point on the plane
    */
   public VirtualPlane(final Point3D normal, final Point3D point)
   {
      this.normal = normal;
      this.normal.normalize();
      this.a = this.normal.getX();
      this.b = this.normal.getY();
      this.c = this.normal.getZ();
      this.d = (-this.a * point.getX()) - (this.b * point.getY()) - (this.c * point.getZ());
      this.plane = -1;
   }

   /**
    * Constructs a plane with 3 points
    * 
    * @param point1
    *           Point 1
    * @param point2
    *           Point 2
    * @param point3
    *           Point 3
    */
   public VirtualPlane(final Point3D point1, final Point3D point2, final Point3D point3)
   {
      final Point3D vector1 = point2.substract(point1);
      final Point3D vector2 = point3.substract(point1);
      this.normal = vector1.product(vector2);
      this.normal.normalize();
      this.a = this.normal.getX();
      this.b = this.normal.getY();
      this.c = this.normal.getZ();
      this.d = (-this.a * point1.getX()) - (this.b * point1.getY()) - (this.c * point1.getZ());
      this.plane = -1;
   }

   /**
    * Create a clip plane (used by the scene renderer, don't use it directly)
    * 
    * @param gl
    *           OpenGL context
    */
   void glClip(final GL2 gl)
   {
      if(this.plane < 0)
      {
         this.plane = VirtualPlane.PLANE;
         VirtualPlane.PLANE++;
      }
      BufferUtils.TEMPORARY_DOUBLE_BUFFER.rewind();
      BufferUtils.TEMPORARY_DOUBLE_BUFFER.put(this.a);
      BufferUtils.TEMPORARY_DOUBLE_BUFFER.put(this.b);
      BufferUtils.TEMPORARY_DOUBLE_BUFFER.put(this.c);
      BufferUtils.TEMPORARY_DOUBLE_BUFFER.put(this.d);
      BufferUtils.TEMPORARY_DOUBLE_BUFFER.rewind();
      gl.glClipPlane(this.plane, BufferUtils.TEMPORARY_DOUBLE_BUFFER);
   }

   /**
    * Indicates if a point is on the plane
    * 
    * @param point
    *           Test point
    * @return {@code true} if a point is on the plane
    */
   public boolean contains(final Point3D point)
   {
      return Math3D.nul((this.a * point.getX()) + (this.b * point.getY()) + (this.c * point.getZ()) + this.d);
   }

   /**
    * Apply the equation of the plane to a point
    * 
    * @param point
    *           Point put on the equation
    * @return The results of the equation
    */
   public float equation(final Point3D point)
   {
      return (this.a * point.getX()) + (this.b * point.getY()) + (this.c * point.getZ()) + this.d;
   }

   /**
    * Distance a point form the plane
    * 
    * @param point
    *           Point test
    * @return Distance a point form the plane
    */
   public float getDistance(final Point3D point)
   {
      return Math.abs(this.getSignedDistance(point));
   }

   /**
    * Normal of the plane
    * 
    * @return Normal of the plane
    */
   public Point3D getNormal()
   {
      if(this.normal == null)
      {
         this.normal = new Point3D(this.a, this.b, this.c);
      }
      return this.normal;
   }

   /**
    * Distance signed a point form the plane
    * 
    * @param point
    *           Point test
    * @return Distance signed a point form the plane
    */
   public float getSignedDistance(final Point3D point)
   {
      final float equation = this.equation(point);
      return equation / ((float) Math.sqrt((this.a * this.a) + (this.b * this.b) + (this.c * this.c)));
   }

   /**
    * Compute orthogonal projection of a point
    * 
    * @param point
    *           Point to project
    * @return Projected point
    */
   public Point3D orthogonalProjection(final Point3D point)
   {
      final float a = this.a;
      final float b = this.b;
      final float c = this.c;
      final float d = this.d;
      final float a2 = a * a;
      final float b2 = b * b;
      final float c2 = c * c;
      final float div = a2 + b2 + c2;
      final float x = point.getX();
      final float y = point.getY();
      final float z = point.getZ();
      return new Point3D(//
            (((b2 + c2) * x) - (a * b * y) - (a * c * z) - (d * a)) / div,//
            (((-a * b * x) + ((a2 + c2) * y)) - (b * c * z) - (d * b)) / div,//
            ((((-a * c * x) - (b * c * y)) + ((a2 + b2) * z)) - (d * c)) / div//
      );
   }

   /**
    * Compute symmetric orthogonal of a point
    * 
    * @param point
    *           Point to symetize
    * @return Symetrized point
    */
   public Point3D symtericOrthogonal(final Point3D point)
   {
      final float a = this.a;
      final float b = this.b;
      final float c = this.c;
      final float d = this.d;
      final float a2 = a * a;
      final float b2 = b * b;
      final float c2 = c * c;
      final float div = a2 + b2 + c2;
      final float x = point.getX();
      final float y = point.getY();
      final float z = point.getZ();
      final float xp = (((b2 + c2) * x) - (a * b * y) - (a * c * z) - (d * a)) / div;
      final float yp = (((-a * b * x) + ((a2 + c2) * y)) - (b * c * z) - (d * b)) / div;
      final float zp = ((((-a * c * x) - (b * c * y)) + ((a2 + b2) * z)) - (d * c)) / div;
      return new Point3D(//
            x + (2 * (xp - x)),//
            y + (2 * (yp - y)),//
            z + (2 * (zp - z)//
                  ));
   }

   /**
    * Compute the side of a point from the plane
    * 
    * @param point
    *           Point test
    * @return Side of the point
    */
   public Side witchSide(final Point3D point)
   {
      final float side = this.equation(point);
      if(Math3D.nul(side))
      {
         return Side.ON;
      }
      if(side < 0)
      {
         return Side.BACK;
      }
      return Side.FRONT;
   }
}