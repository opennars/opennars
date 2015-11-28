/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine<br>
 * Class : NodeType<br>
 * Date : 7 fevr. 2009<br>
 * By JHelp
 */
package jhelp.engine;

/**
 * Node type <br>
 * <br>
 * Last modification : 7 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public enum NodeType
{
   /** Node */
   NODE,
   /** 3D object */
   OBJECT3D,
   /** Clone */
   CLONE,
   /** Box */
   BOX,
   /** Path geometry (Build from tow path) */
   PATH_GEOM,
   /** Plane */
   PLANE,
   /** Revolution (Path rotate around Y axis) */
   REVOLUTION,
   /** Sphere */
   SPHERE,
   /** Equation */
   EQUATION
}