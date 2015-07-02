package jhelp.engine.util;

import jhelp.engine.Vertex;

/**
 * A triangle
 * 
 * @author JHelp
 */
public class Triangle
{
   /** Triangle first vertex */
   public Vertex first;
   /** Triangle second vertex */
   public Vertex second;
   /** Triangle third vertex */
   public Vertex third;

   /**
    * Create a new instance of Triangle
    */
   public Triangle()
   {
      this.first = new Vertex();
      this.second = new Vertex();
      this.third = new Vertex();
   }

   /**
    * Create a new instance of Triangle
    * 
    * @param first
    *           Firt vertex
    * @param second
    *           Second vertex
    * @param third
    *           Third vertex
    */
   public Triangle(final Vertex first, final Vertex second, final Vertex third)
   {
      this.first = first;
      this.second = second;
      this.third = third;
   }
}