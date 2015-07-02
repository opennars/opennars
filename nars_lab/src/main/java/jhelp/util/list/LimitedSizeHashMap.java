package jhelp.util.list;

/**
 * Limited map one number of element, if full some elements are removed to make room
 * 
 * @author JHelp
 * @param <KEY>
 *           Key type
 * @param <VALUE>
 *           Value type
 */
public interface LimitedSizeHashMap<KEY, VALUE>
{
   /**
    * Obtain an element of the map
    * 
    * @param key
    *           Element key
    * @return Element value
    */
   public VALUE get(KEY key);

   /**
    * Map maximum size
    * 
    * @return Map maximum size
    */
   public int getLimit();

   /**
    * Add/update an element
    * 
    * @param key
    *           Key
    * @param value
    *           Value
    */
   public void put(final KEY key, final VALUE value);

   /**
    * Remove an element of the map
    * 
    * @param key
    *           Key of element to remove
    */
   public void remove(final KEY key);

   /**
    * Number of elements
    * 
    * @return Number of elements
    */
   public int size();
}