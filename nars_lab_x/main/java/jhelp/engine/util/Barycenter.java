/**
 */
package jhelp.engine.util;

/**
 * Barycenter of set of value <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Barycenter
{
   /** Actual barycenter */
   private double barycenter;
   /** Number of elements put in the set */
   private int    count;

   /**
    * Constructs Barycenter
    */
   public Barycenter()
   {
      this.count = 0;
   }

   /**
    * Add a value to the set
    * 
    * @param value
    *           Value add
    */
   public void add(double value)
   {
      if(this.count == 0)
      {
         this.barycenter = value;
         this.count = 1;
         return;
      }
      //
      this.barycenter = ((this.count * this.barycenter) + value) / (this.count + 1d);
      this.count++;
   }

   /**
    * The actual barycenter
    * 
    * @return Actual barycenter
    */
   public double getBarycenter()
   {
      return this.barycenter;
   }

   /**
    * Indicates if the barycenter is empty
    * 
    * @return {@code true} if the barycenter is empty
    */
   public boolean isEmpty()
   {
      return this.count == 0;
   }
}