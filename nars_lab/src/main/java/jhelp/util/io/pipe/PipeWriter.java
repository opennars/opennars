package jhelp.util.io.pipe;

import jhelp.util.Utilities;
import jhelp.util.io.Binarizable;
import jhelp.util.io.ByteArray;
import jhelp.util.io.UtilIO;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Write messages in pipe<br>
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
 */
public class PipeWriter
      extends PipeCommons
{
   /** Indicates if writer still able to write */
   private boolean    alive;
   /** Pipe directory */
   private final File pipeDirectory;
   /** Pipe size */
   private int        pipeSize;

   /**
    * Create a new instance of PipeWriter with default size.<br>
    * Same as {@link PipeWriter#PipeWriter(File, int) PipeWriter(pipeDirectory, PipeCommons.DEFAULT_PIPE_ZIZE}
    * 
    * @param pipeDirectory
    *           Pipe directory
    */
   public PipeWriter(final File pipeDirectory)
   {
      this(pipeDirectory, PipeCommons.DEFAULT_PIPE_SIZE);
   }

   /**
    * Create a new instance of PipeWriter.<br>
    * The size is only use on first pipe writing, when pipe already exists, the size is ingored
    * 
    * @param pipeDirectory
    *           Pipe directory
    * @param pipeSize
    *           Pipe size if pipe not already exists
    */
   public PipeWriter(final File pipeDirectory, final int pipeSize)
   {
      if(pipeDirectory == null)
      {
         throw new NullPointerException("pipeDirectory musn't be null");
      }

      this.alive = true;
      this.pipeDirectory = pipeDirectory;
      this.pipeSize = Math.max(PipeCommons.MINIMUM_PIPE_SIZE, pipeSize);
   }

   /**
    * Indicates if write is allowed.<br>
    * To allow writing, use {@link #restartWrite()}
    * 
    * @return {@code true} if write is allow
    */
   public boolean readyToWrite()
   {
      return this.alive;
   }

   /**
    * Activate writing, do nothing if already active
    */
   public void restartWrite()
   {
      this.alive = true;
   }

   /**
    * Stop actual writing (if one) and dissalow future writing (To reactivate, call {@link #restartWrite()}
    */
   public void stopWrite()
   {
      this.alive = false;
   }

   /**
    * Write a binarizable in pipe
    * 
    * @param <B>
    *           Message type
    * @param binarizable
    *           Message to write
    * @throws PipeException
    *            On writing issue
    */
   public <B extends Binarizable> void write(final B binarizable) throws PipeException
   {
      final ByteArray byteArray = new ByteArray();
      byteArray.writeBinarizable(binarizable);
      this.write(byteArray.toArray());
   }

   /**
    * Write a message in the pipe. If the pipe is full, it will wait until reader free space.<br>
    * It possible to stop bruttaly the witing with {@link #stopWrite()}
    * 
    * @param message
    *           Message to write
    * @throws PipeException
    *            On wrting issue
    */
   public synchronized void write(final byte[] message) throws PipeException
   {
      final File config = new File(this.pipeDirectory, PipeCommons.FILE_CONFIG);

      if(config.exists() == false)
      {
         PipeCommons.writeFileInteger(config, this.pipeSize);
      }
      else
      {
         this.pipeSize = PipeCommons.readFileInteger(config);
      }

      final File write = new File(this.pipeDirectory, PipeCommons.FILE_WRITE);
      int next = 0;

      if(write.exists() == true)
      {
         next = PipeCommons.readFileInteger(write) + 1;
      }

      if(next >= this.pipeSize)
      {
         next = 0;
      }

      final File file = new File(this.pipeDirectory, String.valueOf(next));
      while(file.exists() == true)
      {
         Utilities.sleep(128);
         if(this.alive == false)
         {
            return;
         }
      }

      if(UtilIO.createFile(file) == false)
      {
         throw new PipeException("Can't create file ", file.getAbsolutePath());
      }

      FileOutputStream fileOutputStream = null;
      try
      {
         fileOutputStream = new FileOutputStream(file);
         fileOutputStream.write(message);
      }
      catch(final Exception exception)
      {
         throw new PipeException(exception, "Can't create file ", file.getAbsolutePath());
      }
      finally
      {
         if(fileOutputStream != null)
         {
            try
            {
               fileOutputStream.flush();
            }
            catch(final Exception exception)
            {
            }

            try
            {
               fileOutputStream.close();
            }
            catch(final Exception exception)
            {
            }
         }
      }

      PipeCommons.writeFileInteger(write, next);
   }
}