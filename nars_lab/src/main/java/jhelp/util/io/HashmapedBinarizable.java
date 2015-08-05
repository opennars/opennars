package jhelp.util.io;

import jhelp.util.debug.Debug;

import java.util.HashMap;

/**
 * Class with the aim to simplify the data getting and setting in a {@link Binarizable}.<br>
 * Data are store in a hash map, so no need to take care about order.<br>
 * Like {@link Binarizable} any extention of this class MUST have a default empty constructor (This one is called when
 * transfer).<br>
 * It also design to be evolutive, that is to say if in one version you decide an element is a <b>boolean</b> by exemple, and
 * later you need it become a <b>int</b>, with the internal mechanism, you can keep the compatibility. The method
 * {@link #convert(String, Object, Type, Type)} is called when its call one of <b>forceGet</b> (
 * {@link #forceGet(String, boolean)}, {@link #forceGet(String, byte)}, {@link #forceGet(String, char)},
 * {@link #forceGet(String, short)}, ...) method and the stored value type is not the asked one. This method
 * {@link #convert(String, Object, Type, Type)} is ask how convert one type to other
 * 
 * @author JHelp
 */
public abstract class HashmapedBinarizable
      implements Binarizable
{
   /**
    * Stored element
    * 
    * @author JHelp
    */
   private static class Element
   {
      /** Element type */
      final Type type;
      /** Element value */
      Object     value;

      /**
       * Create a new instance of Element
       * 
       * @param type
       *           Type
       * @param value
       *           Value
       */
      Element(final Type type, final Object value)
      {
         this.type = type;
         this.value = value;
      }
   }

   /**
    * Type of stored element
    * 
    * @author JHelp
    */
   public static enum Type
   {
      /** {@link Binarizable Binarizable[]} */
      ARRAY_OF_BINARIZABLE,
      /** boolean[] */
      ARRAY_OF_BOOLEAN,
      /** byte[] */
      ARRAY_OF_BYTE,
      /** char[] */
      ARRAY_OF_CHAR,
      /** double[] */
      ARRAY_OF_DOUBLE,
      /** float[] */
      ARRAY_OF_FLOAT,
      /** int[] */
      ARRAY_OF_INT,
      /** long[] */
      ARRAY_OF_LONG,
      /** short[] */
      ARRAY_OF_SHORT,
      /** String[] */
      ARRAY_OF_STRING,
      /** {@link Binarizable} */
      BINARIZABLE,
      /** boolean */
      BOOLEAN,
      /** byte */
      BYTE,
      /** char */
      CHAR,
      /** double */
      DOUBLE,
      /** float */
      FLOAT,
      /** int */
      INT,
      /** long */
      LONG,
      /** short */
      SHORT,
      /** String */
      STRING
   }

   /** Map of elements */
   private final HashMap<String, Element> elements;

   /**
    * Create a new instance of HashmapedBinarizable
    */
   public HashmapedBinarizable()
   {
      this.elements = new HashMap<String, Element>();
      this.initializeFields();
   }

   /**
    * Check if an element have the desiread type. If its not the case, an exception is throw
    * 
    * @param element
    *           Elemnt tested
    * @param type
    *           Desired type
    * @throws IllegalArgumentException
    *            If the type didn't match
    */
   private void checkType(final Element element, final Type type)
   {
      if(element.type != type)
      {
         throw new IllegalArgumentException("The type of the element is " + element.type + " not " + type);
      }
   }

   /**
    * Read next element inside a {@link ByteArray}
    * 
    * @param byteArray
    *           Byte array to read
    * @return {@code true} if their are more element to read, {@code false} if their are no more element to read
    */
   @SuppressWarnings("unchecked")
   private boolean readNextElement(final ByteArray byteArray)
   {
      try
      {
         final String name = byteArray.readString();

         if(name == null)
         {
            return false;
         }

         final Type type = byteArray.readEnum(Type.class);

         if(type == null)
         {
            return false;
         }

         Class<? extends Binarizable> clas;
         switch(type)
         {
            case ARRAY_OF_BINARIZABLE:
               clas = (Class<? extends Binarizable>) Class.forName(byteArray.readString());
               final Binarizable[] binarizables = byteArray.readBinarizableArray(clas);
               this.elements.put(name, new Element(type, binarizables));
            break;
            case ARRAY_OF_BOOLEAN:
               final boolean[] booleans = byteArray.readBooleanArray();
               this.elements.put(name, new Element(type, booleans));
            break;
            case ARRAY_OF_BYTE:
               final byte[] bytes = byteArray.readByteArray();
               this.elements.put(name, new Element(type, bytes));
            break;
            case ARRAY_OF_CHAR:
               final char[] chars = byteArray.readCharArray();
               this.elements.put(name, new Element(type, chars));
            break;
            case ARRAY_OF_DOUBLE:
               final double[] doubles = byteArray.readDoubleArray();
               this.elements.put(name, new Element(type, doubles));
            break;
            case ARRAY_OF_FLOAT:
               final float[] floats = byteArray.readFloatArray();
               this.elements.put(name, new Element(type, floats));
            break;
            case ARRAY_OF_INT:
               final int[] ints = byteArray.readIntegerArray();
               this.elements.put(name, new Element(type, ints));
            break;
            case ARRAY_OF_LONG:
               final long[] longs = byteArray.readLongArray();
               this.elements.put(name, new Element(type, longs));
            break;
            case ARRAY_OF_SHORT:
               final short[] shorts = byteArray.readShortArray();
               this.elements.put(name, new Element(type, shorts));
            break;
            case ARRAY_OF_STRING:
               final String[] strings = byteArray.readStringArray();
               this.elements.put(name, new Element(type, strings));
            break;
            case BINARIZABLE:
               clas = (Class<? extends Binarizable>) Class.forName(byteArray.readString());
               final Binarizable binarizable = byteArray.readBinarizable(clas);
               this.elements.put(name, new Element(type, binarizable));
            break;
            case BOOLEAN:
               final boolean b = byteArray.readBoolean();
               this.elements.put(name, new Element(type, b));
            break;
            case BYTE:
               final byte by = byteArray.readByte();
               this.elements.put(name, new Element(type, by));
            break;
            case CHAR:
               final char c = byteArray.readChar();
               this.elements.put(name, new Element(type, c));
            break;
            case DOUBLE:
               final double d = byteArray.readDouble();
               this.elements.put(name, new Element(type, d));
            break;
            case FLOAT:
               final float f = byteArray.readFloat();
               this.elements.put(name, new Element(type, f));
            break;
            case INT:
               final int i = byteArray.readInteger();
               this.elements.put(name, new Element(type, i));
            break;
            case LONG:
               final long l = byteArray.readLong();
               this.elements.put(name, new Element(type, l));
            break;
            case SHORT:
               final short s = byteArray.readShort();
               this.elements.put(name, new Element(type, s));
            break;
            case STRING:
               final String string = byteArray.readString();
               this.elements.put(name, new Element(type, string));
            break;
         }

         return true;
      }
      catch(final Exception exception)
      {
         Debug.printException(exception);

         return false;
      }
   }

   /**
    * Called when an element is force get and the type doesn't corresponds.<br>
    * Here have to convert the element of type to an other one.<br>
    * If the convertion is not allowed or impossible, just return {@code null}
    * 
    * @param name
    *           Name of the element to convert
    * @param value
    *           Actual element value
    * @param actual
    *           Actual element type
    * @param toConvert
    *           Desired type
    * @return Converted value or [{@code null} if convertion not allowed or impossible
    */
   protected abstract Object convert(String name, Object value, Type actual, Type toConvert);

   /**
    * Force get a {@link Binarizable} value. If the value is not a {@link Binarizable}
    * {@link #convert(String, Object, Type, Type)} is called to try to convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a {@link Binarizable} and the convertion is
    *           not allowed or impossible
    * @return Element value
    */
   protected final Binarizable forceGet(final String name, final Binarizable defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.BINARIZABLE)
      {
         return (Binarizable) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.BINARIZABLE);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.BINARIZABLE, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.BINARIZABLE, newValue));
      return (Binarizable) newValue;
   }

   /**
    * Force get a {@link Binarizable Binarizable[]} value. If the value is not a {@link Binarizable Binarizable[]}
    * {@link #convert(String, Object, Type, Type)} is called to try to convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a {@link Binarizable Binarizable[]} and the
    *           convertion is not allowed or impossible
    * @return Element value
    */
   protected final Binarizable[] forceGet(final String name, final Binarizable[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.ARRAY_OF_BINARIZABLE)
      {
         return (Binarizable[]) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.ARRAY_OF_BINARIZABLE);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.ARRAY_OF_BINARIZABLE, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_BINARIZABLE, newValue));
      return (Binarizable[]) newValue;
   }

   /**
    * Force get a boolean value. If the value is not a boolean {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a boolean and the convertion is not allowed
    *           or impossible
    * @return Element value
    */
   protected final boolean forceGet(final String name, final boolean defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      if(element.type == Type.BOOLEAN)
      {
         return (Boolean) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.BOOLEAN);

      if(newValue == null)
      {
         this.elements.put(name, new Element(Type.BOOLEAN, defaultValue));

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.BOOLEAN, newValue));
      return (Boolean) newValue;
   }

   /**
    * Force get a boolean[] value. If the value is not a boolean[] {@link #convert(String, Object, Type, Type)} is called to try
    * to convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a boolean[] and the convertion is not allowed
    *           or impossible
    * @return Element value
    */
   protected final boolean[] forceGet(final String name, final boolean[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.ARRAY_OF_BOOLEAN)
      {
         return (boolean[]) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.ARRAY_OF_BOOLEAN);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.ARRAY_OF_BOOLEAN, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_BOOLEAN, newValue));
      return (boolean[]) newValue;
   }

   /**
    * Force get a byte value. If the value is not a byte {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a byte and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final byte forceGet(final String name, final byte defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      if(element.type == Type.BYTE)
      {
         return (Byte) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.BYTE);

      if(newValue == null)
      {
         this.elements.put(name, new Element(Type.BYTE, defaultValue));

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.BYTE, newValue));
      return (Byte) newValue;
   }

   /**
    * Force get a byte[] value. If the value is not a byte[] {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a byte[] and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final byte[] forceGet(final String name, final byte[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.ARRAY_OF_BYTE)
      {
         return (byte[]) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.ARRAY_OF_BYTE);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.ARRAY_OF_BYTE, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_BYTE, newValue));
      return (byte[]) newValue;
   }

   /**
    * Force get a char value. If the value is not a char {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a char and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final char forceGet(final String name, final char defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      if(element.type == Type.CHAR)
      {
         return (Character) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.CHAR);

      if(newValue == null)
      {
         this.elements.put(name, new Element(Type.CHAR, defaultValue));

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.CHAR, newValue));
      return (Character) newValue;
   }

   /**
    * Force get a char[] value. If the value is not a char[] {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a char[] and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final char[] forceGet(final String name, final char[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.ARRAY_OF_CHAR)
      {
         return (char[]) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.ARRAY_OF_CHAR);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.ARRAY_OF_CHAR, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_CHAR, newValue));
      return (char[]) newValue;
   }

   /**
    * Force get a double value. If the value is not a double {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a double and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final double forceGet(final String name, final double defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      if(element.type == Type.DOUBLE)
      {
         return (Double) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.DOUBLE);

      if(newValue == null)
      {
         this.elements.put(name, new Element(Type.DOUBLE, defaultValue));

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.DOUBLE, newValue));
      return (Double) newValue;
   }

   /**
    * Force get a double[] value. If the value is not a double[] {@link #convert(String, Object, Type, Type)} is called to try
    * to convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a double and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final double[] forceGet(final String name, final double[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.ARRAY_OF_DOUBLE)
      {
         return (double[]) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.ARRAY_OF_DOUBLE);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.ARRAY_OF_DOUBLE, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_DOUBLE, newValue));
      return (double[]) newValue;
   }

   /**
    * Force get a float value. If the value is not a float {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a float and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final float forceGet(final String name, final float defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      if(element.type == Type.FLOAT)
      {
         return (Float) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.FLOAT);

      if(newValue == null)
      {
         this.elements.put(name, new Element(Type.FLOAT, defaultValue));

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.FLOAT, newValue));
      return (Float) newValue;
   }

   /**
    * Force get a float[] value. If the value is not a float[] {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a float[] and the convertion is not allowed
    *           or impossible
    * @return Element value
    */
   protected final float[] forceGet(final String name, final float[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.ARRAY_OF_FLOAT)
      {
         return (float[]) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.ARRAY_OF_FLOAT);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.ARRAY_OF_FLOAT, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_FLOAT, newValue));
      return (float[]) newValue;
   }

   /**
    * Force get a int value. If the value is not a int {@link #convert(String, Object, Type, Type)} is called to try to convert
    * it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a int and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final int forceGet(final String name, final int defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      if(element.type == Type.INT)
      {
         return (Integer) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.INT);

      if(newValue == null)
      {
         this.elements.put(name, new Element(Type.INT, defaultValue));

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.INT, newValue));
      return (Integer) newValue;
   }

   /**
    * Force get a int[] value. If the value is not a int[] {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a int[] and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final int[] forceGet(final String name, final int[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.ARRAY_OF_INT)
      {
         return (int[]) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.ARRAY_OF_INT);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.ARRAY_OF_INT, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_INT, newValue));
      return (int[]) newValue;
   }

   /**
    * Force get a long value. If the value is not a long {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a long and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final long forceGet(final String name, final long defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      if(element.type == Type.LONG)
      {
         return (Long) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.LONG);

      if(newValue == null)
      {
         this.elements.put(name, new Element(Type.LONG, defaultValue));

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.LONG, newValue));
      return (Long) newValue;
   }

   /**
    * Force get a long[] value. If the value is not a long[] {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a long[] and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final long[] forceGet(final String name, final long[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.ARRAY_OF_LONG)
      {
         return (long[]) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.ARRAY_OF_LONG);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.ARRAY_OF_LONG, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_LONG, newValue));
      return (long[]) newValue;
   }

   /**
    * Force get a short value. If the value is not a short {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a short and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final short forceGet(final String name, final short defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      if(element.type == Type.SHORT)
      {
         return (Short) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.SHORT);

      if(newValue == null)
      {
         this.elements.put(name, new Element(Type.SHORT, defaultValue));

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.SHORT, newValue));
      return (Short) newValue;
   }

   /**
    * Force get a short[] value. If the value is not a short[] {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a short[] and the convertion is not allowed
    *           or impossible
    * @return Element value
    */
   protected final short[] forceGet(final String name, final short[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.ARRAY_OF_SHORT)
      {
         return (short[]) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.ARRAY_OF_SHORT);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.ARRAY_OF_SHORT, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_SHORT, newValue));
      return (short[]) newValue;
   }

   /**
    * Force get a String value. If the value is not a String {@link #convert(String, Object, Type, Type)} is called to try to
    * convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a String and the convertion is not allowed or
    *           impossible
    * @return Element value
    */
   protected final String forceGet(final String name, final String defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.STRING)
      {
         return (String) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.STRING);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.STRING, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.STRING, newValue));
      return (String) newValue;
   }

   /**
    * Force get a String[] value. If the value is not a String[] {@link #convert(String, Object, Type, Type)} is called to try
    * to convert it
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if the element doesn't exist or if the type is not a String[] and the convertion is not allowed
    *           or impossible
    * @return Element value
    */
   protected final String[] forceGet(final String name, final String[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      if(element.type == Type.ARRAY_OF_STRING)
      {
         return (String[]) element.value;
      }

      final Object newValue = this.convert(name, element.value, element.type, Type.ARRAY_OF_STRING);

      if(newValue == null)
      {
         if(defaultValue != null)
         {
            this.elements.put(name, new Element(Type.ARRAY_OF_STRING, defaultValue));
         }

         return defaultValue;
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_STRING, newValue));
      return (String[]) newValue;
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final Binarizable value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.BINARIZABLE, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final Binarizable[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_BINARIZABLE, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final boolean value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      this.elements.put(name, new Element(Type.BOOLEAN, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final boolean[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_BOOLEAN, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final byte value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      this.elements.put(name, new Element(Type.BYTE, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final byte[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_BYTE, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final char value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      this.elements.put(name, new Element(Type.CHAR, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final char[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_CHAR, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final double value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      this.elements.put(name, new Element(Type.DOUBLE, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final double[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_DOUBLE, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final float value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      this.elements.put(name, new Element(Type.FLOAT, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final float[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_FLOAT, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final int value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      this.elements.put(name, new Element(Type.INT, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final int[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_INT, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final long value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      this.elements.put(name, new Element(Type.LONG, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final long[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_LONG, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final short value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      this.elements.put(name, new Element(Type.SHORT, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final short[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_SHORT, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final String value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.STRING, value));
   }

   /**
    * Store/modify an element. The type is forced, that is to say it is override
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    */
   protected final void forcePut(final String name, final String[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      this.elements.put(name, new Element(Type.ARRAY_OF_STRING, value));
   }

   /**
    * Obtain a {@link Binarizable} element. If the element is not a {@link Binarizable} an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a {@link Binarizable}
    */
   protected final Binarizable get(final String name, final Binarizable defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.BINARIZABLE);

      return (Binarizable) element.value;
   }

   /**
    * Obtain a {@link Binarizable Binarizable[]} element. If the element is not a {@link Binarizable Binarizable[]} an exception
    * is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a {@link Binarizable Binarizable[]}
    */
   protected final Binarizable[] get(final String name, final Binarizable[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue != null)
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.ARRAY_OF_BINARIZABLE);

      return (Binarizable[]) element.value;
   }

   /**
    * Obtain a boolean element. If the element is not a boolean an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a boolean
    */
   protected final boolean get(final String name, final boolean defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      this.checkType(element, Type.BOOLEAN);

      return (Boolean) element.value;
   }

   /**
    * Obtain a boolean[] element. If the element is not a boolean[] an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a boolean[]
    */
   protected final boolean[] get(final String name, final boolean[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue == null)
         {
            this.elements.remove(name);
         }
         else
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.ARRAY_OF_BOOLEAN);

      return (boolean[]) element.value;
   }

   /**
    * Obtain a byte element. If the element is not a byte an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a byte
    */
   protected final byte get(final String name, final byte defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      this.checkType(element, Type.BYTE);

      return (Byte) element.value;
   }

   /**
    * Obtain a byte[] element. If the element is not a byte[] an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a byte[]
    */
   protected final byte[] get(final String name, final byte[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue == null)
         {
            this.elements.remove(name);
         }
         else
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.ARRAY_OF_BYTE);

      return (byte[]) element.value;
   }

   /**
    * Obtain a char element. If the element is not a char an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a char
    */
   protected final char get(final String name, final char defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      this.checkType(element, Type.CHAR);

      return (Character) element.value;
   }

   /**
    * Obtain a char[] element. If the element is not a char[] an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a char[]
    */
   protected final char[] get(final String name, final char[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue == null)
         {
            this.elements.remove(name);
         }
         else
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.ARRAY_OF_CHAR);

      return (char[]) element.value;
   }

   /**
    * Obtain a double element. If the element is not a double an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a double
    */
   protected final double get(final String name, final double defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      this.checkType(element, Type.DOUBLE);

      return (Double) element.value;
   }

   /**
    * Obtain a double[] element. If the element is not a double[] an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a double[]
    */
   protected final double[] get(final String name, final double[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue == null)
         {
            this.elements.remove(name);
         }
         else
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.ARRAY_OF_DOUBLE);

      return (double[]) element.value;
   }

   /**
    * Obtain a float element. If the element is not a float an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a float
    */
   protected final float get(final String name, final float defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      this.checkType(element, Type.FLOAT);

      return (Float) element.value;
   }

   /**
    * Obtain a float[] element. If the element is not a float[] an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a float[]
    */
   protected final float[] get(final String name, final float[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue == null)
         {
            this.elements.remove(name);
         }
         else
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.ARRAY_OF_FLOAT);

      return (float[]) element.value;
   }

   /**
    * Obtain a int element. If the element is not a int an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a int
    */
   protected final int get(final String name, final int defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      this.checkType(element, Type.INT);

      return (Integer) element.value;
   }

   /**
    * Obtain a int[] element. If the element is not a int[] an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a int[]
    */
   protected final int[] get(final String name, final int[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue == null)
         {
            this.elements.remove(name);
         }
         else
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.ARRAY_OF_INT);

      return (int[]) element.value;
   }

   /**
    * Obtain a long element. If the element is not a long an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a long
    */
   protected final long get(final String name, final long defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      this.checkType(element, Type.LONG);

      return (Long) element.value;
   }

   /**
    * Obtain a long[] element. If the element is not a long[] an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a long[]
    */
   protected final long[] get(final String name, final long[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue == null)
         {
            this.elements.remove(name);
         }
         else
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.ARRAY_OF_LONG);

      return (long[]) element.value;
   }

   /**
    * Obtain a short element. If the element is not a short an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a short
    */
   protected final short get(final String name, final short defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         this.put(name, defaultValue);

         return defaultValue;
      }

      this.checkType(element, Type.SHORT);

      return (Short) element.value;
   }

   /**
    * Obtain a short[] element. If the element is not a short[] an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a short[]
    */
   protected final short[] get(final String name, final short[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue == null)
         {
            this.elements.remove(name);
         }
         else
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.ARRAY_OF_SHORT);

      return (short[]) element.value;
   }

   /**
    * Obtain a String element. If the element is not a String an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a String
    */
   protected final String get(final String name, final String defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue == null)
         {
            this.elements.remove(name);
         }
         else
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.STRING);

      return (String) element.value;
   }

   /**
    * Obtain a String[] element. If the element is not a String[] an exception is throw
    * 
    * @param name
    *           Element name
    * @param defaultValue
    *           Value to return if element does'nt exist
    * @return Element value
    * @throws IllegalArgumentException
    *            If the elemnt is not a String[]
    */
   protected final String[] get(final String name, final String[] defaultValue)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      final Element element = this.elements.get(name);

      if(element == null)
      {
         if(defaultValue == null)
         {
            this.elements.remove(name);
         }
         else
         {
            this.put(name, defaultValue);
         }

         return defaultValue;
      }

      this.checkType(element, Type.ARRAY_OF_STRING);

      return (String[]) element.value;
   }

   /**
    * Obtain an element type. If the element not exists, {@code null} is return
    * 
    * @param name
    *           Element name
    * @return Element type or {@code null} if not exists
    */
   protected final Type getType(final String name)
   {
      final Element element = this.elements.get(name);

      if(element == null)
      {
         return null;
      }

      return element.type;
   }

   /**
    * Called at initilization, usefull to initialize some element by default
    */
   protected abstract void initializeFields();

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final Binarizable value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.BINARIZABLE, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.BINARIZABLE);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final Binarizable[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.ARRAY_OF_BINARIZABLE, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.ARRAY_OF_BINARIZABLE);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final boolean value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.BOOLEAN, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.BOOLEAN);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final boolean[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.ARRAY_OF_BOOLEAN, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.ARRAY_OF_BOOLEAN);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final byte value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.BYTE, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.BYTE);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final byte[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.ARRAY_OF_BYTE, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.ARRAY_OF_BYTE);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final char value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.CHAR, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.CHAR);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final char[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.ARRAY_OF_CHAR, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.ARRAY_OF_CHAR);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final double value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.DOUBLE, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.DOUBLE);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final double[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.ARRAY_OF_DOUBLE, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.ARRAY_OF_DOUBLE);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final float value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.FLOAT, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.FLOAT);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final float[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.ARRAY_OF_FLOAT, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.ARRAY_OF_FLOAT);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final int value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.INT, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.INT);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final int[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.ARRAY_OF_INT, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.ARRAY_OF_INT);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final long value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.LONG, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.LONG);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final long[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.ARRAY_OF_LONG, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.ARRAY_OF_LONG);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final short value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.SHORT, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.SHORT);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final short[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.ARRAY_OF_SHORT, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.ARRAY_OF_SHORT);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final String value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.STRING, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.STRING);

      element.value = value;
   }

   /**
    * Change/define an element value. If the element alredy exists and the type is different, an exception is throw
    * 
    * @param name
    *           Element name
    * @param value
    *           Element value
    * @throws IllegalArgumentException
    *            If exists an type not coorect
    */
   protected final void put(final String name, final String[] value)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }

      Element element = this.elements.get(name);

      if(element == null)
      {
         element = new Element(Type.ARRAY_OF_STRING, value);
         this.elements.put(name, element);

         return;
      }

      this.checkType(element, Type.ARRAY_OF_STRING);

      element.value = value;
   }

   /**
    * Remove an element
    * 
    * @param name
    *           Element name
    */
   protected final void remove(final String name)
   {
      this.elements.remove(name);
   }

   /**
    * Parse the byte array to initialiaze elements values <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param byteArray
    *           Byte array to parse
    * @see Binarizable#parseBinary(ByteArray)
    */
   @Override
   public final void parseBinary(final ByteArray byteArray)
   {
      this.elements.clear();

      while(this.readNextElement(byteArray) == true)
      {
         ;
      }
   }

   /**
    * Serialize elements inside a byte array <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param byteArray
    *           Byte array where write
    * @see Binarizable#serializeBinary(ByteArray)
    */
   @Override
   public final void serializeBinary(final ByteArray byteArray)
   {
      Element element;
      Type type;
      for(final String name : this.elements.keySet())
      {
         element = this.elements.get(name);
         type = element.type;

         byteArray.writeString(name);
         byteArray.writeEnum(type);

         switch(type)
         {
            case ARRAY_OF_BINARIZABLE:
               byteArray.writeString(element.value.getClass().getComponentType().getName());
               byteArray.writeBinarizableArray((Binarizable[]) element.value);
            break;
            case ARRAY_OF_BOOLEAN:
               byteArray.writeBooleanArray((boolean[]) element.value);
            break;
            case ARRAY_OF_BYTE:
               byteArray.writeByteArray((byte[]) element.value);
            break;
            case ARRAY_OF_CHAR:
               byteArray.writeCharArray((char[]) element.value);
            break;
            case ARRAY_OF_DOUBLE:
               byteArray.writeDoubleArray((double[]) element.value);
            break;
            case ARRAY_OF_FLOAT:
               byteArray.writeFloatArray((float[]) element.value);
            break;
            case ARRAY_OF_INT:
               byteArray.writeIntegerArray((int[]) element.value);
            break;
            case ARRAY_OF_LONG:
               byteArray.writeLongArray((long[]) element.value);
            break;
            case ARRAY_OF_SHORT:
               byteArray.writeShortArray((short[]) element.value);
            break;
            case ARRAY_OF_STRING:
               byteArray.writeStringArray((String[]) element.value);
            break;
            case BINARIZABLE:
               byteArray.writeString(element.value.getClass().getName());
               byteArray.writeBinarizableArray((Binarizable) element.value);
            break;
            case BOOLEAN:
               byteArray.writeBoolean((Boolean) element.value);
            break;
            case BYTE:
               byteArray.writeByte((Byte) element.value);
            break;
            case CHAR:
               byteArray.writeChar((Character) element.value);
            break;
            case DOUBLE:
               byteArray.writeDouble((Double) element.value);
            break;
            case FLOAT:
               byteArray.writeFloat((Float) element.value);
            break;
            case INT:
               byteArray.writeInteger((Integer) element.value);
            break;
            case LONG:
               byteArray.writeLong((Long) element.value);
            break;
            case SHORT:
               byteArray.writeShort((Short) element.value);
            break;
            case STRING:
               byteArray.writeString((String) element.value);
            break;
         }
      }

      // Terminate whith a null name
      byteArray.writeString(null);
   }
}