package jhelp.util.thread;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;

import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Thread manager.<br>
 * It manage a number of maximum thread in same time, it recycles threads that have finished there task to do next task.<br>
 * It is better than created a new {@link Thread} because here the number of thread is control.<br>
 * If a task as to do now and no thread is free, the task wait for the first free thread with high priority.<br>
 * It is possible to launch on other task inside a task, that is not a good idea in classical schedulling system (Here no
 * interblocking).<br>
 * To use implements {@link ThreadedTask}, {@link ThreadedSimpleTask} or {@link ThreadedVerySimpleTask}, then you can do the
 * task as soon as possible with {@link #doThread(ThreadedTask, Object)}, do action in delayed future with
 * {@link #delayedThread(ThreadedTask, Object, long)} or repeat the task with
 * {@link #repeatThread(ThreadedTask, Object, long, long)}
 * 
 * @author JHelp
 */
public final class ThreadManager
{
   /**
    * Dedicated thread for do the management
    * 
    * @author JHelp
    */
   class DoManagement
         implements Runnable
   {
      /**
       * Do the management in dedicated thread <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @see Runnable#run()
       */
      @Override
      public void run()
      {
         ThreadManager.this.runDoManagement();
      }
   }

   /**
    * Number of maximum thread running in same time (If you change this number, I advice to not go under 8, and if you choose a
    * high value, you may notice a slow or out of memory. For now 128 is a good compromise for what I tested
    */
   private static final int                    NUMBER_OF_ACTIVE_THREAD = 128;
   /** For synchronize the waiting task and the running threads */
   static final Object                         LOCK                    = new Object();
   /**
    * Maximum time to wait a new free thread/task to do , before check. No block infinity (with 0) because can may some bad
    * effects (block). We choose a enought long time to not "agress" the CPU, but enough short to have an escape on bad
    * situation that may happen (never meet for now)
    */
   static final int                            MAXIMUM_WAIT            = 16384;
   /** Minimum time between repeat (Never put under 1) */
   static final int                            MINIMUM_REPEAT          = 1;
   /** Minimum time before do an action (The meaning of "as soon as possible") (never put under 1) */
   static final int                            MINIMUM_WAIT            = 1;
   /** Thread manager unique singleton instance */
   public static final ThreadManager           THREAD_MANAGER;
   static
   {
      THREAD_MANAGER = new ThreadManager();
   }
   /** Indicates if the manager is alive */
   private boolean                             alive;
   /** Do the management */
   private final DoManagement                  doManagement;
   /** Indicates if their at least a task that wait urgently for a free thread */
   private boolean                             haveToActAsSoonAsPossible;
   /** Delayed cancel task */
   private final ThreadedSimpleTask<Integer>   threadCancelTask        = new ThreadedSimpleTask<Integer>()
                                                                       {
                                                                          /**
                                                                           * Cancel a task <br>
                                                                           * <br>
                                                                           * <b>Parent documentation:</b><br>
                                                                           * {@inheritDoc}
                                                                           * 
                                                                           * @param id
                                                                           *           Task ID
                                                                           * @see jhelp.util.thread.ThreadedSimpleTask#doSimpleAction(Object)
                                                                           */
                                                                          @Override
                                                                          protected void doSimpleAction(final Integer id)
                                                                          {
                                                                             synchronized(ThreadManager.LOCK)
                                                                             {
                                                                                final Iterator<ThreadElement<?, ?, ?>> iterator = ThreadManager.this.priorityQueue.iterator();
                                                                                ThreadElement<?, ?, ?> threadElement;

                                                                                while(iterator.hasNext() == true)
                                                                                {
                                                                                   threadElement = iterator.next();

                                                                                   if(threadElement.getID() == id)
                                                                                   {
                                                                                      threadElement.destroy();

                                                                                      ThreadManager.this.priorityQueue.remove(threadElement);

                                                                                      return;
                                                                                   }
                                                                                }

                                                                                for(final ThreadActor threadActor : ThreadManager.this.threads)
                                                                                {
                                                                                   threadActor.stopThread(id);
                                                                                }
                                                                             }
                                                                          }
                                                                       };

   /** Count the number of waiting process */
   private int                                 waitCount               = 0;
   /** Task to do waiting queue */
   final PriorityQueue<ThreadElement<?, ?, ?>> priorityQueue;

   /** Threads that does the tasks */
   final ThreadActor[]                         threads;

   /**
    * Create a new instance of ThreadManager
    */
   private ThreadManager()
   {
      this.doManagement = new DoManagement();
      this.priorityQueue = new PriorityQueue<ThreadElement<?, ?, ?>>();

      this.threads = new ThreadActor[ThreadManager.NUMBER_OF_ACTIVE_THREAD];
      for(int i = 0; i < ThreadManager.NUMBER_OF_ACTIVE_THREAD; i++)
      {
         this.threads[i] = new ThreadActor();
      }

      this.alive = true;
      this.haveToActAsSoonAsPossible = false;

      final Thread thread = new Thread(this.doManagement);
      thread.start();
   }

   /**
    * Call by a thread to say he is free, so ready for do next task
    */
   void anActorIsFree()
   {
      if(this.haveToActAsSoonAsPossible == true)
      {
         synchronized(ThreadManager.LOCK)
         {
            try
            {
               if(this.waitCount > 0)
               {
                  ThreadManager.LOCK.notify();
               }
            }
            catch(final Exception exception)
            {
            }
         }
      }
   }

   /**
    * Adding a task internally.<br>
    * Use by repeat system
    * 
    * @param threadElement
    *           Thread to add
    */
   void internalAdd(final ThreadElement<?, ?, ?> threadElement)
   {
      synchronized(ThreadManager.LOCK)
      {
         this.priorityQueue.add(threadElement);

         try
         {
            if(this.waitCount > 0)
            {
               ThreadManager.LOCK.notify();
            }
         }
         catch(final Exception exception)
         {
         }
      }
   }

   /**
    * Do the management
    */
   void runDoManagement()
   {
      ThreadElement<?, ?, ?> threadElement;
      long time;
      boolean threadLaunch;

      while(this.alive == true)
      {
         threadElement = null;

         while(threadElement == null)
         {
            synchronized(ThreadManager.LOCK)
            {
               threadElement = this.priorityQueue.peek();
            }

            if(threadElement == null)
            {
               synchronized(ThreadManager.LOCK)
               {
                  try
                  {
                     this.waitCount++;
                     ThreadManager.LOCK.wait(ThreadManager.MAXIMUM_WAIT);
                  }
                  catch(final Exception exception)
                  {
                  }

                  this.waitCount--;
               }
            }
            else if(threadElement.isAlive() == false)
            {
               synchronized(ThreadManager.LOCK)
               {
                  this.priorityQueue.remove(threadElement);
               }

               threadElement = null;
            }

            if(this.alive == false)
            {
               Debug.printMark(DebugLevel.VERBOSE, "THREAD MANAGEMENT DIE");

               return;
            }
         }

         this.haveToActAsSoonAsPossible = false;
         time = threadElement.getTimeToAct() - System.currentTimeMillis();
         if ((time <= 0) && (threadElement!=null))
         {
            threadLaunch = false;

            for(final ThreadActor threadActor : this.threads)
            {
               if(threadActor.setThreadElement(threadElement) == true)
               {
                  threadLaunch = true;

                  synchronized(ThreadManager.LOCK)
                  {
                     this.priorityQueue.remove(threadElement);
                  }

                  break;
               }
            }

            if(threadLaunch == false)
            {
               this.haveToActAsSoonAsPossible = true;
               synchronized(ThreadManager.LOCK)
               {
                  try
                  {
                     this.waitCount++;
                     ThreadManager.LOCK.wait(ThreadManager.MAXIMUM_WAIT);
                  }
                  catch(final Exception exception)
                  {
                  }

                  this.waitCount--;
               }
            }
         }
         else
         {
            synchronized(ThreadManager.LOCK)
            {
               try
               {
                  this.waitCount++;
                  ThreadManager.LOCK.wait(time);
               }
               catch(final Exception exception)
               {
               }

               this.waitCount--;
            }
         }
      }

      Debug.printMark(DebugLevel.VERBOSE, "THREAD MANAGEMENT DIE");
   }

   /**
    * Give the approximative value of number of free thread.<br>
    * Because threads are free, and taken at any time, the value can't be accurate, its just an idea of the result
    * 
    * @return Approximative value of number of free thread
    */
   public int approximativeNumberOfFreeThread()
   {
      int count = 0;

      for(final ThreadActor threadActor : this.threads)
      {
         if(threadActor.isFree() == true)
         {
            count++;
         }
      }

      return count;
   }

   /**
    * Cancel a task.<br>
    * The cancel is not immediate, it is do as soon as possible
    * 
    * @param id
    *           Task id to cancel, give by {@link #repeatThread(ThreadedTask, Object, long, long)}
    */
   public void cancelTask(final int id)
   {
      this.doThread(this.threadCancelTask, id);
   }

   /**
    * Do a task in a delayed time. <br>
    * When task turn comes, the given parameter is given to it, so you can can this method several times with the same instance
    * of task, but different parameter.
    * 
    * @param <PARAMETER>
    *           Parameter type
    * @param <RESULT>
    *           Result type of the task
    * @param <PROGRESS>
    *           Progression information type of the task
    * @param threadedTask
    *           Task to do
    * @param parameter
    *           Parameter to give to the task, when its turn comes
    * @param delay
    *           Number of milliseconds to wait before do the task
    */
   public <PARAMETER, RESULT, PROGRESS> void delayedThread(final ThreadedTask<PARAMETER, RESULT, PROGRESS> threadedTask, final PARAMETER parameter, final long delay)
   {
      /*synchronized(ThreadManager.LOCK)*/
      {
         final ThreadElement<PARAMETER, RESULT, PROGRESS> threadElement = new ThreadElement<PARAMETER, RESULT, PROGRESS>(threadedTask, parameter, Math.max(ThreadManager.MINIMUM_WAIT, delay) + System.currentTimeMillis());

         this.priorityQueue.add(threadElement);

//         try
//         {
//            if(this.waitCount > 0)
//            {
//               ThreadManager.LOCK.notify();
//            }
//         }
//         catch(final Exception exception)
//         {
//         }
      }
   }

   /**
    * Destroy properly the manager.<br>
    * Don't use it after call the method
    */
   public void destroy()
   {
      synchronized(ThreadManager.LOCK)
      {
         this.alive = false;

         try
         {
            if(this.waitCount > 0)
            {
               ThreadManager.LOCK.notify();
            }
         }
         catch(final Exception exception)
         {
         }

         for(final ThreadActor threadActor : this.threads)
         {
            threadActor.stopActor();
         }

         while(this.priorityQueue.isEmpty() == false)
         {
            this.priorityQueue.poll().destroy();
         }
      }
   }

   /**
    * Do a task as soon as possible.
    * 
    * @param <PARAMETER>
    *           Parameter tpye
    * @param <RESULT>
    *           Result type
    * @param <PROGRESS>
    *           Progression type
    * @param threadedTask
    *           Task to do
    * @param parameter
    *           Parameter gives to the task when its turn comes
    */
   public <PARAMETER, RESULT, PROGRESS> void doThread(final ThreadedTask<PARAMETER, RESULT, PROGRESS> threadedTask, final PARAMETER parameter)
   {
      this.delayedThread(threadedTask, parameter, ThreadManager.MINIMUM_WAIT);
   }

   /**
    * Indicates if thread manager is near full.<br>
    * Like {@link #approximativeNumberOfFreeThread()} it can't be total accurate, but it give a good idea
    * 
    * @return {@code true} if thread manager is near full
    */
   public boolean nearFull()
   {
      return (ThreadManager.NUMBER_OF_ACTIVE_THREAD - this.approximativeNumberOfFreeThread()) <= 8;
   }

   /**
    * Repeat a task
    * 
    * @param <PARAMETER>
    *           Parameter type
    * @param <RESULT>
    *           Result type
    * @param <PROGRESS>
    *           Progress type
    * @param threadedTask
    *           Task to repeat
    * @param parameter
    *           Parameter give to the task at each repeat
    * @param delay
    *           Time to wait in millisecond before do the first time
    * @param repeat
    *           Time between each repeat in milliseconds
    * @return Task ID to able cancel it later with {@link #cancelTask(int)}
    */
   public <PARAMETER, RESULT, PROGRESS> int repeatThread(final ThreadedTask<PARAMETER, RESULT, PROGRESS> threadedTask, final PARAMETER parameter, final long delay, final long repeat)
   {
      int id;

      synchronized(ThreadManager.LOCK)
      {
         final ThreadElement<PARAMETER, RESULT, PROGRESS> threadElement = new ThreadElement<PARAMETER, RESULT, PROGRESS>(threadedTask, parameter, Math.max(ThreadManager.MINIMUM_WAIT, delay) + System.currentTimeMillis(), Math.max(
               ThreadManager.MINIMUM_REPEAT, repeat));

         this.priorityQueue.add(threadElement);

         id = threadElement.getID();

         try
         {
            if(this.waitCount > 0)
            {
               ThreadManager.LOCK.notify();
            }
         }
         catch(final Exception exception)
         {
         }
      }

      return id;
   }

   /**
    * Restart the thread manager
    */
   public void restart()
   {
      if(this.alive == false)
      {
         this.alive = true;

         final Thread thread = new Thread(this.doManagement);
         thread.start();
      }
   }
}