/**
 * Project : JHelpUtil<br>
 * Package : jhelp.util.cache<br>
 * Class : CacheElement<br>
 * Date : 13 avr. 2010<br>
 * By JHelp
 */
package jhelp.util.cache;

import java.lang.ref.SoftReference;

/**
 * Element of a {@link Cache}<br>
 * It describes how create an element <br>
 * <br>
 * Last modification : 13 avr. 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 * @param <ELEMENT>
 *           Element type
 */
public abstract class CacheElement<ELEMENT>
{
   /** Hide reference */
   private SoftReference<ELEMENT> softReference;

   /**
    * Constructs CacheElement
    */
   public CacheElement()
   {
   }

   /**
    * Create the element
    * 
    * @return Created element
    */
   protected abstract ELEMENT createElement();

   /**
    * Remove the element
    */
   public final void clear()
   {
      if(this.softReference != null)
      {
         this.softReference.clear();
      }

      this.softReference = null;
   }

   /**
    * Obtain the element
    * 
    * @return The element
    */
   public final ELEMENT getElement()
   {
      ELEMENT element = null;

      // Check if element is already store
      if(this.softReference != null)
      {
         element = this.softReference.get();
      }

      if(element != null)
      {
         // If already store, return it
         return element;
      }

      // Create element and store it
      element = this.createElement();
      this.softReference = new SoftReference<ELEMENT>(element);

      return element;
   }
}