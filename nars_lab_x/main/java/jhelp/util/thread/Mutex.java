package jhelp.util.thread;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.text.UtilText;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mutex (Mutual Exclusion) for synchronization
 * 
 * @author JHelp
 */
public class Mutex
{
   /** For enable/disable mutex debugging */
   private static final boolean       DEBUG   = false;
   /** Next mutex ID */
   private static final AtomicInteger NEXT_ID = new AtomicInteger();
   /** Mutex ID */
   private final int                  id;
   /** Indicates if mutex already locked */
   private boolean                    isLocked;
   /** Last mutex information (For printing) */
   private String                     lastMutexInformation;
   /** Lock linked to the mutex */
   private final Object               lock    = new Object();
   /** Thread ID that have locked the mutex */
   private String                     lockerThreadID;

   /**
    * Create a new instance of Mutex
    */
   public Mutex()
   {
      synchronized(Mutex.NEXT_ID)
      {
         this.id = Mutex.NEXT_ID.getAndIncrement();
      }

      this.isLocked = false;
   }

   /**
    * Compute the mutex information with the caller stack trace
    * 
    * @param traceCaller
    *           Caller stack trace
    * @return Information created
    */
   private String createMutexInformation(final StackTraceElement traceCaller)
   {
      return UtilText.concatenate(" MUTEX ", this.id, " : ", traceCaller.getClassName(), '.', traceCaller.getMethodName(), " at ", traceCaller.getLineNumber(), " : ", this.createThreadID());
   }

   /**
    * Create current thread ID
    * 
    * @return Current thread ID
    */
   private String createThreadID()
   {
      final Thread thread = Thread.currentThread();

      return UtilText.concatenate(thread.getName(), ':', thread.getId());
   }

   /**
    * Mutex ID
    * 
    * @return Mutex ID
    */
   public final int getID()
   {
      return this.id;
   }

   /**
    * Lock the mutex
    */
   @SuppressWarnings("unused")
   public final void lock()
   {
      String info;

      if(Mutex.DEBUG == true)
      {
         info = this.createMutexInformation((new Throwable()).getStackTrace()[1]);
      }

      synchronized(this.lock)
      {
         if(this.isLocked == true)
         {
            if(Mutex.DEBUG == true)
            {
               Debug.println(DebugLevel.DEBUG, info, " have to wait, ", this.lastMutexInformation, " have the lock");
            }

            try
            {
               this.lock.wait();
            }
            catch(final Exception exception)
            {
            }
         }

         this.isLocked = true;

         if(Mutex.DEBUG == true)
         {
            this.lockerThreadID = this.createThreadID();
            this.lastMutexInformation = info;
            Debug.println(DebugLevel.DEBUG, info, " take the lock");
         }
      }
   }

   /**
    * String representation <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return String representation
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      return UtilText.concatenate("MUTEX ", this.id);
   }

   /**
    * Unlock the mutex
    */
   @SuppressWarnings("unused")
   public final void unlock()
   {
      String info;

      if(Mutex.DEBUG == true)
      {
         info = this.createMutexInformation((new Throwable()).getStackTrace()[1]);
      }

      synchronized(this.lock)
      {
         if(this.isLocked == true)
         {
            if(Mutex.DEBUG == true)
            {
               Debug.println(DebugLevel.DEBUG, info, " release lock and the owner was ", this.lastMutexInformation);

               if(this.createThreadID().equals(this.lockerThreadID) == false)
               {
                  Debug.println(DebugLevel.WARNING, "Not the same thread that take the lock and release it !");
               }
            }

            this.lock.notify();
         }

         this.isLocked = false;
      }

      Thread.yield();
   }
}