package jhelp.util.thread;

import jhelp.util.list.Pair;

/**
 * Task do in separate thread<br>
 * Extends this class to have a good thread manager with {@link ThreadManager}.<br>
 * Its better to use this mechanism than create a new Thread instance and haven't issue noticed in "Scheduled mechanism" to know
 * more see {@link ThreadManager}.<br>
 * This class is the complete version, it have a parameter, a result and a progress information.<br>
 * <table border=1>
 * <tr>
 * <th>Parameter</th>
 * <td>It is given when this task turn arrive (One time or by repetition)</td>
 * <td>It allows to have only one instance that react to different parameters in different threads</td>
 * </tr>
 * <tr>
 * <th>Result</th>
 * <td>It is is computed by the implementation of the derived class.</td>
 * <td>When the result is computed this class is alerted.</td>
 * </tr>
 * <tr>
 * <th>Progress</th>
 * <td>During the computing, you can signal a progression to for example update a progress bar</td>
 * <td>The progression information is up to implementation and it signal in separate thread, so when received the progression
 * information it is not the exact progression. We have do this choice to not slow down the computation by showing progression</td>
 * </tr>
 * </table>
 * 
 * @author JHelp
 * @param <PARAMETER>
 *           Parameter type
 * @param <RESULT>
 *           Result type
 * @param <PROGRESS>
 *           Progression type
 */
public abstract class ThreadedTask<PARAMETER, RESULT, PROGRESS>
{
   /** For post a progression */
   private PostProgress<PROGRESS> postProgress;

   /**
    * Create a new instance of ThreadedTask
    */
   public ThreadedTask()
   {
   }

   /**
    * Request to cancel the task
    */
   protected final void cancel()
   {
      this.canceled();
   }

   /**
    * It is call when a cancel is requested.<br>
    * Override it to do something on cancel, do nothing by default
    */
   protected void canceled()
   {
   }

   /**
    * It is call when a progression information arrive.<br>
    * Override it to do something on progression, do nothing by default
    * 
    * @param progress
    *           Progression information
    */
   protected void doProgress(final PROGRESS progress)
   {
   }

   /**
    * Call when the turn of this thread comes.<br>
    * It does the action and compute the result
    * 
    * @param parameter
    *           Parameter to use
    * @return The computed result
    */
   protected abstract RESULT doThreadAction(PARAMETER parameter);

   /**
    * Call this method to post a progression during the computing
    * 
    * @param progress
    *           Progression information to post
    */
   protected final void postProgress(final PROGRESS progress)
   {
      if(this.postProgress == null)
      {
         this.postProgress = new PostProgress<PROGRESS>();
      }

      ThreadManager.THREAD_MANAGER.doThread(this.postProgress,//
            new Pair<ThreadedTask<?, ?, PROGRESS>, PROGRESS>(this, progress));
   }

   /**
    * Call when task is finish.<br>
    * Override this method to do something when computing is finish, do nothing by default
    * 
    * @param result
    *           Computed result
    */
   protected void setResult(final RESULT result)
   {
   }
}