/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.geom<br>
 * Class : Revoluton<br>
 * Date : 17 janv. 2009<br>
 * By JHelp
 */
package jhelp.engine.geom;

import jhelp.engine.*;
import jhelp.engine.io.ConstantsXML;
import jhelp.engine.twoD.Line2D;
import jhelp.engine.twoD.Path;
import jhelp.util.list.EnumerationIterator;
import jhelp.util.text.UtilText;
import jhelp.xml.MarkupXML;

import java.util.ArrayList;

/**
 * Revolution is a path draw on (X, Y) plane, then turn it around Y axis <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Revolution
      extends Object3D
{
   /** Angle of rotation */
   private float      angle;
   /** U multiplier for rotation (V defines by the path) */
   private float      multU;
   /** The path (Values are U) */
   private final Path path;
   /** Precision used to draw the path */
   private int        pathPrecision;

   /** Precision for rotation */
   private int        rotationPrecision;

   /**
    * Constructs default Revolution
    */
   public Revolution()
   {
      this(360f, 5, 12, 1f);
   }

   /**
    * Constructs Revolution
    * 
    * @param angle
    *           Angle of rotation
    * @param pathPrecision
    *           Path precision
    * @param rotationPrecision
    *           Rotation precision
    * @param multU
    *           U multiplier (V are carry by the path)
    */
   public Revolution(final float angle, final int pathPrecision, final int rotationPrecision, final float multU)
   {
      this.nodeType = NodeType.REVOLUTION;
      this.path = new Path();
      this.angle = angle;
      this.setPathPrecision(pathPrecision);
      this.setRotationPrecision(rotationPrecision);
      this.multU = multU;
   }

   /**
    * Refresh the revolution's mesh.
    */
   private void recomputeTheMesh()
   {
      this.recomputeTheMesh(false, 0, 1);
   }

   /**
    * Refresh the revolution's mesh.
    * 
    * @param omogeonous
    *           Indicates if try to make it omogenous or not
    * @param start
    *           Interpolation value at path start (Available only for omogenous at {@code true})
    * @param end
    *           Interpolation value at path end (Available only for omogenous at {@code true})
    */
   private void recomputeTheMesh(final boolean omogeonous, final float start, final float end)
   {
      // Initialization
      final Mesh mesh = new Mesh();

      final double radian = Math.toRadians(this.angle);
      double angle, angleFuture;
      double cos, cosFuture, sin, sinFuture;
      double x0, y0, x1, y1, vx, vy;
      double u0, u1, v0, v1;
      double length;

      float xAA, yAA, zAA, uAA, vAA, nxAA, nyAA, nzAA;
      float xAF, yAF, zAF, uAF, vAF, nxAF, nyAF, nzAF;
      float xFA, yFA, zFA, uFA, vFA, nxFA, nyFA, nzFA;
      float xFF, yFF, zFF, uFF, vFF, nxFF, nyFF, nzFF;

      int an;

      ArrayList<Line2D> list;
      if(omogeonous == true)
      {
         list = this.path.computePathOmogenous(this.pathPrecision, start, end);
      }
      else
      {
         list = this.path.computePath(this.pathPrecision);
      }

      // For each line of the path
      for(final Line2D line2D : list)
      {
         // Get start and end point
         x0 = line2D.pointStart.getX();
         y0 = line2D.pointStart.getY();
         v0 = line2D.start;

         x1 = line2D.pointEnd.getX();
         y1 = line2D.pointEnd.getY();
         v1 = line2D.end;

         // Compute the vector start to end and normalize it
         vx = x1 - x0;
         vy = y1 - y0;

         length = Math.sqrt((vx * vx) + (vy * vy));
         if(Math.abs(length) >= 1e-5)
         {
            vx /= length;
            vy /= length;
         }

         // For each rotation step
         for(an = 0; an < this.rotationPrecision; an++)
         {
            // Compute U
            u0 = (an * this.multU) / this.rotationPrecision;
            u1 = ((an + 1f) * this.multU) / this.rotationPrecision;

            // Compute angles, cosinus and sinus
            angle = (radian * an) / this.rotationPrecision;
            angleFuture = (radian * (an + 1)) / this.rotationPrecision;

            cos = Math.cos(angle);
            sin = Math.sin(angle);
            cosFuture = Math.cos(angleFuture);
            sinFuture = Math.sin(angleFuture);

            // Compute each vertex
            xAA = (float) (cos * x0);
            yAA = (float) (y0);
            zAA = (float) (-sin * x0);
            uAA = (float) (u0);
            vAA = (float) (v0);
            nxAA = (float) (cos * vy);
            nyAA = (float) (vx);
            nzAA = (float) (-sin * vy);

            xAF = (float) (cos * x1);
            yAF = (float) (y1);
            zAF = (float) (-sin * x1);
            uAF = (float) (u0);
            vAF = (float) (v1);
            nxAF = (float) (cos * vy);
            nyAF = (float) (vx);
            nzAF = (float) (-sin * vy);

            xFA = (float) (cosFuture * x0);
            yFA = (float) (y0);
            zFA = (float) (-sinFuture * x0);
            uFA = (float) (u1);
            vFA = (float) (v0);
            nxFA = (float) (cosFuture * vy);
            nyFA = (float) (vx);
            nzFA = (float) (-sinFuture * vy);

            xFF = (float) (cosFuture * x1);
            yFF = (float) (y1);
            zFF = (float) (-sinFuture * x1);
            uFF = (float) (u1);
            vFF = (float) (v1);
            nxFF = (float) (cosFuture * vy);
            nyFF = (float) (vx);
            nzFF = (float) (-sinFuture * vy);

            // Draw the face
            mesh.addVertexToTheActualFace(new Vertex(xAA, yAA, zAA, uAA, vAA, nxAA, nyAA, nzAA));
            mesh.addVertexToTheActualFace(new Vertex(xFA, yFA, zFA, uFA, vFA, nxFA, nyFA, nzFA));
            mesh.addVertexToTheActualFace(new Vertex(xFF, yFF, zFF, uFF, vFF, nxFF, nyFF, nzFF));
            mesh.addVertexToTheActualFace(new Vertex(xAF, yAF, zAF, uAF, vAF, nxAF, nyAF, nzAF));

            mesh.endFace();
         }
      }

      // Change object's mesh by the computed one
      this.mesh = mesh;
   }

   /**
    * Refresh the revolution's mesh omogeously
    * 
    * @param start
    *           Interpolation value at path start
    * @param end
    *           Interpolation value at path
    */
   private void recomputeTheMesh(final float start, final float end)
   {
      this.recomputeTheMesh(true, start, end);
   }

   /**
    * @see jhelp.engine.Node#endParseXML()
    */
   @Override
   protected void endParseXML()
   {
      this.refreshRevolution();
   }

   /**
    * Read revolution parameters form XML
    * 
    * @param markupXML
    *           XML to parse
    * @throws Exception
    *            On parsing problem
    * @see jhelp.engine.Node#readFromMarkup
    */
   @Override
   protected void readFromMarkup(final MarkupXML markupXML) throws Exception
   {
      this.readMaterialFromMarkup(markupXML);
      this.angle = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_angle, 360f);
      this.pathPrecision = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_pathPrecision, 5);
      this.rotationPrecision = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_rotationPrecision, 12);
      this.multU = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_multU, 1f);

      final EnumerationIterator<MarkupXML> enumerationIterator = markupXML.obtainChildren(ConstantsXML.MARKUP_PATH);
      if(enumerationIterator.hasMoreElements() == false)
      {
         throw new IllegalArgumentException(UtilText.concatenate("Missing mendatory child ", ConstantsXML.MARKUP_PATH, " in ", markupXML.getName()));
      }

      MarkupXML path = enumerationIterator.getNextElement();
      this.path.loadFromXML(path);
      path = null;
   }

   /**
    * Write revolution in XML
    * 
    * @param markupXML
    *           Markup to fill
    * @see jhelp.engine.Node#writeInMarkup
    */
   @Override
   protected void writeInMarkup(final MarkupXML markupXML)
   {
      this.writeMaterialInMarkup(markupXML);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_angle, this.angle);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_pathPrecision, this.pathPrecision);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_rotationPrecision, this.rotationPrecision);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_multU, this.multU);
      markupXML.addChild(this.path.saveToXML());
   }

   /**
    * Append cubic element to the path
    * 
    * @param startPoint
    *           Start point
    * @param start
    *           start value
    * @param controlPoint1
    *           First control point
    * @param control1
    *           First control value
    * @param controlPoint2
    *           Second control point
    * @param control2
    *           Second control value
    * @param endPoint
    *           End point
    * @param end
    *           End value
    */
   public void appendCubic(final Point2D startPoint, final float start, final Point2D controlPoint1, final float control1, final Point2D controlPoint2, final float control2, final Point2D endPoint, final float end)
   {
      this.path.appendCubic(startPoint, start, controlPoint1, control1, controlPoint2, control2, endPoint, end);
   }

   /**
    * Append cubic element to the path
    * 
    * @param startPoint
    *           Start point
    * @param controlPoint1
    *           First control point
    * @param controlPoint2
    *           Second control point
    * @param endPoint
    *           End point
    */
   public void appendCubic(final Point2D startPoint, final Point2D controlPoint1, final Point2D controlPoint2, final Point2D endPoint)
   {
      this.path.appendCubic(startPoint, controlPoint1, controlPoint2, endPoint);
   }

   /**
    * Append line to the path
    * 
    * @param startPoint
    *           Start point
    * @param start
    *           Start value
    * @param endPoint
    *           End point
    * @param end
    *           End value
    */
   public void appendLine(final Point2D startPoint, final float start, final Point2D endPoint, final float end)
   {
      this.path.appendLine(startPoint, start, endPoint, end);
   }

   /**
    * Append line to the path
    * 
    * @param startPoint
    *           Start point
    * @param endPoint
    *           End point
    */
   public void appendLine(final Point2D startPoint, final Point2D endPoint)
   {
      this.path.appendLine(startPoint, endPoint);
   }

   /**
    * Append quadric element to the path
    * 
    * @param startPoint
    *           Strat point
    * @param start
    *           Start value
    * @param controlPoint
    *           Control point
    * @param control
    *           Control value
    * @param endPoint
    *           End point
    * @param end
    *           End value
    */
   public void appendQuad(final Point2D startPoint, final float start, final Point2D controlPoint, final float control, final Point2D endPoint, final float end)
   {
      this.path.appendQuad(startPoint, start, controlPoint, control, endPoint, end);
   }

   /**
    * Append quadric element to the path
    * 
    * @param startPoint
    *           Start point
    * @param controlPoint
    *           Control point
    * @param endPoint
    *           End point
    */
   public void appendQuad(final Point2D startPoint, final Point2D controlPoint, final Point2D endPoint)
   {
      this.path.appendQuad(startPoint, controlPoint, endPoint);
   }

   /**
    * Return angle
    * 
    * @return angle
    */
   public float getAngle()
   {
      return this.angle;
   }

   /**
    * Return multU
    * 
    * @return multU
    */
   public float getMultU()
   {
      return this.multU;
   }

   /**
    * Obtain revolution path
    * 
    * @return Revolution path
    */
   public Path getPath()
   {
      return this.path;
   }

   /**
    * Return pathPrecision
    * 
    * @return pathPrecision
    */
   public int getPathPrecision()
   {
      return this.pathPrecision;
   }

   /**
    * Return rotationPrecision
    * 
    * @return rotationPrecision
    */
   public int getRotationPrecision()
   {
      return this.rotationPrecision;
   }

   /**
    * Try to linearize the path
    * 
    * @param start
    *           Start value
    * @param end
    *           End value
    */
   public void linearize(final float start, final float end)
   {
      this.path.linearize(start, end);
   }

   /**
    * Refresh the revolution.<br>
    * Call it when you made modification and want see the result.
    */
   public void refreshRevolution()
   {
      this.recomputeTheMesh();
      this.reconstructTheList();
   }

   /**
    * Refresh the revolution's mesh omogeously
    * 
    * @param start
    *           Interpolation value at path start
    * @param end
    *           Interpolation value at path
    */
   public void refreshRevolution(final float start, final float end)
   {
      this.recomputeTheMesh(start, end);
      this.reconstructTheList();
   }

   /**
    * Modify angle
    * 
    * @param angle
    *           New angle value
    */
   public void setAngle(final float angle)
   {
      this.angle = angle;
   }

   /**
    * Modify multU
    * 
    * @param multU
    *           New multU value
    */
   public void setMultU(final float multU)
   {
      this.multU = multU;
   }

   /**
    * Modify pathPrecision
    * 
    * @param pathPrecision
    *           New pathPrecision value
    */
   public void setPathPrecision(int pathPrecision)
   {
      if(pathPrecision < 2)
      {
         pathPrecision = 2;
      }
      this.pathPrecision = pathPrecision;
   }

   /**
    * Modify rotationPrecision
    * 
    * @param rotationPrecision
    *           New rotationPrecision value
    */
   public void setRotationPrecision(int rotationPrecision)
   {
      if(rotationPrecision < 3)
      {
         rotationPrecision = 3;
      }
      this.rotationPrecision = rotationPrecision;
   }
}