/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package nars.struct;

import com.thoughtworks.xstream.core.util.Base64Encoder;
import javolution.context.LocalContext;
import javolution.io.UTF8ByteBufferReader;
import javolution.io.UTF8ByteBufferWriter;
import javolution.lang.MathLib;
import javolution.lang.Realtime;
import javolution.text.TextBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * <p> Equivalent to a  <code>C/C++ struct</code>; this class confers
 *     interoperability between Java classes and C/C++ struct.</p>
 *
 * <p> Unlike <code>C/C++</code>, the storage layout of Java objects is not
 *     determined by the compiler. The layout of objects in memory is deferred
 *     to run time and determined by the interpreter (or just-in-time compiler).
 *     This approach allows for dynamic loading and binding; but also makes
 *     interfacing with <code>C/C++</code> code difficult. Hence, this class for
 *     which the memory layout is defined by the initialization order of the
 *     {@link Fuct}'s {@link Member members} and follows the same wordSize
 *      rules as <code>C/C++ structs</code>.</p>
 *
 * <p> This class (as well as the {@link Union} sub-class) facilitates:
 *     <ul>
 *     <li> Memory sharing between Java applications and native libraries.</li>
 *     <li> Direct encoding/decoding of streams for which the structure
 *          is defined by legacy C/C++ code.</li>
 *     <li> Serialization/deserialization of Java objects (complete control,
 *          e.g. no class header)</li>
 *     <li> Mapping of Java objects to physical addresses (with JNI).</li>
 *     </ul></p>
 *
 * <p> Because of its one-to-one mapping, it is relatively easy to convert C
 *     header files (e.g. OpenGL bindings) to Java {@link Fuct}/{@link Union}
 *     using simple text macros. Here is an example of C struct:<code><pre>
 *     enum Gender{MALE, FEMALE};
 *     struct Date {
 *         unsigned short year;
 *         unsigned byte month;
 *         unsigned byte day;
 *     };
 *     struct Student {
 *         enum Gender gender;
 *         char        name[64];
 *         struct Date birth;
 *         float       grades[10];
 *         Student*    next;
 *     };</pre></code>
 *     and here is the Java equivalent using this class:[code]
 *     public enum Gender { MALE, FEMALE };
 *     public static class Date extends Struct {
 *         public final Unsigned16 year = new Unsigned16();
 *         public final Unsigned8 month = new Unsigned8();
 *         public final Unsigned8 day   = new Unsigned8();
 *     }
 *     public static class Student extends Struct {
 *         public final Enum32<Gender>       gender = new Enum32<Gender>(Gender.values());
 *         public final UTF8String           name   = new UTF8String(64);
 *         public final Date                 birth  = inner(new Date());
 *         public final Float32[]            grades = array(new Float32[10]);
 *         public final Reference32<Student> next   =  new Reference32<Student>();
 *     }[/code]
 *     Struct's members are directly accessible:[code]
 *     Student student = new Student();
 *     student.gender.set(Gender.MALE);
 *     student.name.set("John Doe"); // Null terminated (C compatible)
 *     int age = 2003 - student.birth.year.get();
 *     student.grades[2].set(12.5f);
 *     student = student.next.get();[/code]</p>
 *
 * <p> Applications can work with the raw {@link #getByteBuffer() bytes}
 *     directly. The following illustrate how {@link Fuct} can be used to
 *     decode/encode UDP messages directly:[code]
 *     class UDPMessage extends Struct {
 *          Unsigned16 xxx = new Unsigned16();
 *          ...
 *     }
 *     public void run() {
 *         byte[] bytes = new byte[1024];
 *         DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
 *         UDPMessage message = new UDPMessage();
 *         message.setByteBuffer(ByteBuffer.wrap(bytes), 0);
 *         // packet and message are now two different views of the same data.
 *         while (isListening) {
 *             multicastSocket.receive(packet);
 *             int xxx = message.xxx.get();
 *             ... // Process message fields directly.
 *         }
 *     }[/code]</p>
 *
 * <p> It is relatively easy to map instances of this class to any physical
 *     address using
 *     <a href="http://java.sun.com/docs/books/tutorial/native1.1/index.html">
 *     JNI</a>. Here is an example:[code]
 *     import java.nio.ByteBuffer;
 *     class Clock extends Struct { // Hardware clock mapped to memory.
 *         Unsigned16 seconds  = new Unsigned16(5); // unsigned short seconds:5
 *         Unsigned16 minutes  = new Unsigned16(5); // unsigned short minutes:5
 *         Unsigned16 hours    = new Unsigned16(4); // unsigned short hours:4
 *         Clock() {
 *             setByteBuffer(Clock.nativeBuffer(), 0);
 *         }
 *         private static native ByteBuffer nativeBuffer();
 *     }[/code]
 *     Below is the <code>nativeBuffer()</code> implementation
 *     (<code>Clock.c</code>):[code]
 *     #include <jni.h>
 *     #include "Clock.h" // Generated using javah
 *     JNIEXPORT jobject JNICALL Java_Clock_nativeBuffer (JNIEnv *env, jclass) {
 *         return (*env)->NewDirectByteBuffer(env, clock_address, buffer_size)
 *     }[/code]</p>
 *
 * <p> Bit-fields are supported (see <code>Clock</code> example above).
 *     Bit-fields allocation order is defined by the Struct {@link #byteOrder}
 *     return value. Leftmost bit to rightmost bit if
 *     <code>BIG_ENDIAN</code> and rightmost bit to leftmost bit if
 *     <code>LITTLE_ENDIAN</code> (same layout as Microsoft Visual C++).
 *     C/C++ Bit-fields cannot straddle the storage-unit boundary as defined
 *     by their base type (padding is inserted at the end of the first bit-field
 *     and the second bit-field is put into the next storage unit).
 *     It is possible to avoid bit padding by using the {@link BitField}
 *     member (or a sub-class). In which case the allocation order is always
 *     from the leftmost to the rightmost bit (same as <code>BIG_ENDIAN</code>).
 *     </p>
 *
 * <p> Finally, it is possible to change the {@link #set ByteBuffer}
 *     and/or the Struct {@link #pos position} in its
 *     <code>ByteBuffer</code> to allow for a single {@link Fuct} object to
 *     encode/decode multiple memory mapped instances.</p>
 *
 * <p><i>Note: Because Struct/Union are basically wrappers around
 *             <code>java.nio.ByteBuffer</code>, tutorials/usages for the
 *             Java NIO package are directly applicable to Struct/Union.</i></p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.5.1, April 1, 2010
 */
@SuppressWarnings("unchecked")
@Realtime
public class Fuct {

    /**
     * Configurable holding the maximum wordSize in bytes
     * (default <code>4</code>). Should be a value greater or equal to 1.
     */
    public static final LocalContext.Parameter<Integer> MAXIMUM_ALIGNMENT = new LocalContext.Parameter<Integer>() {
        @Override
        protected Integer getDefault() {
            return 4;
        }
    };

    /**
     * Holds the outer struct if any.
     */
    Fuct outer;
    /**
     * Holds the byte buffer backing the struct (top struct).
     */
    protected ByteBuffer bb;
    /**
     * Holds the offset of this struct relative to the outer struct or
     * to the byte buffer if there is no outer.
     */
    int outerOffset;
    /**
     * Holds this struct alignment in bytes (largest word size of its members).
     */
    int alignment = 1;
    /**
     * Holds this struct's length.
     */
    int length;
    /**
     * Holds the index position during construction.
     * This is the index a the first unused byte available.
     */
    int index;
    /**
     * Holds the word size during construction (for bit fields).
     * This is the size of the last word used.
     */
    int words;
    /**
     * Holds the bits used in the word during construction (for bit fields).
     * This is the number of bits used in the last word.
     */
    int bitsUsed;
    /**
     * Indicates if the index has to be reset for each new field (
     * <code>true</code> only for Union subclasses).
     */
    boolean resetIndex;
    /**
     * Holds bytes array for Stream I/O when byteBuffer has no intrinsic array.
     */
    byte[] bytes;

    /**
     * Default constructor.
     */
    public Fuct() {
        resetIndex = isUnion();
    }

    /**
     * Returns the size in bytes of this struct. The size includes
     * tail padding to satisfy the struct word size requirement
     * (defined by the largest word size of its {@link Member members}).
     *
     * @return the C/C++ <code>sizeof(this)</code>.
     */
    public final int size() {
        return (alignment <= 1) ? length
                : ((length + alignment - 1) / alignment) * alignment;
    }

    /**
     * Returns the outer of this struct or <code>null</code> if this struct
     * is not an inner struct.
     *
     * @return the outer struct or <code>null</code>.
     */
    public Fuct outer() {
        return outer;
    }

    /**
     * Returns the byte buffer for this struct. This method will allocate
     * a new <b>direct</b> buffer if none has been set.
     * <p>
     * <p> Changes to the buffer's content are visible in this struct,
     * and vice versa.</p>
     * <p> The buffer of an inner struct is the same as its parent struct.</p>
     * <p> If no byte buffer has been {@link Fuct#set set},
     * a direct buffer is allocated with a capacity equals to this
     * struct's {@link Fuct#size() size}.</p>
     *
     * @return the current byte buffer or a new direct buffer if none set.
     * @see #set
     */
    public ByteBuffer getByteBuffer() {
        Fuct f = this;
        while (true) {
            if (f.outer != null) {
                f = f.outer;
                continue;
            }
            return (f.bb != null) ? f.bb : f.newBuffer();
        }
    }

    private synchronized ByteBuffer newBuffer() {
        if (bb != null) return bb; // Synchronized check.
        ByteBuffer bf = ByteBuffer.allocateDirect(size());
        bf.order(byteOrder());
        set(bf, 0);
        return bb;
    }

    /**
     * Sets the current byte buffer for this struct.
     * The specified byte buffer can be mapped to memory for direct memory
     * access or can wrap a shared byte array for I/O purpose
     * (e.g. <code>DatagramPacket</code>).
     * The capacity of the specified byte buffer should be at least the
     * {@link Fuct#size() size} of this struct plus the offset position.
     *
     * @param byteBuffer the new byte buffer.
     * @param position the position of this struct in the specified byte buffer.
     * @return <code>this</code>
     * @throws IllegalArgumentException if the specified byteBuffer has a
     *         different byte order than this struct.
     * @throws UnsupportedOperationException if this struct is an inner struct.
     * @see #byteOrder()
     */
    public final Fuct set(ByteBuffer byteBuffer, int position) {
        if (byteBuffer.order() != byteOrder()) throw new IllegalArgumentException(
                "The byte order of the specified byte buffer"
                        + " is different from this struct byte order");
        if (outer != null) throw new UnsupportedOperationException(
                "Inner struct byte buffer is inherited from outer");
        bb = byteBuffer;
        outerOffset = position;
        return this;
    }


    /**
     * Sets the byte position of this struct within its byte buffer.
     *
     * @param position the position of this struct in its byte buffer.
     * @return <code>this</code>
     * @throws UnsupportedOperationException if this struct is an inner struct.
     */
    public final Fuct pos(final int position) {
        //final ByteBuffer b = getByteBuffer();
        /*if (byteBuffer.order() != byteOrder()) throw new IllegalArgumentException(
                "The byte order of the specified byte buffer"
                        + " is different from this struct byte order");*/
        /*if (outer != null) throw new UnsupportedOperationException(
                "Inner struct byte buffer is inherited from outer");*/
        outerOffset = position;
        return this;
    }



    /**
     * Returns the absolute byte position of this struct within its associated
     * {@link #getByteBuffer byte buffer}.
     *
     * @return the absolute position of this struct (can be an inner struct)
     *         in the byte buffer.
     */
    public final int pos() {
        return (outer != null) ? outer.pos() + outerOffset
                : outerOffset;
    }

    /**
     * Reads this struct from the specified input stream
     * (convenience method when using Stream I/O). For better performance,
     * use of Block I/O (e.g. <code>java.nio.channels.*</code>) is recommended.
     *  This method behaves appropriately when not all of the data is available
     *  from the input stream. Incomplete data is extremely common when the 
     *  input stream is associated with something like a TCP connection. 
     *  The typical usage pattern in those scenarios is to repeatedly call
     *  read() until the entire message is received.
     *
     * @param in the input stream being read from.
     * @return the number of bytes read (typically the {@link #size() size}
     *         of this struct.
     * @throws IOException if an I/O error occurs.
     */
    public int read(InputStream in) throws IOException {
        ByteBuffer buffer = getByteBuffer();
        int size = size();
        int remaining = size - buffer.position();
        if (remaining == 0) remaining = size;// at end so move to beginning
        int alreadyRead = size - remaining; // typically 0
        if (buffer.hasArray()) {
            int offset = buffer.arrayOffset() + pos();
            int bytesRead = in
                    .read(buffer.array(), offset + alreadyRead, remaining);
            buffer.position(pos() + alreadyRead + bytesRead
                    - offset);
            return bytesRead;
        } else {
            synchronized (buffer) {
                if (bytes == null) {
                    bytes = new byte[size()];
                }
                int bytesRead = in.read(bytes, 0, remaining);
                buffer.position(pos() + alreadyRead);
                buffer.put(bytes, 0, bytesRead);
                return bytesRead;
            }
        }
    }

    /**
     * Writes this struct to the specified output stream
     * (convenience method when using Stream I/O). For better performance,
     * use of Block I/O (e.g. <code>java.nio.channels.*</code>) is recommended.
     *
     * @param out the output stream to write to.
     * @throws IOException if an I/O error occurs.
     */
    public void write(OutputStream out) throws IOException {
        ByteBuffer buffer = getByteBuffer();
        if (buffer.hasArray()) {
            int offset = buffer.arrayOffset() + pos();
            out.write(buffer.array(), offset, size());
        } else {
            synchronized (buffer) {
                if (bytes == null) {
                    bytes = new byte[size()];
                }
                buffer.position(pos());
                buffer.get(bytes);
                out.write(bytes);
            }
        }
    }

    /**
     * Returns this struct address. This method allows for structs
     * to be referenced (e.g. pointer) from other structs.
     *
     * @return the struct memory address.
     * @throws UnsupportedOperationException if the struct buffer is not
     *         a direct buffer.
     * @see    Reference32
     * @see    Reference64
     */
    public final long address() {
        ByteBuffer thisBuffer = this.getByteBuffer();
        if (thisBuffer instanceof sun.nio.ch.DirectBuffer)
            return ((sun.nio.ch.DirectBuffer)thisBuffer).address();
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the <code>String</code> representation of this struct
     * in the form of its constituing bytes (hexadecimal). For example:[code]
     *     public static class Student extends Struct {
     *         Utf8String name  = new Utf8String(16);
     *         Unsigned16 year  = new Unsigned16();
     *         Float32    grade = new Float32();
     *     }
     *     Student student = new Student();
     *     student.name.set("John Doe");
     *     student.year.set(2003);
     *     student.grade.set(12.5f);
     *     System.out.println(student);
     *
     *     4A 6F 68 6E 20 44 6F 65 00 00 00 00 00 00 00 00
     *     07 D3 00 00 41 48 00 00[/code]
     *
     * @return a hexadecimal representation of the bytes content for this
     *         struct.
     */
    public String toString() {
        TextBuilder tmp = new TextBuilder();
        final int size = size();
        final ByteBuffer buffer = getByteBuffer();
        final int start = pos();
        for (int i = 0; i < size; i++) {
            int b = buffer.get(start + i) & 0xFF;
            tmp.append(HEXA[b >> 4]);
            tmp.append(HEXA[b & 0xF]);
            tmp.append(((i & 0xF) == 0xF) ? '\n' : ' ');
        }
        return tmp.toString();
    }

    public String toStringBase64() {
        byte[] b = new byte[size()];
        getByteBuffer().get(b, pos(), size());
        return new Base64Encoder().encode(b);

    }

    private static final char[] HEXA = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    ///////////////////
    // CONFIGURATION //
    ///////////////////
    /**
     * Indicates if this struct's members are mapped to the same location
     * in memory (default <code>false</code>). This method is useful for
     * applications extending {@link Fuct} with new member types in order to
     * create unions from these new structs. For example:[code]
     * public abstract class FortranStruct extends Struct {
     *     public class FortranString extends Member {...}
     *     protected FortranString[] array(FortranString[] array, int stringLength) { ... }
     * }
     * public abstract class FortranUnion extends FortranStruct {
     *     // Inherits new members and methods.
     *     public final isUnion() {
     *         return true;
     *     }
     * }[/code]
     *
     * @return <code>true</code> if this struct's members are mapped to
     *         to the same location in memory; <code>false</code>
     *         otherwise.
     * @see Union
     */
    public boolean isUnion() {
        return false;
    }

    /**
     * Returns the byte order for this struct (configurable).
     * The byte order is inherited by inner structs. Sub-classes may change
     * the byte order by overriding this method. For example:[code]
     * public class TopStruct extends Struct {
     *     ... // Members initialization.
     *     public ByteOrder byteOrder() {
     *         // TopStruct and its inner structs use hardware byte order.
     *         return ByteOrder.nativeOrder();
     *    }
     * }}[/code]</p></p>
     *
     * @return the byte order when reading/writing multibyte values
     *         (default: network byte order, <code>BIG_ENDIAN</code>).
     */
    public ByteOrder byteOrder() {
        return (outer != null) ? outer.byteOrder() : ByteOrder.BIG_ENDIAN;
    }

    /**
     * Indicates if this struct is packed (configurable).
     * By default, {@link Member members} of a struct are aligned on the
     * boundary corresponding to the member base type; padding is performed
     * if necessary. This directive is <b>not</b> inherited by inner structs.
     * Sub-classes may change the packing directive by overriding this method.
     * For example:[code]
     * public class MyStruct extends Struct {
     *     ... // Members initialization.
     *     public boolean isPacked() {
     *         return true; // MyStruct is packed.
     *     }
     * }}[/code]
     *
     * @return <code>true</code> if word size requirements are ignored.
     *         <code>false</code> otherwise (default).
     */
    public boolean isPacked() {
        return false;
    }

    /**
     * Defines the specified struct as inner of this struct.
     *
     * @param struct the inner struct.
     * @return the specified struct.
     * @throws IllegalArgumentException if the specified struct is already
     *         an inner struct.
     */
    protected <S extends Fuct> S inner(S struct) {
        if (struct.outer != null) throw new IllegalArgumentException(
                "struct: Already an inner struct");
        Member inner = new Member(struct.size() << 3, struct.alignment); // Update indexes.
        struct.outer = this;
        struct.outerOffset = inner.offset();
        return struct;
    }

    /**
     * Defines the specified array of structs as inner structs.
     * The array is populated if necessary using the struct component
     * default constructor (which must be public).
     *
     * @param structs the struct array.
     * @return the specified struct array.
     * @throws IllegalArgumentException if the specified array contains
     *         inner structs.
     */
    protected <S extends Fuct> S[] array(S[] structs) {
        Class<?> structClass = null;
        boolean resetIndexSaved = resetIndex;
        if (resetIndex) {
            index = 0;
            resetIndex = false; // Ensures the array elements are sequential.
        }
        for (int i = 0; i < structs.length;) {
            S struct = structs[i];
            if (struct == null) {
                try {
                    if (structClass == null) {
                        String arrayName = structs.getClass().getName();
                        String structName = arrayName.substring(2,
                                arrayName.length() - 1);
                        structClass = Class.forName(structName);
                        if (structClass == null) { throw new IllegalArgumentException(
                                "Struct class: " + structName + " not found"); }
                    }
                    struct = (S) structClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
            structs[i++] = inner(struct);
        }
        resetIndex = resetIndexSaved;
        return structs;
    }

    /**
     * Defines the specified two-dimensional array of structs as inner
     * structs. The array is populated if necessary using the struct component
     * default constructor (which must be public).
     *
     * @param structs the two dimensional struct array.
     * @return the specified struct array.
     * @throws IllegalArgumentException if the specified array contains
     *         inner structs.
     */
    protected <S extends Fuct> S[][] array(S[][] structs) {
        boolean resetIndexSaved = resetIndex;
        if (resetIndex) {
            index = 0;
            resetIndex = false; // Ensures the array elements are sequential.
        }
        for (int i = 0; i < structs.length; i++) {
            array(structs[i]);
        }
        resetIndex = resetIndexSaved;
        return structs;
    }

    /**
     * Defines the specified three dimensional array of structs as inner
     * structs. The array is populated if necessary using the struct component
     * default constructor (which must be public).
     *
     * @param structs the three dimensional struct array.
     * @return the specified struct array.
     * @throws IllegalArgumentException if the specified array contains
     *         inner structs.
     */
    protected <S extends Fuct> S[][][] array(S[][][] structs) {
        boolean resetIndexSaved = resetIndex;
        if (resetIndex) {
            index = 0;
            resetIndex = false; // Ensures the array elements are sequential.
        }
        for (int i = 0; i < structs.length; i++) {
            array(structs[i]);
        }
        resetIndex = resetIndexSaved;
        return structs;
    }

    /**
     * Defines the specified array member. For predefined members,
     * the array is populated when empty; custom members should use
     * literal (populated) arrays.
     *
     * @param  arrayMember the array member.
     * @return the specified array member.
     * @throws UnsupportedOperationException if the specified array
     *         is empty and the member type is unknown.
     */
    protected <M extends Member> M[] array(M[] arrayMember) {
        boolean resetIndexSaved = resetIndex;
        if (resetIndex) {
            index = 0;
            resetIndex = false; // Ensures the array elements are sequential.
        }
        if (BOOL.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;) {
                arrayMember[i++] = (M) this.new Bool();
            }
        } else if (SIGNED_8.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;) {
                arrayMember[i++] = (M) this.new Signed8();
            }
        } else if (UNSIGNED_8.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;) {
                arrayMember[i++] = (M) this.new Unsigned8();
            }
        } else if (SIGNED_16.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;) {
                arrayMember[i++] = (M) this.new Signed16();
            }
        } else if (UNSIGNED_16.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;) {
                arrayMember[i++] = (M) this.new Unsigned16();
            }
        } else if (SIGNED_32.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;) {
                arrayMember[i++] = (M) this.new Signed32();
            }
        } else if (UNSIGNED_32.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;) {
                arrayMember[i++] = (M) this.new Unsigned32();
            }
        } else if (SIGNED_64.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;) {
                arrayMember[i++] = (M) this.new Signed64();
            }
        } else if (FLOAT_32.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;) {
                arrayMember[i++] = (M) this.new Float32();
            }
        } else if (FLOAT_64.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;) {
                arrayMember[i++] = (M) this.new Float64();
            }
        } else {
            throw new UnsupportedOperationException(
                    "Cannot create member elements, the arrayMember should "
                            + "contain the member instances instead of null");
        }
        resetIndex = resetIndexSaved;
        return arrayMember;
    }

    private static final Class<? extends Bool[]> BOOL = new Bool[0].getClass();
    private static final Class<? extends Signed8[]> SIGNED_8 = new Signed8[0]
            .getClass();
    private static final Class<? extends Unsigned8[]> UNSIGNED_8 = new Unsigned8[0]
            .getClass();
    private static final Class<? extends Signed16[]> SIGNED_16 = new Signed16[0]
            .getClass();
    private static final Class<? extends Unsigned16[]> UNSIGNED_16 = new Unsigned16[0]
            .getClass();
    private static final Class<? extends Signed32[]> SIGNED_32 = new Signed32[0]
            .getClass();
    private static final Class<? extends Unsigned32[]> UNSIGNED_32 = new Unsigned32[0]
            .getClass();
    private static final Class<? extends Signed64[]> SIGNED_64 = new Signed64[0]
            .getClass();
    private static final Class<? extends Float32[]> FLOAT_32 = new Float32[0]
            .getClass();
    private static final Class<? extends Float64[]> FLOAT_64 = new Float64[0]
            .getClass();

    /**
     * Defines the specified two-dimensional array member. For predefined
     * members, the array is populated when empty; custom members should use
     * literal (populated) arrays.
     *
     * @param  arrayMember the two-dimensional array member.
     * @return the specified array member.
     * @throws UnsupportedOperationException if the specified array
     *         is empty and the member type is unknown.
     */
    protected <M extends Member> M[][] array(M[][] arrayMember) {
        boolean resetIndexSaved = resetIndex;
        if (resetIndex) {
            index = 0;
            resetIndex = false; // Ensures the array elements are sequential.
        }
        for (int i = 0; i < arrayMember.length; i++) {
            array(arrayMember[i]);
        }
        resetIndex = resetIndexSaved;
        return arrayMember;
    }

    /**
     * Defines the specified three-dimensional array member. For predefined
     * members, the array is populated when empty; custom members should use
     * literal (populated) arrays.
     *
     * @param  arrayMember the three-dimensional array member.
     * @return the specified array member.
     * @throws UnsupportedOperationException if the specified array
     *         is empty and the member type is unknown.
     */
    protected <M extends Member> M[][][] array(M[][][] arrayMember) {
        boolean resetIndexSaved = resetIndex;
        if (resetIndex) {
            index = 0;
            resetIndex = false; // Ensures the array elements are sequential.
        }
        for (int i = 0; i < arrayMember.length; i++) {
            array(arrayMember[i]);
        }
        resetIndex = resetIndexSaved;
        return arrayMember;
    }

    /**
     * Defines the specified array of UTF-8 strings, all strings having the
     * specified length (convenience method).
     *
     * @param  array the string array.
     * @param stringLength the length of the string elements.
     * @return the specified string array.
     */
    protected UTF8String[] array(UTF8String[] array, int stringLength) {
        boolean resetIndexSaved = resetIndex;
        if (resetIndex) {
            index = 0;
            resetIndex = false; // Ensures the array elements are sequential.
        }
        for (int i = 0; i < array.length; i++) {
            array[i] = new UTF8String(stringLength);
        }
        resetIndex = resetIndexSaved;
        return array;
    }


    
    /**
     * Reads the specified bits from this Struct as an long (signed) integer
     * value.
     *
     * @param  bitOffset the bit start position in the Struct.
     * @param  bitSize the number of bits.
     * @return the specified bits read as a signed long.
     * @throws IllegalArgumentException if
     *         <code>(bitOffset + bitSize - 1) / 8 >= this.size()</code>
     */
    public long readBits(int bitOffset, int bitSize) {
        if ((bitOffset + bitSize - 1) >> 3 >= this.size()) throw new IllegalArgumentException(
                "Attempt to read outside the Struct");
        int offset = bitOffset >> 3;
        int bitStart = bitOffset - (offset << 3);
        bitStart = (byteOrder() == ByteOrder.BIG_ENDIAN) ? bitStart : 64
                - bitSize - bitStart;
        int index = pos() + offset;
        long value = readByteBufferLong(index);
        value <<= bitStart; // Clears preceding bits.
        value >>= (64 - bitSize); // Signed shift.
        return value;
    }

    private long readByteBufferLong(int index) {
        ByteBuffer byteBuffer = getByteBuffer();
        if (index + 8 < byteBuffer.limit()) return byteBuffer.getLong(index);
        // Else possible buffer overflow.
        if (byteBuffer.order() == ByteOrder.LITTLE_ENDIAN) {
            return (readByte(index, byteBuffer) & 0xff)
                    + ((readByte(++index, byteBuffer) & 0xff) << 8)
                    + ((readByte(++index, byteBuffer) & 0xff) << 16)
                    + ((readByte(++index, byteBuffer) & 0xffL) << 24)
                    + ((readByte(++index, byteBuffer) & 0xffL) << 32)
                    + ((readByte(++index, byteBuffer) & 0xffL) << 40)
                    + ((readByte(++index, byteBuffer) & 0xffL) << 48)
                    + ((readByte(++index, byteBuffer) & 0xffL) << 56);
        } else {
            return (((long) readByte(index, byteBuffer)) << 56)
                    + ((readByte(++index, byteBuffer) & 0xffL) << 48)
                    + ((readByte(++index, byteBuffer) & 0xffL) << 40)
                    + ((readByte(++index, byteBuffer) & 0xffL) << 32)
                    + ((readByte(++index, byteBuffer) & 0xffL) << 24)
                    + ((readByte(++index, byteBuffer) & 0xff) << 16)
                    + ((readByte(++index, byteBuffer) & 0xff) << 8)
                    + (readByte(++index, byteBuffer) & 0xffL);
        }
    }

    private static byte readByte(int index, ByteBuffer byteBuffer) {
        return (index < byteBuffer.limit()) ? byteBuffer.get(index) : 0;
    }

    /**
     * Writes the specified bits into this Struct.
     *
     * @param  value the bits value as a signed long.
     * @param  bitOffset the bit start position in the Struct.
     * @param  bitSize the number of bits.
     * @throws IllegalArgumentException if
     *         <code>(bitOffset + bitSize - 1) / 8 >= this.size()</code>
     */
    public void writeBits(long value, int bitOffset, int bitSize) {
        if ((bitOffset + bitSize - 1) >> 3 >= this.size()) throw new IllegalArgumentException(
                "Attempt to write outside the Struct");
        int offset = bitOffset >> 3;
        int bitStart = (byteOrder() == ByteOrder.BIG_ENDIAN) ? bitOffset
                - (offset << 3) : 64 - bitSize - (bitOffset - (offset << 3));
        long mask = -1L;
        mask <<= bitStart; // Clears preceding bits
        mask >>>= (64 - bitSize); // Unsigned shift.
        mask <<= 64 - bitSize - bitStart;
        value <<= (64 - bitSize - bitStart);
        value &= mask; // Protects against out of range values.
        int index = pos() + offset;
        long oldValue = readByteBufferLong(index);
        long resetValue = oldValue & (~mask);
        long newValue = resetValue | value;
        writeByteBufferLong(index, newValue);
    }

    private void writeByteBufferLong(int index, long value) {
        ByteBuffer byteBuffer = getByteBuffer();
        if (index + 8 < byteBuffer.limit()) {
            byteBuffer.putLong(index, value);
            return;
        }
        // Else possible buffer overflow.
        if (byteBuffer.order() == ByteOrder.LITTLE_ENDIAN) {
            writeByte(index, byteBuffer, (byte) value);
            writeByte(++index, byteBuffer, (byte) (value >> 8));
            writeByte(++index, byteBuffer, (byte) (value >> 16));
            writeByte(++index, byteBuffer, (byte) (value >> 24));
            writeByte(++index, byteBuffer, (byte) (value >> 32));
            writeByte(++index, byteBuffer, (byte) (value >> 40));
            writeByte(++index, byteBuffer, (byte) (value >> 48));
            writeByte(++index, byteBuffer, (byte) (value >> 56));
        } else {
            writeByte(index, byteBuffer, (byte) (value >> 56));
            writeByte(++index, byteBuffer, (byte) (value >> 48));
            writeByte(++index, byteBuffer, (byte) (value >> 40));
            writeByte(++index, byteBuffer, (byte) (value >> 32));
            writeByte(++index, byteBuffer, (byte) (value >> 24));
            writeByte(++index, byteBuffer, (byte) (value >> 16));
            writeByte(++index, byteBuffer, (byte) (value >> 8));
            writeByte(++index, byteBuffer, (byte) value);
        }
    }

    private static void writeByte(int index, ByteBuffer byteBuffer, byte value) {
        if (index < byteBuffer.limit()) {
            byteBuffer.put(index, value);
        }
    }

    /////////////
    // MEMBERS //
    /////////////
    /**
     * This inner class represents the base class for all {@link Fuct}
     * members. It allows applications to define additional member types.
     * For example:[code]
     *    public class MyStruct extends Struct {
     *        BitSet bits = new BitSet(256);
     *        ...
     *        public BitSet extends Member {
     *            public BitSet(int nbrBits) {
     *                super(nbrBits, 0); // Direct bit access.
     *            }
     *            public boolean get(int i) { ... }
     *            public void set(int i, boolean value) { ...}
     *        }
     *    }[/code]
     */
    protected class Member {

        /**
         * Holds the relative offset (in bytes) of this member within its struct.
         */
        private final int _offset;
        /**
         * Holds the relative bit offset of this member to its struct offset.
         */
        private final int _bitIndex;
        /**
         * Holds the bit length of this member.
         */
        private final int _bitLength;

        /**
         * Base constructor for custom member types.
         *
         * The word size can be zero, in which case the {@link #offset}
         * of the member does not change, only {@link #bitIndex} is
         * incremented.
         *
         * @param  bitLength the number of bits or <code>0</code>
         *         to force next member on next word boundary.
         * @param  wordSize the word size in bytes used when accessing
         *         this member data or <code>0</code> if the data is accessed
         *         at the bit level.
         */
        protected Member(int bitLength, int wordSize) {
            _bitLength = bitLength;

            // Resets index if union.
            if (resetIndex) {
                index = 0;
            }

            // Check if we can merge bitfields (always true if no word boundary).
            if ((wordSize == 0)
                    || ((bitLength != 0) && (wordSize == words) && ((bitsUsed + bitLength) <= (wordSize << 3)))) {

                _offset = index - words;
                _bitIndex = bitsUsed;
                bitsUsed += bitLength;

                // Straddling word boundary only possible if (wordSize == 0)
                while (bitsUsed > (words << 3)) {
                    index++;
                    words++;
                    length = MathLib.max(length, index);
                }
                return; // Bit field merge done.
            }

            // Check alignment.
            if (!isPacked()) {

                // Updates struct's alignment constraint, based on largest word size.
                if ((alignment < wordSize)) {
                    alignment = wordSize;
                }

                // Adds padding if misaligned.
                int misaligned = index % wordSize;
                if (misaligned != 0) {
                    index += wordSize - misaligned;
                }
            }

            // Sets member indices.
            _offset = index;
            _bitIndex = 0;

            // Update struct indices.
            index += MathLib.max(wordSize, (bitLength + 7) >> 3);
            words = wordSize;
            bitsUsed = bitLength;
            length = MathLib.max(length, index);
            // size and index may differ because of {@link Union}
        }

        final public int get(final int index) {
            return getByteBuffer().get(index);
        }

        /**
         * Returns the outer {@link Fuct struct} container.
         *
         * @return the outer struct.
         */
        public final Fuct struct() {
            return Fuct.this;
        }



        /**
         * Returns the byte offset of this member in its struct.
         * Equivalent to C/C++ <code>offsetof(struct(), this)</code>
         *
         * @return the offset of this member in the Struct.
         */
        public final int offset() {
            return _offset;
        }

        /**
         * Holds the bit offset of this member (if any).
         * The actual position of the bits data depends upon the endianess and
         * the word size.
         */
        public final int bitIndex() {
            return _bitIndex;
        }

        /**
         * Returns the number of bits in this member. Can be zero if this
         * member is used to force the next member to the next word boundary.
         *
         * @return the number of bits in the member.
         */
        public final int bitLength() {
            return _bitLength;
        }

        // Returns the member int value.
        final int get(int wordSize, int word) {
            final int shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? (wordSize << 3)
                    - bitIndex() - bitLength()
                    : bitIndex();
            word >>= shift;
            int mask = 0xFFFFFFFF >>> (32 - bitLength());
            return word & mask;
        }

        // Sets the member int value.
        final int set(int value, int wordSize, int word) {
            final int shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? (wordSize << 3)
                    - bitIndex() - bitLength()
                    : bitIndex();
            int mask = 0xFFFFFFFF >>> (32 - bitLength());
            mask <<= shift;
            value <<= shift;
            return (word & ~mask) | (value & mask);
        }

        // Returns the member long value.
        final long get(int wordSize, long word) {
            final int shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? (wordSize << 3)
                    - bitIndex() - bitLength()
                    : bitIndex();
            word >>= shift;
            long mask = 0xFFFFFFFFFFFFFFFFL >>> (64 - bitLength());
            return word & mask;
        }

        // Sets the member long value.
        final long set(long value, int wordSize, long word) {
            final int shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? (wordSize << 3)
                    - bitIndex() - bitLength()
                    : bitIndex();
            long mask = 0xFFFFFFFFFFFFFFFFL >>> (64 - bitLength());
            mask <<= shift;
            value <<= shift;

            
            return (word & ~mask) | (value & mask);
        }

        public int index() {
            return pos() + offset();
        }
    }

    ///////////////////////
    // PREDEFINED FIELDS //
    ///////////////////////
    /**
     * This class represents a UTF-8 character string, null terminated
     * (for C/C++ compatibility)
     */
    public class UTF8String extends Member {

        private final UTF8ByteBufferWriter _writer = new UTF8ByteBufferWriter();
        private final UTF8ByteBufferReader _reader = new UTF8ByteBufferReader();
        private final int _length;

        public UTF8String(int length) {
            super(length << 3, 1);
            _length = length; // Takes into account 0 terminator.
        }

        public void set(byte[] b) {
            final ByteBuffer buffer = getByteBuffer();
            synchronized (buffer) {

                if (b.length + 1 > _length)
                    throw new Error("string overflow");

                int i = index();
                buffer.rewind();
                buffer.position(i);
                buffer.put(b);
                buffer.position(i + b.length);
                buffer.put((byte)0);


            }

        }

        public void set(String string) {
            final ByteBuffer buffer = getByteBuffer();
            synchronized (buffer) {
                try {
                    int index = pos() + offset();
                    buffer.position(index);
                    _writer.setOutput(buffer);
                    if (string.length() < _length) {
                        _writer.write(string);
                        _writer.write(0); // Marks end of string.
                    } else if (string.length() > _length) { // Truncates.
                        _writer.write(string.substring(0, _length));
                    } else { // Exact same length.
                        _writer.write(string);
                    }
                } catch (IOException e) { // Should never happen.
                    throw new Error(e.getMessage());
                } finally {
                    _writer.reset();
                }
            }
        }

        public String get() {
            final ByteBuffer buffer = getByteBuffer();
            synchronized (buffer) {
                TextBuilder tmp = new TextBuilder();
                try {
                    int index = pos() + offset();
                    buffer.position(index);
                    _reader.setInput(buffer);
                    for (int i = 0; i < _length; i++) {
                        char c = (char) _reader.read();
                        if (c == 0) { // Null terminator.
                            return tmp.toString();
                        } else {
                            tmp.append(c);
                        }
                    }
                    return tmp.toString();
                } catch (IOException e) { // Should never happen.
                    throw new Error(e.getMessage());
                } finally {
                    _reader.reset();
                }
            }
        }

        public String toString() {
            return this.get();
        }
    }

    /**
     * This class represents a 8 bits boolean with <code>true</code> represented
     * by <code>1</code> and <code>false</code> represented by <code>0</code>.
     */
    public class Bool extends Member {

        public Bool() {
            super(8, 1);
        }

        public Bool(int nbrOfBits) {
            super(nbrOfBits, 1);
        }

        public boolean get() {
            final int index = index();
            int word = get(index);
            word = (bitLength() == 8) ? word : get(1, word);
            return word != 0;
        }

        public void set(boolean value) {
            final int index = index();
            if (bitLength() == 8) {
                getByteBuffer().put(index, (byte) (value ? -1 : 0));
            } else {
                getByteBuffer().put(
                        index,
                        (byte) set(value ? -1 : 0, 1, getByteBuffer()
                                .get(index)));
            }
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents a 8 bits signed integer.
     */
    public class Signed8 extends Member {

        public Signed8() {
            super(8, 1);
        }

        public Signed8(int nbrOfBits) {
            super(nbrOfBits, 1);
        }

        public byte get() {
            final int index = index();
            int word = get(index);
            return (byte) ((bitLength() == 8) ? word : get(1, word));
        }

        public void set(byte value) {
            final int index = index();
            if (bitLength() == 8) {
                getByteBuffer().put(index, value);
            } else {
                getByteBuffer().put(index,
                        (byte) set(value, 1, get(index)));
            }
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents a 8 bits unsigned integer.
     */
    public class Unsigned8 extends Member {

        public Unsigned8() {
            super(8, 1);
        }

        public Unsigned8(int nbrOfBits) {
            super(nbrOfBits, 1);
        }

        public short get() {
            final int index = index();
            int word = get(index);
            return (short) (0xFF & ((bitLength() == 8) ? word : get(1, word)));
        }

        public void set(short value) {
            final int index = index();

            final byte v;
            if (bitLength() == 8) {
                v = (byte) value;
            } else {
                v = (byte) set(value, 1, get(index));
            }

            getByteBuffer().put(index, v);
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents a 16 bits signed integer.
     */
    public class Signed16 extends Member {

        public Signed16() {
            super(16, 2);
        }

        public Signed16(int nbrOfBits) {
            super(nbrOfBits, 2);
        }

        public short get() {
            final int index = index();
            int word = getByteBuffer().getShort(index);
            return (short) ((bitLength() == 16) ? word : get(2, word));
        }

        public void set(short value) {
            final int index = index();
            if (bitLength() == 16) {
                getByteBuffer().putShort(index, value);
            } else {
                getByteBuffer().putShort(index,
                        (short) set(value, 2, getByteBuffer().getShort(index)));
            }
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents a 16 bits unsigned integer.
     */
    public class Unsigned16 extends Member {

        public Unsigned16() {
            super(16, 2);
        }

        public Unsigned16(int nbrOfBits) {
            super(nbrOfBits, 2);
        }

        public int get() {
            final int index = index();
            int word = getByteBuffer().getShort(index);
            return 0xFFFF & ((bitLength() == 16) ? word : get(2, word));
        }

        public void set(int value) {
            final int index = index();
            if (bitLength() == 16) {
                getByteBuffer().putShort(index, (short) value);
            } else {
                getByteBuffer().putShort(index,
                        (short) set(value, 2, getByteBuffer().getShort(index)));
            }
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents a 32 bits signed integer.
     */
    public class Signed32 extends Member {

        public Signed32() {
            super(32, 4);
        }

        public Signed32(int nbrOfBits) {
            super(nbrOfBits, 4);
        }

        public int get() {
            final int index = index();
            int word = getByteBuffer().getInt(index);
            return (bitLength() == 32) ? word : get(4, word);
        }

        public void set(int value) {
            final int index = index();
            if (bitLength() == 32) {
                getByteBuffer().putInt(index, value);
            } else {
                getByteBuffer().putInt(index,
                        set(value, 4, getByteBuffer().getInt(index)));
            }
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents a 32 bits unsigned integer.
     */
    public class Unsigned32 extends Member {

        public Unsigned32() {
            super(32, 4);
        }

        public Unsigned32(int nbrOfBits) {
            super(nbrOfBits, 4);
        }

        public long get() {
            final int index = index();
            int word = getByteBuffer().getInt(index);
            return 0xFFFFFFFFL & ((bitLength() == 32) ? word : get(4, word));
        }

        public void set(long value) {
            final int index = index();
            if (bitLength() == 32) {
                getByteBuffer().putInt(index, (int) value);
            } else {
                getByteBuffer().putInt(index,
                        set((int) value, 4, getByteBuffer().getInt(index)));
            }
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents a 64 bits signed integer.
     */
    public class Signed64 extends Member {

        public Signed64() {
            super(64, 8);
        }

        public Signed64(int nbrOfBits) {
            super(nbrOfBits, 8);
        }

        public long get() {
            final int index = index();
            long word = getByteBuffer().getLong(index);
            return (bitLength() == 64) ? word : get(8, word);
        }

        public void set(long value) {
            final int index = index();
            if (bitLength() == 64) {
                getByteBuffer().putLong(index, value);
            } else {
                getByteBuffer().putLong(index,
                        set(value, 8, getByteBuffer().getLong(index)));
            }
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents an arbitrary size (unsigned) bit field with
     * no word size constraint (they can straddle words boundaries).
     */
    public class BitField extends Member {

        public BitField(int nbrOfBits) {
            super(nbrOfBits, 0);
        }

        public long longValue() {
            long signedValue = readBits(bitIndex() + (offset() << 3),
                    bitLength());
            return ~(-1L << bitLength()) & signedValue;
        }

        public int intValue() {
            return (int) longValue();
        }

        public short shortValue() {
            return (short) longValue();
        }

        public byte byteValue() {
            return (byte) longValue();
        }

        public void set(long value) {
            writeBits(value, bitIndex() + (offset() << 3), bitLength());
        }

        public String toString() {
            return String.valueOf(longValue());
        }
    }

    /**
     * This class represents a 32 bits float (C/C++/Java <code>float</code>).
     */
    public class Float32 extends Member {

        public Float32() {
            super(32, 4);
        }

        public float get() {
            final int index = index();
            return getByteBuffer().getFloat(index);
        }

        final public void set(final float value) {
            final int index = index();
            getByteBuffer().putFloat(index, value);
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents a 64 bits float (C/C++/Java <code>double</code>).
     */
    public class Float64 extends Member {

        public Float64() {
            super(64, 8);
        }

        public double get() {
            final int index = index();
            return getByteBuffer().getDouble(index);
        }

        public void set(double value) {
            final int index = index();
            getByteBuffer().putDouble(index, value);
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * <p> This class represents a 32 bits reference (C/C++ pointer) to
     *     a {@link Fuct} object (other types may require a {@link Fuct}
     *     wrapper).</p>
     * <p> Note: For references which can be externally modified, an application
     *           may want to check the {@link #isUpToDate up-to-date} status of
     *           the reference. For out-of-date references, a {@link Fuct}
     *           can be created at the address specified by {@link #value}
     *           (using JNI) and the reference {@link #set set} accordingly.</p>
     */
    public class Reference32<S extends Fuct> extends Member {

        private S _struct;

        public Reference32() {
            super(32, 4);
        }

        public void set(S struct) {
            final int index = index();
            if (struct != null) {
                getByteBuffer().putInt(index, (int) struct.address());
            } else {
                getByteBuffer().putInt(index, 0);
            }
            _struct = struct;
        }

        public S get() {
            return _struct;
        }

        public int value() {
            final int index = index();
            return getByteBuffer().getInt(index);
        }

        public boolean isUpToDate() {
            final int index = index();
            if (_struct != null) {
                return getByteBuffer().getInt(index) == (int) _struct.address();
            } else {
                return getByteBuffer().getInt(index) == 0;
            }
        }
    }

    /**
     * <p> This class represents a 64 bits reference (C/C++ pointer) to
     *     a {@link Fuct} object (other types may require a {@link Fuct}
     *     wrapper).</p>
     * <p> Note: For references which can be externally modified, an application
     *           may want to check the {@link #isUpToDate up-to-date} status of
     *           the reference. For out-of-date references, a new {@link Fuct}
     *           can be created at the address specified by {@link #value}
     *           (using JNI) and then {@link #set set} to the reference.</p>
     */
    public class Reference64<S extends Fuct> extends Member {

        private S _struct;

        public Reference64() {
            super(64, 8);
        }

        public void set(S struct) {
            final int index = index();
            if (struct != null) {
                getByteBuffer().putLong(index, struct.address());
            } else if (struct == null) {
                getByteBuffer().putLong(index, 0L);
            }
            _struct = struct;
        }

        public S get() {
            return _struct;
        }

        public long value() {
            final int index = index();
            return getByteBuffer().getLong(index);
        }

        public boolean isUpToDate() {
            final int index = index();
            if (_struct != null) {
                return getByteBuffer().getLong(index) == _struct.address();
            } else {
                return getByteBuffer().getLong(index) == 0L;
            }
        }
    }

    /**
     * This class represents a 8 bits {@link Enum}.
     */
    public class Enum8<T extends Enum<T>> extends Member {

        private final T[] _values;

        public Enum8(T[] values) {
            super(8, 1);
            _values = values;
        }

        public Enum8(T[] values, int nbrOfBits) {
            super(nbrOfBits, 1);
            _values = values;
        }

        public T get() {
            final int index = index();
            int word = get(index);
            return _values[0xFF & get(1, word)];
        }

        public void set(T e) {
            int value = e.ordinal();
            if (_values[value] != e) throw new IllegalArgumentException(
                    "enum: "
                            + e
                            + ", ordinal value does not reflect enum values position");
            final int index = index();
            int word = get(index);
            getByteBuffer().put(index, (byte) set(value, 1, word));
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents a 16 bits {@link Enum}.
     */
    public class Enum16<T extends Enum<T>> extends Member {

        private final T[] _values;

        public Enum16(T[] values) {
            super(16, 2);
            _values = values;
        }

        public Enum16(T[] values, int nbrOfBits) {
            super(nbrOfBits, 2);
            _values = values;
        }

        public T get() {
            final int index = index();
            int word = getByteBuffer().getShort(index);
            return _values[0xFFFF & get(2, word)];
        }

        public void set(T e) {
            int value = e.ordinal();
            if (_values[value] != e) throw new IllegalArgumentException(
                    "enum: "
                            + e
                            + ", ordinal value does not reflect enum values position");
            final int index = index();
            int word = getByteBuffer().getShort(index);
            getByteBuffer().putShort(index, (short) set(value, 2, word));
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents a 32 bits {@link Enum}.
     */
    public class Enum32<T extends Enum<T>> extends Member {

        private final T[] _values;

        public Enum32(T[] values) {
            super(32, 4);
            _values = values;
        }

        public Enum32(T[] values, int nbrOfBits) {
            super(nbrOfBits, 4);
            _values = values;
        }

        public T get() {
            final int index = index();
            int word = getByteBuffer().getInt(index);
            return _values[get(4, word)];
        }

        public void set(T e) {
            int value = e.ordinal();
            if (_values[value] != e) throw new IllegalArgumentException(
                    "enum: "
                            + e
                            + ", ordinal value does not reflect enum values position");
            final int index = index();
            int word = getByteBuffer().getInt(index);
            getByteBuffer().putInt(index, set(value, 4, word));
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }

    /**
     * This class represents a 64 bits {@link Enum}.
     */
    public class Enum64<T extends Enum<T>> extends Member {

        private final T[] _values;

        public Enum64(T[] values) {
            super(64, 8);
            _values = values;
        }

        public Enum64(T[] values, int nbrOfBits) {
            super(nbrOfBits, 8);
            _values = values;
        }

        public T get() {
            final int index = index();
            long word = getByteBuffer().getLong(index);
            return _values[(int) get(8, word)];
        }

        public void set(T e) {
            long value = e.ordinal();
            if (_values[(int) value] != e) throw new IllegalArgumentException(
                    "enum: "
                            + e
                            + ", ordinal value does not reflect enum values position");
            final int index = index();
            long word = getByteBuffer().getLong(index);
            getByteBuffer().putLong(index, set(value, 8, word));
        }

        public String toString() {
            return String.valueOf(this.get());
        }
    }
}
