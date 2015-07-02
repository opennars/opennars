package jhelp.util.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Stream for read a {@link String}
 * 
 * @author JHelp
 */
public class StringInputStream
      extends InputStream
{
   /** Stream for read {@link String} bytes */
   private final ByteArrayInputStream byteArrayInputStream;

   /**
    * Create a new instance of StringInputStream
    * 
    * @param string
    *           {@link String} to read
    */
   public StringInputStream(final String string)
   {
      final int length = string.length();
      final char[] characters = string.toCharArray();
      final byte[] array = new byte[length];

      for(int i = 0; i < length; i++)
      {
         array[i] = (byte) (characters[i] & 0xFF);
      }

      this.byteArrayInputStream = new ByteArrayInputStream(array);
   }

   /**
    * Number of available bytes <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Number of available bytes
    * @throws IOException
    *            On reading issue
    * @see InputStream#available()
    */
   @Override
   public int available() throws IOException
   {
      return this.byteArrayInputStream.available();
   }

   /**
    * Close the stream <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @throws IOException
    *            On closing issue
    * @see InputStream#close()
    */
   @Override
   public void close() throws IOException
   {
      this.byteArrayInputStream.close();
   }

   /**
    * Mark current reading position <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param readlimit
    *           Read limit
    * @see InputStream#mark(int)
    */
   @Override
   public synchronized void mark(final int readlimit)
   {
      this.byteArrayInputStream.mark(readlimit);
   }

   /**
    * Indicates if mark are supported <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return {@code true} if mark are supported
    * @see InputStream#markSupported()
    */
   @Override
   public boolean markSupported()
   {
      return this.byteArrayInputStream.markSupported();
   }

   /**
    * Read one byte <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Byte read or -1 if reach stream end
    * @throws IOException
    *            On reading issue
    * @see InputStream#read()
    */
   @Override
   public int read() throws IOException
   {
      return this.byteArrayInputStream.read();
   }

   /**
    * Read some bytes and fill an array.<br>
    * Same as {@link #read(byte[], int, int) read(b, 0, b.length)} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param b
    *           Array to fill
    * @return Number of read bytes or -1 if reach stream end
    * @throws IOException
    *            On reading issue
    * @see InputStream#read(byte[])
    */
   @Override
   public int read(final byte[] b) throws IOException
   {
      return this.byteArrayInputStream.read(b);
   }

   /**
    * Read some bytes and fill a part of array <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param array
    *           Array to fill
    * @param offset
    *           Offset to start write in array
    * @param length
    *           Number of byte to read
    * @return Number of read bytes or -1 if reach stream end
    * @throws IOException
    *            On reading issue
    * @see InputStream#read(byte[], int, int)
    */
   @Override
   public int read(final byte[] array, final int offset, final int length) throws IOException
   {
      return this.byteArrayInputStream.read(array, offset, length);
   }

   /**
    * Reset the stream <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @throws IOException
    *            On resting issue
    * @see InputStream#reset()
    */
   @Override
   public synchronized void reset() throws IOException
   {
      this.byteArrayInputStream.reset();
   }

   /**
    * Skip a number of bytes <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param n
    *           Number of bytes to skip
    * @return Number of skipped bytes
    * @throws IOException
    *            On skipping issue
    * @see InputStream#skip(long)
    */
   @Override
   public long skip(final long n) throws IOException
   {
      return this.byteArrayInputStream.skip(n);
   }
}