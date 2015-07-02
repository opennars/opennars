/**
 * Project : JHelpUtil<br>
 * Package : jhelp.util.io<br>
 * Class : InputStreamCopy<br>
 * Date : 15 sept. 2010<br>
 * By JHelp
 */
package jhelp.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Input stream that write what it reads in output stream at same time<br>
 * <br>
 * Last modification : 15 sept. 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class InputStreamCopy
      extends InputStream
{
   /** Stream to read */
   private InputStream  inputStream;
   /** Stream where write */
   private OutputStream outputStream;

   /**
    * Constructs InputStreamCopy
    * 
    * @param inputStream
    *           Stream to read
    * @param outputStream
    *           Stream where copy
    */
   public InputStreamCopy(final InputStream inputStream, final OutputStream outputStream)
   {
      this.inputStream = inputStream;
      this.outputStream = outputStream;
   }

   /**
    * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking by
    * the next invocation of a method for this input stream. The next invocation might be the same thread or another thread. A
    * single read or skip of this many bytes will not block, but may read or skip fewer bytes.
    * <p>
    * Note that while some implementations of {@code InputStream} will return the total number of bytes in the stream, many will
    * not. It is never correct to use the return value of this method to allocate a buffer intended to hold all data in this
    * stream.
    * <p>
    * A subclass' implementation of this method may choose to throw an {@link IOException} if this input stream has been closed
    * by invoking the {@link #close()} method.
    * <p>
    * The {@code available} method for class {@code InputStream} always returns {@code 0}.
    * <p>
    * This method should be overridden by subclasses.
    * 
    * @return an estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking or
    *         {@code 0} when it reaches the end of the input stream.
    * @exception IOException
    *               if an I/O error occurs.
    * @see InputStream#available()
    */
   @Override
   public int available() throws IOException
   {
      return this.inputStream.available();
   }

   /**
    * Close the streams
    * 
    * @throws IOException
    *            On closing issue
    * @see InputStream#close()
    */
   @Override
   public void close() throws IOException
   {
      this.outputStream.flush();
      this.outputStream.close();
      this.outputStream = null;

      this.inputStream.close();
      this.inputStream = null;
   }

   /**
    * Mark aren't manage here, so do nothing
    * 
    * @param readlimit
    *           Ignore here
    * @see InputStream#mark(int)
    */
   @Override
   public synchronized void mark(final int readlimit)
   {
   }

   /**
    * Mark aren't manage, so not supported
    * 
    * @return {@code false}
    * @see InputStream#markSupported()
    */
   @Override
   public boolean markSupported()
   {
      return false;
   }

   /**
    * Read one byte (Byte is also write in the output stream)
    * 
    * @return Byte read
    * @throws IOException
    *            On read/write issue
    * @see InputStream#read()
    */
   @Override
   public int read() throws IOException
   {
      final int read = this.inputStream.read();

      if(read >= 0)
      {
         this.outputStream.write(read);
         this.outputStream.flush();
      }

      return read;
   }

   /**
    * Read bytes and copy them in an array. Remember that there also copy in the output stream
    * 
    * @param b
    *           Array to fill
    * @return Number of bytes read
    * @throws IOException
    *            On read/write issue
    * @see InputStream#read(byte[])
    */
   @Override
   public int read(final byte[] b) throws IOException
   {
      final int read = this.inputStream.read(b);

      if(read >= 0)
      {
         this.outputStream.write(b, 0, read);
         this.outputStream.flush();
      }

      return read;
   }

   /**
    * Read bytes and copy them in an array. Remember that there also copy in the output stream
    * 
    * @param b
    *           Array to fill
    * @param off
    *           Offset to start to read
    * @param len
    *           Number of bytes to read
    * @return Number of bytes read
    * @throws IOException
    *            On read/write issue
    * @see InputStream#read(byte[], int, int)
    */
   @Override
   public int read(final byte[] b, final int off, final int len) throws IOException
   {
      final int read = this.inputStream.read(b, off, len);

      if(read >= 0)
      {
         this.outputStream.write(b, off, read);
         this.outputStream.flush();
      }

      return read;
   }

   /**
    * Mark aren't manage, so do nothing
    * 
    * @throws IOException
    *            Not throw
    * @see InputStream#reset()
    */
   @Override
   public synchronized void reset() throws IOException
   {
   }

   /**
    * Skip several bytes. The skipped bytes are also copy in the output stream
    * 
    * @param n
    *           Number of bytes to skip
    * @return Real number skipped bytes
    * @throws IOException
    *            On read/write issue
    * @see InputStream#skip(long)
    */
   @Override
   public long skip(long n) throws IOException
   {
      int read = 0;
      long count = 0;

      while((n > 0) && (read >= 0))
      {
         read = this.inputStream.read();

         if(read >= 0)
         {
            this.outputStream.write(read);

            n--;
            count++;
         }
      }

      this.outputStream.flush();

      return count;
   }
}