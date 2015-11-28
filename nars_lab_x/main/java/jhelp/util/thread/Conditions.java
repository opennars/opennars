package jhelp.util.thread;

import jhelp.util.list.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Create and manage conditions for launching thread. <br>
 * For example if a thread A need wait that an other thread B have finish computing something (But B may still running to do
 * other work) create a condition C. Then invalidate C with {@link #invalidate(String)}. Then launch B and register A with
 * {@link Conditions#waitFor(String, ThreadedTask, Object)}. In B when the condition is complete call {@link #validate(String)}.
 * It will launch A.<br>
 * If condition is complete before A is register, A will be launch as soon as possible after the registering.<br>
 * Important, by default a condition is consider like valid, you have to invalidate it to make it invalid
 * 
 * @author JHelp
 */
public class Conditions
{
   /** Conditions singleton */
   public static final Conditions                                       CONDITIONS = new Conditions();
   /** Conditions list */
   @SuppressWarnings("rawtypes")
   private final HashMap<String, ArrayList<Pair<ThreadedTask, Object>>> conditions;
   /** Mutex for synchronized */
   private final Mutex                                                  mutex;

   /**
    * Create a new instance of Conditions
    */
   @SuppressWarnings("rawtypes")
   private Conditions()
   {
      this.conditions = new HashMap<String, ArrayList<Pair<ThreadedTask, Object>>>();
      this.mutex = new Mutex();
   }

   /**
    * Invalidate a condition. <br>
    * All thread that register, for the given condition, will wait for someone who call {@link #validate(String)}
    * 
    * @param condition
    *           Condition to invalidate
    */
   @SuppressWarnings("rawtypes")
   public void invalidate(final String condition)
   {
      this.mutex.lock();

      try
      {
         final ArrayList<Pair<ThreadedTask, Object>> waiters = this.conditions.get(condition);

         if(waiters == null)
         {
            this.conditions.put(condition, new ArrayList<Pair<ThreadedTask, Object>>());
         }
      }
      finally
      {
         this.mutex.unlock();
      }
   }

   /**
    * Indicates if a condition is valid
    * 
    * @param condition
    *           Condition to test
    * @return Condition validity
    */
   public boolean isValid(final String condition)
   {
      boolean result;

      this.mutex.lock();

      try
      {
         result = this.conditions.get(condition) == null;
      }
      finally
      {
         this.mutex.unlock();
      }

      return result;
   }

   /**
    * Validate a condition and launch all waiting threads
    * 
    * @param <PARAMETER>
    *           Thread parameter type
    * @param condition
    *           Condition to validate
    */
   @SuppressWarnings(
   {
         "rawtypes", "unchecked"
   })
   public <PARAMETER> void validate(final String condition)
   {
      this.mutex.lock();

      try
      {
         final ArrayList<Pair<ThreadedTask, Object>> waiters = this.conditions.get(condition);

         if(waiters != null)
         {
            for(final Pair<ThreadedTask, Object> task : waiters)
            {
               ThreadManager.THREAD_MANAGER.doThread((ThreadedTask<PARAMETER, ?, ?>) task.element1, (PARAMETER) task.element2);
            }

            waiters.clear();
            this.conditions.remove(condition);
         }
      }
      finally
      {
         this.mutex.unlock();
      }
   }

   /**
    * Register a thread to wait a condition to be valid before launch.<br>
    * If the condition is already valid (As by default) the thread is launch as soon as possible
    * 
    * @param <PARAMETER>
    *           Thread parameter type
    * @param condition
    *           Condition to wait
    * @param task
    *           Thread to launch
    * @param parameter
    *           Thread parameter
    */
   @SuppressWarnings("rawtypes")
   public <PARAMETER> void waitFor(final String condition, final ThreadedTask<PARAMETER, ?, ?> task, final PARAMETER parameter)
   {
      this.mutex.lock();

      try
      {
         final ArrayList<Pair<ThreadedTask, Object>> waiters = this.conditions.get(condition);

         if(waiters == null)
         {
            ThreadManager.THREAD_MANAGER.doThread(task, parameter);
         }
         else
         {
            waiters.add(new Pair<ThreadedTask, Object>(task, parameter));
         }
      }
      finally
      {
         this.mutex.unlock();
      }
   }
}