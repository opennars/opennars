package jhelp.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream that you can know the reading index (Number of bytes actually read) at any time
 * 
 * @author JHelp
 */
public class IndexedInputStream
      extends InputStream
{
   /** Indicates if we are at the end of the stream */
   private boolean           finish;
   /** Actual index */
   private long              index;
   /** Stream to read */
   private final InputStream inputStream;

   /**
    * Create a new instance of IndexedInputStream
    * 
    * @param inputStream
    *           Stream to read
    */
   public IndexedInputStream(final InputStream inputStream)
   {
      if(inputStream == null)
      {
         throw new NullPointerException("inputStream musn't be null");
      }

      this.inputStream = inputStream;
      this.index = 0;
      this.finish = false;
   }

   /**
    * Current index (Number of bytes actually read)
    * 
    * @return Current index
    */
   public long getCurrentIndex()
   {
      return this.index;
   }

   /**
    * Indicates if we are at the end of the stream
    * 
    * @return {@code true} if we are at the end of the stream
    */
   public boolean isFinish()
   {
      return this.finish;
   }

   /**
    * Read one byte on the stream <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return The byte read or -1 if we are at the end of the stream
    * @throws IOException
    *            On reading issue
    * @see InputStream#read()
    */
   @Override
   public int read() throws IOException
   {
      if(this.finish == true)
      {
         return -1;
      }

      final int read = this.inputStream.read();

      if(read < 0)
      {
         this.finish = true;

         return read;
      }

      this.index++;

      return read;
   }

   /**
    * Read some bytes on trying to fill the given array <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param b
    *           Array to fill
    * @return Number of bytes read or -1 if we are at the end of the stream
    * @throws IOException
    *            On reading issue
    * @see InputStream#read(byte[])
    */
   @Override
   public int read(final byte[] b) throws IOException
   {
      if(this.finish == true)
      {
         return -1;
      }

      final int read = this.inputStream.read(b);

      if(read < 0)
      {
         this.finish = true;

         return read;
      }

      this.index += read;

      return read;
   }

   /**
    * Read some byte and write them inside a given array <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param b
    *           Array where write read bytes
    * @param off
    *           Offset inside array where start write bytes
    * @param len
    *           Number of bytes to write at maximum
    * @return Number of bytes effectively read from stream and write to the array OR -1 if we are at the end of the stream
    * @throws IOException
    *            On reading issue
    * @see InputStream#read(byte[], int, int)
    */
   @Override
   public int read(final byte[] b, final int off, final int len) throws IOException
   {
      if(this.finish == true)
      {
         return -1;
      }

      final int read = this.inputStream.read(b, off, len);

      if(read < 0)
      {
         this.finish = true;

         return read;
      }

      this.index += read;

      return read;
   }

   /**
    * Skip a number of bytes. It tries to fullfill the contract <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param toSkip
    *           Number of bytes to skip
    * @return Number of really skipped bytes
    * @throws IOException
    *            On skipping issue
    * @see InputStream#skip(long)
    */
   @Override
   public long skip(long toSkip) throws IOException
   {
      if(toSkip <= 0)
      {
         return 0;
      }

      long skipped = 0;

      long skip = this.inputStream.skip(toSkip);
      while((skip >= 0) && (toSkip > 0))
      {
         skipped += skip;
         toSkip -= skip;

         if(toSkip > 0)
         {
            skip = this.inputStream.skip(toSkip);
         }
      }

      return skipped;
   }
}