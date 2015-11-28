package jhelp.util.thread;

/**
 * Thread that do a threaded task {@link ThreadedTask}, {@link ThreadedSimpleTask} or {@link ThreadedVerySimpleTask}, then wait
 * for an other one to do
 * 
 * @author JHelp
 */
final class ThreadActor
      implements Runnable
{
   /** Actual thread task description to do */
   private ThreadElement<?, ?, ?> actualThreadElement;
   /** Indicates if the thread is alive */
   private boolean                alive;
   /** For synchronize the access to current task */
   private final Object           LOCK = new Object();

   /**
    * Create a new instance of ThreadActor
    */
   ThreadActor()
   {
      this.alive = false;
   }

   /**
    * Indicates if the thread have nothing to do
    * 
    * @return {@code true} if the thread have nothing to do
    */
   boolean isFree()
   {
      ThreadElement<?, ?, ?> threadElement = null;

      synchronized(this.LOCK)
      {
         threadElement = this.actualThreadElement;
      }

      return threadElement == null;
   }

   /**
    * Define, if the thread is free, the actual task to do
    * 
    * @param threadElement
    *           Task to do
    * @return {@code true} if the settings have done. {@code false} if the settings not do because the thread already on doing a
    *         task
    */
   boolean setThreadElement(final ThreadElement<?, ?, ?> threadElement)
   {
      if(this.isFree() == false)
      {
         return false;
      }

      synchronized(this.LOCK)
      {
         this.actualThreadElement = threadElement;
      }

      if(this.alive == false)
      {
         this.alive = true;
         (new Thread(this)).start();
      }
      else
      {
         synchronized(this.LOCK)
         {
            try
            {
               this.LOCK.notify();
            }
            catch(final Exception exception)
            {
            }
         }
      }

      return true;
   }

   /**
    * Stop the actor
    */
   void stopActor()
   {
      synchronized(this.LOCK)
      {
         if(this.actualThreadElement != null)
         {
            this.actualThreadElement.destroy();
         }

         this.alive = false;
      }
   }

   /**
    * Stop the thread carry if it is the one who have the given ID
    * 
    * @param id
    *           ID of thread to stop
    */
   void stopThread(final int id)
   {
      synchronized(this.LOCK)
      {
         if((this.actualThreadElement != null) && (this.actualThreadElement.getID() == id))
         {
            this.actualThreadElement.destroy();
         }
      }
   }

   /**
    * Do the thread live (wait for task, do the task, wait for task, do the task ,....) <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @see Thread#run()
    */
   @Override
   public void run()
   {
      while(this.alive == true)
      {
         ThreadElement<?, ?, ?> threadElement = null;

         // While there are no task to do
         while(threadElement == null)
         {
            synchronized(this.LOCK)
            {
               // Get current task do to
               threadElement = this.actualThreadElement;

               // If nothing to do, wait for task
               if(threadElement == null)
               {
                  try
                  {
                     this.LOCK.wait(ThreadManager.MAXIMUM_WAIT);
                  }
                  catch(final Exception exception)
                  {
                  }
               }

               if(this.alive == false)
               {
                  return;
               }
            }
         }

         // Do the task
         if(threadElement.isAlive() == true)
         {
            threadElement.run();
         }

         synchronized(this.LOCK)
         {
            // Task is done
            this.actualThreadElement = null;
         }

         // Signal the thread manager that the thread is free
         ThreadManager.THREAD_MANAGER.anActorIsFree();

         // If the task have to be repeat, post it again
         if(threadElement.doRepeat() == true)
         {
            ThreadManager.THREAD_MANAGER.internalAdd(threadElement);
         }
      }
   }
}