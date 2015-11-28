package jhelp.util.list;

/**
 * Queue of element
 * 
 * @author JHelp
 * @param <TYPE>
 *           Type of elements in the queue
 */
public class Queue<TYPE>
{
   /**
    * Element of the queue
    * 
    * @author JHelp
    * @param <ELEMENT>
    *           Type of carry element
    */
   static class Element<ELEMENT>
   {
      /** Element carry */
      public ELEMENT          element;
      /** Next element */
      public Element<ELEMENT> next;

      /**
       * Create a new instance of Element
       * 
       * @param element
       *           Element carry
       */
      public Element(final ELEMENT element)
      {
         this.element = element;
      }
   }

   /** Queue head (first element) */
   private Element<TYPE> head;
   /** Queue queue (last element) */
   private Element<TYPE> queue;

   /**
    * Create a new instance of empty Queue
    */
   public Queue()
   {
   }

   /**
    * Clear the queue
    */
   public void clear()
   {
      Element<TYPE> elt;
      while(this.head != null)
      {
         this.head.element = null;
         elt = this.head;

         this.head = this.head.next;
         elt.next = null;
      }

      this.queue = null;
   }

   /**
    * Add element at the end of the queue
    * 
    * @param element
    *           Element to add
    */
   public void inQueue(final TYPE element)
   {
      if(this.head == null)
      {
         this.head = this.queue = new Element<TYPE>(element);

         return;
      }

      this.queue.next = new Element<TYPE>(element);
      this.queue = this.queue.next;
   }

   /**
    * Indicates if queue is empty
    * 
    * @return {@code true} if queue is empty
    */
   public boolean isEmpty()
   {
      return this.head == null;
   }

   /**
    * Get the head of the queue and remove it
    * 
    * @return Head of the queue
    * @throws IllegalStateException
    *            If queue is empty
    */
   public TYPE outQueue()
   {
      if(this.head == null)
      {
         throw new IllegalStateException("The queue is empty");
      }

      final TYPE element = this.head.element;

      this.head = this.head.next;

      return element;
   }

   /**
    * Look the head of the queue but not remove
    * 
    * @return Head of the queue
    * @throws IllegalStateException
    *            If queue is empty
    */
   public TYPE peek()
   {
      if(this.head == null)
      {
         throw new IllegalStateException("The queue is empty");
      }

      return this.head.element;
   }

   /**
    * Remove a specific element of the queue
    * 
    * @param element
    *           Element to remove
    */
   public void remove(final TYPE element)
   {
      Element<TYPE> prev = null;
      Element<TYPE> elt = this.head;

      while(elt != null)
      {
         if((elt.element == null && element == null) || (elt.element != null && elt.element.equals(element) == true))
         {
            if(prev == null)
            {
               this.head = elt.next;

               return;
            }

            prev.next = elt.next;

            return;
         }

         prev = elt;
         elt = elt.next;
      }
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
      final StringBuilder stringBuilder = new StringBuilder("Ring[");

      if(this.head != null)
      {
         stringBuilder.append(this.head.element);

         Element<TYPE> element = this.head.next;
         while(element != null)
         {
            stringBuilder.append(", ");
            stringBuilder.append(element.element);

            element = element.next;
         }
      }

      stringBuilder.append(']');

      return stringBuilder.toString();
   }
}