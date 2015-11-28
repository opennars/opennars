package jhelp.util.thread;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Describes a thread element.<br>
 * For {@link ThreadManager} internal use only
 * 
 * @author JHelp
 * @param <PARAMETER>
 *           Parameter type
 * @param <RESULT>
 *           Result type
 * @param <PROGRESS>
 *           Progress type
 */
final class ThreadElement<PARAMETER, RESULT, PROGRESS>
      implements Comparable<ThreadElement<PARAMETER, RESULT, PROGRESS>>
{
   /** Next thread element ID */
   private static int                                nextId        = 0;
   /** Indicates if the element is alive */
   private boolean                                   alive;
   /** Thread element ID */
   private int                                       id;
   /** Parameter to give to the thread */
   private PARAMETER                                 parameter;
   /** For synchronize */
   private final ReentrantLock                       reentrantLock = new ReentrantLock(true);
   /** Repeat time */
   private long                                      repeatTime;
   /** Task to do */
   private ThreadedTask<PARAMETER, RESULT, PROGRESS> threadedTask;
   /** Time when do the action */
   private long                                      timeToAct;

   /**
    * Create a new instance of ThreadElement
    * 
    * @param threadedTask
    *           Task to do
    * @param parameter
    *           Parameter to give to the task
    * @param timeToAct
    *           Time when do the task
    */
   ThreadElement(final ThreadedTask<PARAMETER, RESULT, PROGRESS> threadedTask, final PARAMETER parameter, final long timeToAct)
   {
      this(threadedTask, parameter, timeToAct, -1);
   }

   /**
    * Create a new instance of ThreadElement
    * 
    * @param threadedTask
    *           Task to do
    * @param parameter
    *           Parameter to give to the task
    * @param timeToAct
    *           Time when do the task
    * @param repeatTime
    *           Repeat time
    */
   ThreadElement(final ThreadedTask<PARAMETER, RESULT, PROGRESS> threadedTask, final PARAMETER parameter, final long timeToAct, final long repeatTime)
   {
      this.id = ThreadElement.nextId++;

      this.threadedTask = threadedTask;
      this.parameter = parameter;
      this.timeToAct = timeToAct;
      this.repeatTime = repeatTime;

      this.alive = true;
   }

   /**
    * Destroy properly the element to free some memory
    */
   void destroy()
   {
      this.reentrantLock.lock();

      try
      {
         this.alive = false;
         this.repeatTime = -1;

         if(this.threadedTask != null)
         {
            this.threadedTask.canceled();

            this.threadedTask = null;
            this.parameter = null;
         }
      }
      finally
      {
         this.reentrantLock.unlock();
      }
   }

   /**
    * Apply the repeat time, if need
    * 
    * @return {@code true} if the task need to be repeat. {@code false} if the task is just do once
    */
   boolean doRepeat()
   {
      if(this.repeatTime < 0)
      {
         return false;
      }

      this.timeToAct = System.currentTimeMillis() + this.repeatTime;
      return true;
   }

   /**
    * Thread element ID
    * 
    * @return Thread element ID
    */
   int getID()
   {
      return this.id;
   }

   /**
    * Time when do the task
    * 
    * @return Time when do the task
    */
   long getTimeToAct()
   {
      return this.timeToAct;
   }

   /**
    * Indicates if the element still alive
    * 
    * @return {@code true} if the element still alive
    */
   boolean isAlive()
   {
      this.reentrantLock.lock();

      try
      {
         return this.alive;
      }
      finally
      {
         this.reentrantLock.unlock();
      }
   }

   /**
    * Compare the element to an other.<br>
    * It returns :
    * <table border=1>
    * <tr>
    * <th>-1</th>
    * <td>If is is before the compared task</td>
    * </tr>
    * <tr>
    * <th>0</th>
    * <td>If It is on same time as compared task</td>
    * </tr>
    * <tr>
    * <th>1</th>
    * <td>If it is after the compared task</td>
    * </tr>
    * </table>
    * <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param threadElement
    *           Element to compare
    * @return Comparison result
    * @see Comparable#compareTo(Object)
    */
   @Override
   public int compareTo(final ThreadElement<PARAMETER, RESULT, PROGRESS> threadElement)
   {
      if(this.timeToAct < threadElement.timeToAct)
      {
         return -1;
      }

      if(this.timeToAct > threadElement.timeToAct)
      {
         return 1;
      }

      return 0;
   }

   /**
    * Do the action
    */
   public void run()
   {
      this.reentrantLock.lock();

      try
      {
         if((this.threadedTask != null) && (this.alive == true))
         {
            final RESULT result = this.threadedTask.doThreadAction(this.parameter);

            this.threadedTask.setResult(result);
         }
      }
      finally
      {
         this.reentrantLock.unlock();
      }
   }
}