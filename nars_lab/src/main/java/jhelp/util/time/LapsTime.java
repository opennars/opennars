/**
 * Project : JHelpUtil<br>
 * Package : jhelp.util.time<br>
 * Class : LapsTime<br>
 * Date : 5 avr. 2009<br>
 * By JHelp
 */
package jhelp.util.time;

/**
 * For measure a laps of time <br>
 * <br>
 * Last modification : 5 avr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class LapsTime
      implements Comparable<LapsTime>
{
   /** Last measured time */
   private static long LAST_MEASURE;

   /**
    * Add two laps time.<br>
    * It's made in way the you can use safely the same instance in <code>lapsTime1</code> and/or <code>lapsTime2</code> and/or
    * <code>result</code>
    * 
    * @param lapsTime1
    *           First laps time
    * @param lapsTime2
    *           Second laps time
    * @param result
    *           For collect result. If {@code null}, it create a new instance
    * @return Result
    */
   public static LapsTime addition(final LapsTime lapsTime1, final LapsTime lapsTime2, LapsTime result)
   {
      if(lapsTime1 == null)
      {
         throw new NullPointerException("lapsTime1 musn't be null");
      }
      if(lapsTime2 == null)
      {
         throw new NullPointerException("lapsTime2 musn't be null");
      }
      if(result == null)
      {
         result = new LapsTime();
      }
      result.microseconds = lapsTime1.microseconds + lapsTime2.microseconds;
      result.update();
      return result;
   }

   /**
    * Divide by a factor.<br>
    * It's made in way the you can use safely the same instance in <code>lapsTime</code> and <code>result</code>
    * 
    * @param lapsTime
    *           Laps time to divide
    * @param factor
    *           Division factor
    * @param result
    *           For collect result. If {@code null}, it create a new instance
    * @return Result
    */
   public static LapsTime divide(final LapsTime lapsTime, final int factor, LapsTime result)
   {
      if(lapsTime == null)
      {
         throw new NullPointerException("lapsTime musn't be null");
      }
      if(factor < 0)
      {
         throw new IllegalArgumentException("factor must be >0");
      }
      if(factor == 0)
      {
         throw new ArithmeticException("Not divide by 0");
      }
      if(result == null)
      {
         result = new LapsTime();
      }
      result.microseconds = lapsTime.microseconds / factor;
      result.update();
      return result;
   }

   /**
    * End measure
    * 
    * @return Time pass since the last {@link #startMeasure()}
    */
   public static LapsTime endMeasure()
   {
      return new LapsTime((System.nanoTime() - LapsTime.LAST_MEASURE) / 1000L);
   }

   /**
    * Multiply by a factor.<br>
    * It's made in way the you can use safely the same instance in <code>lapsTime</code> and <code>result</code>
    * 
    * @param lapsTime
    *           Laps time to divide
    * @param factor
    *           Multiplication factor
    * @param result
    *           For collect result. If {@code null}, it create a new instance
    * @return Result
    */
   public static LapsTime multiply(final LapsTime lapsTime, final int factor, LapsTime result)
   {
      if(lapsTime == null)
      {
         throw new NullPointerException("lapsTime musn't be null");
      }
      if(factor < 0)
      {
         throw new IllegalArgumentException("factor must be >=0");
      }
      if(result == null)
      {
         result = new LapsTime();
      }
      result.microseconds = lapsTime.microseconds * factor;
      result.update();
      return result;
   }

   /**
    * Start measure
    */
   public static void startMeasure()
   {
      LapsTime.LAST_MEASURE = System.nanoTime();
   }

   /**
    * Subtract two laps time.<br>
    * It's made in way the you can use safely the same instance in <code>lapsTime1</code> and/or <code>lapsTime2</code> and/or
    * <code>result</code>
    * 
    * @param lapsTime1
    *           First laps time
    * @param lapsTime2
    *           Second laps time
    * @param result
    *           For collect result. If {@code null}, it create a new instance
    * @return Result
    */
   public static LapsTime substract(final LapsTime lapsTime1, final LapsTime lapsTime2, LapsTime result)
   {
      if(lapsTime1 == null)
      {
         throw new NullPointerException("lapsTime1 musn't be null");
      }
      if(lapsTime2 == null)
      {
         throw new NullPointerException("lapsTime2 musn't be null");
      }
      if(lapsTime1.microseconds < lapsTime2.microseconds)
      {
         throw new IllegalArgumentException("The first time must be >= to the second");
      }
      if(result == null)
      {
         result = new LapsTime();
      }
      result.microseconds = lapsTime1.microseconds - lapsTime2.microseconds;
      result.update();
      return result;
   }

   /** Hour part */
   private int  hour;
   /** Microsecond part */
   private int  microsecond;
   /** Total of microseconds */
   private long microseconds;
   /** Millisecond part */
   private int  millisecond;
   /** minute */
   private int  minute;

   /** Second part */
   private int  second;

   /**
    * Constructs LapsTime
    */
   public LapsTime()
   {
   }

   /**
    * Constructs LapsTime
    * 
    * @param microsecond
    *           Microsecond part
    */
   public LapsTime(final int microsecond)
   {
      if(microsecond < 0)
      {
         throw new IllegalArgumentException("microsecond musn't be <0");
      }
      this.microsecond = microsecond;
      this.updateMicroSeconds();
   }

   /**
    * Constructs LapsTime
    * 
    * @param millisecond
    *           Millisecond part
    * @param microsecond
    *           Microsecond part
    */
   public LapsTime(final int millisecond, final int microsecond)
   {
      this(microsecond);
      if(millisecond < 0)
      {
         throw new IllegalArgumentException("millisecond musn't be <0");
      }
      this.millisecond = millisecond;
      this.updateMicroSeconds();
   }

   /**
    * Constructs LapsTime
    * 
    * @param second
    *           Second part
    * @param millisecond
    *           Millisecond part
    * @param microsecond
    *           Microsecond part
    */
   public LapsTime(final int second, final int millisecond, final int microsecond)
   {
      this(millisecond, microsecond);
      if(second < 0)
      {
         throw new IllegalArgumentException("second musn't be <0");
      }
      this.second = second;
      this.updateMicroSeconds();
   }

   /**
    * Constructs LapsTime
    * 
    * @param minute
    *           Minute part
    * @param second
    *           Second part
    * @param millisecond
    *           Millisecond part
    * @param microsecond
    *           Microsecond part
    */
   public LapsTime(final int minute, final int second, final int millisecond, final int microsecond)
   {
      this(second, millisecond, microsecond);
      if(minute < 0)
      {
         throw new IllegalArgumentException("minute musn't be <0");
      }
      this.minute = minute;
      this.updateMicroSeconds();
   }

   /**
    * Constructs LapsTime
    * 
    * @param hour
    *           Hour part
    * @param minute
    *           Minute part
    * @param second
    *           Second part
    * @param millisecond
    *           Millisecond part
    * @param microsecond
    *           Microsecond part
    */
   public LapsTime(final int hour, final int minute, final int second, final int millisecond, final int microsecond)
   {
      this(minute, second, millisecond, microsecond);
      if(hour < 0)
      {
         throw new IllegalArgumentException("minute musn't be <0");
      }
      this.hour = hour;
      this.updateMicroSeconds();
   }

   /**
    * Constructs LapsTime
    * 
    * @param microseconds
    *           Total of microsecond
    */
   public LapsTime(final long microseconds)
   {
      if(microseconds < 0)
      {
         throw new IllegalArgumentException("microseconds must be >=0, not " + microseconds);
      }
      this.microseconds = microseconds;
      this.update();
   }

   /**
    * Compute each part with total microseconds
    */
   private void update()
   {
      long microseconds = this.microseconds;
      this.microsecond = (int) (microseconds % 1000L);
      microseconds /= 1000L;
      this.millisecond = (int) (microseconds % 1000L);
      microseconds /= 1000L;
      this.second = (int) (microseconds % 60L);
      microseconds /= 60L;
      this.minute = (int) (microseconds % 60L);
      microseconds /= 60L;
      this.hour = (int) microseconds;
   }

   /**
    * Compute total microseconds with each parts
    */
   private void updateMicroSeconds()
   {
      this.microseconds = this.microsecond + (1000L * (this.millisecond + (1000L * (this.second + (60L * (this.minute + (60L * this.hour)))))));
      this.update();
   }

   /**
    * Compare with an other laps time
    * 
    * @param lapsTime
    *           Laps time to compare with
    * @return Comparison result
    * @see Comparable#compareTo(Object)
    */
   @Override
   public int compareTo(final LapsTime lapsTime)
   {
      final long sub = lapsTime.microseconds - this.microseconds;
      if(sub == 0L)
      {
         return 0;
      }
      if(sub > 0L)
      {
         return 1;
      }
      return -1;
   }

   /**
    * Indicates if an object is same as this laps time
    * 
    * @param obj
    *           Object to compare
    * @return {@code true} if the same
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(final Object obj)
   {
      if(this == obj)
      {
         return true;
      }
      if(obj == null)
      {
         return false;
      }
      if((obj instanceof LapsTime) == false)
      {
         return false;
      }
      final LapsTime other = (LapsTime) obj;
      if(this.microseconds != other.microseconds)
      {
         return false;
      }
      return true;
   }

   /**
    * Return hour
    * 
    * @return hour
    */
   public int getHour()
   {
      return this.hour;
   }

   /**
    * Return microsecond
    * 
    * @return microsecond
    */
   public int getMicrosecond()
   {
      return this.microsecond;
   }

   /**
    * Return millisecond
    * 
    * @return millisecond
    */
   public int getMillisecond()
   {
      return this.millisecond;
   }

   /**
    * Return minute
    * 
    * @return minute
    */
   public int getMinute()
   {
      return this.minute;
   }

   /**
    * Return second
    * 
    * @return second
    */
   public int getSecond()
   {
      return this.second;
   }

   /**
    * Hash code
    * 
    * @return Hash code
    * @see Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + (int) (this.microseconds ^ (this.microseconds >>> 32));
      return result;
   }

   /**
    * Total of microseconds
    * 
    * @return Total of microseconds
    */
   public long inMicroseconds()
   {
      return this.microseconds;
   }

   /**
    * Total of milliseconds
    * 
    * @return Total of milliseconds
    */
   public long inMilliseconds()
   {
      return this.microseconds / 1000L;
   }

   /**
    * Modify hour
    * 
    * @param hour
    *           New hour value
    */
   public void setHour(final int hour)
   {
      if(hour < 0)
      {
         throw new IllegalArgumentException("hour musn't be <0");
      }
      this.hour = hour;
      this.updateMicroSeconds();
   }

   /**
    * Modify microsecond
    * 
    * @param microsecond
    *           New microsecond value
    */
   public void setMicrosecond(final int microsecond)
   {
      if(microsecond < 0)
      {
         throw new IllegalArgumentException("microsecond musn't be <0");
      }
      this.microsecond = microsecond;
      this.updateMicroSeconds();
   }

   /**
    * Modify millisecond
    * 
    * @param millisecond
    *           New millisecond value
    */
   public void setMillisecond(final int millisecond)
   {
      if(millisecond < 0)
      {
         throw new IllegalArgumentException("millisecond musn't be <0");
      }
      this.millisecond = millisecond;
      this.updateMicroSeconds();
   }

   /**
    * Modify minute
    * 
    * @param minute
    *           New minute value
    */
   public void setMinute(final int minute)
   {
      if(minute < 0)
      {
         throw new IllegalArgumentException("minute musn't be <0");
      }
      this.minute = minute;
      this.updateMicroSeconds();
   }

   /**
    * Modify second
    * 
    * @param second
    *           New second value
    */
   public void setSecond(final int second)
   {
      if(second < 0)
      {
         throw new IllegalArgumentException("second musn't be <0");
      }
      this.second = second;
      this.updateMicroSeconds();
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
      final StringBuilder stringBuilder = new StringBuilder(33);

      boolean force = false;

      if(this.hour > 0)
      {
         stringBuilder.append(this.hour);
         stringBuilder.append("h ");

         force = true;
      }

      if((force == true) || (this.minute > 0))
      {
         stringBuilder.append(this.minute);
         stringBuilder.append("m ");

         force = true;
      }

      if((force == true) || (this.second > 0))
      {
         stringBuilder.append(this.second);
         stringBuilder.append("s ");

         force = true;
      }

      if((force == true) || (this.millisecond > 0))
      {
         stringBuilder.append(this.millisecond);
         stringBuilder.append("ms ");
      }

      stringBuilder.append(this.microsecond);
      stringBuilder.append("micros");

      return stringBuilder.toString();
   }
}