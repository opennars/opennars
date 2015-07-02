/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.util<br>
 * Class : Queue<br>
 * Date : 4 sept. 2008<br>
 * By JHelp
 */
package jhelp.util.list;

/**
 * A queue <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 * @param <Element>
 *           Element type
 */
public class QueueSynchronized<Element>
{
   /**
    * Link between tow elements in the queue <br>
    * <br>
    * Last modification : 25 janv. 2009<br>
    * Version 0.0.1<br>
    * 
    * @author JHelp
    * @param <Elt>
    *           Element type
    */
   static class Link<Elt>
   {
      /** Element carry */
      public Elt       element;
      /** Next link */
      public Link<Elt> next;
   }

   /** Head link */
   private Link<Element> head;
   /** Queue size */
   private int           size;
   /** Tail link */
   private Link<Element> tail;

   /**
    * Constructs Queue
    */
   public QueueSynchronized()
   {
      this.head = this.tail = null;
      this.size = 0;
   }

   /**
    * In queue an element
    * 
    * @param element
    *           Element to in queue
    */
   public synchronized void inQueue(final Element element)
   {
      if(element == null)
      {
         throw new NullPointerException("The element musn't be null !");
      }

      if(this.head == null)
      {
         this.head = new Link<Element>();
         this.head.element = element;

         this.tail = this.head;

         this.size = 1;

         return;
      }

      this.tail.next = new Link<Element>();
      this.tail = this.tail.next;
      this.tail.element = element;

      this.size++;
   }

   /**
    * Indicates if the queue is empty
    * 
    * @return {@code true} if the queue is empty
    */
   public synchronized boolean isEmpty()
   {
      return this.head == null;
   }

   /**
    * Look the next element in the queue
    * 
    * @return Element look
    */
   public synchronized Element lookQueue()
   {
      if(this.head == null)
      {
         throw new IllegalStateException("The queue is empty !");
      }

      return this.head.element;
   }

   /**
    * Out queue element
    * 
    * @return Element out queue
    */
   public synchronized Element outQueue()
   {
      Element element;

      if(this.head == null)
      {
         throw new IllegalStateException("The queue is empty !");
      }

      element = this.head.element;
      this.head = this.head.next;
      if(this.head == null)
      {
         this.tail = null;
      }

      this.size--;

      return element;
   }

   /**
    * Queue size
    * 
    * @return Queue size
    */
   public synchronized int size()
   {
      return this.size;
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
      StringBuffer stringBuffer;
      Link<Element> link;

      stringBuffer = new StringBuffer("Queue : [");

      link = this.head;
      while(link != null)
      {
         stringBuffer.append(link.element);

         link = link.next;

         if(link != null)
         {
            stringBuffer.append(" | ");
         }
      }

      stringBuffer.append(']');

      return stringBuffer.toString();
   }
}