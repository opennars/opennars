/**
 */
package jhelp.engine;
//

import com.jogamp.opengl.glu.GLU;
import jhelp.engine.io.ConstantsXML;
import jhelp.engine.util.Math3D;
import jhelp.engine.util.Tool3D;
import jhelp.math.Mat4f;
import jhelp.math.Rotf;
import jhelp.math.Vec3f;
import jhelp.math.Vec4f;
import jhelp.xml.MarkupXML;

/**
 * Camera correspond at the GLU function : <code>gluLookAt</code>.<br>
 * So it have position, look vector and up vector.<br>
 * We can also decided that the camera always look the the node <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Camera
{
   /** Node that the camera stare */
   private Node    keepLookOnMe;
   /** Where camera look */
   private Point3D look;
   /** Camera position */
   private Point3D position;
   /** Camera up */
   private Point3D up;
   /** Name of the looked object */
   public String   lookName;

   /**
    * Create default camera
    */
   public Camera()
   {
      this(new Point3D(), new Point3D(0, 0, -1), new Point3D(0, 1, 0));
   }

   /**
    * Create Camera
    * 
    * @param position
    *           Position
    * @param look
    *           Look vector
    * @param up
    *           UP vector
    */
   public Camera(final Point3D position, final Point3D look, final Point3D up)
   {
      this.position = position;
      this.look = look;
      this.up = up;
   }

   /**
    * Compute the rotation of a vector around an axis
    * 
    * @param vectorToRotate
    *           Vector to rotate
    * @param axis
    *           Axis to turn around
    * @param angle
    *           Rotation angle
    * @return Rotated vector
    */
   private Vec3f computeRotate(final Point3D vectorToRotate, final Point3D axis, float angle)
   {
      angle = Math3D.degreToRadian(angle);
      final Rotf rot = new Rotf(axis.toVect3f(), angle);
      final Vec3f vec = vectorToRotate.toVect3f();
      return rot.rotateVector(vec);
   }

   /**
    * Compute the rotation of a point around an other point if direction off an axis
    * 
    * @param pointToRotate
    *           Point to rotate
    * @param center
    *           Rotation center
    * @param axis
    *           Axis to turn around
    * @param angle
    *           Rotation angle
    * @return Rotated point
    */
   private Vec4f computeRotate(final Point3D pointToRotate, final Point3D center, final Point3D axis, float angle)
   {
      angle = Math3D.degreToRadian(angle);
      final Mat4f mat4fRot = new Mat4f();
      mat4fRot.makeIdent();
      final Rotf rot = new Rotf(axis.toVect3f(), angle);
      mat4fRot.setRotation(rot);
      final Mat4f mat4fTransl = new Mat4f();
      Vec3f vec3f = center.toVect3f();
      mat4fTransl.makeIdent();
      mat4fTransl.setTranslation(vec3f);
      final Mat4f mat4fAntiTransl = new Mat4f();
      vec3f = center.getOptsite().toVect3f();
      mat4fAntiTransl.makeIdent();
      mat4fAntiTransl.setTranslation(vec3f);
      final Mat4f mat = mat4fAntiTransl.mul(mat4fRot).mul(mat4fTransl);
      final Vec4f vec4f = pointToRotate.toVect4f();
      final Vec4f result = new Vec4f();
      mat.xformVec(vec4f, result);
      return result;
   }

   /**
    * Render the camera
    * 
    * @param glu
    *           GLU context
    */
   void render(final GLU glu)
   {
      // If the camera must look a node, compute look vector
      if(this.keepLookOnMe != null)
      {
         this.look = this.keepLookOnMe.getProjection(this.keepLookOnMe.getCenter()).substract(this.position);
      }

      glu.gluLookAt(//
            this.position.getX(), this.position.getY(), this.position.getZ(),//
            this.look.getX(), this.look.getY(), this.look.getZ(),//
            this.up.getX(), this.up.getY(), this.up.getZ());
   }

   /**
    * Look vector
    * 
    * @return Look vector
    */
   public Point3D getLook()
   {
      return this.look;
   }

   /**
    * Compute the look point
    * 
    * @return Look point
    */
   public Point3D getLookPoint()
   {
      return this.look.add(this.position);
   }

   /**
    * Position
    * 
    * @return Position
    */
   public Point3D getPosition()
   {
      return this.position;
   }

   /**
    * Up vector
    * 
    * @return Up vector
    */
   public Point3D getUp()
   {
      return this.up;
   }

   /**
    * Load camera parameter from XML
    * 
    * @param markupXML
    *           Markup to parse
    */
   public void loadFromXML(final MarkupXML markupXML)
   {
      this.look = Tool3D.getPoint3DParameter(markupXML, ConstantsXML.MARKUP_CAMERA_look);
      this.position = Tool3D.getPoint3DParameter(markupXML, ConstantsXML.MARKUP_CAMERA_position);
      this.up = Tool3D.getPoint3DParameter(markupXML, ConstantsXML.MARKUP_CAMERA_up);
      this.lookName = markupXML.obtainParameter(ConstantsXML.MARKUP_CAMERA_keepLookOnMe, (String) null);
   }

   /**
    * Force the camera to look a node.<br>
    * Use {@code null} to free the camera
    * 
    * @param node
    *           Node to look
    */
   public void lookAt(final Node node)
   {
      this.keepLookOnMe = node;
   }

   /**
    * Rotate camera around UP vector
    * 
    * @param angle
    *           Angle
    */
   public void rotateUP(final float angle)
   {
      if(this.keepLookOnMe != null)
      {
         final Point3D center = this.keepLookOnMe.getProjection(this.keepLookOnMe.getCenter());
         this.position.set(this.computeRotate(this.position, center, this.up, angle));
      }
      else
      {
         this.look.set(this.computeRotate(this.look, this.up, angle));
      }
   }

   /**
    * Save camera in XML
    * 
    * @return XML representation
    */
   public MarkupXML saveToXML()
   {
      final MarkupXML markupXML = new MarkupXML(ConstantsXML.MARKUP_CAMERA);
      Tool3D.addPoint3DParameter(markupXML, ConstantsXML.MARKUP_CAMERA_look, this.look);
      Tool3D.addPoint3DParameter(markupXML, ConstantsXML.MARKUP_CAMERA_position, this.position);
      Tool3D.addPoint3DParameter(markupXML, ConstantsXML.MARKUP_CAMERA_up, this.up);
      if(this.keepLookOnMe != null)
      {
         markupXML.addParameter(ConstantsXML.MARKUP_CAMERA_keepLookOnMe, this.keepLookOnMe.nodeName);
      }
      return markupXML;
   }

   /**
    * Change look vector
    * 
    * @param look
    *           New look vector
    */
   public void setLook(final Point3D look)
   {
      if(look == null)
      {
         throw new NullPointerException("The look couldn't be null");
      }
      this.look = look;
   }

   /**
    * Change position
    * 
    * @param position
    *           New position
    */
   public void setPosition(final Point3D position)
   {
      if(position == null)
      {
         throw new NullPointerException("The position couldn't be null");
      }
      this.position = position;
   }

   /**
    * Change UP vector
    * 
    * @param up
    *           New up vector
    */
   public void setUp(final Point3D up)
   {
      if(up == null)
      {
         throw new NullPointerException("The up couldn't be null");
      }
      this.up = up;
   }

   /**
    * String representation of the camera
    * 
    * @return String representation of the camera
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      return "Camera : Position=" + this.position + " Look=" + this.look + " Up=" + this.up;
   }

   /**
    * Translate the camera
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
      this.position.translate(x, y, z);
      this.look.translate(x, y, z);
   }

   /**
    * Translate the camera on its look direction
    * 
    * @param length
    *           Length of translation
    */
   public void translateOnLookWay(final float length)
   {
      this.look.normalize();
      this.position.translate(length * this.look.getX(), length * this.look.getY(), length * this.look.getZ());
   }
}