package jhelp.util.list;

import java.lang.reflect.Array;

/**
 * List based on a fix array.<br>
 * If an element change from this list, it is also modified to the linked array.<br>
 * This ist have some restriction, you can't add or remove element from it. But its possible to read/modfy and element
 * 
 * @author JHelp
 * @param <TYPE>
 *           Type of the element in the array
 */
public class ListFromArray<TYPE>
      implements List<TYPE>
{
   /** Linked array */
   private final TYPE[]  array;
   /** Array length */
   private final int     length;
   /** Indicates if modify an element is allowed */
   private final boolean modifiable;

   /**
    * Create a new instance of ListFromArray
    * 
    * @param modifiable
    *           Indicates if modify an element is allowed
    * @param array
    *           Array to link to the list
    */
   public ListFromArray(final boolean modifiable, final TYPE... array)
   {
      this.modifiable = modifiable;
      this.array = array;
      this.length = array.length;
   }

   /**
    * Create a new instance of ListFromArray
    * 
    * @param array
    *           Array to link to the list
    */
   public ListFromArray(final TYPE... array)
   {
      this(true, array);
   }

   /**
    * Check if an index is inside the array
    * 
    * @param index
    *           Index to check
    */
   private void checkIndex(final int index)
   {
      if((index < 0) || (index >= this.length))
      {
         throw new IndexOutOfBoundsException("index must be in [0, " + this.length + "[ not " + index);
      }
   }

   /**
    * Not permitted operation. Always throw {@link UnsupportedOperationException} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param index
    *           Ignored
    * @param element
    *           Ignored
    * @see List#add(int, Object)
    */
   @Override
   public void add(final int index, final TYPE element)
   {
      throw new UnsupportedOperationException("Can't add element to an array");
   }

   /**
    * Not permitted operation. Always throw {@link UnsupportedOperationException} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param e
    *           Ignored
    * @return Never return
    * @see List#add(Object)
    */
   @Override
   public boolean add(final TYPE e)
   {
      throw new UnsupportedOperationException("Can't add element to an array");
   }

   /**
    * Not permitted operation. Always throw {@link UnsupportedOperationException} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param c
    *           Ignored
    * @return Never return
    * @see List#addAll(Collection)
    */
   @Override
   public boolean addAll(final Collection<? extends TYPE> c)
   {
      throw new UnsupportedOperationException("Can't add element to an array");
   }

   /**
    * Not permitted operation. Always throw {@link UnsupportedOperationException} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param index
    *           Ignored
    * @param c
    *           Ignored
    * @return Never return
    * @see List#addAll(int, Collection)
    */
   @Override
   public boolean addAll(final int index, final Collection<? extends TYPE> c)
   {
      throw new UnsupportedOperationException("Can't add element to an array");
   }

   /**
    * Not permitted operation. Always throw {@link UnsupportedOperationException} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @see List#clear()
    */
   @Override
   public void clear()
   {
      throw new UnsupportedOperationException("Can't clear elements of an array");
   }

   /**
    * Indicates if an element is inside the list <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param o
    *           Object test
    * @return {@code true} if element is inside
    * @see List#contains(Object)
    */
   @Override
   public boolean contains(final Object o)
   {
      for(final TYPE element : this.array)
      {
         if(element == null)
         {
            if(o == null)
            {
               return true;
            }
         }
         else if(element.equals(o) == true)
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Indicates if all elements of a collection is inside the list <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param c
    *           Collection of element to test
    * @return {@code true} if all elements are inside
    * @see List#containsAll(Collection)
    */
   @Override
   public boolean containsAll(final Collection<?> c)
   {
      for(final Object o : c)
      {
         if(this.contains(o) == false)
         {
            return false;
         }
      }

      return true;
   }

   /**
    * Obtain an element from the list <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param index
    *           Element index
    * @return Element get
    * @see List#get(int)
    */
   @Override
   public TYPE get(final int index)
   {
      this.checkIndex(index);

      return this.array[index];
   }

   /**
    * Compute index of an element <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param o
    *           Element search
    * @return Element index
    * @see List#indexOf(Object)
    */
   @Override
   public int indexOf(final Object o)
   {
      TYPE element;

      for(int i = 0; i < this.length; i++)
      {
         element = this.array[i];

         if(element == null)
         {
            if(o == null)
            {
               return i;
            }
         }
         else if(element.equals(o) == true)
         {
            return i;
         }
      }

      return -1;
   }

   /**
    * Indicates if list is empty <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return {@code true} if list is empty
    * @see List#isEmpty()
    */
   @Override
   public boolean isEmpty()
   {
      return this.length == 0;
   }

   /**
    * Indicates if it is allowed to modify elements inside the list throw {@link #set(int, Object)} method
    * 
    * @return {@code true} if modification are allowed
    */
   public boolean isModifiable()
   {
      return this.modifiable;
   }

   /**
    * Create iterator over the list <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Iterator
    * @see List#iterator()
    */
   @Override
   public Iterator<TYPE> iterator()
   {
      return new EnumerationIterator<TYPE>(this.array);
   }

   /**
    * Compute index, form the end, that corresponds to a specific object <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param o
    *           Object search
    * @return Last index of the object
    * @see List#lastIndexOf(Object)
    */
   @Override
   public int lastIndexOf(final Object o)
   {
      TYPE element;

      for(int i = this.length - 1; i >= 0; i--)
      {
         element = this.array[i];

         if(element == null)
         {
            if(o == null)
            {
               return i;
            }
         }
         else if(element.equals(o) == true)
         {
            return i;
         }
      }

      return -1;
   }

   /**
    * Create list iterator over the list <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return List iterator
    * @see List#listIterator()
    */
   @Override
   public ListIterator<TYPE> listIterator()
   {
      return new EnumerationListIterator<TYPE>(this.array);
   }

   /**
    * Create list iterator that start to a given index <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param index
    *           Index to start
    * @return List iterator
    * @see List#listIterator(int)
    */
   @Override
   public ListIterator<TYPE> listIterator(final int index)
   {
      this.checkIndex(index);

      return new EnumerationListIterator<TYPE>(index, this.array);
   }

   /**
    * Not permitted operation. Always throw {@link UnsupportedOperationException} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param index
    *           Ignored
    * @return Never return
    * @see List#remove(int)
    */
   @Override
   public TYPE remove(final int index)
   {
      throw new UnsupportedOperationException("Can't remove element to an array");
   }

   /**
    * Not permitted operation. Always throw {@link UnsupportedOperationException} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param o
    *           Ignored
    * @return Never return
    * @see List#remove(Object)
    */
   @Override
   public boolean remove(final Object o)
   {
      throw new UnsupportedOperationException("Can't remove element to an array");
   }

   /**
    * Not permitted operation. Always throw {@link UnsupportedOperationException} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param c
    *           Ignored
    * @return Never return
    * @see List#removeAll(Collection)
    */
   @Override
   public boolean removeAll(final Collection<?> c)
   {
      throw new UnsupportedOperationException("Can't remove element to an array");
   }

   /**
    * Not permitted operation. Always throw {@link UnsupportedOperationException} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param c
    *           Ignored
    * @return Never return
    * @see List#retainAll(Collection)
    */
   @Override
   public boolean retainAll(final Collection<?> c)
   {
      throw new UnsupportedOperationException("Can't remove element to an array");
   }

   /**
    * Modify a element of the list.<br>
    * It throws a {@link UnsupportedOperationException} if the list is not modifiable <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param index
    *           Index of element to change
    * @param element
    *           New value
    * @return Previous value
    * @see List#set(int, Object)
    */
   @Override
   public TYPE set(final int index, final TYPE element)
   {
      if(this.modifiable == false)
      {
         throw new UnsupportedOperationException("This list array is not modifiable");
      }

      this.checkIndex(index);

      final TYPE previous = this.array[index];
      this.array[index] = element;

      return previous;
   }

   /**
    * List size <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return List size
    * @see List#size()
    */
   @Override
   public int size()
   {
      return this.length;
   }

   /**
    * Extract a sub list from the list <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param fromIndex
    *           Start index
    * @param toIndex
    *           End index
    * @return Sub list
    * @see List#subList(int, int)
    */
   @Override
   public List<TYPE> subList(final int fromIndex, final int toIndex)
   {
      if((fromIndex < 0) || (toIndex > this.length) || (fromIndex > toIndex))
      {
         throw new IndexOutOfBoundsException("fromIndex=" + fromIndex + " toIndex=" + toIndex + " : One of following condition is not respected : fromIndex<=toIndex, fromIndex>=0, toIndex<=" + this.length);
      }

      if(fromIndex == toIndex)
      {
         return Collections.emptyList();
      }

      final int size = toIndex - fromIndex;
      @SuppressWarnings("unchecked")
      final TYPE[] subList = (TYPE[]) Array.newInstance(this.array.getClass().getComponentType(), size);
      System.arraycopy(this.array, fromIndex, subList, 0, size);

      return new ListFromArray<TYPE>(this.modifiable, subList);
   }

   /**
    * Convert list to array <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Array result (copy of linked array)
    * @see List#toArray()
    */
   @Override
   public Object[] toArray()
   {
      return Arrays.copyOf(this.array, this.length);
   }

   /**
    * Convert list to array <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param <T>
    *           Element type
    * @param a
    *           Array to receive result
    * @return Array copy
    * @see List#toArray(T[])
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> T[] toArray(T[] a)
   {
      if(a == null)
      {
         a = (T[]) (new Object[this.length]);
      }

      System.arraycopy(this.array, 0, a, 0, Math.min(this.length, a.length));

      return a;
   }
}