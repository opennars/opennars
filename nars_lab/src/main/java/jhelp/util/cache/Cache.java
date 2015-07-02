/**
 * Project : JHelpUtil<br>
 * Package : jhelp.util.cache<br>
 * Class : Cache<br>
 * Date : 13 avr. 2010<br>
 * By JHelp
 */
package jhelp.util.cache;

import java.util.HashMap;

/**
 * Manage a only RAM cache<br>
 * <br>
 * Last modification : 13 avr. 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 * @param <ELEMENT>
 *           Element type
 */
public class Cache<ELEMENT>
{
   /** The cache itself */
   private HashMap<String, CacheElement<ELEMENT>> cache;

   /**
    * Constructs Cache
    */
   public Cache()
   {
      this.cache = new HashMap<String, CacheElement<ELEMENT>>();
   }

   /**
    * Add element inside the cache
    * 
    * @param key
    *           Key associate
    * @param element
    *           Describe how create element
    */
   public void add(final String key, final CacheElement<ELEMENT> element)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }

      if(element == null)
      {
         throw new NullPointerException("element musn't be null");
      }

      this.cache.put(key, element);
   }

   /**
    * Clear the cache
    */
   public void clear()
   {
      CacheElement<ELEMENT> cacheElement;
      for(final String key : this.cache.keySet())
      {
         cacheElement = this.cache.get(key);
         if(cacheElement != null)
         {
            cacheElement.clear();
         }
      }

      this.cache.clear();
   }

   /**
    * Destroy the cache (Never use it after that)
    */
   public void destroy()
   {
      this.clear();

      this.cache = null;
   }

   /**
    * Obtain an element
    * 
    * @param key
    *           Element key
    * @return Element
    */
   public ELEMENT get(final String key)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }

      final CacheElement<ELEMENT> cacheElement = this.cache.get(key);
      if(cacheElement != null)
      {
         return cacheElement.getElement();
      }

      return null;
   }

   /**
    * Obtain an element and give a default value if key is not already present
    * 
    * @param key
    *           Key to get
    * @param cacheElement
    *           Cache element to store and use if the key is not already defined
    * @return The element
    */
   public ELEMENT get(final String key, final CacheElement<ELEMENT> cacheElement)
   {
      final ELEMENT element = this.get(key);

      if(element != null)
      {
         return element;
      }

      this.add(key, cacheElement);

      return this.get(key);
   }

   /**
    * Remove an element from cache
    * 
    * @param key
    *           Element key
    */
   public void remove(final String key)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }

      final CacheElement<ELEMENT> cacheElement = this.cache.get(key);
      if(cacheElement != null)
      {
         cacheElement.clear();
         this.cache.remove(key);
      }
   }
}