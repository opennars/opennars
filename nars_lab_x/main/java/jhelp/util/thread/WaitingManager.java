package jhelp.util.thread;

import jhelp.util.list.Pair;
import jhelp.util.list.Queue;

import java.util.HashMap;

/**
 * Manager for make waiting task for an specific event before play
 * 
 * @author JHelp
 */
public class WaitingManager
{
   /** For synchronize */
   private static final Mutex                                           MUTEX   = new Mutex();
   /** Waiter manager singleton instance */
   public static final WaitingManager                                   MANAGER = new WaitingManager();
   /** Threaded task that are waiting */
   private final HashMap<String, Queue<Pair<ThreadedTask<?, ?, ?>, ?>>> waiters;

   /**
    * Create a new instance of WaitingManager
    */
   private WaitingManager()
   {
      this.waiters = new HashMap<String, Queue<Pair<ThreadedTask<?, ?, ?>, ?>>>();
   }

   /**
    * Register a task to be play on specific event
    * 
    * @param <PARAMETER>
    *           Task parameter type
    * @param <RESULT>
    *           Task result type
    * @param <PROGRESS>
    *           Task progress type
    * @param threadedTask
    *           Task that wait
    * @param parameter
    *           Parameter to give to task when its time to play comes
    * @param waitingFor
    *           Waiting event
    */
   public <PARAMETER, RESULT, PROGRESS> void register(final ThreadedTask<PARAMETER, RESULT, PROGRESS> threadedTask, final PARAMETER parameter, final String waitingFor)
   {
      if(threadedTask == null)
      {
         throw new NullPointerException("threadedTask musn't be null");
      }

      if(waitingFor == null)
      {
         throw new NullPointerException("waitingFor musn't be null");
      }

      WaitingManager.MUTEX.lock();

      try
      {
         Queue<Pair<ThreadedTask<?, ?, ?>, ?>> queue = this.waiters.get(waitingFor);

         if(queue == null)
         {
            queue = new Queue<Pair<ThreadedTask<?, ?, ?>, ?>>();

            this.waiters.put(waitingFor, queue);
         }

         queue.inQueue(new Pair<ThreadedTask<?, ?, ?>, PARAMETER>(threadedTask, parameter));
      }
      finally
      {
         WaitingManager.MUTEX.unlock();
      }
   }

   /**
    * Wake up all task that are waiting for a specific event
    * 
    * @param <PARAMETER>
    *           Task parameter type
    * @param <RESULT>
    *           Task result type
    * @param <PROGRESS>
    *           Task progress type
    * @param waitingFor
    *           Event to wake up
    */
   @SuppressWarnings("unchecked")
   public <PARAMETER, RESULT, PROGRESS> void wakeupAll(final String waitingFor)
   {
      if(waitingFor == null)
      {
         throw new NullPointerException("waitingFor musn't be null");
      }

      WaitingManager.MUTEX.lock();

      try
      {
         final Queue<Pair<ThreadedTask<?, ?, ?>, ?>> queue = this.waiters.get(waitingFor);

         if(queue != null)
         {
            while(queue.isEmpty() == false)
            {
               final Pair<ThreadedTask<?, ?, ?>, ?> pair = queue.outQueue();

               ThreadManager.THREAD_MANAGER.doThread((ThreadedTask<PARAMETER, RESULT, PROGRESS>) pair.element1, (PARAMETER) pair.element2);
            }

            this.waiters.remove(waitingFor);
         }
      }
      finally
      {
         WaitingManager.MUTEX.unlock();
      }
   }

   /**
    * Wake up the head of waiting queue for an event
    * 
    * @param <PARAMETER>
    *           Task parameter type
    * @param <RESULT>
    *           Task result type
    * @param <PROGRESS>
    *           Task progress type
    * @param waitingFor
    *           Event to wake up
    */
   @SuppressWarnings("unchecked")
   public <PARAMETER, RESULT, PROGRESS> void wakeupNext(final String waitingFor)
   {
      if(waitingFor == null)
      {
         throw new NullPointerException("waitingFor musn't be null");
      }

      WaitingManager.MUTEX.lock();

      try
      {
         final Queue<Pair<ThreadedTask<?, ?, ?>, ?>> queue = this.waiters.get(waitingFor);

         if((queue != null) && (queue.isEmpty() == false))
         {
            final Pair<ThreadedTask<?, ?, ?>, ?> pair = queue.outQueue();

            ThreadManager.THREAD_MANAGER.doThread((ThreadedTask<PARAMETER, RESULT, PROGRESS>) pair.element1, (PARAMETER) pair.element2);

            if(queue.isEmpty() == true)
            {
               this.waiters.remove(waitingFor);
            }
         }
      }
      finally
      {
         WaitingManager.MUTEX.unlock();
      }
   }
}