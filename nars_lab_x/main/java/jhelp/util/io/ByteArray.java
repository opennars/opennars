package jhelp.util.io;

import jhelp.util.math.UtilMath;
import jhelp.util.reflection.Reflector;
import jhelp.util.text.UtilText;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

/**
 * Byte array can be use as {@link InputStream} and {@link OutputStream}
 * 
 * @author JHelp
 */
public class ByteArray
{
   /**
    * Internal Input stream for read the array
    * 
    * @author JHelp
    */
   class InternalInputStream
         extends InputStream
   {
      /**
       * Number of left byte to read <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @return Number of left byte to read
       * @throws IOException
       *            On reading issue
       * @see InputStream#available()
       */
      @Override
      public int available() throws IOException
      {
         return ByteArray.this.available();
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
         // Nothing to do
      }

      /**
       * Mark actual read position <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param readlimit
       *           Maximum limit
       * @see InputStream#mark(int)
       */
      @Override
      public synchronized void mark(final int readlimit)
      {
         ByteArray.this.mark();
      }

      /**
       * Indicates if mark are supported <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @return {@code true}
       * @see InputStream#markSupported()
       */
      @Override
      public boolean markSupported()
      {
         return true;
      }

      /**
       * Read newt byte <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @return Read byte or -1 if end of stream
       * @throws IOException
       *            On reading issue
       * @see InputStream#read()
       */
      @Override
      public int read() throws IOException
      {
         return ByteArray.this.read();
      }

      /**
       * Read some bytes and fill an array.<br>
       * Do same as {@link #read(byte[], int, int) read(b, 0, b.length)} <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param b
       *           Array to fill
       * @return Number of read bytes
       * @throws IOException
       *            On reading issue
       * @see InputStream#read(byte[])
       */
      @Override
      public int read(final byte[] b) throws IOException
      {
         return ByteArray.this.read(b, 0, b.length);
      }

      /**
       * Read some bytes and fill an array. <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param b
       *           Array to fill
       * @param off
       *           Offset where start filling the array
       * @param len
       *           Number maximum of byte to read
       * @return Number of read bytes
       * @throws IOException
       *            On reading issue
       * @see InputStream#read(byte[], int, int)
       */
      @Override
      public int read(final byte[] b, final int off, final int len) throws IOException
      {
         return ByteArray.this.read(b, off, len);
      }

      /**
       * Reset the mark <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @throws IOException
       *            On access issue
       * @see InputStream#reset()
       */
      @Override
      public synchronized void reset() throws IOException
      {
         ByteArray.this.reset();
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
         return ByteArray.this.skip(n);
      }
   }

   /**
    * Internal output stream for write on the array
    * 
    * @author JHelp
    */
   class InternalOutputStream
         extends OutputStream
   {
      /**
       * Close the stream <br>
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
         // Nothing to do
      }

      /**
       * Flush last changes <br>
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
         // Nothing to do
      }

      /**
       * Write an array of bytes.<br>
       * Do same as {@link #write(byte[], int, int) write(b, 0, b.length)} <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param b
       *           Array to write
       * @throws IOException
       *            On writing issue
       * @see OutputStream#write(byte[])
       */
      @Override
      public void write(final byte[] b) throws IOException
      {
         ByteArray.this.write(b, 0, b.length);
      }

      /**
       * Write a part off an array of bytes <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param b
       *           Array to write
       * @param off
       *           Offset where start read the array
       * @param len
       *           Number of bytes to write
       * @throws IOException
       *            on writing issue
       * @see OutputStream#write(byte[], int, int)
       */
      @Override
      public void write(final byte[] b, final int off, final int len) throws IOException
      {
         ByteArray.this.write(b, off, len);
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
         ByteArray.this.write(b);
      }
   }

   /** Byte array */
   private byte[]                     array;
   /** Read index */
   private int                        index;
   /** Internal input stream for read the stream */
   private final InternalInputStream  internalInputStream;
   /** Internal output stream for write */
   private final InternalOutputStream internalOutputStream;
   /** Actual mark */
   private int                        mark;
   /** Actual size */
   private int                        size;

   /**
    * Create a new instance of ByteArray
    */
   public ByteArray()
   {
      this.array = new byte[4096];
      this.size = 0;
      this.internalInputStream = new InternalInputStream();
      this.internalOutputStream = new InternalOutputStream();
   }

   /**
    * Expands, if need, the array
    * 
    * @param more
    *           Number of need free space
    */
   private void expand(final int more)
   {
      if((this.size + more) > this.array.length)
      {
         final int newSize = this.size + more;

         final byte[] temp = new byte[newSize + (newSize / 9) + 1];
         System.arraycopy(this.array, 0, temp, 0, this.size);

         this.array = temp;
      }
   }

   /**
    * Mark current read position
    */
   void mark()
   {
      this.mark = this.index;
   }

   /**
    * Return to last marked position and clear the mark
    */
   void reset()
   {
      this.index = this.mark;
      this.mark = 0;
   }

   /**
    * Skip a number of bytes
    * 
    * @param n
    *           Number of bytes to skip
    * @return Number of skipped bytes
    */
   long skip(long n)
   {
      n = Math.min(n, this.size - this.index);

      this.index += n;

      return n;
   }

   /**
    * Number of bytes left to read
    * 
    * @return Number of bytes left to read
    */
   public int available()
   {
      return this.size - this.index;
   }

   /**
    * Clear the array
    */
   public void clear()
   {
      this.index = 0;
      this.size = 0;
   }

   /**
    * Input stream for read
    * 
    * @return Input stream for read
    */
   public InputStream getInputStream()
   {
      return this.internalInputStream;
   }

   /**
    * Output stream for write
    * 
    * @return Output stream for write
    */
   public OutputStream getOutputStream()
   {
      return this.internalOutputStream;
   }

   /**
    * Array size
    * 
    * @return Array size
    */
   public int getSize()
   {
      return this.size;
   }

   /**
    * Read one byte
    * 
    * @return Byte read or -1 if no more to read
    */
   public int read()
   {
      if(this.index >= this.size)
      {
         return -1;
      }

      return this.array[this.index++] & 0xFF;
   }

   /**
    * Read some bytes and fill an array.<br>
    * Do same as {@link #read(byte[], int, int) read(b, 0, b.length)}
    * 
    * @param b
    *           Array to fill
    * @return Number of read bytes
    */
   public int read(final byte[] b)
   {
      return this.read(b, 0, b.length);
   }

   /**
    * Read some bytes and fill a part of array
    * 
    * @param b
    *           Array to fill
    * @param off
    *           Where start fill the array
    * @param len
    *           Number maximum of byte to read
    * @return Number of read bytes
    */
   public int read(final byte[] b, final int off, int len)
   {
      if(this.index >= this.size)
      {
         return -1;
      }

      len = UtilMath.minIntegers(len, b.length - off, this.size - this.index);

      if(len <= 0)
      {
         return 0;
      }

      System.arraycopy(this.array, this.index, b, off, len);
      this.index += len;

      return len;
   }

   /***
    * Read a {@link Binarizable} from the byte array.<br>
    * See {@link #writeBinarizable(Binarizable)} for write a {@link Binarizable}
    * 
    * @param <B>
    *           Type of {@link Binarizable}
    * @param clas
    *           Class of the {@link Binarizable} to read
    * @return The read {@link Binarizable}
    * @throws Exception
    *            If the actual read data in the byte array doesn't represents the {@link Binarizable} asked
    */
   public <B extends Binarizable> B readBinarizable(final Class<B> clas) throws Exception
   {
      if(this.read() == 0)
      {
         return null;
      }

      @SuppressWarnings("unchecked")
      final B binarizable = (B) Reflector.newInstance(clas);

      binarizable.parseBinary(this);

      return binarizable;
   }

   /**
    * Read an array of {@link Binarizable}.<br>
    * See {@link #writeBinarizableArray(Binarizable...)} for write an array of {@link Binarizable}
    * 
    * @param <B>
    *           Type of {@link Binarizable}
    * @param clas
    *           Class of the {@link Binarizable} to read
    * @return The read array
    * @throws Exception
    *            If the byte array doesn't contains an array of desired {@link Binarizable} at the actual read index
    */
   public <B extends Binarizable> B[] readBinarizableArray(final Class<B> clas) throws Exception
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      @SuppressWarnings("unchecked")
      final B[] array = (B[]) Array.newInstance(clas, length);

      for(int i = 0; i < length; i++)
      {
         array[i] = this.readBinarizable(clas);
      }

      return array;
   }

   /**
    * Read a boolean.<br>
    * See {@link #writeBoolean(boolean)}
    * 
    * @return Boolean read
    */
   public boolean readBoolean()
   {
      if(this.read() == 1)
      {
         return true;
      }

      return false;
   }

   /**
    * Read a boolean array from the byte array.<br>
    * See {@link #writeBooleanArray(boolean...)} for write the boolean array
    * 
    * @return Boolean array read
    */
   public boolean[] readBooleanArray()
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      final boolean[] bools = new boolean[length];

      if(length == 0)
      {
         return bools;
      }

      int shift = 7;
      int b = this.read();

      for(int index = 0; index < length; index++)
      {
         bools[index] = ((b >> shift) & 1) == 1;

         shift--;

         if(shift < 0)
         {
            b = this.read();
            shift = 7;
         }
      }

      return bools;
   }

   /**
    * Read a byte.<br>
    * See {@link #writeByte(byte)}
    * 
    * @return Byte read
    */
   public byte readByte()
   {
      return (byte) (this.read() & 0xFF);
   }

   /**
    * Read a byte array.<br>
    * See {@link #writeByteArray(byte...)} for write the byte array
    * 
    * @return Read byte array
    */
   public byte[] readByteArray()
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      final byte[] array = new byte[length];
      this.read(array);

      return array;
   }

   /**
    * Read a char.<br>
    * See {@link #writeChar(char)}
    * 
    * @return Char read
    */
   public char readChar()
   {
      final int val = (this.read() << 8) | this.read();

      return (char) (val & 0xFFFF);
   }

   /**
    * Read a char array from the byte array<br>
    * See {@link #writeCharArray(char...)}
    * 
    * @return Read char array
    */
   public char[] readCharArray()
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      final char[] array = new char[length];

      for(int i = 0; i < length; i++)
      {
         array[i] = this.readChar();
      }

      return array;
   }

   /**
    * Read a double.<br>
    * See {@link #writeDouble(double)}
    * 
    * @return Double read
    */
   public double readDouble()
   {
      return Double.longBitsToDouble(this.readLong());
   }

   /**
    * Read a double array from the byte array.<br>
    * See {@link #writeDoubleArray(double...)}
    * 
    * @return The read double array
    */
   public double[] readDoubleArray()
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      final double[] array = new double[length];

      for(int i = 0; i < length; i++)
      {
         array[i] = this.readDouble();
      }

      return array;
   }

   /**
    * read an eum from the byte array.<br>
    * See {@link #writeEnum(Enum)} for write it
    * 
    * @param <E>
    *           Enum type
    * @param clas
    *           Class of the enum
    * @return Read enum
    * @throws Exception
    *            If data not corresponds to the asked enum
    */
   @SuppressWarnings(
   {
         "unchecked", "rawtypes"
   })
   public <E extends Enum> E readEnum(final Class<E> clas) throws Exception
   {
      final String name = this.readString();

      if(name == null)
      {
         return null;
      }

      return (E) clas.getMethod("valueOf", clas.getClass(), String.class).invoke(null, clas, name);
   }

   /**
    * Read an enum array from the byte array.<br>
    * See {@link #writeEnumArray(Enum...)} for write it
    * 
    * @param <E>
    *           Enum type
    * @param clas
    *           Enum class to read
    * @return Read enum
    * @throws Exception
    *            If data not corresponds to an array of desired enum
    */
   @SuppressWarnings("rawtypes")
   public <E extends Enum> E[] readEnumArray(final Class<E> clas) throws Exception
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      @SuppressWarnings("unchecked")
      final E[] array = (E[]) Array.newInstance(clas, length);

      for(int i = 0; i < length; i++)
      {
         array[i] = this.readEnum(clas);
      }

      return array;
   }

   /**
    * Read a float.<br>
    * See {@link #writeFloat(float)}
    * 
    * @return Float read
    */
   public float readFloat()
   {
      return Float.intBitsToFloat(this.readInteger());
   }

   /**
    * Read a float array from the byte array.<br>
    * See {@link #writeFloatArray(float...)}
    * 
    * @return Float array read
    */
   public float[] readFloatArray()
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      final float[] array = new float[length];

      for(int i = 0; i < length; i++)
      {
         array[i] = this.readFloat();
      }

      return array;
   }

   /**
    * Restart the reading from start
    */
   public void readFromStart()
   {
      this.index = 0;
   }

   /**
    * Read an integer.<br>
    * See {@link #writeInteger(int)}
    * 
    * @return Integer read
    */
   public int readInteger()
   {
      return (this.read() << 24) | (this.read() << 16) | (this.read() << 8) | this.read();
   }

   /**
    * Read an integer array from the byte array.<br>
    * See {@link #writeIntegerArray(int...)}
    * 
    * @return Integer array read
    */
   public int[] readIntegerArray()
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      final int[] array = new int[length];

      for(int i = 0; i < length; i++)
      {
         array[i] = this.readInteger();
      }

      return array;
   }

   /**
    * Read a long.<br>
    * See {@link #writeLong(long)}
    * 
    * @return Long read
    */
   public long readLong()
   {
      return ((long) this.read() << 56L) | ((long) this.read() << 48L) | ((long) this.read() << 40L) | ((long) this.read() << 32L) | ((long) this.read() << 24L) | ((long) this.read() << 16L) | ((long) this.read() << 8L) | this.read();
   }

   /**
    * Read a long array from byte array.<br>
    * See {@link #writeLongArray(long...)}
    * 
    * @return Read long array
    */
   public long[] readLongArray()
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      final long[] array = new long[length];

      for(int i = 0; i < length; i++)
      {
         array[i] = this.readLong();
      }

      return array;
   }

   /**
    * Read a short.<br>
    * See {@link #writeShort(short)}
    * 
    * @return Short read
    */
   public short readShort()
   {
      final int val = (this.read() << 8) | this.read();

      return (short) (val & 0xFFFF);
   }

   /**
    * read short array from data.<br>
    * See {@link #writeShortArray(short...)}
    * 
    * @return Read short array
    */
   public short[] readShortArray()
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      final short[] array = new short[length];

      for(int i = 0; i < length; i++)
      {
         array[i] = this.readShort();
      }

      return array;
   }

   /**
    * Read a String.<br>
    * See {@link #writeString(String)}
    * 
    * @return String read
    */
   public String readString()
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      final byte[] utf8 = new byte[length];

      this.read(utf8);

      return UtilText.readUTF8(utf8, 0, length);
   }

   /**
    * read string array from the byte array.<br>
    * See {@link #writeStringArray(String...)}
    * 
    * @return String array read
    */
   public String[] readStringArray()
   {
      final int length = this.readInteger();

      if(length < 0)
      {
         return null;
      }

      final String[] array = new String[length];

      for(int i = 0; i < length; i++)
      {
         array[i] = this.readString();
      }

      return array;
   }

   /**
    * Get an array
    * 
    * @return Get an array
    */
   public byte[] toArray()
   {
      final byte[] result = new byte[this.size];

      System.arraycopy(this.array, 0, result, 0, this.size);

      return result;
   }

   /**
    * Write an array.<br>
    * Do same as {@link ByteArray#write(byte[], int, int) write(b, 0, b.length)}
    * 
    * @param b
    *           Array to write
    */
   public void write(final byte[] b)
   {
      this.write(b, 0, b.length);
   }

   /**
    * Write a part of array of bytes
    * 
    * @param b
    *           Array to write
    * @param off
    *           Where start read in the array
    * @param len
    *           Number of bytes to write
    */
   public void write(final byte[] b, int off, int len)
   {
      if(off < 0)
      {
         len += off;
         off = 0;
      }

      len = Math.min(b.length - off, len);

      if(len < 1)
      {
         return;
      }

      this.expand(len);

      System.arraycopy(b, off, this.array, this.size, len);

      this.size += len;
   }

   /**
    * Write one byte
    * 
    * @param b
    *           Byte to write
    */
   public void write(final int b)
   {
      this.expand(1);

      this.array[this.size] = (byte) (b & 0xFF);

      this.size++;
   }

   /**
    * Write a {@link Binarizable} to the byte array.<br>
    * See {@link #readBinarizable(Class)} for read it later
    * 
    * @param <B>
    *           {@link Binarizable} type
    * @param binarizable
    *           {@link Binarizable} to write. {@code null} is accept
    */
   public <B extends Binarizable> void writeBinarizable(final B binarizable)
   {
      if(binarizable == null)
      {
         this.write(0);

         return;
      }

      this.write(1);
      binarizable.serializeBinary(this);
   }

   /**
    * Write {@link Binarizable} array to the byte array.<br>
    * See {@link #readBinarizableArray(Class)} for read it
    * 
    * @param <B>
    *           {@link Binarizable} type
    * @param array
    *           {@link Binarizable} array to write
    */
   public <B extends Binarizable> void writeBinarizableArray(final B... array)
   {
      if(array == null)
      {
         this.writeInteger(-1);
         return;
      }

      final int length = array.length;
      this.writeInteger(length);

      for(int i = 0; i < length; i++)
      {
         this.writeBinarizable(array[i]);
      }
   }

   /**
    * Write a boolean.<br>
    * See {@link #readBoolean()}
    * 
    * @param booleanValue
    *           Boolean to write
    */
   public void writeBoolean(final boolean booleanValue)
   {
      if(booleanValue == true)
      {
         this.write(1);
      }
      else
      {
         this.write(0);
      }
   }

   /**
    * Write a boolean array to the byte array.<br>
    * The number of bytes takes here is 4(Array length)+(NumberOfBoolean)/8, that mean if you have to store several boolen, its
    * more efficient to use this method (Number of byte took for store data are less) if the number of boolean is at least 5.<br>
    * See {@link #readBooleanArray()} for read it later
    * 
    * @param bools
    *           Boolean array to store
    */
   public void writeBooleanArray(final boolean... bools)
   {
      if(bools == null)
      {
         this.write(-1);

         return;
      }

      final int length = bools.length;
      final int size = (length >> 3) + (((length & 7) == 0)
            ? 0
            : 1);

      this.writeInteger(length);

      if(length == 0)
      {
         return;
      }

      this.expand(size);

      int b = 0;
      int shift = 7;

      for(int index = 0; index < length; index++)
      {
         b |= ((bools[index] == true)
               ? 1
               : 0) << shift;

         shift--;

         if(shift < 0)
         {
            this.write(b);
            b = 0;
            shift = 7;
         }
      }

      if(shift != 7)
      {
         this.write(b);
      }
   }

   /**
    * write a byte.<br>
    * See {@link #readByte()}
    * 
    * @param byteValue
    *           Byte to write
    */
   public void writeByte(final byte byteValue)
   {
      this.write(byteValue & 0xFF);
   }

   /**
    * Write byte array.<br>
    * See {@link #readByteArray()}
    * 
    * @param array
    *           Array to store
    */
   public void writeByteArray(final byte... array)
   {
      if(array == null)
      {
         this.writeInteger(-1);
         return;
      }

      this.writeInteger(array.length);
      this.write(array);
   }

   /**
    * Write a char.<br>
    * See {@link #readChar()}
    * 
    * @param charValue
    *           Char to write
    */
   public void writeChar(final char charValue)
   {
      final int val = charValue & 0xFFFF;

      this.write((val >> 8) & 0xFF);
      this.write(val & 0xFF);
   }

   /**
    * Write a char array.<br>
    * See {@link #readCharArray()} for read
    * 
    * @param array
    *           Array to write
    */
   public void writeCharArray(final char... array)
   {
      if(array == null)
      {
         this.writeInteger(-1);
         return;
      }

      final int length = array.length;
      this.writeInteger(length);

      for(int i = 0; i < length; i++)
      {
         this.writeChar(array[i]);
      }
   }

   /**
    * Write a double.<br>
    * See {@link #readDouble()}
    * 
    * @param doubleValue
    *           Double to write
    */
   public void writeDouble(final double doubleValue)
   {
      this.writeLong(Double.doubleToLongBits(doubleValue));
   }

   /**
    * Write double array.<br>
    * See {@link #readDoubleArray()} for read
    * 
    * @param array
    *           Array of double to write
    */
   public void writeDoubleArray(final double... array)
   {
      if(array == null)
      {
         this.writeInteger(-1);
         return;
      }

      final int length = array.length;
      this.writeInteger(length);

      for(int i = 0; i < length; i++)
      {
         this.writeDouble(array[i]);
      }
   }

   /**
    * Write an enum.<br>
    * See {@link #readEnum(Class)} to read
    * 
    * @param <E>
    *           Enum type
    * @param e
    *           Enum to write
    */
   @SuppressWarnings("rawtypes")
   public <E extends Enum> void writeEnum(final E e)
   {
      if(e == null)
      {
         this.writeString(null);

         return;
      }

      this.writeString(e.name());
   }

   /**
    * Write an enum array.<br>
    * See {@link #readEnumArray(Class)} for read it
    * 
    * @param <E>
    *           Enum type
    * @param array
    *           Array of enum to write
    */
   @SuppressWarnings("rawtypes")
   public <E extends Enum> void writeEnumArray(final E... array)
   {
      if(array == null)
      {
         this.writeInteger(-1);
         return;
      }

      final int length = array.length;
      this.writeInteger(length);

      for(int i = 0; i < length; i++)
      {
         this.writeEnum(array[i]);
      }
   }

   /**
    * Write a float.<br>
    * See {@link #readFloat()}
    * 
    * @param floatValue
    *           Float to write
    */
   public void writeFloat(final float floatValue)
   {
      this.writeInteger(Float.floatToIntBits(floatValue));
   }

   /**
    * Write a float array.<br>
    * See {@link #readFloatArray()} for read
    * 
    * @param array
    *           Array to read
    */
   public void writeFloatArray(final float... array)
   {
      if(array == null)
      {
         this.writeInteger(-1);
         return;
      }

      final int length = array.length;
      this.writeInteger(length);

      for(int i = 0; i < length; i++)
      {
         this.writeFloat(array[i]);
      }
   }

   /**
    * Write an integer.<br>
    * See {@link #readInteger()}
    * 
    * @param intValue
    *           Integer to write
    */
   public void writeInteger(final int intValue)
   {
      this.write((intValue >> 24) & 0xFF);
      this.write((intValue >> 16) & 0xFF);
      this.write((intValue >> 8) & 0xFF);
      this.write(intValue & 0xFF);
   }

   /**
    * Write integer array.<br>
    * See {@link #readIntegerArray()} for read
    * 
    * @param array
    *           Array of integer to write
    */
   public void writeIntegerArray(final int... array)
   {
      if(array == null)
      {
         this.writeInteger(-1);
         return;
      }

      final int length = array.length;
      this.writeInteger(length);

      for(int i = 0; i < length; i++)
      {
         this.writeInteger(array[i]);
      }
   }

   /**
    * Write a long.<br>
    * See {@link #readLong()}
    * 
    * @param longValue
    *           Long to write
    */
   public void writeLong(final long longValue)
   {
      this.write((int) ((longValue >> 56) & 0xFF));
      this.write((int) ((longValue >> 48) & 0xFF));
      this.write((int) ((longValue >> 40) & 0xFF));
      this.write((int) ((longValue >> 32) & 0xFF));
      this.write((int) ((longValue >> 24) & 0xFF));
      this.write((int) ((longValue >> 16) & 0xFF));
      this.write((int) ((longValue >> 8) & 0xFF));
      this.write((int) (longValue & 0xFF));
   }

   /**
    * Write long array.<br>
    * See {@link #readLongArray()} for read
    * 
    * @param array
    *           Array of long to write
    */
   public void writeLongArray(final long... array)
   {
      if(array == null)
      {
         this.writeInteger(-1);
         return;
      }

      final int length = array.length;
      this.writeInteger(length);

      for(int i = 0; i < length; i++)
      {
         this.writeLong(array[i]);
      }
   }

   /**
    * Write a short.<br>
    * See {@link #readShort()}
    * 
    * @param shortValue
    *           Short to write
    */
   public void writeShort(final short shortValue)
   {
      final int val = shortValue & 0xFFFF;

      this.write((val >> 8) & 0xFF);
      this.write(val & 0xFF);
   }

   /**
    * Write short array.<br>
    * See {@link #readShortArray()} for read
    * 
    * @param array
    *           Array of short to write
    */
   public void writeShortArray(final short... array)
   {
      if(array == null)
      {
         this.writeInteger(-1);
         return;
      }

      final int length = array.length;
      this.writeInteger(length);

      for(int i = 0; i < length; i++)
      {
         this.writeShort(array[i]);
      }
   }

   /**
    * Write a String.<br>
    * See {@link #readString()}
    * 
    * @param string
    *           String to write
    */
   public void writeString(final String string)
   {
      if(string == null)
      {
         this.writeInteger(-1);

         return;
      }

      final byte[] utf8 = UtilText.toUTF8(string);
      this.writeInteger(utf8.length);
      this.write(utf8);
   }

   /**
    * Write String array.<br>
    * See {@link #readStringArray()} for read
    * 
    * @param array
    *           Array of String to write
    */
   public void writeStringArray(final String... array)
   {
      if(array == null)
      {
         this.writeInteger(-1);
         return;
      }

      final int length = array.length;
      this.writeInteger(length);

      for(int i = 0; i < length; i++)
      {
         this.writeString(array[i]);
      }
   }
}