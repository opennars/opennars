/**
 */
package jhelp.engine.geom;

import jhelp.engine.*;
import jhelp.engine.io.ConstantsXML;
import jhelp.xml.MarkupXML;

/**
 * 3D object sphere, center (0, 0, 0), ray 1 <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Sphere
      extends Object3D
{
   /** Last point index */
   private final int lastPoint;
   /** U multiplier */
   private float     multU;
   /** V multiplier */
   private float     multV;
   /** Number of slice */
   private int       slice;
   /** Number of stack */
   private int       stack;

   /** South pole index */
   protected int     southPole;

   /**
    * Constructs default Sphere
    */
   public Sphere()
   {
      this(8, 8);
   }

   /**
    * Constructs Sphere
    * 
    * @param multU
    *           Number of repetition of U
    * @param multV
    *           Number of repetition of V
    */
   public Sphere(final float multU, final float multV)
   {
      this(33, 33, multU, multV);
   }

   /**
    * Constructs Sphere
    * 
    * @param slice
    *           Number of slice (If <2, then 2 is taken)
    * @param stack
    *           Number of stack (If <2, then 2 is taken)
    */
   public Sphere(final int slice, final int stack)
   {
      this(slice, stack, 1, 1);
   }

   /**
    * Constructs Sphere
    * 
    * @param slice
    *           Number of slice (If <2, then 2 is taken)
    * @param stack
    *           Number of stack (If <2, then 2 is taken)
    * @param multU
    *           Number of repetition of U
    * @param multV
    *           Number of repetition of V
    */
   public Sphere(int slice, int stack, final float multU, final float multV)
   {
      this.southPole = -1;

      this.nodeType = NodeType.SPHERE;

      // Temporary vertex
      Vertex vertex;

      // Angles compute for slice and stack
      double sliceAngle;
      double stackAngle;
      double sliceAngleFutur;
      double stackAngleFutur;

      // Cosinus and sinus of angles
      double cosSliceAngle;
      double cosStackAngle;
      double cosSliceAngleFutur;
      double cosStackAngleFutur;
      double sinSliceAngle;
      double sinStackAngle;
      double sinSliceAngleFutur;
      double sinStackAngleFutur;

      // Computed UV
      float uA;
      float vA;
      float uF;
      float vF;

      // Computed normals
      float nxAA;
      float nyAA;
      float nzAA;
      float nxFA;
      float nyFA;
      float nzFA;
      float nxAF;
      float nyAF;
      float nzAF;
      float nxFF;
      float nyFF;
      float nzFF;

      // To walk throw slice and stack
      int sli;
      int sta;

      // Initialize
      if(slice < 2)
      {
         slice = 2;
      }
      if(stack < 2)
      {
         stack = 2;
      }

      this.multU = multU;
      this.multV = multV;
      this.slice = slice;
      this.stack = stack;

      vertex = new Vertex();

      // For each slice
      for(sli = 0; sli < slice; sli++)
      {
         // Compute slice angles, cosinus and sinus
         sliceAngle = ((2d * Math.PI * sli) / slice) - Math.PI;
         sliceAngleFutur = ((2d * Math.PI * (sli + 1)) / slice) - Math.PI;
         //
         cosSliceAngle = Math.cos(sliceAngle);
         cosSliceAngleFutur = Math.cos(sliceAngleFutur);
         sinSliceAngle = Math.sin(sliceAngle);
         sinSliceAngleFutur = Math.sin(sliceAngleFutur);

         // Computes U (Slice walk throw U)
         uA = multU - ((multU * sli) / slice);
         uF = multU - ((multU * (sli + 1)) / slice);

         // For each stack
         for(sta = 0; sta < stack; sta++)
         {
            // Compute stack angles, cosinus and sinus
            stackAngle = ((Math.PI * sta) / stack) - (Math.PI / 2d);
            stackAngleFutur = ((Math.PI * (sta + 1)) / stack) - (Math.PI / 2d);
            //
            cosStackAngle = Math.cos(stackAngle);
            cosStackAngleFutur = Math.cos(stackAngleFutur);
            sinStackAngle = Math.sin(stackAngle);
            sinStackAngleFutur = Math.sin(stackAngleFutur);

            // Computes V (Stack walk throw V)
            vA = (multV * sta) / stack;
            vF = (multV * (sta + 1)) / stack;

            // Computes normals
            nxAA = (float) (cosSliceAngle * cosStackAngle);
            nyAA = (float) (sinSliceAngle * cosStackAngle);
            nzAA = (float) sinStackAngle;
            nxFA = (float) (cosSliceAngleFutur * cosStackAngle);
            nyFA = (float) (sinSliceAngleFutur * cosStackAngle);
            nzFA = (float) sinStackAngle;
            nxAF = (float) (cosSliceAngle * cosStackAngleFutur);
            nyAF = (float) (sinSliceAngle * cosStackAngleFutur);
            nzAF = (float) sinStackAngleFutur;
            nxFF = (float) (cosSliceAngleFutur * cosStackAngleFutur);
            nyFF = (float) (sinSliceAngleFutur * cosStackAngleFutur);
            nzFF = (float) sinStackAngleFutur;

            // Compute each vertex of the actual face
            vertex.setPosition(new Point3D(nxAA, nyAA, nzAA));
            vertex.setUv(new Point2D(uA, vA));
            vertex.setNormal(new Point3D(-nxAA, -nyAA, -nzAA));
            this.add(vertex);
            //
            vertex.setPosition(new Point3D(nxAF, nyAF, nzAF));
            vertex.setUv(new Point2D(uA, vF));
            vertex.setNormal(new Point3D(-nxAF, -nyAF, -nzAF));
            this.add(vertex);
            //
            vertex.setPosition(new Point3D(nxFF, nyFF, nzFF));
            vertex.setUv(new Point2D(uF, vF));
            vertex.setNormal(new Point3D(-nxFF, -nyFF, -nzFF));
            this.add(vertex);
            //
            vertex.setPosition(new Point3D(nxFA, nyFA, nzFA));
            vertex.setUv(new Point2D(uF, vA));
            vertex.setNormal(new Point3D(-nxFA, -nyFA, -nzFA));
            this.add(vertex);

            // Pass to the next face
            this.nextFace();
         }

         if(this.southPole < 0)
         {
            this.southPole = this.mesh.lastIndexPoint();
         }
      }

      this.lastPoint = this.mesh.lastIndexPoint();
   }

   /**
    * Call when parsing is done
    * 
    * @see jhelp.engine.Node#endParseXML()
    */
   @Override
   protected void endParseXML()
   {
      this.recompute(this.slice, this.stack, this.multU, this.multV);
   }

   /**
    * Read sphere parameters from XML
    * 
    * @param markupXML
    *           Markup to parse
    * @see jhelp.engine.Node#readFromMarkup
    */
   @Override
   protected void readFromMarkup(final MarkupXML markupXML)
   {
      this.readMaterialFromMarkup(markupXML);
      this.multU = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_multU, 1f);
      this.multU = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_multV, 1f);
      this.slice = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_slice, 33);
      this.stack = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_stack, 33);
   }

   /**
    * Write sphere in XML
    * 
    * @param markupXML
    *           Markup to fill
    * @see jhelp.engine.Node#writeInMarkup
    */
   @Override
   protected void writeInMarkup(final MarkupXML markupXML)
   {
      this.writeMaterialInMarkup(markupXML);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_multU, this.multU);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_multV, this.multV);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_slice, this.slice);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_stack, this.stack);
   }

   /**
    * Last draw point index
    * 
    * @return Last draw point index
    */
   public int lastPoint()
   {
      return this.lastPoint;
   }

   /**
    * North pole index
    * 
    * @return North pole index
    */
   public int northPole()
   {
      return 0;
   }

   /**
    * Recompute the sphere to default sphere
    */
   public void recompute()
   {
      this.recompute(33, 33);
   }

   /**
    * Recompute the sphere
    * 
    * @param multU
    *           Number of repetition of U
    * @param multV
    *           Number of repetition of V
    */
   public void recompute(final float multU, final float multV)
   {
      this.recompute(33, 33, multU, multV);
   }

   /**
    * Recompute the sphere
    * 
    * @param slice
    *           Number of slice (If <2, then 2 is taken)
    * @param stack
    *           Number of stack (If <2, then 2 is taken)
    */
   public void recompute(final int slice, final int stack)
   {
      this.recompute(slice, stack, 1, 1);
   }

   /**
    * Recompute the sphere
    * 
    * @param slice
    *           Number of slice (If <2, then 2 is taken)
    * @param stack
    *           Number of stack (If <2, then 2 is taken)
    * @param multU
    *           Number of repetition of U
    * @param multV
    *           Number of repetition of V
    */
   public void recompute(int slice, int stack, final float multU, final float multV)
   {
      this.southPole = -1;

      // Same algorithm see for constructs the sphere
      // Just put it on new mesh, and change the sphere's mesh at the end
      final Mesh mesh = new Mesh();
      //
      Vertex vertex;
      double sliceAngle;
      double stackAngle;
      double sliceAngleFutur;
      double stackAngleFutur;
      double cosSliceAngle;
      double cosStackAngle;
      double cosSliceAngleFutur;
      double cosStackAngleFutur;
      double sinSliceAngle;
      double sinStackAngle;
      double sinSliceAngleFutur;
      double sinStackAngleFutur;
      float uA;
      float vA;
      float uF;
      float vF;
      float nxAA;
      float nyAA;
      float nzAA;
      float nxFA;
      float nyFA;
      float nzFA;
      float nxAF;
      float nyAF;
      float nzAF;
      float nxFF;
      float nyFF;
      float nzFF;
      int sli;
      int sta;
      //
      if(slice < 2)
      {
         slice = 2;
      }
      if(stack < 2)
      {
         stack = 2;
      }

      this.multU = multU;
      this.multV = multV;
      this.slice = slice;
      this.stack = stack;

      vertex = new Vertex();
      for(sli = 0; sli < slice; sli++)
      {
         sliceAngle = ((2d * Math.PI * sli) / slice) - Math.PI;
         sliceAngleFutur = ((2d * Math.PI * (sli + 1)) / slice) - Math.PI;
         //
         cosSliceAngle = Math.cos(sliceAngle);
         cosSliceAngleFutur = Math.cos(sliceAngleFutur);
         sinSliceAngle = Math.sin(sliceAngle);
         sinSliceAngleFutur = Math.sin(sliceAngleFutur);
         //
         uA = (multU * sli) / slice;
         uF = (multU * (sli + 1)) / slice;
         //
         for(sta = 0; sta < stack; sta++)
         {
            stackAngle = ((Math.PI * sta) / stack) - (Math.PI / 2d);
            stackAngleFutur = ((Math.PI * (sta + 1)) / stack) - (Math.PI / 2d);
            //
            cosStackAngle = Math.cos(stackAngle);
            cosStackAngleFutur = Math.cos(stackAngleFutur);
            sinStackAngle = Math.sin(stackAngle);
            sinStackAngleFutur = Math.sin(stackAngleFutur);
            //
            vA = (multV * sta) / stack;
            vF = (multV * (sta + 1)) / stack;
            //
            nxAA = (float) (cosSliceAngle * cosStackAngle);
            nyAA = (float) (sinSliceAngle * cosStackAngle);
            nzAA = (float) sinStackAngle;
            nxFA = (float) (cosSliceAngleFutur * cosStackAngle);
            nyFA = (float) (sinSliceAngleFutur * cosStackAngle);
            nzFA = (float) sinStackAngle;
            nxAF = (float) (cosSliceAngle * cosStackAngleFutur);
            nyAF = (float) (sinSliceAngle * cosStackAngleFutur);
            nzAF = (float) sinStackAngleFutur;
            nxFF = (float) (cosSliceAngleFutur * cosStackAngleFutur);
            nyFF = (float) (sinSliceAngleFutur * cosStackAngleFutur);
            nzFF = (float) sinStackAngleFutur;
            //
            vertex.setPosition(new Point3D(nxAA, nyAA, nzAA));
            vertex.setUv(new Point2D(uA, vA));
            vertex.setNormal(new Point3D(-nxAA, -nyAA, -nzAA));
            mesh.addVertexToTheActualFace(vertex);
            //
            vertex.setPosition(new Point3D(nxAF, nyAF, nzAF));
            vertex.setUv(new Point2D(uA, vF));
            vertex.setNormal(new Point3D(-nxAF, -nyAF, -nzAF));
            mesh.addVertexToTheActualFace(vertex);
            //
            vertex.setPosition(new Point3D(nxFF, nyFF, nzFF));
            vertex.setUv(new Point2D(uF, vF));
            vertex.setNormal(new Point3D(-nxFF, -nyFF, -nzFF));
            mesh.addVertexToTheActualFace(vertex);
            //
            vertex.setPosition(new Point3D(nxFA, nyFA, nzFA));
            vertex.setUv(new Point2D(uF, vA));
            vertex.setNormal(new Point3D(-nxFA, -nyFA, -nzFA));
            mesh.addVertexToTheActualFace(vertex);
            //
            mesh.endFace();
         }

         if(this.southPole < 0)
         {
            this.southPole = this.mesh.lastIndexPoint();
         }
      }
      //
      this.mesh = mesh;
      this.reconstructTheList();
   }

   /**
    * South pole index
    * 
    * @return South pole index
    */
   public int southPole()
   {
      return this.southPole;
   }
}