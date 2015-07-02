package jhelp.util.io.pipe;

import jhelp.util.text.UtilText;

/**
 * Exception happen in pipe management. <br>
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
public class PipeException
      extends Exception
{
   /**
    * Create a new instance of PipeException
    * 
    * @param message
    *           Exception message
    */
   public PipeException(final Object... message)
   {
      super(UtilText.concatenate(message));
   }

   /**
    * Create a new instance of PipeException
    * 
    * @param cause
    *           Reason of the exception
    * @param message
    *           Exception message
    */
   public PipeException(final Throwable cause, final Object... message)
   {
      super(UtilText.concatenate(message), cause);
   }
}