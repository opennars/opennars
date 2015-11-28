package jhelp.util.list;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.reflection.Reflector;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Array of sorted element.<br>
 * The sort is "natural order" (Only possible for {@link Comparable} elements) or as indicates by a {@link Comparator}.<br>
 * The sort can be unique, that is to say if try to add an element and exists an other inside array that compare equals, in
 * unique mode, the element is not add.<br>
 * The adding, remove are in O(n * LN(n)). The search is in O(LN(n)).
 * 
 * @author JHelp
 * @param <TYPE>
 *           Type of stored elements
 */
public final class SortedArray<TYPE>
      implements Iterable<TYPE>
{
   /**
    * Comparator for "natural order" of {@link Comparable}
    * 
    * @author JHelp
    * @param <ELEMENT>
    *           Element type
    */
   static class ComparatorNatural<ELEMENT>
         implements Comparator<ELEMENT>
   {
      /**
       * Compare 2 elements in "natural order" of {@link Comparable}.<br>
       * It is just the result of {@link Comparable#compareTo(Object) element1.compareTo(element2)} <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param element1
       *           First element to compare
       * @param element2
       *           Second element to compare
       * @return Comparison result
       * @see Comparator#compare(Object, Object)
       */
      @SuppressWarnings("unchecked")
      @Override
      public int compare(final ELEMENT element1, final ELEMENT element2)
      {
         return ((Comparable<ELEMENT>) element1).compareTo(element2);
      }
   }

   /** Stored elements */
   private TYPE[]      array;

   /** Comparator to use */
   @SuppressWarnings("rawtypes")
   private Comparator  comparator;
   /** Actual array size */
   private int         size;
   /** Type of element stored */
   private Class<TYPE> typeClass;
   /** Indicates if we are in unique mode */
   private boolean     unique;

   /**
    * Create a new instance of SortedArray in not unique mode.<br>
    * The type of class must be a {@link Comparable} to use the "natural order"
    * 
    * @param typeClass
    *           Type of element to store
    */
   public SortedArray(final Class<TYPE> typeClass)
   {
      this(typeClass, null, 128, false);
   }

   /**
    * Create a new instance of SortedArray.<br>
    * The type of class must be a {@link Comparable} to use the "natural order"
    * 
    * @param typeClass
    *           Type of element to store
    * @param unique
    *           Indicates if use unique mode or not
    */
   public SortedArray(final Class<TYPE> typeClass, final boolean unique)
   {
      this(typeClass, null, 128, unique);
   }

   /**
    * Create a new instance of SortedArray in not unique mode.<br>
    * If the comparator is {@code null}, the type of class must be a {@link Comparable} to use the "natural order"
    * 
    * @param typeClass
    *           Type of elements
    * @param comparator
    *           Comparator to use
    */
   public SortedArray(final Class<TYPE> typeClass, final Comparator<TYPE> comparator)
   {
      this(typeClass, comparator, 128, false);
   }

   /**
    * Create a new instance of SortedArray.<br>
    * If the comparator is {@code null}, the type of class must be a {@link Comparable} to use the "natural order"
    * 
    * @param typeClass
    *           Type of element
    * @param comparator
    *           Comparator to use
    * @param unique
    *           Indicates if the unique mode is activated
    */
   public SortedArray(final Class<TYPE> typeClass, final Comparator<TYPE> comparator, final boolean unique)
   {
      this(typeClass, comparator, 128, unique);
   }

   /**
    * Create a new instance of SortedArray.<br>
    * If the comparator is {@code null}, the type of class must be a {@link Comparable} to use the "natural order"
    * 
    * @param typeClass
    *           Type of element
    * @param comparator
    *           Comparator to use
    * @param initialSize
    *           Initial array capacity
    * @param unique
    *           Indicates if the unique mode is enable
    */
   @SuppressWarnings("unchecked")
   public SortedArray(final Class<TYPE> typeClass, final Comparator<TYPE> comparator, final int initialSize, final boolean unique)
   {
      if(typeClass == null)
      {
         throw new NullPointerException("typeClass musn't be null");
      }

      this.comparator = comparator;

      if(comparator == null)
      {
         if(Reflector.isSubTypeOf(typeClass, Comparable.class) == true)
         {
            this.comparator = new ComparatorNatural<TYPE>();
         }
         else
         {
            throw new IllegalArgumentException("comparator is null and the type class " + typeClass.getName() + " is not comparable");
         }
      }

      this.typeClass = typeClass;
      this.array = (TYPE[]) Array.newInstance(typeClass, Math.max(128, initialSize));

      this.unique = unique;
      this.size = 0;
   }

   /**
    * Check if an index is out of the array
    * 
    * @param index
    *           Index to set
    * @throws IllegalArgumentException
    *            If the index is out of bounds
    */
   private void checkIndex(final int index)
   {
      if((index < 0) || (index >= this.size))
      {
         throw new IllegalArgumentException("index must be in [0, " + this.size + "[ not " + index);
      }
   }

   /**
    * Expand the array (if need) to have more space
    * 
    * @param more
    *           Number space more need
    */
   @SuppressWarnings("unchecked")
   private void expand(final int more)
   {
      if((this.size + more) >= this.array.length)
      {
         int newSize = this.size + more;
         newSize += (newSize / 10) + 1;

         final TYPE[] temp = (TYPE[]) Array.newInstance(this.typeClass, Math.max(128, newSize));
         System.arraycopy(this.array, 0, temp, 0, this.size);

         this.array = temp;
      }
   }

   /**
    * Compute the index where insert an element.<br>
    * The result may be negative. It happen if the element already present and we are in unique mode. We can have the index by
    * using the formula : <br>
    * {@code index = insertIn(element); if(index<0) index = -index-1; }
    * 
    * @param element
    *           Element search
    * @return Index of insertion
    */
   @SuppressWarnings("unchecked")
   private int insertIn(final TYPE element)
   {
      if(this.size == 0)
      {
         return 0;
      }

      int min = 0;
      int comp = this.comparator.compare(element, this.array[0]);

      if(comp < 0)
      {
         return 0;
      }

      if(comp == 0)
      {
         if(this.unique == true)
         {
            return -1;
         }

         return 0;
      }

      int max = this.size - 1;
      comp = this.comparator.compare(element, this.array[max]);

      if(comp > 0)
      {
         return this.size;
      }

      if(comp == 0)
      {
         if(this.unique == true)
         {
            return -1 - max;
         }

         return max;
      }

      int mil;

      while(min < (max - 1))
      {
         mil = (max + min) >> 1;

         comp = this.comparator.compare(element, this.array[mil]);

         if(comp == 0)
         {
            if(this.unique == true)
            {
               return -1 - mil;
            }

            return mil;
         }

         if(comp > 0)
         {
            min = mil;
         }
         else
         {
            max = mil;
         }
      }

      return max;
   }

   /**
    * Called when garbage collector want free is memory <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @throws Throwable
    *            On issue
    * @see Object#finalize()
    */
   @Override
   protected void finalize() throws Throwable
   {
      if(this.array != null)
      {
         for(int i = this.array.length - 1; i >= 0; i--)
         {
            this.array[i] = null;
         }
      }
      this.array = null;

      this.comparator = null;
      this.typeClass = null;

      this.size = 0;

      Debug.println(DebugLevel.VERBOSE, "Delete from memory");

      super.finalize();
   }

   /**
    * Add an element
    * 
    * @param element
    *           element to add
    * @return {@code true} if element added. {@code false} if unique mode and already present, so not added
    * @throws NullPointerException
    *            if the element is {@code null}
    */
   public boolean add(final TYPE element)
   {
      if(element == null)
      {
         throw new NullPointerException("element musn't be null");
      }

      this.expand(1);

      final int index = this.insertIn(element);

      if(index >= 0)
      {
         if(index < this.size)
         {
            System.arraycopy(this.array, index, this.array, index + 1, this.size - index);
         }

         this.array[index] = element;

         this.size++;

         return true;
      }

      return false;
   }

   /**
    * Clear the array.<br>
    * It will be empty after that
    */
   public void clear()
   {
      for(int i = this.array.length - 1; i >= 0; i--)
      {
         this.array[i] = null;
      }

      this.size = 0;
   }

   /**
    * Indicates if an element is inside the array
    * 
    * @param element
    *           Tested element
    * @return {@code true} if an element is inside the array
    * @throws NullPointerException
    *            if the element is {@code null}
    */
   public boolean contains(final TYPE element)
   {
      if(element == null)
      {
         throw new NullPointerException("element musn't be null");
      }

      return this.indexOf(element) >= 0;
   }

   /**
    * Give an element of the array
    * 
    * @param index
    *           Element index
    * @return Element
    * @throws IllegalArgumentException
    *            if the index is out of bounds
    */
   public TYPE getElement(final int index)
   {
      this.checkIndex(index);

      return this.array[index];
   }

   /**
    * Actual array size
    * 
    * @return Actual array size
    */
   public int getSize()
   {
      return this.size;
   }

   /**
    * Get an element index or -1 if not present
    * 
    * @param element
    *           Element tested
    * @return Element index or -1 if not present
    * @throws NullPointerException
    *            if element is {@code null}
    */
   @SuppressWarnings("unchecked")
   public int indexOf(final TYPE element)
   {
      if(element == null)
      {
         throw new NullPointerException("element musn't be null");
      }

      int index = this.insertIn(element);

      if(index < 0)
      {
         index = -index - 1;
      }

      if(index >= this.size)
      {
         return -1;
      }

      if(this.comparator.compare(element, this.array[index]) == 0)
      {
         return index;
      }

      return -1;
   }

   /**
    * Compute interval index where should be insert a given element.<br>
    * The couple <b>(min, max)</b> returned can be interpreted like that (where <i>size</i> is the size of the list) :
    * <ul>
    * <li><b>(-1, 0)</b> means that the element is before the first element of the list</li>
    * <li><b>(size, -1)</b> means that the element is after the last element of the list</li>
    * <li><b>(index, index)</b> in other word <b>min==max</b>, means that the element is at exactly the index <b>min</b></li>
    * <li><b>Other case (min ,max) min &lt; max </b> means that the element is after the element at <b>min</b> index and before
    * the element at <b>max</b> index</li>
    * </ul>
    * 
    * @param element
    *           Element search
    * @return Couple (min, max)
    */
   @SuppressWarnings("unchecked")
   public Pair<Integer, Integer> intervalOf(final TYPE element)
   {
      if(element == null)
      {
         throw new NullPointerException("element musn't be null");
      }

      int index = this.insertIn(element);

      if(index < 0)
      {
         index = -index - 1;
      }

      if(index >= this.size)
      {
         return new Pair<Integer, Integer>(this.size, -1);
      }

      if(this.comparator.compare(element, this.array[index]) == 0)
      {
         return new Pair<Integer, Integer>(index, index);
      }

      return new Pair<Integer, Integer>(index - 1, index);
   }

   /**
    * Indicates if array is empty
    * 
    * @return {@code true} if array is empty
    */
   public boolean isEmpty()
   {
      return this.size == 0;
   }

   /**
    * Indicates if array is in unique mode
    * 
    * @return {@code true} if array is in unique mode
    */
   public boolean isUnique()
   {
      return this.unique;
   }

   /**
    * Compute iterator over the list <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Iterator
    * @see Iterable#iterator()
    */
   @Override
   public Iterator<TYPE> iterator()
   {
      return new EnumerationIterator<TYPE>(this.toArray());
   }

   /**
    * Search an element similar to the object. Because it use the comparator, is the comparator say the object is the same of
    * one element inside the array, they are declared similar
    * 
    * @param object
    *           Object to be similar
    * @return Element similar to the object, or {@code null} if no element are similar
    */
   @SuppressWarnings("unchecked")
   public TYPE obtainElement(final Object object)
   {
      for(int index = 0; index < this.size; index++)
      {
         if(this.comparator.compare(this.array[index], object) == 0)
         {
            return this.array[index];
         }
      }

      return null;
   }

   /**
    * Search index of similar element. Because it use the comparator, is the comparator say the object is the same of one
    * element inside the array, they are declared similar
    * 
    * @param object
    *           Object to be similar
    * @return Index of similar element, or -1 if not found
    */
   @SuppressWarnings("unchecked")
   public int obtainIndex(final Object object)
   {
      for(int index = 0; index < this.size; index++)
      {
         if(this.comparator.compare(this.array[index], object) == 0)
         {
            return index;
         }
      }

      return -1;
   }

   /**
    * Remove an element
    * 
    * @param index
    *           Element index
    * @return The removed element
    * @throws IllegalArgumentException
    *            if index is out of bounds
    */
   public TYPE remove(final int index)
   {
      this.checkIndex(index);

      final TYPE element = this.array[index];

      System.arraycopy(this.array, index + 1, this.array, index, this.size - index - 1);
      this.size--;

      this.array[this.size] = null;

      return element;
   }

   /**
    * Remove an element.<br>
    * It removes an element equals to the given element, not necessary the element itself
    * 
    * @param element
    *           Element to remove
    * @return Real element removed or {@code null} if element not found
    * @throws NullPointerException
    *            if element is {@code null}
    */
   public TYPE remove(final TYPE element)
   {
      if(element == null)
      {
         throw new NullPointerException("element musn't be null");
      }

      final int index = this.indexOf(element);

      if(index < 0)
      {
         return null;
      }

      return this.remove(index);
   }

   /**
    * Seek first element that match a test
    * 
    * @param seekTest
    *           Seek test
    * @return First matches element or {@code null} if none match
    */
   public TYPE seekElement(final SeekTest<TYPE> seekTest)
   {
      for(int index = 0; index < this.size; index++)
      {
         if(seekTest.isElementSeek(this.array[index]) == true)
         {
            return this.array[index];
         }
      }

      return null;
   }

   /**
    * Collect all matches element is a sorted array list of same class element and unque value
    * 
    * @param seekTest
    *           Test of seek elements
    * @return List of all matches elements
    */
   public SortedArray<TYPE> seekElements(final SeekTest<TYPE> seekTest)
   {
      final SortedArray<TYPE> sortedArray = new SortedArray<TYPE>(this.typeClass, this.unique);

      for(int index = 0; index < this.size; index++)
      {
         if(seekTest.isElementSeek(this.array[index]) == true)
         {
            sortedArray.add(this.array[index]);
         }
      }

      return sortedArray;
   }

   /**
    * Transform the array to a an array with size the same as this array, with same element in same order
    * 
    * @return Extracted array
    */
   @SuppressWarnings("unchecked")
   public TYPE[] toArray()
   {
      final TYPE[] array = (TYPE[]) Array.newInstance(this.typeClass, this.size);

      System.arraycopy(this.array, 0, array, 0, this.size);

      return array;
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
      final StringBuilder stringBuilder = new StringBuilder("[");

      if(this.size > 0)
      {
         stringBuilder.append(this.array[0]);

         for(int i = 1; i < this.size; i++)
         {
            stringBuilder.append(", ");
            stringBuilder.append(this.array[i]);
         }
      }

      stringBuilder.append(']');

      return stringBuilder.toString();
   }
}