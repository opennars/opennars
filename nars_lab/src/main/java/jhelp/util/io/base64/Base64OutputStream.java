package jhelp.util.io.base64;

import jhelp.util.debug.Debug;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Stream for write in Base 64
 * 
 * @author JHelp
 */
public class Base64OutputStream
      extends OutputStream
{
   /** First step */
   private static final int STEP_1 = 1;
   /** Second step */
   private static final int STEP_2 = 2;
   /** Third step */
   private static final int STEP_3 = 3;
   /** Stream where write */
   private OutputStream     outputStream;
   /** Previous value */
   private int              previous;
   /** current step */
   private int              step;

   /**
    * Create a new instance of Base64OutputStream
    * 
    * @param outputStream
    *           Stream where write
    */
   public Base64OutputStream(final OutputStream outputStream)
   {
      if(outputStream == null)
      {
         throw new NullPointerException("outputStream musn't be null");
      }

      this.outputStream = outputStream;
      this.step = Base64OutputStream.STEP_1;
   }

   /**
    * Add the need = and Close the stream <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @throws IOException
    *            On closing issue
    * @see OutputStream#close()
    */
   @Override
   public void close() throws IOException
   {
      IOException ioException = null;

      try
      {
         switch(this.step)
         {
            case STEP_2:
               this.outputStream.write(Base64Common.COMPLEMENT);
               // No break
            case STEP_3:
               this.outputStream.write(Base64Common.COMPLEMENT);
               this.outputStream.write(Base64Common.COMPLEMENT);
            break;
         }

         this.outputStream.flush();
      }
      catch(final IOException exception)
      {
         ioException = exception;
      }

      try
      {
         this.outputStream.close();
      }
      catch(final IOException exception)
      {
         if(ioException == null)
         {
            ioException = exception;
         }
         else
         {
            if(exception.getCause() == null)
            {
               exception.initCause(ioException);
               ioException = exception;
            }
            else if(ioException.getCause() == null)
            {
               ioException.initCause(exception);
            }
            else
            {
               Debug.printException(ioException, "This exception is consumed");

               ioException = exception;
            }
         }
      }

      this.outputStream = null;

      if(ioException != null)
      {
         throw ioException;
      }
   }

   /**
    * Flush the stream <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @throws IOException
    *            On flushing issue
    * @see OutputStream#flush()
    */
   @Override
   public void flush() throws IOException
   {
      this.outputStream.flush();
   }

   /**
    * Write one byte <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param b
    *           Byte to write
    * @throws IOException
    *            On writing issue
    * @see OutputStream#write(int)
    */
   @Override
   public void write(final int b) throws IOException
   {
      switch(this.step)
      {
         case STEP_1:
            this.outputStream.write(Base64Common.getSymbol((b >> 2) & 0x1F));

            this.previous = b & 0x03;

            this.step = Base64OutputStream.STEP_2;
         break;
         case STEP_2:
            this.outputStream.write(Base64Common.getSymbol((this.previous << 4) | ((b >> 4) & 0x0F)));

            this.previous = b & 0x0F;

            this.step = Base64OutputStream.STEP_3;
         break;
         case STEP_3:
            this.outputStream.write(Base64Common.getSymbol((this.previous << 2) | ((b >> 6) & 0x03)));

            this.outputStream.write(Base64Common.getSymbol(b & 0x1F));

            this.step = Base64OutputStream.STEP_1;
         break;
      }
   }
}