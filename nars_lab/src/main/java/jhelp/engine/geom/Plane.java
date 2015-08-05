/**
 */
package jhelp.engine.geom;

import jhelp.engine.*;
import jhelp.engine.io.ConstantsXML;
import jhelp.xml.MarkupXML;

/**
 * A plane.<br>
 * It's cut on gird, so it is possible to make elevation grid with it <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Plane
      extends Object3D
{
   /** Number of horizontal part */
   private int     horizontal;
   /** Indicates if U are inverted */
   private boolean invU;
   /** Indicates if V are inverted */
   private boolean invV;
   /** Number of vertical part */
   private int     vertical;

   /**
    * A basic plane
    */
   public Plane()
   {
      this(1, 1);
   }

   /**
    * Constructs Plane
    * 
    * @param invU
    *           Indicates if U are inverted
    * @param invV
    *           Indicates if V are inverted
    */
   public Plane(final boolean invU, final boolean invV)
   {
      this(1, 1, invU, invV);
   }

   /**
    * Constructs Plane
    * 
    * @param horizontal
    *           Number of horizontal part
    * @param vertical
    *           Number of vertical part
    */
   public Plane(final int horizontal, final int vertical)
   {
      this(horizontal, vertical, false, false);
   }

   /**
    * Constructs Plane
    * 
    * @param horizontal
    *           Number of horizontal part
    * @param vertical
    *           Number of vertical part
    * @param invU
    *           Indicates if U are inverted
    * @param invV
    *           Indicates if V are inverted
    */
   public Plane(final int horizontal, final int vertical, final boolean invU, final boolean invV)
   {
      this.initialize(horizontal, vertical, invU, invV);
   }

   /**
    * Reconstruct the plane
    * 
    * @param horizontal
    *           Number of horizontal parts
    * @param vertical
    *           Number of vertical parts
    * @param invU
    *           Indicates if U are inverted
    * @param invV
    *           Indicates if V are inverted
    */
   private void initialize(int horizontal, int vertical, final boolean invU, final boolean invV)
   {
      this.nodeType = NodeType.PLANE;
      // Initialization
      if(horizontal < 1)
      {
         horizontal = 1;
      }
      if(vertical < 1)
      {
         vertical = 1;
      }
      this.horizontal = horizontal;
      this.vertical = vertical;
      this.invU = invU;
      this.invV = invV;

      this.mesh = new Mesh();

      int y;
      int x;
      float xx;
      float yy;
      float xx1;
      float yy1;
      final float v = vertical;
      final float h = horizontal;
      final Vertex vertex = new Vertex();
      vertex.setNormal(0, 0, -1f);

      // For each vertical part
      for(y = 0; y < vertical; y++)
      {
         // Compute Y
         yy = y / v;
         yy1 = (y + 1f) / v;

         // For each horizontal part
         for(x = 0; x < horizontal; x++)
         {
            // Compute X
            xx = x / h;
            xx1 = (x + 1f) / h;

            // Add the face
            vertex.setPosition(new Point3D(xx - 0.5f, yy - 0.5f, 0));
            vertex.setUv(this.makeUV(xx, yy, invU, invV));
            this.add(vertex);
            vertex.setPosition(new Point3D(xx - 0.5f, yy1 - 0.5f, 0));
            vertex.setUv(this.makeUV(xx, yy1, invU, invV));
            this.add(vertex);
            vertex.setPosition(new Point3D(xx1 - 0.5f, yy1 - 0.5f, 0));
            vertex.setUv(this.makeUV(xx1, yy1, invU, invV));
            this.add(vertex);
            vertex.setPosition(new Point3D(xx1 - 0.5f, yy - 0.5f, 0));
            vertex.setUv(this.makeUV(xx1, yy, invU, invV));
            this.add(vertex);
            //
            this.nextFace();
         }
      }

      this.reconstructTheList();
   }

   /**
    * Compute real UV
    * 
    * @param u
    *           Actual U
    * @param v
    *           Actual V
    * @param invU
    *           Indicates if U are inverted
    * @param invV
    *           Indicates if V are inverted
    * @return Rel UV
    */
   private Point2D makeUV(float u, float v, final boolean invU, final boolean invV)
   {
      if(invU)
      {
         u = 1f - u;
      }
      if(invV)
      {
         v = 1f - v;
      }
      return new Point2D(u, v);
   }

   /**
    * Call on parsing end
    * 
    * @see jhelp.engine.Node#endParseXML()
    */
   @Override
   protected void endParseXML()
   {
      this.initialize(this.horizontal, this.vertical, this.invU, this.invV);
   }

   /**
    * Read Plane parameters from XML
    * 
    * @param markupXML
    *           Markup to parse
    * @see jhelp.engine.Node#readFromMarkup
    */
   @Override
   protected void readFromMarkup(final MarkupXML markupXML)
   {
      this.readMaterialFromMarkup(markupXML);
      this.horizontal = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_horizontal, 1);
      this.vertical = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_vertical, 1);
      this.invU = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_invU, false);
      this.invV = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_invV, false);
   }

   /**
    * Write plane on XML
    * 
    * @param markupXML
    *           Markup to fill
    * @see jhelp.engine.Node#writeInMarkup
    */
   @Override
   protected void writeInMarkup(final MarkupXML markupXML)
   {
      this.writeMaterialInMarkup(markupXML);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_horizontal, this.horizontal);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_vertical, this.vertical);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_invU, this.invU);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_invV, this.invV);
   }

   /**
    * Number part in horizontal
    * 
    * @return Number part in horizontal
    */
   public int getHorizontal()
   {
      return this.horizontal;
   }

   /**
    * Number part in vertical
    * 
    * @return Number part in vertical
    */
   public int getVertical()
   {
      return this.vertical;
   }

   /**
    * Indicates if U are inverted
    * 
    * @return {@code true} if U are inverted
    */
   public boolean isInvU()
   {
      return this.invU;
   }

   /**
    * Indicates if V are inverted
    * 
    * @return {@code true} if V are inverted
    */
   public boolean isInvV()
   {
      return this.invV;
   }
}