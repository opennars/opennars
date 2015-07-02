/**
 */
package jhelp.engine;

/**
 * A bone <br>
 * Now its consist of 2 points that we try let the length not change<br>
 * For work, change only one of point (Not to big change for a better work) and call {@link #updateBone()} <br>
 * 
 * @author JHelp
 */
public class Bone
{
   /** Actual first point */
   private Point3D point1;
   /** Actual second point */
   private Point3D point2;
   /** Start first point */
   private Point3D startPoint1;
   /** Start second point */
   private Point3D startPoint2;
   /** Length to preserve */
   private float   length;
   /** Indicates if bone is active */
   public boolean  active = true;

   /**
    * Constructs Bone
    * 
    * @param point1
    *           First point
    * @param point2
    *           Second point
    */
   public Bone(Point3D point1, Point3D point2)
   {
      this.point1 = point1;
      this.point2 = point2;
      this.startPoint1 = new Point3D(point1);
      this.startPoint2 = new Point3D(point2);
      float x = this.point2.getX() - this.point1.getX();
      float y = this.point2.getY() - this.point1.getY();
      float z = this.point2.getZ() - this.point1.getZ();
      this.length = (float) Math.sqrt(x * x + y * y + z * z);
   }

   /**
    * Indicates if a point is a point of the bone
    * 
    * @param point
    *           Point test
    * @return If the point is one off two points of the bone
    */
   public boolean isAPoint(Point3D point)
   {
      return this.point1.equals(point) || this.point2.equals(point);
   }

   /**
    * Update the bone<br>
    * It does nothing if the bone is not active, first and second point not move or move together.<br>
    * Only one point must have changed
    * 
    * @return <code>true</br> if the update append
    */
   public boolean updateBone()
   {
      if(this.active == false)
      {
         return false;
      }
      if(this.point1.equals(this.startPoint1) && this.point2.equals(this.startPoint2))
      {
         return false;
      }
      if(this.point1.equals(this.startPoint1))
      {
         this.updateBone2();
      }
      else if(this.point2.equals(this.startPoint2))
      {
         this.updateBone1();
      }
      else
      {
         return false;
      }
      this.startPoint1.set(this.point1);
      this.startPoint2.set(this.point2);
      return true;
   }

   /**
    * Move second point to keep length (First not change)
    */
   private void updateBone1()
   {
      float x = this.point2.getX() - this.point1.getX();
      float y = this.point2.getY() - this.point1.getY();
      float z = this.point2.getZ() - this.point1.getZ();
      float length = (float) Math.sqrt(x * x + y * y + z * z);
      this.point2.set(this.point1.getX() + (x * this.length) / length, this.point1.getY() + (y * this.length) / length, this.point1.getZ() + (z * this.length) / length);
   }

   /**
    * Move first point to keep length (Second not change)
    */
   private void updateBone2()
   {
      float x = this.point1.getX() - this.point2.getX();
      float y = this.point1.getY() - this.point2.getY();
      float z = this.point1.getZ() - this.point2.getZ();
      float length = (float) Math.sqrt(x * x + y * y + z * z);
      this.point1.set(this.point2.getX() + (x * this.length) / length, this.point2.getY() + (y * this.length) / length, this.point2.getZ() + (z * this.length) / length);
   }
}