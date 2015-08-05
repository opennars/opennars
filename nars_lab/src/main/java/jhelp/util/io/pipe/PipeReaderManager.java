package jhelp.util.io.pipe;

import jhelp.util.debug.Debug;
import jhelp.util.io.Binarizable;
import jhelp.util.io.ByteArray;
import jhelp.util.list.Triplet;
import jhelp.util.thread.ThreadManager;
import jhelp.util.thread.ThreadedSimpleTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper for read {@link Binarizable} throw listener (Call back when new message arrive) in a pipe <br>
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
 *           Type to read
 */
public class PipeReaderManager<BINARIZABLE extends Binarizable>
{
   /**
    * Task in thread do by the pipe
    * 
    * @author JHelp
    */
   class TaskReadPipe
         extends ThreadedSimpleTask<PipeReader>
   {
      /**
       * Create a new instance of TaskReadPipe
       */
      TaskReadPipe()
      {
      }

      /**
       * Do the read task in pipe <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param parameter
       *           Pipe reader to use
       * @see jhelp.util.thread.ThreadedSimpleTask#doSimpleAction(Object)
       */
      @Override
      protected void doSimpleAction(final PipeReader parameter)
      {
         try
         {
            final ByteArray byteArray = new ByteArray();
            byteArray.write(parameter.read());
            final BINARIZABLE binarizable = byteArray.readBinarizable(PipeReaderManager.this.binarizebleClass);
            if(binarizable != null)
            {
               PipeReaderManager.this.signalToListenersMessage(binarizable);
            }
         }
         catch(final Exception exception)
         {
            Debug.printException(exception);
         }

         synchronized(PipeReaderManager.this.alive)
         {
            if(PipeReaderManager.this.alive.get() == true)
            {
               ThreadManager.THREAD_MANAGER.doThread(this, parameter);
            }
         }
      }
   }

   /**
    * Task to signal to a listener that a new message arrive
    * 
    * @author JHelp
    * @param <B>
    *           Message type
    */
   class TaskSignalMessage<B extends Binarizable>
         extends ThreadedSimpleTask<Triplet<PipeReaderManagerListener<B>, PipeReader, B>>
   {
      /**
       * Create a new instance of TaskSignalMessage
       */
      TaskSignalMessage()
      {
      }

      /**
       * Signal to a listener the new message <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param parameter
       *           Triplet of listener to alert, used piper reader and the message itself
       * @see jhelp.util.thread.ThreadedSimpleTask#doSimpleAction(Object)
       */
      @Override
      protected void doSimpleAction(final Triplet<PipeReaderManagerListener<B>, PipeReader, B> parameter)
      {
         parameter.element1.receiveMessageFromPipe(parameter.element2, parameter.element3);
      }
   }

   /** Registered listeners */
   private final List<PipeReaderManagerListener<BINARIZABLE>> listeners;
   /** Pipe reader to use */
   private PipeReader                                         pipeReader;
   /** Task for signal a message */
   private final TaskSignalMessage<BINARIZABLE>               taskSignalListener = new TaskSignalMessage<BINARIZABLE>();
   /** Task for read messages from pipe */
   private final TaskReadPipe                                 taskReadPipe;
   /** Indicates if manager is alive */
   final AtomicBoolean                                        alive               = new AtomicBoolean(false);
   /** Type of binarizable to read */
   Class<BINARIZABLE>                                         binarizebleClass;

   /**
    * Create a new instance of PipeReaderManager
    * 
    * @param binarizebleClass
    *           Type of binarizable to read
    * @param pipeReader
    *           Pipe reader to use
    */
   public PipeReaderManager(final Class<BINARIZABLE> binarizebleClass, final PipeReader pipeReader)
   {
      if(binarizebleClass == null)
      {
         throw new NullPointerException("binarizebleClass musn't be null");
      }

      if(pipeReader == null)
      {
         throw new NullPointerException("pipeReader musn't be null");
      }

      this.binarizebleClass = binarizebleClass;
      this.taskReadPipe = new TaskReadPipe();
      this.listeners = new ArrayList<PipeReaderManagerListener<BINARIZABLE>>();
   }

   /**
    * Signal to listeners a new message is read
    * 
    * @param message
    *           Message read
    */
   void signalToListenersMessage(final BINARIZABLE message)
   {
      synchronized(this.listeners)
      {
         for(final PipeReaderManagerListener<BINARIZABLE> listener : this.listeners)
         {
            ThreadManager.THREAD_MANAGER.doThread(this.taskSignalListener, new Triplet<PipeReaderManagerListener<BINARIZABLE>, PipeReader, BINARIZABLE>(listener, this.pipeReader, message));
         }
      }
   }

   /**
    * Indicates if manager still reading
    * 
    * @return {@code true} if manager still reading
    */
   public boolean isManaging()
   {
      synchronized(this.alive)
      {
         return this.alive.get();
      }
   }

   /**
    * Register a listener of receive messages
    * 
    * @param listener
    *           Listner to register
    */
   public void registerPipeReaderManagerListener(final PipeReaderManagerListener<BINARIZABLE> listener)
   {
      if(listener == null)
      {
         throw new NullPointerException("listener musn't be null");
      }

      synchronized(this.listeners)
      {
         this.listeners.add(listener);
      }
   }

   /**
    * Start reading,have to be called at least one time, for reading happen
    */
   public void startManagement()
   {
      synchronized(this.alive)
      {
         if(this.alive.get() == true)
         {
            return;
         }

         this.alive.set(true);
      }

      this.pipeReader.restartRead();
      ThreadManager.THREAD_MANAGER.doThread(this.taskReadPipe, this.pipeReader);
   }

   /**
    * Stop reading pipe, call it for stop prpoerly threads.<br>
    * Can be restart with {@link #startManagement()}
    */
   public void stopManagement()
   {
      synchronized(this.alive)
      {
         if(this.alive.get() == false)
         {
            return;
         }

         this.alive.set(false);
      }

      this.pipeReader.stopRead();
   }

   /**
    * Unregister a messgaes listener
    * 
    * @param listener
    *           Listener to unregister
    */
   public void unregisterPipeReaderManagerListener(final PipeReaderManagerListener<BINARIZABLE> listener)
   {
      synchronized(this.listeners)
      {
         this.listeners.remove(listener);
      }
   }
}