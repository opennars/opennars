package jhelp.engine.util;

import jhelp.engine.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Triangle list
 * 
 * @author JHelp
 */
public class Triangles
{
   /** Trinale list */
   private final ArrayList<Triangle> triangles;

   /**
    * Create a new instance of Triangles
    */
   public Triangles()
   {
      this.triangles = new ArrayList<Triangle>();
   }

   /**
    * Add a triangle
    * 
    * @param triangle
    *           Triangle to add
    */
   public void addTriangle(final Triangle triangle)
   {
      this.triangles.add(triangle);
   }

   /**
    * Add a triangle
    * 
    * @param first
    *           First point of triangle
    * @param second
    *           Second point of triangle
    * @param third
    *           Third point of triangle
    */
   public void addTriangle(final Vertex first, final Vertex second, final Vertex third)
   {
      this.triangles.add(new Triangle(first, second, third));
   }

   /**
    * Add a convex polygon in traingle set
    * 
    * @param polygon
    *           Points of the convex polygon
    */
   public void convertInTriangles(final Vertex... polygon)
   {
      if((polygon == null) || (polygon.length < 3))
      {
         return;
      }

      final int length = polygon.length;
      final Vertex first = polygon[0];

      for(int i = 2; i < length; i++)
      {
         this.triangles.add(new Triangle(first, polygon[i - 1], polygon[i]));
      }
   }

   /**
    * List of triangles
    * 
    * @return List of triangles
    */
   public List<Triangle> obtainTriangleList()
   {
      return Collections.unmodifiableList(this.triangles);
   }
}