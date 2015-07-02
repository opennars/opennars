/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.geom<br>
 * Class : PathGeom<br>
 * Date : 17 janv. 2009<br>
 * By JHelp
 */
package jhelp.engine.geom;

import jhelp.engine.NodeType;
import jhelp.engine.Object3D;
import jhelp.engine.Point2D;
import jhelp.engine.io.ConstantsXML;
import jhelp.engine.twoD.Path;
import jhelp.engine.util.Tool3D;
import jhelp.util.list.EnumerationIterator;
import jhelp.util.text.UtilText;
import jhelp.xml.MarkupXML;

/**
 * Path geom is build with a path for U, and path for V.<br>
 * V path was repeat several times throw U path <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class PathGeom
      extends Object3D
{
   /** Indicates if path is joined */
   private boolean   joined;
   /** Indicates if path is linearize */
   private boolean   linearize;
   /** U multiplier */
   private float     multU;
   /** precision for U path */
   private int       precisionU;
   /** precision for V path */
   private int       precisionV;
   /** U path */
   public final Path pathU;
   /** V path */
   public final Path pathV;

   /**
    * Constructs default PathGeom
    */
   public PathGeom()
   {
      this(5, 5);
   }

   /**
    * Constructs PathGeom
    * 
    * @param precisionU
    *           U path precision
    * @param precisionV
    *           V path precision
    */
   public PathGeom(final int precisionU, final int precisionV)
   {
      this.nodeType = NodeType.PATH_GEOM;

      this.pathU = new Path();
      this.pathV = new Path();
      this.setPrecisionU(precisionU);
      this.setPrecisionV(precisionV);

      this.joined = true;
      this.multU = 1;
      this.linearize = false;
   }

   /**
    * @see jhelp.engine.Node#endParseXML()
    */
   @Override
   protected void endParseXML()
   {
      if(this.joined == true)
      {
         this.refreshJoinedPath(this.multU, this.linearize);
         return;
      }
      this.refreshPath();
   }

   /**
    * Read parameters for XML
    * 
    * @param markupXML
    *           Markup to parse
    * @throws Exception
    *            On reading/parsing issue
    * @see jhelp.engine.Node#readFromMarkup
    */
   @Override
   protected void readFromMarkup(final MarkupXML markupXML) throws Exception
   {
      this.readMaterialFromMarkup(markupXML);
      this.precisionU = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_precisionU, 5);
      this.precisionV = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_precisionV, 5);
      this.multU = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_multU, 1f);
      this.joined = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_joined, true);
      this.linearize = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_linearize, false);

      EnumerationIterator<MarkupXML> enumerationIterator = markupXML.obtainChildren(ConstantsXML.MARKUP_PATH_U);
      if(enumerationIterator.hasMoreElements() == false)
      {
         throw new IllegalArgumentException(UtilText.concatenate("Missing mendatory child ", ConstantsXML.MARKUP_PATH_U, " in ", markupXML.getName()));
      }

      MarkupXML path = enumerationIterator.getNextElement();

      enumerationIterator = path.obtainChildren(ConstantsXML.MARKUP_PATH);
      if(enumerationIterator.hasMoreElements() == false)
      {
         throw new IllegalArgumentException(UtilText.concatenate("Missing mandatory child ", ConstantsXML.MARKUP_PATH, " in ", path.getName()));
      }

      path = enumerationIterator.getNextElement();
      this.pathU.loadFromXML(path);

      //

      enumerationIterator = markupXML.obtainChildren(ConstantsXML.MARKUP_PATH_V);
      if(enumerationIterator.hasMoreElements() == false)
      {
         throw new IllegalArgumentException(UtilText.concatenate("Missing mandatory child ", ConstantsXML.MARKUP_PATH_V, " in ", markupXML.getName()));
      }

      path = enumerationIterator.getNextElement();

      enumerationIterator = path.obtainChildren(ConstantsXML.MARKUP_PATH);
      if(enumerationIterator.hasMoreElements() == false)
      {
         throw new IllegalArgumentException(UtilText.concatenate("Missing mandatory child ", ConstantsXML.MARKUP_PATH, " in ", path.getName()));
      }

      path = enumerationIterator.getNextElement();
      this.pathV.loadFromXML(path);

      path = null;
   }

   /**
    * Write on XML
    * 
    * @param markupXML
    *           Markup to fill
    * @see jhelp.engine.Node#writeInMarkup
    */
   @Override
   protected void writeInMarkup(final MarkupXML markupXML)
   {
      this.writeMaterialInMarkup(markupXML);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_precisionU, this.precisionU);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_precisionV, this.precisionV);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_multU, this.multU);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_joined, this.joined);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_linearize, this.linearize);

      MarkupXML path = new MarkupXML(ConstantsXML.MARKUP_PATH_U);
      path.addChild(this.pathU.saveToXML());
      markupXML.addChild(path);

      path = new MarkupXML(ConstantsXML.MARKUP_PATH_V);
      path.addChild(this.pathV.saveToXML());
      markupXML.addChild(path);

      path = null;
   }

   /**
    * Append cubic to U path
    * 
    * @param startPoint
    *           Start point
    * @param start
    *           Start value
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
   public void appendCubicU(final Point2D startPoint, final float start, final Point2D controlPoint1, final float control1, final Point2D controlPoint2, final float control2, final Point2D endPoint, final float end)
   {
      this.pathU.appendCubic(startPoint, start, controlPoint1, control1, controlPoint2, control2, endPoint, end);
   }

   /**
    * Append cubic to U path
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
   public void appendCubicU(final Point2D startPoint, final Point2D controlPoint1, final Point2D controlPoint2, final Point2D endPoint)
   {
      this.pathU.appendCubic(startPoint, controlPoint1, controlPoint2, endPoint);
   }

   /**
    * Append cubic to V path
    * 
    * @param startPoint
    *           Start point
    * @param start
    *           Start value
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
   public void appendCubicV(final Point2D startPoint, final float start, final Point2D controlPoint1, final float control1, final Point2D controlPoint2, final float control2, final Point2D endPoint, final float end)
   {
      this.pathV.appendCubic(startPoint, start, controlPoint1, control1, controlPoint2, control2, endPoint, end);
   }

   /**
    * Append cubic to V path
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
   public void appendCubicV(final Point2D startPoint, final Point2D controlPoint1, final Point2D controlPoint2, final Point2D endPoint)
   {
      this.pathV.appendCubic(startPoint, controlPoint1, controlPoint2, endPoint);
   }

   /**
    * Append line on U path
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
   public void appendLineU(final Point2D startPoint, final float start, final Point2D endPoint, final float end)
   {
      this.pathU.appendLine(startPoint, start, endPoint, end);
   }

   /**
    * Append line on U path
    * 
    * @param startPoint
    *           Start point
    * @param endPoint
    *           End point
    */
   public void appendLineU(final Point2D startPoint, final Point2D endPoint)
   {
      this.pathU.appendLine(startPoint, endPoint);
   }

   /**
    * Append line on V path
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
   public void appendLineV(final Point2D startPoint, final float start, final Point2D endPoint, final float end)
   {
      this.pathV.appendLine(startPoint, start, endPoint, end);
   }

   /**
    * Append line on V path
    * 
    * @param startPoint
    *           Start point
    * @param endPoint
    *           End point
    */
   public void appendLineV(final Point2D startPoint, final Point2D endPoint)
   {
      this.pathV.appendLine(startPoint, endPoint);
   }

   /**
    * Append quadric to U path
    * 
    * @param startPoint
    *           Start point
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
   public void appendQuadU(final Point2D startPoint, final float start, final Point2D controlPoint, final float control, final Point2D endPoint, final float end)
   {
      this.pathU.appendQuad(startPoint, start, controlPoint, control, endPoint, end);
   }

   /**
    * Append quadric to U path
    * 
    * @param startPoint
    *           Start point
    * @param controlPoint
    *           Control point
    * @param endPoint
    *           End point
    */
   public void appendQuadU(final Point2D startPoint, final Point2D controlPoint, final Point2D endPoint)
   {
      this.pathU.appendQuad(startPoint, controlPoint, endPoint);
   }

   /**
    * Append quadric to V path
    * 
    * @param startPoint
    *           Start point
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
   public void appendQuadV(final Point2D startPoint, final float start, final Point2D controlPoint, final float control, final Point2D endPoint, final float end)
   {
      this.pathV.appendQuad(startPoint, start, controlPoint, control, endPoint, end);
   }

   /**
    * Append quadric to V path
    * 
    * @param startPoint
    *           Start point
    * @param controlPoint
    *           Control point
    * @param endPoint
    *           End point
    */
   public void appendQuadV(final Point2D startPoint, final Point2D controlPoint, final Point2D endPoint)
   {
      this.pathV.appendQuad(startPoint, controlPoint, endPoint);
   }

   /**
    * Return precisionU
    * 
    * @return precisionU
    */
   public int getPrecisionU()
   {
      return this.precisionU;
   }

   /**
    * Return precisionV
    * 
    * @return precisionV
    */
   public int getPrecisionV()
   {
      return this.precisionV;
   }

   /**
    * Try to linearize paths
    * 
    * @param startU
    *           Start U
    * @param endU
    *           End U
    * @param startV
    *           Start V
    * @param endV
    *           End V
    */
   public void linearize(final float startU, final float endU, final float startV, final float endV)
   {
      this.pathU.linearize(startU, endU);
      this.pathV.linearize(startV, endV);
   }

   /**
    * Refresh path and force join each part
    * 
    * @param multU
    *           U multiplier
    * @param linearize
    *           Indicates if we try to linearize (Some try done good effect, some don't, so try)
    */
   public void refreshJoinedPath(final float multU, final boolean linearize)
   {
      this.refreshJoinedPath(multU, linearize, false);
   }

   /**
    * Refresh path and force join each part
    * 
    * @param multU
    *           U multiplier
    * @param linearize
    *           Indicates if we try to linearize (Some try done good effect, some don't, so try)
    * @param reverseNormals
    *           Indicates if normals must be reversed
    */
   public void refreshJoinedPath(final float multU, final boolean linearize, final boolean reverseNormals)
   {
      this.joined = true;
      this.multU = multU;
      this.linearize = linearize;
      this.mesh = Tool3D.createJoinedMesh(this.pathU, this.precisionU, this.pathV, this.precisionV, multU, linearize, reverseNormals);
      this.reconstructTheList();
   }

   /**
    * Refresh the path
    */
   public void refreshPath()
   {
      this.joined = false;
      this.multU = 1;
      this.linearize = false;
      this.mesh = Tool3D.createMesh(this.pathU, this.precisionU, this.pathV, this.precisionV);
      this.reconstructTheList();
   }

   /**
    * Modify precisionU
    * 
    * @param precisionU
    *           New precisionU value
    */
   public void setPrecisionU(int precisionU)
   {
      if(precisionU < 2)
      {
         precisionU = 2;
      }
      this.precisionU = precisionU;
   }

   /**
    * Modify precisionV
    * 
    * @param precisionV
    *           New precisionV value
    */
   public void setPrecisionV(int precisionV)
   {
      if(precisionV < 2)
      {
         precisionV = 2;
      }
      this.precisionV = precisionV;
   }
}