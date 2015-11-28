package jhelp.util.debug;

import jhelp.util.reflection.Reflector;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class for debugging and thread safe.<br>
 * We avoid dummy printings
 * 
 * @author JHelp
 */
public final class Debug
{
   /** Actual debug level */
   private static DebugLevel          debugLevel     = DebugLevel.VERBOSE;
   /** For synchronize the printing (To be thread safe) */
   private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock(true);

   /**
    * Print an integer
    * 
    * @param integer
    *           Integer to print
    * @param characterNumber
    *           Number of character must show
    */
   private static void printInteger(int integer, final int characterNumber)
   {
      int ten = 1;

      for(int i = 1; i < characterNumber; i++)
      {
         ten *= 10;
      }

      while(ten > 0)
      {
         System.out.print(integer / ten);

         integer %= ten;
         ten /= 10;
      }
   }

   /**
    * Print a message
    * 
    * @param debugLevel
    *           Debug level
    * @param stackTraceElement
    *           Trace of the source
    * @param message
    *           Message to print
    */
   private static void printMessage(final DebugLevel debugLevel, final StackTraceElement stackTraceElement, final Object... message)
   {
      // Level test
      if(debugLevel.getLevel() > Debug.debugLevel.getLevel())
      {
         return;
      }

      // Print time
      final GregorianCalendar gregorianCalendar = new GregorianCalendar();

      Debug.printInteger(gregorianCalendar.get(Calendar.DAY_OF_MONTH), 2);
      System.out.print('/');
      Debug.printInteger(gregorianCalendar.get(Calendar.MONTH) + 1, 2);
      System.out.print('/');
      Debug.printInteger(gregorianCalendar.get(Calendar.YEAR), 4);
      System.out.print(" : ");
      Debug.printInteger(gregorianCalendar.get(Calendar.HOUR_OF_DAY), 2);
      System.out.print('h');
      Debug.printInteger(gregorianCalendar.get(Calendar.MINUTE), 2);
      System.out.print('m');
      Debug.printInteger(gregorianCalendar.get(Calendar.SECOND), 2);
      System.out.print('s');
      Debug.printInteger(gregorianCalendar.get(Calendar.MILLISECOND), 3);
      System.out.print("ms : ");

      // Print level
      System.out.print(debugLevel.getHeader());

      // Print code location
      System.out.print(stackTraceElement.getClassName());
      System.out.print('.');
      System.out.print(stackTraceElement.getMethodName());
      System.out.print(" at ");
      System.out.print(stackTraceElement.getLineNumber());
      System.out.print(" : ");

      // Print message
      for(final Object element : message)
      {
         Debug.printObject(element);
      }

      System.out.println();
   }

   /**
    * Print an object
    * 
    * @param object
    *           Object to print
    */
   private static void printObject(final Object object)
   {
      if((object == null) || (object.getClass().isArray() == false))
      {
         System.out.print(object);

         return;
      }

      System.out.print('[');

      if(object.getClass().getComponentType().isPrimitive() == true)
      {
         final String name = object.getClass().getComponentType().getName();

         if(Reflector.PRIMITIVE_BOOLEAN.equals(name) == true)
         {
            final boolean[] array = (boolean[]) object;
            final int length = array.length;

            if(length > 0)
            {
               System.out.print(array[0]);

               for(int i = 1; i < length; i++)
               {
                  System.out.print(", ");

                  System.out.print(array[i]);
               }
            }
         }
         else if(Reflector.PRIMITIVE_BYTE.equals(name) == true)
         {
            final byte[] array = (byte[]) object;
            final int length = array.length;

            if(length > 0)
            {
               System.out.print(array[0]);

               for(int i = 1; i < length; i++)
               {
                  System.out.print(", ");

                  System.out.print(array[i]);
               }
            }
         }
         else if(Reflector.PRIMITIVE_CHAR.equals(name) == true)
         {
            final char[] array = (char[]) object;
            final int length = array.length;

            if(length > 0)
            {
               System.out.print(array[0]);

               for(int i = 1; i < length; i++)
               {
                  System.out.print(", ");

                  System.out.print(array[i]);
               }
            }
         }
         else if(Reflector.PRIMITIVE_DOUBLE.equals(name) == true)
         {
            final double[] array = (double[]) object;
            final int length = array.length;

            if(length > 0)
            {
               System.out.print(array[0]);

               for(int i = 1; i < length; i++)
               {
                  System.out.print(", ");

                  System.out.print(array[i]);
               }
            }
         }
         else if(Reflector.PRIMITIVE_FLOAT.equals(name) == true)
         {
            final float[] array = (float[]) object;
            final int length = array.length;

            if(length > 0)
            {
               System.out.print(array[0]);

               for(int i = 1; i < length; i++)
               {
                  System.out.print(", ");

                  System.out.print(array[i]);
               }
            }
         }
         else if(Reflector.PRIMITIVE_INT.equals(name) == true)
         {
            final int[] array = (int[]) object;
            final int length = array.length;

            if(length > 0)
            {
               System.out.print(array[0]);

               for(int i = 1; i < length; i++)
               {
                  System.out.print(", ");

                  System.out.print(array[i]);
               }
            }
         }
         else if(Reflector.PRIMITIVE_LONG.equals(name) == true)
         {
            final long[] array = (long[]) object;
            final int length = array.length;

            if(length > 0)
            {
               System.out.print(array[0]);

               for(int i = 1; i < length; i++)
               {
                  System.out.print(", ");

                  System.out.print(array[i]);
               }
            }
         }
         else if(Reflector.PRIMITIVE_SHORT.equals(name) == true)
         {
            final short[] array = (short[]) object;
            final int length = array.length;

            if(length > 0)
            {
               System.out.print(array[0]);

               for(int i = 1; i < length; i++)
               {
                  System.out.print(", ");

                  System.out.print(array[i]);
               }
            }
         }
      }
      else
      {
         final Object[] array = (Object[]) object;
         final int length = array.length;

         if(length > 0)
         {
            Debug.printObject(array[0]);

            for(int i = 1; i < length; i++)
            {
               System.out.print(", ");

               Debug.printObject(array[i]);
            }
         }
      }

      System.out.print(']');
   }

   /**
    * Print a trace
    * 
    * @param debugLevel
    *           Debug level
    * @param throwable
    *           Trace to print
    * @param start
    *           Offset to start reading the trace
    */
   private static void printTrace(final DebugLevel debugLevel, Throwable throwable, int start)
   {
      // Level test
      if(debugLevel.getLevel() > Debug.debugLevel.getLevel())
      {
         return;
      }

      // Print trace
      StackTraceElement[] stackTraceElements;
      StackTraceElement stackTraceElement;
      int length;

      while(throwable != null)
      {
         System.out.println(throwable.getMessage());
         System.out.println(throwable.getLocalizedMessage());
         System.out.println(throwable.toString());

         stackTraceElements = throwable.getStackTrace();
         length = stackTraceElements.length;

         for(int index = start; index < length; index++)
         {
            stackTraceElement = stackTraceElements[index];

            System.out.print('\t');
            System.out.print(stackTraceElement.getClassName());
            System.out.print('.');
            System.out.print(stackTraceElement.getMethodName());
            System.out.print(" at ");
            System.out.println(stackTraceElement.getLineNumber());
         }

         throwable = throwable.getCause();

         if(throwable != null)
         {
            System.out.println("Caused by : ");
         }

         start = 0;
      }
   }

   /**
    * Actual debug level
    * 
    * @return Actual debug level
    */
   public static DebugLevel getDebugLevel()
   {
      return Debug.debugLevel;
   }

   /**
    * Print information to know which part of code, called a method.<br>
    * Short version of {@link #printTrace(DebugLevel, Object...)}
    * 
    * @param debugLevel
    *           Debug level
    */
   public static void printCalledFrom(final DebugLevel debugLevel)
   {
      // Level test
      if(debugLevel.getLevel() > Debug.debugLevel.getLevel())
      {
         return;
      }

      Debug.REENTRANT_LOCK.lock();

      try
      {
         final Throwable throwable = new Throwable();
         final StackTraceElement[] traces = throwable.getStackTrace();
         final StackTraceElement stackTraceElement = traces[2];

         Debug.printMessage(debugLevel, traces[1], "Called from ", stackTraceElement.getClassName(), '.', stackTraceElement.getMethodName(), " at ", stackTraceElement.getLineNumber());
      }
      finally
      {
         Debug.REENTRANT_LOCK.unlock();
      }
   }

   /**
    * Print an error with its trace
    * 
    * @param error
    *           Error to print
    * @param message
    *           Message information
    */
   public static void printError(final Error error, final Object... message)
   {
      // Level test
      if(DebugLevel.ERROR.getLevel() > Debug.debugLevel.getLevel())
      {
         return;
      }

      Debug.REENTRANT_LOCK.lock();

      try
      {
         if((message != null) && (message.length > 0))
         {
            Debug.printMessage(DebugLevel.ERROR, (new Throwable()).getStackTrace()[1], message);
         }

         System.out.println("<-- ERROR");

         Debug.printTrace(DebugLevel.ERROR, error, 0);

         System.out.println("ERROR -->");
      }
      finally
      {
         Debug.REENTRANT_LOCK.unlock();
      }
   }

   /**
    * Print an exception with its trace
    * 
    * @param exception
    *           Exception to print
    * @param message
    *           Message information
    */
   public static void printException(final Exception exception, final Object... message)
   {
      // Level test
      if(DebugLevel.WARNING.getLevel() > Debug.debugLevel.getLevel())
      {
         return;
      }

      Debug.REENTRANT_LOCK.lock();

      try
      {
         if((message != null) && (message.length > 0))
         {
            Debug.printMessage(DebugLevel.WARNING, (new Throwable()).getStackTrace()[1], message);
         }

         System.out.println("<-- EXCEPTION");

         Debug.printTrace(DebugLevel.WARNING, exception, 0);

         System.out.println("EXCEPTION -->");
      }
      finally
      {
         Debug.REENTRANT_LOCK.unlock();
      }
   }

   /**
    * Print some information
    * 
    * @param debugLevel
    *           Debug level
    * @param message
    *           Message to print
    */
   public static void println(final DebugLevel debugLevel, final Object... message)
   {
      // Level test
      if(debugLevel.getLevel() > Debug.debugLevel.getLevel())
      {
         return;
      }

      Debug.REENTRANT_LOCK.lock();

      try
      {
         Debug.printMessage(debugLevel, (new Throwable()).getStackTrace()[1], message);
      }
      finally
      {
         Debug.REENTRANT_LOCK.unlock();
      }
   }

   /**
    * Print a mark
    * 
    * @param debugLevel
    *           Debug level
    * @param mark
    *           Mark to print
    */
   public static void printMark(final DebugLevel debugLevel, final String mark)
   {
      // Level test
      if(debugLevel.getLevel() > Debug.debugLevel.getLevel())
      {
         return;
      }

      Debug.REENTRANT_LOCK.lock();

      try
      {
         final int length = mark.length() + 12;
         final char[] headers = new char[length];

         for(int i = 0; i < length; i++)
         {
            headers[i] = '*';
         }

         final String header = new String(headers);

         final StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[1];

         Debug.printMessage(debugLevel, stackTraceElement, header);
         Debug.printMessage(debugLevel, stackTraceElement, "***   ", mark, "   ***");
         Debug.printMessage(debugLevel, stackTraceElement, header);
      }
      finally
      {
         Debug.REENTRANT_LOCK.unlock();
      }
   }

   /**
    * Print a to do message
    * 
    * @param message
    *           Message to print
    */
   public static void printTodo(final Object... message)
   {
      // Level test
      if((DebugLevel.VERBOSE.getLevel() > Debug.debugLevel.getLevel()) || (message == null))
      {
         return;
      }

      Debug.REENTRANT_LOCK.lock();

      try
      {
         final Object[] todoMessage = new Object[message.length + 2];

         todoMessage[0] = "TODO --- ";
         System.arraycopy(message, 0, todoMessage, 1, message.length);
         todoMessage[todoMessage.length - 1] = " --- TODO";

         Debug.printMessage(DebugLevel.VERBOSE, (new Throwable()).getStackTrace()[1], todoMessage);
      }
      finally
      {
         Debug.REENTRANT_LOCK.unlock();
      }
   }

   /**
    * Print an informative trace (To know the execution stack)
    * 
    * @param debugLevel
    *           Debug level
    * @param message
    *           Message to print
    */
   public static void printTrace(final DebugLevel debugLevel, final Object... message)
   {
      // Level test
      if(debugLevel.getLevel() > Debug.debugLevel.getLevel())
      {
         return;
      }

      Debug.REENTRANT_LOCK.lock();

      try
      {
         final Throwable throwable = new Throwable();

         if((message != null) && (message.length > 0))
         {
            Debug.printMessage(debugLevel, throwable.getStackTrace()[1], message);
         }

         System.out.println("<-- TRACE");

         Debug.printTrace(debugLevel, throwable, 1);

         System.out.println("TRACE -->");
      }
      finally
      {
         Debug.REENTRANT_LOCK.unlock();
      }
   }

   /**
    * Change debug level
    * 
    * @param debugLevel
    *           New debug level
    */
   public static void setDebugLevel(final DebugLevel debugLevel)
   {
      if(debugLevel == null)
      {
         throw new NullPointerException("debugLevel musn't be null");
      }

      Debug.debugLevel = debugLevel;
   }

   /**
    * To avoid instance
    */
   private Debug()
   {
   }
}