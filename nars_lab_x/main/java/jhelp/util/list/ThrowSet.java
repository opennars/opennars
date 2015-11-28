package jhelp.util.list;

import jhelp.util.text.UtilText;

import java.util.ArrayList;

/**
 * Set of elements, where elements have no order
 * 
 * @author JHelp
 * @param <T>
 *           Elements type
 */
public class ThrowSet<T>
{
   /** Elements list */
   private final ArrayList<T> set;

   /**
    * Create a new instance of ThrowSet
    */
   public ThrowSet()
   {
      this.set = new ArrayList<T>();
   }

   /**
    * Indicates if set is empty
    * 
    * @return {@code true} if set is empty
    */
   public boolean isEmpty()
   {
      return this.set.isEmpty();
   }

   /**
    * Set size
    * 
    * @return Number of elements
    */
   public int size()
   {
      return this.set.size();
   }

   /**
    * Take an element from the set.<br>
    * The element is removed from the set
    * 
    * @return An element
    */
   public T take()
   {
      final int size = this.set.size();

      if(size == 0)
      {
         throw new IllegalStateException("ThrowSet is empty");
      }

      final int index = (int) (Math.random() * size);

      final T element = this.set.get(index);
      this.set.remove(index);

      return element;
   }

   /**
    * Add an element to the set
    * 
    * @param element
    *           Element to add
    */
   public void throwElement(final T element)
   {
      final int size = this.set.size();

      if(size == 0)
      {
         this.set.add(element);

         return;
      }

      this.set.add((int) (Math.random() * size), element);
   }

   /**
    * Return the throw set to an array of element
    * 
    * @param array
    *           Array to store the result
    * @return Array of elements
    */
   public T[] toArray(final T[] array)
   {
      return this.set.toArray(array);
   }

   /**
    * String representation of the throw set <br>
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
      return UtilText.concatenate("ThrowSet:", this.set);
   }
}