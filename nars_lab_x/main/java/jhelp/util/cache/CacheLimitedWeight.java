package jhelp.util.cache;

import jhelp.util.list.HeavyObject;
import jhelp.util.list.HeavyObjectCreator;
import jhelp.util.list.LimitedWeightHashMap;
import jhelp.util.list.LimitedWeightHashMap.Result;

import java.util.HashMap;

/**
 * Cache with elements number is limited in memory
 * 
 * @author JHelp
 * @param <TYPE>
 *           Element type
 */
public class CacheLimitedWeight<TYPE extends HeavyObject>
{
   /** The cache */
   private final HashMap<String, HeavyObjectCreator<TYPE>> cache;
   /** Stored computed element (Limited in size) */
   private final LimitedWeightHashMap<String, TYPE>        limitedWeightMap;

   /**
    * Create a new instance of CacheLimitedWeight
    * 
    * @param limitedWeightMap
    *           Type of limitor to use
    */
   public CacheLimitedWeight(final LimitedWeightHashMap<String, TYPE> limitedWeightMap)
   {
      if(limitedWeightMap == null)
      {
         throw new NullPointerException("limitedWeightMap musn't be null");
      }

      this.cache = new HashMap<String, HeavyObjectCreator<TYPE>>();
      this.limitedWeightMap = limitedWeightMap;
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
      final TYPE heavy = this.limitedWeightMap.get(key);

      if(heavy != null)
      {
         return heavy;
      }

      final HeavyObjectCreator<TYPE> creator = this.cache.get(key);

      if(creator == null)
      {
         return null;
      }

      if(this.limitedWeightMap.put(key, creator, true) == Result.TOO_MUCH_HEAVY)
      {
         return null;
      }

      return this.limitedWeightMap.get(key);
   }

   /**
    * Get cache element, or create it if not exists
    * 
    * @param key
    *           Element key
    * @param creatorDefault
    *           Creator to use if element not already inside the cache
    * @return The asked element
    */
   public TYPE get(final String key, final HeavyObjectCreator<TYPE> creatorDefault)
   {
      final TYPE heavy = this.get(key);

      if(heavy != null)
      {
         return heavy;
      }

      if(this.put(key, creatorDefault) == false)
      {
         return null;
      }

      return this.get(key);
   }

   /**
    * Add/modify an element
    * 
    * @param key
    *           Key
    * @param creator
    *           Describe how create the element
    * @return {@code true} if put succed
    */
   public boolean put(final String key, final HeavyObjectCreator<TYPE> creator)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }

      if(creator == null)
      {
         throw new NullPointerException("creator musn't be null");
      }

      this.cache.put(key, creator);

      return this.limitedWeightMap.put(key, creator, true) != Result.TOO_MUCH_HEAVY;
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
      this.limitedWeightMap.remove(key);
   }
}