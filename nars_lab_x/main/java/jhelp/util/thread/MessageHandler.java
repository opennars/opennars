package jhelp.util.thread;

import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handler of messages.<br>
 * It reacts to messages posted for him.<br>
 * Its thread safe, that is to say, if 2 threads post a message in "same time", the {@link #messageArrived(Object)} is called
 * for one of them, then AFTER the code execution its done, its called again.<br>
 * In other words, {@link #messageArrived(Object)} will never called in "same time". It also means, it waits that a message is
 * completely manage (exit from {@link #messageArrived(Object)}) before doing an other stuff<br>
 * You have to call {@link #terminate()} when you need no more the handler
 * 
 * @author JHelp
 * @param <MESSAGE>
 *           Message type
 */
public abstract class MessageHandler<MESSAGE>
{
   /**
    * A message
    * 
    * @author JHelp
    * @param <ELEMENT>
    *           Element type
    */
   private static class Message<ELEMENT>
         implements Comparable<Message<ELEMENT>>
   {
      /** Message content */
      ELEMENT element;
      /** Time when message should arrived */
      long    time;

      /**
       * Create a new instance of Message
       * 
       * @param time
       *           Time when message should arrived
       * @param element
       *           Message content
       */
      public Message(final long time, final ELEMENT element)
      {
         this.time = time;
         this.element = element;
      }

      /**
       * Compare with an other message, to know if this message must be receive before, after or in same time <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param message
       *           Message to compare with
       * @return -1 (Before), 1 (After) or 0 (Same time)
       * @see Comparable#compareTo(Object)
       */
      @Override
      public int compareTo(final Message<ELEMENT> message)
      {
         if(this.time < message.time)
         {
            return -1;
         }

         if(this.time > message.time)
         {
            return 1;
         }

         return 0;
      }

      /**
       * Indicates if an object is a message with same content <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param object
       *           Object to compare with
       * @return {@code true} if the object is a message with same content
       * @see Object#equals(Object)
       */
      @SuppressWarnings("unchecked")
      @Override
      public boolean equals(final Object object)
      {
         if((object == null) || (object.getClass().equals(this.getClass()) == false))
         {
            return false;
         }

         return this.element.equals(((Message<ELEMENT>) object).element);
      }
   }

   /** Default waiting time */
   private static final int                      DEFAULT_WAIT   = 16864;
   /** Indicates if handler still alive */
   private boolean                               alive;
   /** Internal thread */
   private Thread                                internalThread = new Thread()
                                                                {
                                                                   /**
                                                                    * Execute the internal thread <br>
                                                                    * <br>
                                                                    * <b>Parent documentation:</b><br>
                                                                    * {@inheritDoc}
                                                                    * 
                                                                    * @see Thread#run()
                                                                    */
                                                                   @Override
                                                                   public void run()
                                                                   {
                                                                      MessageHandler.this.doTheJob();
                                                                   }
                                                                };
   /** Indicates if lock is on or off */
   private final AtomicBoolean                   isLocked;
   /** Object for synchronization */
   private final Object                          LOCK           = new Object();
   /** Mutex for exclusive access */
   private final Mutex                           mutex;
   /** Queue of messages to give */
   private final PriorityQueue<Message<MESSAGE>> priorityQueue;

   /**
    * Create a new instance of MessageHandler
    */
   public MessageHandler()
   {
      this.priorityQueue = new PriorityQueue<Message<MESSAGE>>();

      this.mutex = new Mutex();
      this.isLocked = new AtomicBoolean(false);
      this.alive = true;
      this.internalThread.start();
   }

   /**
    * Manage the messages to delivered
    */
   final void doTheJob()
   {
      int wait;
      Message<MESSAGE> message = null;

      while(this.alive == true)
      {
         this.mutex.lock();

         if(this.priorityQueue.isEmpty() == true)
         {
            message = null;
         }
         else
         {
            message = this.priorityQueue.peek();
         }

         this.mutex.unlock();

         if(message == null)
         {
            wait = MessageHandler.DEFAULT_WAIT;
         }
         else
         {
            wait = (int) (message.time - System.currentTimeMillis());

            if(wait < 1)
            {
               this.messageArrived(message.element);

               this.priorityQueue.poll();

               wait = 1;
            }
         }

         synchronized(this.LOCK)
         {
            this.isLocked.set(true);

            try
            {
               this.LOCK.wait(wait);
            }
            catch(final Exception exception)
            {
            }

            this.isLocked.set(false);
         }
      }
   }

   /**
    * Try to free memory if user forget to call {@link #terminate()} and the object is garbage collected <br>
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
      this.terminate();

      super.finalize();
   }

   /**
    * Called when a message arrived.<br>
    * It is their that the user can do the dedicated job
    * 
    * @param message
    *           Message arrived
    */
   protected abstract void messageArrived(MESSAGE message);

   /**
    * Called just before handler will be destroyed, to do some safe stufs.<br>
    * Do nothing be default
    */
   protected void willBeTerminated()
   {
   }

   /**
    * Cancel a message.<br>
    * Usually used with message post with {@link #postDelayedMessage(Object, int)} to cancel it before it is called.<br>
    * If the message already delivered, this method does nothing<br>
    * Can be used for timeout for example
    * 
    * @param message
    *           Message to cancel
    */
   public final void cancelMessage(final MESSAGE message)
   {
      if(this.alive == false)
      {
         return;
      }

      if(message == null)
      {
         throw new NullPointerException("message musn't be null");
      }

      this.mutex.lock();

      this.priorityQueue.remove(new Message<MESSAGE>(0, message));

      this.mutex.unlock();
   }

   /**
    * Indicates if the handler still alive and can be used
    * 
    * @return {@code true} if the handler still alive and can be used
    */
   public final boolean isAlive()
   {
      return this.alive;
   }

   /**
    * Post a message delayed in a given time
    * 
    * @param message
    *           Message to delivered
    * @param millisecond
    *           Time in millisecond to wait before sending the message
    */
   public final void postDelayedMessage(final MESSAGE message, final int millisecond)
   {
      if(this.alive == false)
      {
         return;
      }

      if(message == null)
      {
         throw new NullPointerException("message musn't be null");
      }

      this.mutex.lock();

      this.priorityQueue.offer(new Message<MESSAGE>(System.currentTimeMillis() + Math.max(1, millisecond), message));

      this.mutex.unlock();

      synchronized(this.LOCK)
      {
         if(this.isLocked.get() == true)
         {
            this.LOCK.notify();
         }
      }
   }

   /**
    * Post a message and deliver it as soon as possible
    * 
    * @param message
    *           Message to post
    */
   public final void postMessage(final MESSAGE message)
   {
      if(this.alive == false)
      {
         return;
      }

      if(message == null)
      {
         throw new NullPointerException("message musn't be null");
      }

      this.postDelayedMessage(message, 1);
   }

   /**
    * Stop the handler and destroy it.<br>
    * It can't be used after the calling of this method.<br>
    * It is strongly recommend to call it as soon as the handler is not still necessary
    */
   public final void terminate()
   {
      if(this.alive == false)
      {
         return;
      }

      this.alive = false;
      this.willBeTerminated();

      synchronized(this.LOCK)
      {
         if(this.isLocked.get() == true)
         {
            this.LOCK.notify();
         }
      }

      this.internalThread = null;
   }
}