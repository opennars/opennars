/**
 * Project : JHelpUtil<br>
 * Package : jhelp.util.list<br>
 * Class : QueuePriority<br>
 * Date : 8 mai 2010<br>
 * By JHelp
 */
package jhelp.util.list;

import jhelp.util.text.UtilText;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Queue with priority<br>
 * You decide the priority just at the moment of add, the information of priority not link to the element instance, so you can
 * add the same instance two times with a different priority<br>
 * You needn't that element is {@link Comparable}<br>
 * <br>
 * Last modification : 8 mai 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 * @param <T>
 *           Elements type
 */
public class QueuePriority<T>
{
   /**
    * Comparator of {@link Element} <br>
    * <br>
    * Last modification : 19 juin 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    * @param <T>
    *           Element type
    */
   static class CompatorElement<T>
         implements Comparator<Element<T>>
   {
      /** Increment order multiplier */
      int increment;

      /**
       * Constructs CompatorElement
       * 
       * @param increment
       *           Indicates if priority are take in increment order
       */
      CompatorElement(final boolean increment)
      {
         if(increment)
         {
            this.increment = 1;
         }
         else
         {
            this.increment = -1;
         }
      }

      /**
       * Compare two elements
       * 
       * @param o1
       *           First element
       * @param o2
       *           Second element
       * @return Compare result
       * @see Comparator#compare(Object, Object)
       */
      @Override
      public int compare(final Element<T> o1, final Element<T> o2)
      {
         return this.increment * (o1.priority - o2.priority);
      }
   }

   /**
    * Element of the queue <br>
    * <br>
    * Last modification : 19 juin 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    * @param <T>
    *           Element type
    */
   static class Element<T>
   {
      /** Element itself */
      T   element;
      /** Priority link to the element */
      int priority;

      /**
       * Constructs Element
       * 
       * @param priority
       *           Priority link to the element
       * @param element
       *           Element itself
       */
      Element(final int priority, final T element)
      {
         this.priority = priority;
         this.element = element;
      }

      /**
       * String representation
       * 
       * @return String representation
       * @see Object#toString()
       */
      @Override
      public String toString()
      {
         return UtilText.concatenate('[', this.priority, "] : ", this.element);
      }
   }

   /** Priority queue */
   private final PriorityQueue<Element<T>> queue;

   /**
    * Constructs QueuePriority
    * 
    * @param increment
    *           Indicates if priority are take in increment order
    */
   public QueuePriority(final boolean increment)
   {
      this.queue = new PriorityQueue<Element<T>>(123, new CompatorElement<T>(increment));
   }

   /**
    * Dequeue an element from queue
    * 
    * @return Element dequeued
    */
   public T dequeue()
   {
      final Element<T> element = this.queue.poll();
      if(element == null)
      {
         return null;
      }

      return element.element;
   }

   /**
    * Enqueue an element
    * 
    * @param priority
    *           Priority
    * @param element
    *           Element
    */
   public void enqueue(final int priority, final T element)
   {
      if(element == null)
      {
         throw new NullPointerException("element musn't be null");
      }

      this.queue.add(new Element<T>(priority, element));
   }

   /**
    * Indicates if queue is empty
    * 
    * @return {@code true} if queue is empty
    */
   public boolean isEmpty()
   {
      return this.queue.isEmpty();
   }

   /**
    * Queue size
    * 
    * @return Queue size
    */
   public int size()
   {
      return this.queue.size();
   }

   /**
    * String representation
    * 
    * @return String representation
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      return this.queue.toString();
   }
}