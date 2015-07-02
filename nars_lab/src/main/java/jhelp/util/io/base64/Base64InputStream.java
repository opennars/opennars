package jhelp.util.io.base64;

import java.io.IOException;
import java.io.InputStream;

/**
 * Read a base 64 stream
 * 
 * @author JHelp
 */
public class Base64InputStream
      extends InputStream
{
   /** First step */
   private static final int STEP_1   = 1;
   /** Second step */
   private static final int STEP_2   = 2;
   /** Third step */
   private static final int STEP_3   = 3;
   /** Final step */
   private static final int STEP_END = -1;
   /** Stream to read */
   private InputStream      inputStream;
   /** Previous value */
   private int              previous;
   /** Current step */
   private int              step;

   /**
    * Create a new instance of Base64InputStream
    * 
    * @param inputStream
    *           Stream to read
    */
   public Base64InputStream(final InputStream inputStream)
   {
      if(inputStream == null)
      {
         throw new NullPointerException("inputStream musn't be null");
      }

      this.inputStream = inputStream;
      this.step = Base64InputStream.STEP_1;
   }

   /**
    * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking by
    * the next invocation of a method for this input stream <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking by the
    *         next invocation of a method for this input stream
    * @throws IOException
    *            On computing issue
    * @see InputStream#available()
    */
   @Override
   public int available() throws IOException
   {
      return this.inputStream.available();
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
      this.inputStream.close();

      this.inputStream = null;
   }

   /**
    * Mark current position <br>
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
   }

   /**
    * Indicates if mark is supported <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return {@code true} if mark is supported
    * @see InputStream#markSupported()
    */
   @Override
   public boolean markSupported()
   {
      return false;
   }

   /**
    * Read one byte <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Byte read
    * @throws IOException
    *            On reading issue
    * @see InputStream#read()
    */
   @Override
   public int read() throws IOException
   {
      int read, index, actual;

      switch(this.step)
      {
         case STEP_1:
            this.step = Base64InputStream.STEP_2;

            read = this.inputStream.read();

            if((read < 0) || (read == Base64Common.COMPLEMENT))
            {
               this.step = Base64InputStream.STEP_END;

               return -1;
            }

            index = Base64Common.getIndex(read);

            if(index < 0)
            {
               throw new IOException("Unexpected symbol inside the Base 64 stream : 0x" + Integer.toHexString(read));
            }

            actual = index << 2;

            read = this.inputStream.read();

            if((read < 0) || (read == Base64Common.COMPLEMENT))
            {
               this.step = Base64InputStream.STEP_END;

               return actual;
            }

            index = Base64Common.getIndex(read);

            if(index < 0)
            {
               throw new IOException("Unexpected symbol inside the Base 64 stream : 0x" + Integer.toHexString(read));
            }

            this.previous = index & 0x0F;

            return actual | ((index >> 4) & 0x03);
         case STEP_2:
            this.step = Base64InputStream.STEP_3;

            actual = this.previous << 4;

            read = this.inputStream.read();

            if((read < 0) || (read == Base64Common.COMPLEMENT))
            {
               this.step = Base64InputStream.STEP_END;

               return actual;
            }

            index = Base64Common.getIndex(read);

            if(index < 0)
            {
               throw new IOException("Unexpected symbol inside the Base 64 stream : 0x" + Integer.toHexString(read));
            }

            this.previous = index & 0x03;

            return actual | ((index >> 2) & 0x0F);
         case STEP_3:
            this.step = Base64InputStream.STEP_1;

            actual = this.previous << 6;

            read = this.inputStream.read();

            if((read < 0) || (read == Base64Common.COMPLEMENT))
            {
               this.step = Base64InputStream.STEP_END;

               return actual;
            }

            index = Base64Common.getIndex(read);

            if(index < 0)
            {
               throw new IOException("Unexpected symbol inside the Base 64 stream : 0x" + Integer.toHexString(read));
            }

            return actual | (index & 0x1F);
         case STEP_END:
            return -1;
      }

      return -1;
   }

   /**
    * Reset to previous mark <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @throws IOException
    *            If mark not supported
    * @see InputStream#reset()
    */
   @Override
   public synchronized void reset() throws IOException
   {
   }

   /**
    * Skip some bytes <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param n
    *           Number of bytes to skip
    * @return Number of skipped bytes
    * @throws IOException
    *            On skipping bytes
    * @see InputStream#skip(long)
    */
   @Override
   public long skip(long n) throws IOException
   {
      int read;
      long skip = 0;

      while(n > 0)
      {
         read = this.read();

         if(read < 0)
         {
            break;
         }

         skip++;
         n--;
      }

      return skip;
   }
}