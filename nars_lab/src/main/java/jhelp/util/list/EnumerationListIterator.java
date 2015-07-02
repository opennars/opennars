package jhelp.util.list;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;

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
public class EnumerationListIterator<T>
      implements Enumeration<T>, ListIterator<T>, Iterable<T>
{
   /** Index to run over iterator or enumeration */
   private int                   actualIndex;

   /** Base array */
   private final T[]             array;

   /** Base iterator */
   private final ListIterator<T> iterator;

   /**
    * Constructs EnumerationIterator<br>
    * With array
    * 
    * @param startIndex
    *           Index where start the iteration
    * @param array
    *           Base array
    */
   public EnumerationListIterator(final int startIndex, final T... array)
   {
      this.array = array;
      this.actualIndex = startIndex;
      this.iterator = null;
   }

   /**
    * Constructs EnumerationIterator<br>
    * With iterator
    * 
    * @param iterator
    *           Base iterator
    */
   public EnumerationListIterator(final ListIterator<T> iterator)
   {
      this.iterator = iterator;
      this.actualIndex = 0;
      this.array = null;
   }

   /**
    * Create a new instance of EnumerationListIterator<br>
    * With list iterator
    * 
    * @param iterator
    *           Iterator source
    * @param startIndex
    *           Index where start read the list iterator
    */
   public EnumerationListIterator(final ListIterator<T> iterator, final int startIndex)
   {
      this.iterator = iterator;
      this.actualIndex = startIndex;
      this.array = null;
   }

   /**
    * Constructs EnumerationIterator<br>
    * With array
    * 
    * @param array
    *           Base array
    */
   public EnumerationListIterator(final T... array)
   {
      this.array = array;
      this.actualIndex = 0;
      this.iterator = null;
   }

   /**
    * Add an element <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param e
    *           Element to add
    * @see ListIterator#add(Object)
    */
   @Override
   public void add(final T e)
   {
   }

   /**
    * Next element
    * 
    * @return Next element
    */
   public T getNextElement()
   {
      if(this.iterator != null)
      {
         this.actualIndex++;
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
      if(this.iterator != null)
      {
         return this.iterator.hasNext();
      }

      return (this.array != null) && (this.actualIndex < this.array.length);
   }

   /**
    * Indicates if there are a previous element <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return {@code true} if there are a previous element
    * @see ListIterator#hasPrevious()
    */
   @Override
   public boolean hasPrevious()
   {
      if(this.iterator != null)
      {
         return this.iterator.hasPrevious();
      }

      if(this.array != null)
      {
         return this.actualIndex > 0;
      }

      return false;
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
    * Next index <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Next index
    * @see ListIterator#nextIndex()
    */
   @Override
   public int nextIndex()
   {
      return this.actualIndex;
   }

   /**
    * Previous element <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Previous element
    * @see ListIterator#previous()
    */
   @Override
   public T previous()
   {
      if(this.iterator != null)
      {
         this.actualIndex--;
         return this.iterator.previous();
      }

      if((this.array != null) && (this.actualIndex > 0))
      {
         return this.array[--this.actualIndex];
      }

      return null;
   }

   /**
    * Previous index <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Previous index
    * @see ListIterator#previousIndex()
    */
   @Override
   public int previousIndex()
   {
      return this.actualIndex - 1;
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

   /**
    * Does nothing <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param e
    *           ignore
    * @see ListIterator#set(Object)
    */
   @Override
   public void set(final T e)
   {
   }
}