package jhelp.util.list;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An enumeration can be see like iterator and iterator can be see like enumeration <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 * @param <T>
 *           Type of elements
 */
public class EnumerationIterator<T>
      implements Enumeration<T>, Iterator<T>, Iterable<T>
{
   /** Index to run over iterator or enumeration */
   private int            actualIndex;

   /** Base array */
   private T[]            array;

   /** Base enumeration */
   private Enumeration<T> enumeration;

   /** Base iterator */
   private Iterator<T>    iterator;

   /**
    * Constructs EnumerationIterator<br>
    * With an enumeration
    * 
    * @param enumeration
    *           Base enumeration
    */
   public EnumerationIterator(final Enumeration<T> enumeration)
   {
      this.enumeration = enumeration;
   }

   /**
    * Constructs EnumerationIterator<br>
    * With iterator
    * 
    * @param iterator
    *           Base iterator
    */
   public EnumerationIterator(final Iterator<T> iterator)
   {
      this.iterator = iterator;
   }

   /**
    * Constructs EnumerationIterator<br>
    * With array
    * 
    * @param array
    *           Base array
    */
   public EnumerationIterator(final T... array)
   {
      this.array = array;
      this.actualIndex = 0;
   }

   /**
    * Next element
    * 
    * @return Next element
    */
   public T getNextElement()
   {
      if(this.enumeration != null)
      {
         return this.enumeration.nextElement();
      }

      if(this.iterator != null)
      {
         return this.iterator.next();
      }

      if((this.array != null) && (this.actualIndex < this.array.length))
      {
         return this.array[this.actualIndex++];
      }

      return null;
   }

   /**
    * Indicates if there a next element
    * 
    * @return {@code true} if there a next element
    * @see Enumeration#hasMoreElements()
    */
   @Override
   public boolean hasMoreElements()
   {
      return this.hasNextElement();
   }

   /**
    * Indicates if there a next element
    * 
    * @return {@code true} if there a next element
    * @see Iterator#hasNext()
    */
   @Override
   public boolean hasNext()
   {
      return this.hasNextElement();
   }

   /**
    * Indicates if there a next element
    * 
    * @return {@code true} if there a next element
    */
   public boolean hasNextElement()
   {
      if(this.enumeration != null)
      {
         return this.enumeration.hasMoreElements();
      }

      if(this.iterator != null)
      {
         return this.iterator.hasNext();
      }

      return (this.array != null) && (this.actualIndex < this.array.length);
   }

   /**
    * Iterator
    * 
    * @return Iterator
    * @see Iterable#iterator()
    */
   @Override
   public Iterator<T> iterator()
   {
      return this;
   }

   /**
    * Next element
    * 
    * @return Next element
    * @see Iterator#next()
    */
   @Override
   public T next()
   {
      return this.getNextElement();
   }

   /**
    * Next element
    * 
    * @return Next element
    * @see Enumeration#nextElement()
    */
   @Override
   public T nextElement()
   {
      return this.getNextElement();
   }

   /**
    * Does nothing
    * 
    * @see Iterator#remove()
    */
   @Override
   public void remove()
   {
   }
}