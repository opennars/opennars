/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine<br>
 * Class : Polygon3D<br>
 * Date : 9 juin 2009<br>
 * By JHelp
 */
package jhelp.engine;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallback;

import java.awt.geom.PathIterator;
import java.util.ArrayList;

/**
 * Polygon 3D<br>
 * <br>
 * Last modification : 9 juin 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Polygon3D
      implements GLUtessellatorCallback
{
   /** Vertex count, exact meaning depends on triangle description type */
   private int                     count;
   /** Last triangle description type */
   private int                     lastType;
   /** Object to add polygon */
   private Object3D                object3D;
   /** Polygon vertex list */
   private final ArrayList<Vertex> polygon;
   /** Temporary last read vertex. Use depends on triangle description type */
   private Vertex                  vertex1;
   /** Temporary last read vertex. Use depends on triangle description type */
   private Vertex                  vertex2;
   /** Winding rule */
   private int                     windingRule;

   /**
    * Constructs Polygon3D
    * 
    * @param windingRule
    *           Winding rule
    */
   public Polygon3D(final int windingRule)
   {
      this.polygon = new ArrayList<Vertex>();

      switch(windingRule)
      {
         case PathIterator.WIND_EVEN_ODD:
            this.windingRule = GLU.GLU_TESS_WINDING_ODD;
         break;
         case PathIterator.WIND_NON_ZERO:
            this.windingRule = GLU.GLU_TESS_WINDING_NONZERO;
         break;
      }
   }

   /**
    * Add polygon to an object
    * 
    * @param object3D
    *           Object where add
    * @param glu
    *           OpenGL utilities
    */
   void addToObject(final Object3D object3D, final GLU glu)
   {
      this.object3D = object3D;
      this.object3D.nextFace();

      final GLUtessellator tessellator = glu.gluNewTess();

      glu.gluTessCallback(tessellator, GLU.GLU_TESS_BEGIN, this);
      glu.gluTessCallback(tessellator, GLU.GLU_TESS_COMBINE, this);
      glu.gluTessCallback(tessellator, GLU.GLU_TESS_ERROR, this);
      glu.gluTessCallback(tessellator, GLU.GLU_TESS_VERTEX, this);

      glu.gluTessProperty(tessellator, GLU.GLU_TESS_WINDING_RULE, this.windingRule);
      glu.gluTessBeginPolygon(tessellator, null);
      glu.gluTessBeginContour(tessellator);
      final double[] point = new double[3];
      for(final Vertex vertex : this.polygon)
      {
         point[0] = vertex.getPosition().x;
         point[1] = vertex.getPosition().y;
         point[2] = vertex.getPosition().z;

         glu.gluTessVertex(tessellator, point, 0, vertex);
      }
      glu.gluTessEndContour(tessellator);
      glu.gluTessEndPolygon(tessellator);
   }

   /**
    * Ad a vertex
    * 
    * @param vertex
    *           Vertex to add
    */
   public void add(final Vertex vertex)
   {
      this.polygon.add(vertex);
   }

   /**
    * Call when triangle list begin
    * 
    * @param type
    *           Triangle list mode
    * @see javax.media.opengl.glu.GLUtessellatorCallback#begin(int)
    */
   @Override
   public void begin(final int type)
   {
      this.lastType = type;
      this.count = 0;
   }

   /**
    * Do nothing here
    * 
    * @param type
    *           Unused
    * @param polygonData
    *           Unused
    * @see javax.media.opengl.glu.GLUtessellatorCallback#beginData(int, Object)
    */
   @Override
   public void beginData(final int type, final Object polygonData)
   {
   }

   /**
    * Combine coordinate, usually call on edge crossing
    * 
    * @param coords
    *           Coordinates
    * @param data
    *           Data
    * @param weight
    *           Weights
    * @param outData
    *           Where write result
    * @see javax.media.opengl.glu.GLUtessellatorCallback#combine(double[], Object[], float[], Object[])
    */
   @Override
   public void combine(final double[] coords, final Object[] data, final float[] weight, final Object[] outData)
   {
      final Vertex newVertex = new Vertex((float) coords[0], (float) coords[1], (float) coords[2]);

      final Vertex v0 = (Vertex) data[0];
      final Vertex v1 = (Vertex) data[1];
      final Vertex v2 = (Vertex) data[2];
      final Vertex v3 = (Vertex) data[3];

      if((v0 != null) && (v1 != null))
      {
         if((v2 != null) && (v3 != null))
         {
            newVertex.setPosition(((weight[0] * v0.getPosition().getX()) + (weight[1] * v1.getPosition().getX()) + (weight[2] * v2.getPosition().getX()) + (weight[3] * v3.getPosition().getX())), ((weight[0] * v0.getPosition().getY())
                  + (weight[1] * v1.getPosition().getY()) + (weight[2] * v2.getPosition().getY()) + (weight[3] * v3.getPosition().getY())), ((weight[0] * v0.getPosition().getZ()) + (weight[1] * v1.getPosition().getZ())
                  + (weight[2] * v2.getPosition().getZ()) + (weight[3] * v3.getPosition().getZ())));
            newVertex.setUV(((weight[0] * v0.getUv().getX()) + (weight[1] * v1.getUv().getX()) + (weight[2] * v2.getUv().getX()) + (weight[3] * v3.getUv().getX())), ((weight[0] * v0.getUv().getY()) + (weight[1] * v1.getUv().getY())
                  + (weight[2] * v2.getUv().getY()) + (weight[3] * v3.getUv().getY())));
            newVertex.setNormal(((weight[0] * v0.getNormal().getX()) + (weight[1] * v1.getNormal().getX()) + (weight[2] * v2.getNormal().getX()) + (weight[3] * v3.getNormal().getX())), ((weight[0] * v0.getNormal().getY())
                  + (weight[1] * v1.getNormal().getY()) + (weight[2] * v2.getNormal().getY()) + (weight[3] * v3.getNormal().getY())), ((weight[0] * v0.getNormal().getZ()) + (weight[1] * v1.getNormal().getZ())
                  + (weight[2] * v2.getNormal().getZ()) + (weight[3] * v3.getNormal().getZ())));
         }
         else
         {
            newVertex.setPosition(((weight[0] * v0.getPosition().getX()) + (weight[1] * v1.getPosition().getX())), ((weight[0] * v0.getPosition().getY()) + (weight[1] * v1.getPosition().getY())),
                  ((weight[0] * v0.getPosition().getZ()) + (weight[1] * v1.getPosition().getZ())));
            newVertex.setUV(((weight[0] * v0.getUv().getX()) + (weight[1] * v1.getUv().getX())), ((weight[0] * v0.getUv().getY()) + (weight[1] * v1.getUv().getY())));
            newVertex.setNormal(((weight[0] * v0.getNormal().getX()) + (weight[1] * v1.getNormal().getX())), ((weight[0] * v0.getNormal().getY()) + (weight[1] * v1.getNormal().getY())),
                  ((weight[0] * v0.getNormal().getZ()) + (weight[1] * v1.getNormal().getZ())));
         }
      }

      outData[0] = newVertex;
   }

   /**
    * Do nothing here
    * 
    * @param coords
    *           Unused
    * @param data
    *           Unused
    * @param weight
    *           Unused
    * @param outData
    *           Unused
    * @param polygonData
    *           Unused
    * @see javax.media.opengl.glu.GLUtessellatorCallback#combineData(double[], Object[], float[], Object[],
    *      Object)
    */
   @Override
   public void combineData(final double[] coords, final Object[] data, final float[] weight, final Object[] outData, final Object polygonData)
   {
   }

   /**
    * Do nothing here
    * 
    * @param boundaryEdge
    *           Unused
    * @see javax.media.opengl.glu.GLUtessellatorCallback#edgeFlag(boolean)
    */
   @Override
   public void edgeFlag(final boolean boundaryEdge)
   {
   }

   /**
    * Do nothing here
    * 
    * @param boundaryEdge
    *           Unused
    * @param polygonData
    *           Unused
    * @see javax.media.opengl.glu.GLUtessellatorCallback#edgeFlagData(boolean, Object)
    */
   @Override
   public void edgeFlagData(final boolean boundaryEdge, final Object polygonData)
   {
   }

   /**
    * Do nothing here
    * 
    * @see javax.media.opengl.glu.GLUtessellatorCallback#end()
    */
   @Override
   public void end()
   {
   }

   /**
    * Do nothing here
    * 
    * @param polygonData
    *           Unused
    * @see javax.media.opengl.glu.GLUtessellatorCallback#endData(Object)
    */
   @Override
   public void endData(final Object polygonData)
   {
   }

   /**
    * Do nothing here
    * 
    * @param errnum
    *           Unused
    * @see javax.media.opengl.glu.GLUtessellatorCallback#error(int)
    */
   @Override
   public void error(final int errnum)
   {
   }

   /**
    * Do nothing here
    * 
    * @param errnum
    *           Unused
    * @param polygonData
    *           Unused
    * @see javax.media.opengl.glu.GLUtessellatorCallback#errorData(int, Object)
    */
   @Override
   public void errorData(final int errnum, final Object polygonData)
   {
   }

   /**
    * Indicates if polygon is empty
    * 
    * @return {@code true} if polygon is empty
    */
   public boolean isEmpty()
   {
      return this.polygon.isEmpty();
   }

   /**
    * Add vertex as first element
    * 
    * @param vertex
    *           Vertex to add
    */
   public void push(final Vertex vertex)
   {
      this.polygon.add(0, vertex);
   }

   /**
    * Call when meet a vertex
    * 
    * @param vertexData
    *           Vertex
    * @see javax.media.opengl.glu.GLUtessellatorCallback#vertex(Object)
    */
   @Override
   public void vertex(final Object vertexData)
   {
      switch(this.lastType)
      {
         case GL.GL_TRIANGLES:
            this.object3D.add((Vertex) vertexData);
            this.count++;
            if(this.count == 2)
            {
               this.count = 0;
               this.object3D.nextFace();
            }
         break;
         case GL.GL_TRIANGLE_STRIP:
            switch(this.count)
            {
               case 0:
                  this.vertex1 = (Vertex) vertexData;
               break;
               case 1:
                  this.vertex2 = (Vertex) vertexData;
               break;
               default:
                  if((this.count % 2) == 0)
                  {
                     this.object3D.add(this.vertex1);
                     this.object3D.add(this.vertex2);
                     this.object3D.add((Vertex) vertexData);
                  }
                  else
                  {
                     this.object3D.add((Vertex) vertexData);
                     this.object3D.add(this.vertex2);
                     this.object3D.add(this.vertex1);
                  }
                  this.object3D.nextFace();

                  this.vertex1 = this.vertex2;
                  this.vertex2 = (Vertex) vertexData;
               break;
            }
            this.count++;
         break;
         case GL.GL_TRIANGLE_FAN:
            switch(this.count)
            {
               case 0:
                  this.vertex1 = (Vertex) vertexData;
               break;
               case 1:
                  this.vertex2 = (Vertex) vertexData;
               break;
               default:
                  this.object3D.add(this.vertex1);
                  this.object3D.add(this.vertex2);
                  this.object3D.add((Vertex) vertexData);
                  this.object3D.nextFace();

                  this.vertex2 = (Vertex) vertexData;
               break;
            }
            this.count++;
         break;
      }
   }

   /**
    * Do nothing here
    * 
    * @param vertexData
    *           Unused
    * @param polygonData
    *           Unused
    * @see javax.media.opengl.glu.GLUtessellatorCallback#vertexData(Object, Object)
    */
   @Override
   public void vertexData(final Object vertexData, final Object polygonData)
   {
   }
}