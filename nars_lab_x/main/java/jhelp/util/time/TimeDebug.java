package jhelp.util.time;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;

import java.util.ArrayList;

/**
 * Measure time during operation.<br>
 * The creation initialize the time.<br>
 * Each time a step is done, call an {@link #add(String)}, then call {@link #dump()} to have a report
 * 
 * @author JHelp
 */
public class TimeDebug
{
   /**
    * Time information of a step
    */
   private static class TimeInfo
   {
      /** Information name */
      final String name;
      /** Information time in millisecond */
      final long   time;

      /**
       * Create the time information
       * 
       * @param name
       *           Information name
       * @param time
       *           Information time
       */
      public TimeInfo(final String name, final long time)
      {
         this.name = name;
         this.time = time;
      }
   }

   /** Time logging generic label */
   private final String              label;
   /** Last time */
   private long                      time;
   /** Collected information */
   private final ArrayList<TimeInfo> times;

   /**
    * Create a time logging.<br>
    * It initializes the time
    * 
    * @param label
    *           Generic label
    */
   public TimeDebug(final String label)
   {
      this.label = label;
      this.times = new ArrayList<TimeInfo>();
      this.time = System.currentTimeMillis();
   }

   /**
    * Called when object is collect by Garbage collector <br>
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
      this.times.clear();

      super.finalize();
   }

   /**
    * Add a step
    * 
    * @param label
    *           Label that identify the step
    */
   public void add(final String label)
   {
      this.times.add(new TimeInfo(label, System.currentTimeMillis() - this.time));
      this.time = System.currentTimeMillis();
   }

   /**
    * Show times of each steps and the total time
    */
   public void dump()
   {
      long total = System.currentTimeMillis() - this.time;

      Debug.println(DebugLevel.VERBOSE, "TIME OF : ", this.label);

      for(final TimeInfo timeInfo : this.times)
      {
         Debug.println(DebugLevel.VERBOSE, this.label, " :     ", timeInfo.time, " ms, ", timeInfo.name);

         total += timeInfo.time;
      }

      Debug.println(DebugLevel.VERBOSE, "RESULT OF ", this.label, " ", total, " ms");

      this.times.clear();
      this.time = System.currentTimeMillis();
   }
}