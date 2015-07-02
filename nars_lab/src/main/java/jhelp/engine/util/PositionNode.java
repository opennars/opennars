/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.util<br>
 * Class : Position<br>
 * Date : 4 sept. 2008<br>
 * By JHelp
 */
package jhelp.engine.util;

import jhelp.engine.Node;

/**
 * Position for node <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class PositionNode
{
   /** Angle for X axis */
   public float angleX;
   /** Angle for Y axis */
   public float angleY;
   /** Angle for Z axis */
   public float angleZ;
   /** Scale on X axis */
   public float scaleX;
   /** Scale on Y axis */
   public float scaleY;
   /** Scale on Z axis */
   public float scaleZ;
   /** X */
   public float x;
   /** Y */
   public float y;
   /** z */
   public float z;

   /**
    * Constructs PositionNode
    */
   public PositionNode()
   {
      this.x = this.y = this.z = this.angleX = this.angleY = this.angleZ = 0;
      this.scaleX = this.scaleY = this.scaleZ = 1;
   }

   /**
    * Create a new instance of PositionNode
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    */
   public PositionNode(final float x, final float y, final float z)
   {
      this();

      this.x = x;
      this.y = y;
      this.z = z;
   }

   /**
    * Create a new instance of PositionNode
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    * @param angleX
    *           Angle X
    * @param angleY
    *           Angle Y
    * @param angleZ
    *           Angle Z
    */
   public PositionNode(final float x, final float y, final float z, final float angleX, final float angleY, final float angleZ)
   {
      this(x, y, z);

      this.angleX = angleX;
      this.angleY = angleY;
      this.angleZ = angleZ;
   }

   /**
    * Create a new instance of PositionNode
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    * @param angleX
    *           Angle X
    * @param angleY
    *           Angle Y
    * @param angleZ
    *           Angle Z
    * @param scaleX
    *           Scale X
    * @param scaleY
    *           Scale Y
    * @param scaleZ
    *           Scale Z
    */
   public PositionNode(final float x, final float y, final float z, final float angleX, final float angleY, final float angleZ, final float scaleX, final float scaleY, final float scaleZ)
   {
      this(x, y, z, angleX, angleY, angleZ);

      this.scaleX = scaleX;
      this.scaleY = scaleY;
      this.scaleZ = scaleZ;
   }

   /**
    * Constructs PositionNode
    * 
    * @param node
    *           Base node
    */
   public PositionNode(final Node node)
   {
      this.x = node.getX();
      this.y = node.getY();
      this.z = node.getZ();
      this.angleX = node.getAngleX();
      this.angleY = node.getAngleY();
      this.angleZ = node.getAngleZ();
      this.scaleX = node.getScaleX();
      this.scaleY = node.getScaleY();
      this.scaleZ = node.getScaleZ();
   }

   /**
    * Create a new instance of PositionNode copy of an other one
    * 
    * @param positionNode
    *           Position to copy
    */
   public PositionNode(final PositionNode positionNode)
   {
      this(positionNode.x, positionNode.y, positionNode.z, positionNode.angleX, positionNode.angleY, positionNode.angleZ, positionNode.scaleX, positionNode.scaleY, positionNode.scaleZ);
   }

   /**
    * Create a copy of the position
    * 
    * @return The copy
    */
   public PositionNode copy()
   {
      return new PositionNode(this);
   }

   /**
    * String representation <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return String representation
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      final StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append('(');
      stringBuilder.append(this.x);
      stringBuilder.append(", ");
      stringBuilder.append(this.y);
      stringBuilder.append(", ");
      stringBuilder.append(this.z);
      stringBuilder.append(") RX=");
      stringBuilder.append(this.angleX);
      stringBuilder.append(" RY=");
      stringBuilder.append(this.angleY);
      stringBuilder.append(" RZ=");
      stringBuilder.append(this.angleZ);
      stringBuilder.append(" [");
      stringBuilder.append(this.scaleX);
      stringBuilder.append('x');
      stringBuilder.append(this.scaleY);
      stringBuilder.append('x');
      stringBuilder.append(this.scaleZ);
      stringBuilder.append(']');
      return stringBuilder.toString();
   }
}