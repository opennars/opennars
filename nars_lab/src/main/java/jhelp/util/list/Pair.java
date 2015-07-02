package jhelp.util.list;

/**
 * Pair of 2 elements
 * 
 * @author JHelp
 * @param <ELEMENT1>
 *           First element type
 * @param <ELEMENT2>
 *           Second element type
 */
public class Pair<ELEMENT1, ELEMENT2>
{
   /** First element */
   public ELEMENT1 element1;
   /** Second element */
   public ELEMENT2 element2;

   /**
    * Create a new instance of Pair
    */
   public Pair()
   {
   }

   /**
    * Create a new instance of Pair
    * 
    * @param element1
    *           First element
    * @param element2
    *           Second element
    */
   public Pair(final ELEMENT1 element1, final ELEMENT2 element2)
   {
      this.element1 = element1;
      this.element2 = element2;
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
      return "Pair {" + this.element1 + ", " + this.element2 + "}";
   }
}