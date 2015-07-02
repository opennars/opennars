/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.util<br>
 * Class : Quaternion<br>
 * Date : 2 sept. 2008<br>
 * By JHelp
 */
package jhelp.engine.util;

import com.jogamp.opengl.GL2;

/**
 * Quaternion : W + Xi + Yj + Zk<br>
 * <br>
 * Last modification : 24 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Quaternion
{
   /**
    * Compute interpolation between 2 quaternions
    * 
    * @param quaternionStart
    *           Quaternion for start
    * @param quaternionEnd
    *           Quaternion for end
    * @param percent
    *           Percent of way into start to end (In [0, 1] 0 : satrt, 1: end, other somewhere between)
    * @param quaternionInterpolate
    *           Quaternion instance receive the interpolated value (If it is <code>null</null> a new quaternion is created)
    * @return Interpolated quaternion (The modified <code>quaternionInterpolate </code> if it is not {@code null})
    */
   public static Quaternion computeInterpolation(final Quaternion quaternionStart, final Quaternion quaternionEnd, final float percent, Quaternion quaternionInterpolate)
   {
      if(quaternionStart == null)
      {
         throw new NullPointerException("The quaternionStart musn't be null !");
      }

      if(quaternionEnd == null)
      {
         throw new NullPointerException("The quaternionEnd musn't be null !");
      }

      if(quaternionInterpolate == null)
      {
         quaternionInterpolate = new Quaternion();
      }

      float startX = quaternionStart.x;
      float startY = quaternionStart.y;
      float startZ = quaternionStart.z;
      float startW = quaternionStart.w;

      float dot = (startX * quaternionEnd.x) + (startY * quaternionEnd.y) + (startZ * quaternionEnd.z) + (startW * quaternionEnd.w);
      if(dot < 0)
      {
         startX *= -1;
         startY *= -1;
         startZ *= -1;
         startW *= -1;
         dot *= -1;
      }
      float angle, sinAngle, sinPercentStart, sinPercentEnd;
      sinPercentStart = sinPercentEnd = 0f;
      if((1f - dot) > 1e-6f)
      {
         angle = (float) Math.acos(dot);
         sinAngle = (float) Math.sin(angle);
         sinPercentStart = (float) Math.sin(angle * (1f - percent)) / sinAngle;
         sinPercentEnd = (float) Math.sin(angle * percent) / sinAngle;
      }
      else
      {
         sinPercentStart = 1f - percent;
         sinPercentEnd = percent;
      }

      quaternionInterpolate.x = (startX * sinPercentStart) + (quaternionEnd.x * sinPercentEnd);
      quaternionInterpolate.y = (startY * sinPercentStart) + (quaternionEnd.y * sinPercentEnd);
      quaternionInterpolate.z = (startZ * sinPercentStart) + (quaternionEnd.z * sinPercentEnd);
      quaternionInterpolate.w = (startW * sinPercentStart) + (quaternionEnd.w * sinPercentEnd);

      return quaternionInterpolate;
   }

   /**
    * Create quaternion base on rotation
    * 
    * @param rotationX
    *           Rotation axis' X
    * @param rotationY
    *           Rotation axis' Y
    * @param rotationZ
    *           Rotation axis' Z
    * @param rotationAngle
    *           Rotation angle
    * @return Created quaternion
    */
   public static Quaternion createQuaternion(final float rotationX, final float rotationY, final float rotationZ, final float rotationAngle)
   {
      final Quaternion quaternion = new Quaternion();
      quaternion.w = (float) Math.cos(rotationAngle / 2f);
      final float size = (float) Math.sqrt((rotationX * rotationX) + (rotationY * rotationY) + (rotationZ * rotationZ));
      final float sin = (float) Math.sin(rotationAngle / 2f);
      quaternion.x = (rotationX * sin) / size;
      quaternion.y = (rotationY * sin) / size;
      quaternion.z = (rotationZ * sin) / size;
      quaternion.normalize();
      return quaternion;
   }

   /** W */
   public float w;
   /** X */
   public float x;

   /** Y */
   public float y;

   /** Z */
   public float z;

   /**
    * Constructs Zero Quaternion
    */
   public Quaternion()
   {
      this(0, 0, 0, 0);
   }

   /**
    * Constructs real Quaternion
    * 
    * @param r
    *           Real
    */
   public Quaternion(final float r)
   {
      this(r, 0, 0, 0);
   }

   /**
    * Constructs Quaternion
    * 
    * @param w
    *           W
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    */
   public Quaternion(final float w, final float x, final float y, final float z)
   {
      this.w = w;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   /**
    * Constructs Quaternion
    * 
    * @param rotation
    *           Base rotation for create the quaternion
    */
   public Quaternion(final Rotation rotation)
   {
      this.w = (float) Math.cos(rotation.angle / 2f);
      final float size = (float) Math.sqrt((rotation.x * rotation.x) + (rotation.y * rotation.y) + (rotation.z * rotation.z));
      final float sin = (float) Math.sin(rotation.angle / 2f);
      this.x = (rotation.x * sin) / size;
      this.y = (rotation.y * sin) / size;
      this.z = (rotation.z * sin) / size;
      this.normalize();
   }

   /**
    * Quaternion conjugate
    * 
    * @return Quaternion conjugate
    */
   public Quaternion getConjugate()
   {
      return new Quaternion(this.w, -this.x, -this.y, this.z);
   }

   /**
    * Quaternion norme
    * 
    * @return Quaternion norme
    */
   public float getNorme()
   {
      return (float) Math.sqrt((this.w * this.w) + (this.x * this.x) + (this.y * this.y) + (this.z * this.z));
   }

   /**
    * Apply quaternion like a rotation
    * 
    * @param gl
    *           OpenGL context
    */
   public void glRotatef(final GL2 gl)
   {
      this.normalize();
      final float angle = (float) Math.acos(this.w) * 2f;
      float vx = this.x;
      float vy = this.y;
      float vz = this.z;
      final float norm = (float) Math.sqrt((vx * vx) + (vy * vy) + (vz * vz));
      if(norm > 0.0005)
      {
         vx /= norm;
         vy /= norm;
         vz /= norm;
      }
      gl.glRotatef(angle, vx, vy, vz);
   }

   /**
    * Multiply by a quaternion
    * 
    * @param quaternion
    *           Quaternion multiplied
    * @return Result
    */
   public Quaternion multiply(final Quaternion quaternion)
   {
      return new Quaternion(//
            (this.w * quaternion.w) - (this.x * quaternion.x) - (this.y * quaternion.y) - (this.z * quaternion.z), //
            ((this.w * quaternion.x) + (this.x * quaternion.w) + (this.y * quaternion.z)) - (this.z * quaternion.y), //
            ((this.w * quaternion.y) + (this.y * quaternion.w) + (this.x * quaternion.z)) - (this.z * quaternion.x), //
            ((this.w * quaternion.z) + (this.z * quaternion.w) + (this.x * quaternion.y)) - (this.y * quaternion.x//
                  ));
   }

   /**
    * Nomalize quaternion
    */
   public void normalize()
   {
      final float norme = this.getNorme();
      if(Math3D.nul(norme))
      {
         return;
      }
      this.w /= norme;
      this.x /= norme;
      this.y /= norme;
      this.z /= norme;
   }

   /**
    * Rotate around X axis
    * 
    * @param angle
    *           Rotation angle
    */
   public void rotateX(final float angle)
   {
      float x;
      float y;
      float z;
      float w;
      float rx;
      float rw;

      x = this.x;
      y = this.y;
      z = this.z;
      w = this.w;

      rx = (float) Math.sin(angle / 2f);
      rw = (float) Math.cos(angle / 2f);

      this.w = (w * rw) - (x * rx);
      this.x = (w * rx) + (x * rw);
      this.y = (y * rw) - (z * rx);
      this.z = (z * rw) - (y * rx);
   }

   /**
    * Rotate around Y axis
    * 
    * @param angle
    *           Rotation angle
    */
   public void rotateY(final float angle)
   {
      float x;
      float y;
      float z;
      float w;
      float ry;
      float rw;

      x = this.x;
      y = this.y;
      z = this.z;
      w = this.w;

      ry = (float) Math.sin(angle / 2f);
      rw = (float) Math.cos(angle / 2f);

      this.w = (w * rw) - (y * ry);
      this.x = (x * rw) - (z * ry);
      this.y = (w * ry) + (y * rw);
      this.z = (z * rw) + (x * ry);
   }

   /**
    * Rotate around Z axis
    * 
    * @param angle
    *           Rotation angle
    */
   public void rotateZ(final float angle)
   {
      float x;
      float y;
      float z;
      float w;
      float rz;
      float rw;

      x = this.x;
      y = this.y;
      z = this.z;
      w = this.w;

      rz = (float) Math.sin(angle / 2f);
      rw = (float) Math.cos(angle / 2f);

      this.w = (w * rw) - (z * rz);
      this.x = (x * rw) + (y * rz);
      this.y = (y * rw) + (x * rz);
      this.z = (w * rz) + (z * rw);
   }

   /**
    * Transform quaternion to rotation
    * 
    * @return Result rotation
    */
   public Rotation toRotation()
   {
      this.normalize();
      final float angle = (float) Math.acos(this.w) * 2f;
      float vx = this.x;
      float vy = this.y;
      float vz = this.z;
      final float norm = (float) Math.sqrt((vx * vx) + (vy * vy) + (vz * vz));
      if(norm > 0.0005)
      {
         vx /= norm;
         vy /= norm;
         vz /= norm;
      }
      return new Rotation(vx, vy, vz, angle);
   }
}