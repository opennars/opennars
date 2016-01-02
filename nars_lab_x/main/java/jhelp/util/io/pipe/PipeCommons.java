package jhelp.util.io.pipe;

import jhelp.util.io.UtilIO;

/**
 * Pipe common operations, <br>
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
public class PipeCommons
{
   /** Default pipe size (Don't put to low, else it may wait long in write) */
   public static final int    DEFAULT_PIPE_SIZE = 1024;
   /** Config pipe file (To know what is actual pipe size) */
   public static final String FILE_CONFIG       = ".config";
   /** Read pipe file (To know witch file to read next) */
   public static final String FILE_READ         = ".read";
   /** Write pipe file (To know witch file to write next) */
   public static final String FILE_WRITE        = ".write";
   /** Minimum pipe size (Don't put to low, else it may wait long in write) */
   public static final int    MINIMUM_PIPE_SIZE = 128;

   /**
    * Read a file contains a integer (Write by {@link #writeFileInteger(File, int)})
    * 
    * @param file
    *           File to read
    * @return Integer read
    * @throws PipeException
    *            On reading issue
    */
   static int readFileInteger(final File file) throws PipeException
   {
      BufferedReader bufferedReader = null;

      try
      {
         bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

         return Integer.parseInt(bufferedReader.readLine());
      }
      catch(final Exception exception)
      {
         throw new PipeException(exception, "Failed to get integer from ", file.getAbsolutePath());
      }
      finally
      {
         if(bufferedReader != null)
         {
            try
            {
               bufferedReader.close();
            }
            catch(final Exception exception)
            {
            }
         }
      }
   }

   /**
    * Write an integer in a file, can be read by {@link #readFileInteger(File)}
    * 
    * @param file
    *           File where write
    * @param integer
    *           Integer to write in file
    * @throws PipeException
    *            On writing issue
    */
   static void writeFileInteger(final File file, final int integer) throws PipeException
   {
      BufferedWriter bufferedWriter = null;

      try
      {
         if(UtilIO.createFile(file) == false)
         {
            throw new IOException("Can't create file : " + file.getAbsolutePath());
         }

         bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
         bufferedWriter.write(String.valueOf(integer));
         bufferedWriter.newLine();
      }
      catch(final Exception exception)
      {
         throw new PipeException(exception, "Failed to set ", integer, " to ", file.getAbsolutePath());
      }
      finally
      {
         if(bufferedWriter != null)
         {
            try
            {
               bufferedWriter.flush();
            }
            catch(final Exception exception)
            {
            }

            try
            {
               bufferedWriter.close();
            }
            catch(final Exception exception)
            {
            }
         }
      }
   }
}