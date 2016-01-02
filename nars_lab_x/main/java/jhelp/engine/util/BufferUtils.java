package jhelp.engine.util;

/**
 * Buffer utilities.<br>
 * To not take too much memory, we reuse always the same ByteBuffer and see it like IntBuffer, FloatBuffer or DoubleBuffer
 * depends on the situation.<br>
 * The calls is here to provide method for manipulate this buffer. <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class BufferUtils
{
   /** Maximum dimension */
   private static final int         MAX_DIMENSION           = BufferUtils.MAX_WIDTH * BufferUtils.MAX_HEIGHT;
   /** Maximum buffer size enough to put the biggest texture */
   private static final int         MAX_DIMENSION_IN_BYTES  = BufferUtils.MAX_DIMENSION * 4;
   /** Maximum texture height */
   private static final int         MAX_HEIGHT              = 4096;
   /** Maximum texture width */
   private static final int         MAX_WIDTH               = 4096;
   /** The buffer */
   public static final ByteBuffer   TEMPORARY_BYTE_BUFFER   = ByteBuffer.allocateDirect(BufferUtils.MAX_DIMENSION_IN_BYTES).order(ByteOrder.nativeOrder());
   /** See the buffer in DoubleBuffer */
   public static final DoubleBuffer TEMPORARY_DOUBLE_BUFFER = BufferUtils.TEMPORARY_BYTE_BUFFER.asDoubleBuffer();
   /** See the buffer in FloatBuffer */
   public static final FloatBuffer  TEMPORARY_FLOAT_BUFFER  = BufferUtils.TEMPORARY_BYTE_BUFFER.asFloatBuffer();
   /** See the buffer in IntBuffer */
   public static final IntBuffer    TEMPORARY_INT_BUFFER    = BufferUtils.TEMPORARY_BYTE_BUFFER.asIntBuffer();

   /**
    * Fill byte array with the buffer
    * 
    * @param array
    *           Array to fill
    */
   public static void fill(final byte[] array)
   {
      BufferUtils.TEMPORARY_BYTE_BUFFER.rewind();
      BufferUtils.TEMPORARY_BYTE_BUFFER.get(array);
      BufferUtils.TEMPORARY_BYTE_BUFFER.rewind();
   }

   /**
    * Fill double array with the buffer
    * 
    * @param array
    *           Array to fill
    */
   public static void fill(final double[] array)
   {
      BufferUtils.TEMPORARY_DOUBLE_BUFFER.rewind();
      BufferUtils.TEMPORARY_DOUBLE_BUFFER.get(array);
      BufferUtils.TEMPORARY_DOUBLE_BUFFER.rewind();
   }

   /**
    * Fill float array with the buffer
    * 
    * @param array
    *           Array to fill
    */
   public static void fill(final float[] array)
   {
      BufferUtils.TEMPORARY_FLOAT_BUFFER.rewind();
      BufferUtils.TEMPORARY_FLOAT_BUFFER.get(array);
      BufferUtils.TEMPORARY_FLOAT_BUFFER.rewind();
   }

   /**
    * Fill int array with the buffer
    * 
    * @param array
    *           Array to fill
    */
   public static void fill(final int[] array)
   {
      BufferUtils.TEMPORARY_INT_BUFFER.rewind();
      BufferUtils.TEMPORARY_INT_BUFFER.get(array);
      BufferUtils.TEMPORARY_INT_BUFFER.rewind();
   }

   /**
    * Transform a single dimension array to a double dimension array
    * 
    * @param ar
    *           Array to transform
    * @return Transformed array
    */
   public static float[][] toTwoDimensionFloatArray(final float[] ar)
   {
      final int nb = ar.length;
      final int n = (int) (Math.sqrt(nb));
      final float[][] array = new float[n][n];
      int index = 0;
      for(int i = 0; i < n; i++)
      {
         for(int j = 0; j < n; j++)
         {
            array[i][j] = ar[index++];
         }
      }
      return array;
   }

   /**
    * Transfer byte array to the buffer
    * 
    * @param array
    *           Array to transfer
    * @return The buffer filled
    */
   public static ByteBuffer transferByte(final byte... array)
   {
      BufferUtils.TEMPORARY_BYTE_BUFFER.rewind();
      final int size = array.length;
      for(int i = 0; i < size; i++)
      {
         BufferUtils.TEMPORARY_BYTE_BUFFER.put(i, array[i]);
      }
      return BufferUtils.TEMPORARY_BYTE_BUFFER;
   }

   /**
    * Transfer float array to the buffer
    * 
    * @param array
    *           Array to transfer
    * @return The buffer filled
    */
   public static FloatBuffer transferFloat(final float... array)
   {
      BufferUtils.TEMPORARY_FLOAT_BUFFER.rewind();
      final int size = array.length;
      for(int i = 0; i < size; i++)
      {
         BufferUtils.TEMPORARY_FLOAT_BUFFER.put(i, array[i]);
      }
      return BufferUtils.TEMPORARY_FLOAT_BUFFER;
   }

   /**
    * Transfer int array to the buffer
    * 
    * @param array
    *           Array to transfer
    * @return The buffer filled
    */
   public static IntBuffer transferInteger(final int... array)
   {
      BufferUtils.TEMPORARY_INT_BUFFER.rewind();
      final int size = array.length;
      for(int i = 0; i < size; i++)
      {
         BufferUtils.TEMPORARY_INT_BUFFER.put(i, array[i]);
      }
      return BufferUtils.TEMPORARY_INT_BUFFER;
   }
}