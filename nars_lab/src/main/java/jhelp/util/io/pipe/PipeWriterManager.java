package jhelp.util.io.pipe;

import jhelp.util.debug.Debug;
import jhelp.util.io.Binarizable;
import jhelp.util.io.ByteArray;
import jhelp.util.list.Queue;

/**
 * Hellper for write messages in pipe.<br>
 * It manage a queue of message, so you don't have to care about waiting writer is ready, its done for you<br>
 * <br>
 * 1) What is a pipe ?<br>
 * <br>
 * A pipe is a way to commuicate between two differents java application, that not neccessary use the same main process, but
 * runs on same computer, or two computer that shares a same shared directory.<br>
 * A pipe is here simply a one way communication theire a one, and only one, writer (The guys who write/send messages) and one,
 * and only one, reader (The guy who read/receive messages)<br>
 * For a two ways communication, have to create two pipes, one for A =&gt; B and one for B =&gt; A..<br>
 * The implementation here is 100% Java, and compatible in any system have file system with at least an area readable/writable
 * by the program.<br>
 * The implementation use a directory and temporary files for exchange message, so no need socket, and avoid in that way proxy
 * issue and other network issue.<br>
 * The security is the same as the security of the used directory for echange.<br>
 * <br>
 * 2) How to use pipes ? <br>
 * <br>
 * Choose a deticated directory, this directory will be change durring time, so don't use this directory for other stuff. Then
 * create the writer in application that will send messages, and reader in application that receive messages. <br>
 * If you create several readers (For same pipe), you will notice some lost messages (That why only one reader is highly
 * recommended) <br>
 * If you create several writers (For same pipe), you will notice some lost messages and/or some writing issue (That why only
 * one writer is highly recommended) <br>
 * You can transfer byte[] or Binarizable. You will find a helper for manage sending message in queue and one helper for receive
 * messages in listener.
 * 
 * @author JHelp
 * @param <BINARIZABLE>
 *           Message type
 */
public class PipeWriterManager<BINARIZABLE extends Binarizable>
      implements Runnable
{
   /** Lock for synchronization */
   private final Object             lock = new Object();
   /** Pipe writer to use */
   private final PipeWriter         pipeWriter;
   /** Queue of message to write */
   private final Queue<BINARIZABLE> queueMessages;
   /** Thread for write messages */
   private Thread                   thread;
   /** Indicate if waiting */
   private boolean                  waiting;

   /**
    * Create a new instance of PipeWriterManager
    * 
    * @param pipeWriter
    *           Pipe writer to use
    */
   public PipeWriterManager(final PipeWriter pipeWriter)
   {
      if(pipeWriter == null)
      {
         throw new NullPointerException("pipeWriter musn't be null");
      }

      this.waiting = false;
      this.pipeWriter = pipeWriter;
      this.queueMessages = new Queue<BINARIZABLE>();
   }

   /**
    * Indicates if manager is alive
    * 
    * @return {@code true} if manager is alive
    */
   public boolean isManaging()
   {
      return this.pipeWriter.readyToWrite();
   }

   /**
    * Do the pipe writer manager task <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @see Runnable#run()
    */
   @Override
   public void run()
   {
      while(this.thread != null)
      {
         synchronized(this.lock)
         {
            while(this.queueMessages.isEmpty() == true)
            {
               this.waiting = true;
               try
               {
                  this.lock.wait(16386);
               }
               catch(final Exception exception)
               {
               }
               this.waiting = false;
               if(this.thread == null)
               {
                  return;
               }
            }
         }

         if(this.thread == null)
         {
            return;
         }

         BINARIZABLE message = null;
         synchronized(this.lock)
         {
            message = this.queueMessages.outQueue();
         }

         try
         {
            final ByteArray byteArray = new ByteArray();
            byteArray.writeBinarizable(message);
            this.pipeWriter.write(byteArray.toArray());
         }
         catch(final Exception exception)
         {
            Debug.printException(exception, "Failed to write message : ", message);
         }
      }
   }

   /**
    * Stop properly the thread of pipe management thread
    */
   public void stopManagement()
   {
      this.thread = null;
      synchronized(this.lock)
      {
         if(this.waiting == true)
         {
            this.lock.notify();
         }
      }
      this.pipeWriter.stopWrite();
   }

   /**
    * Put a message in a queue for write, the method returns immediately. It start the management thread if need
    * 
    * @param message
    *           Message to write
    */
   public void writeMessage(final BINARIZABLE message)
   {
      if(message == null)
      {
         throw new NullPointerException("message musn't be null");
      }

      this.pipeWriter.restartWrite();
      synchronized(this.lock)
      {
         this.queueMessages.inQueue(message);
      }

      if(this.thread == null)
      {
         this.thread = new Thread(this);
         this.thread.start();
      }
      else
      {
         synchronized(this.lock)
         {
            if(this.waiting == true)
            {
               this.lock.notify();
            }
         }
      }
   }
}