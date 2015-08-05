package jhelp.util.cache;

import jhelp.util.list.LimitedSizeHashMap;

import java.util.HashMap;

/**
 * Cache with elements number is limited in memory
 * 
 * @author JHelp
 * @param <TYPE>
 *           Element type
 */
public class CacheLimitedSize<TYPE>
{
   /** The cache */
   private final HashMap<String, CacheElement<TYPE>> cache;
   /** Stored computed element (Limited in size) */
   private final LimitedSizeHashMap<String, TYPE>    limitedSizeMap;

   /**
    * Create a new instance of CacheLimitedSize
    * 
    * @param limitedSizeMap
    *           Type of limitor to use
    */
   public CacheLimitedSize(final LimitedSizeHashMap<String, TYPE> limitedSizeMap)
   {
      if(limitedSizeMap == null)
      {
         throw new NullPointerException("limitedSizeMap musn't be null");
      }

      this.cache = new HashMap<String, CacheElement<TYPE>>();
      this.limitedSizeMap = limitedSizeMap;
   }

   /**
    * Get cache element
    * 
    * @param key
    *           Element key
    * @return Element value (May be recreated) or {@code null} if no element in the cache
    */
   public TYPE get(final String key)
   {
      TYPE value = this.limitedSizeMap.get(key);

      if(value == null)
      {
         final CacheElement<TYPE> elementCreator = this.cache.get(key);

         if(elementCreator == null)
         {
            return null;
         }

         value = elementCreator.getElement();
         this.limitedSizeMap.put(key, value);
      }

      return value;
   }

   /**
    * Get cache element, or create it if not exists
    * 
    * @param key
    *           Element key
    * @param elementCreatorDefault
    *           Creator to use if element not already inside the cache
    * @return The asked element
    */
   public TYPE get(final String key, final CacheElement<TYPE> elementCreatorDefault)
   {
      final TYPE value = this.get(key);

      if(value != null)
      {
         return value;
      }

      this.put(key, elementCreatorDefault);

      return this.get(key);
   }

   /**
    * Add/modify an element
    * 
    * @param key
    *           Key
    * @param elementCreator
    *           Describe how create the element
    */
   public void put(final String key, final CacheElement<TYPE> elementCreator)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }

      if(elementCreator == null)
      {
         throw new NullPointerException("elementCreator musn't be null");
      }

      this.cache.put(key, elementCreator);
      this.limitedSizeMap.put(key, elementCreator.getElement());
   }

   /**
    * Remove an element
    * 
    * @param key
    *           Key of element to remove
    */
   public void remove(final String key)
   {
      this.cache.remove(key);
      this.limitedSizeMap.remove(key);
   }
}