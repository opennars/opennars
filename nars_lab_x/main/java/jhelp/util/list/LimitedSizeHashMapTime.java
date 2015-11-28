package jhelp.util.list;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Limited map one number of element, if full some elements are removed to make room
 * 
 * @author JHelp
 * @param <KEY>
 *           Key type
 * @param <VALUE>
 *           Value type
 */
public class LimitedSizeHashMapTime<KEY, VALUE>
      implements LimitedSizeHashMap<KEY, VALUE>
{
   /**
    * Element description
    * 
    * @author JHelp
    */
   class Element
   {
      /** Last usage time */
      long  time;
      /** Element value */
      VALUE value;

      /**
       * Create a new instance of Element
       * 
       * @param value
       *           Associated value
       */
      Element(final VALUE value)
      {
         this.time = System.currentTimeMillis();
         this.value = value;
      }
   }

   /** Hash map */
   private final HashMap<KEY, Element> hashMap;
   /** Size limit */
   private final int                   limit;

   /**
    * Create a new instance of LimitedSizeHashMapTime
    * 
    * @param limit
    *           Size limit
    */
   public LimitedSizeHashMapTime(final int limit)
   {
      this.hashMap = new HashMap<KEY, Element>();
      this.limit = Math.max(limit, 128);
   }

   /**
    * Obtain an element of the map <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param key
    *           Element key
    * @return Element value
    * @see LimitedSizeHashMap#get(Object)
    */
   @Override
   public synchronized VALUE get(final KEY key)
   {
      final Element element = this.hashMap.get(key);

      if(element == null)
      {
         return null;
      }

      element.time = System.currentTimeMillis();
      return element.value;
   }

   /**
    * Map maximum size <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Map maximum size
    * @see LimitedSizeHashMap#getLimit()
    */
   @Override
   public int getLimit()
   {
      return this.limit;
   }

   /**
    * Add/update an element <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param key
    *           Key
    * @param value
    *           Value
    * @see LimitedSizeHashMap#put(Object, Object)
    */
   @Override
   public synchronized void put(final KEY key, final VALUE value)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      final Element element = this.hashMap.get(key);

      if(element != null)
      {
         element.time = System.currentTimeMillis();
         element.value = value;

         return;
      }

      final int size = this.hashMap.size();

      if(size >= this.limit)
      {
         long min = Long.MAX_VALUE;
         KEY k = null;

         for(final Entry<KEY, Element> entry : this.hashMap.entrySet())
         {
            if(entry.getValue().time < min)
            {
               k = entry.getKey();
               min = entry.getValue().time;
            }
         }

         this.hashMap.remove(k);
         // Debug.println(DebugLevel.VERBOSE, "Removed key = ", k);
      }

      this.hashMap.put(key, new Element(value));
   }

   /**
    * Remove an element of the map <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param key
    *           Key of element to remove
    * @see LimitedSizeHashMap#remove(Object)
    */
   @Override
   public synchronized void remove(final KEY key)
   {
      this.hashMap.remove(key);
   }

   /**
    * Number of elements <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Number of elements
    * @see LimitedSizeHashMap#size()
    */
   @Override
   public synchronized int size()
   {
      return this.hashMap.size();
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
      return this.hashMap.toString();
   }
}