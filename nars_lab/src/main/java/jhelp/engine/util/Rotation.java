/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.util<br>
 * Class : Rotation<br>
 * Date : 2 sept. 2008<br>
 * By JHelp
 */
package jhelp.engine.util;

import com.jogamp.opengl.GL2;

/**
 * Rotation aroud an axis <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Rotation
{
   /** Rotation angle */
   public float angle;
   /** Axis' X */
   public float x;
   /** Axis' Y */
   public float y;
   /** Axis' Z */
   public float z;

   /**
    * Constructs Rotation
    */
   public Rotation()
   {
   }

   /**
    * Constructs Rotation
    * 
    * @param x
    *           Axis' X
    * @param y
    *           Axis' Y
    * @param z
    *           Axis' Z
    * @param angle
    *           Rotation angle
    */
   public Rotation(final float x, final float y, final float z, final float angle)
   {
      this.x = x;
      this.y = y;
      this.z = z;
      this.angle = angle;
   }

   /**
    * Apply rotation to OpenGL
    * 
    * @param gl
    *           OpenGL context
    */
   public void glRotatef(final GL2 gl)
   {
      if(Math3D.nul(this.angle) == false)
      {
         gl.glRotatef(this.angle, this.x, this.y, this.z);
      }
   }
}