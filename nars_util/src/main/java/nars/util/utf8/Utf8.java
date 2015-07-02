package nars.util.utf8;

import com.google.common.primitives.Chars;
import sun.nio.cs.ThreadLocalCoders;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.Arrays;

/**
 * http://www.ascii-code.com/ we can use the 0..31 control characters
 * for 1 byte representation of NAL operators
 * which will be interpreted as such when not between double quotes
 *
 * @author https://github.com/squito/jutf8
 *         http://svn.apache.org/viewvc/avro/trunk/lang/java/avro/src/main/java/org/apache/avro/other/Utf8.java?revision=1552418&view=co
 */
public class Utf8 implements CharSequence, Comparable<Utf8> {

    public static final Charset utf8Charset = Charset.forName("UTF-8");
    final static CharsetEncoder utf8Encoder = ThreadLocalCoders.encoderFor(utf8Charset)
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.IGNORE);
    final static CharsetDecoder utf8Decoder = ThreadLocalCoders.decoderFor(utf8Charset)
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);


    final byte[] bytes;
    final int start;
    final int end;
    int length = -1;
    int hash = 0;


    protected Utf8(byte[] bytes, int start, int end, int length) {
        this.bytes = bytes;
        this.start = start;
        this.end = end;
        this.length = length;
    }

    public Utf8(byte[] bytes, int start, int end) {
        this(bytes, 0, bytes.length, computeLength(bytes, start, end));
    }

    public Utf8(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public Utf8(String s) {
        this(toUtf8(s));
    }

    public static char[] fromUtf8ToChars(final byte[] bytes) {
        return fromUtf8ToChars(bytes, bytes.length);
    }

    /**
     * use this method when possible, because fromUtf8 creates a String
     * which creates an unnecessary duplicate of the decode buffer
     */
    public static char[] fromUtf8ToChars(final byte[] bytes, final int length) {
        utf8Decoder.reset();
        try {
            int n = (int)(length * utf8Decoder.averageCharsPerByte());
            CharBuffer d = CharBuffer.allocate(n);
            utf8Decoder.decode(ByteBuffer.wrap(bytes, 0, length), d, true);
            return trim(d);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * use fromUtf8ToChars when possible, because fromUtf8 creates a String
     * which creates an unnecessary duplicate of the decode buffer
     */
    public static final String fromUtf8(final byte[] bytes, final int length) {
        //return new String(bytes, 0, length, utf8Charset);
        return new String(fromUtf8ToChars(bytes, length));
    }

    /**
     * use fromUtf8ToChars when possible, because fromUtf8 creates a String
     * which creates an unnecessary duplicate of the decode buffer
     */
    public static final String fromUtf8(final byte[] bytes) {
        return fromUtf8(bytes, bytes.length);
    }

    final static char[] emptyChars = new char[0];

    public char[] toChars() {
        if (this.length == 0) {
            return emptyChars;
        }
        return Utf8.fromUtf8ToChars(bytes, length);
    }

    public static byte[] trim(final ByteBuffer c) {
        byte[] x = c.array();
        final int l = c.limit();
        if (l!=c.capacity()) {
            x = Arrays.copyOf(x, l);
        }
        return x;
    }

    public static char[] trim(final CharBuffer c) {
        char[] x = c.array();
        int l = c.limit();

        boolean needsCopy = false;

        if (x[x.length-1] == '\u0000') {
            l--; //skip suffix zero charcter
            needsCopy = true;
        }

        if (l!=c.capacity()) {
            needsCopy = true;
        }

        if (needsCopy) {
            x = Arrays.copyOf(x, l);
        }
        return x;
    }

    public static final byte[] toUtf8(final CharBuffer c) {
        utf8Encoder.reset();
        try {
            ByteBuffer e = utf8Encoder.encode(c);
            return trim(e);
        } catch (CharacterCodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static final byte[] toUtf8(final String str) {
        return toUtf8(CharBuffer.wrap(str));

        //unsafe version;
        //return toUtf8(StringHack.chars(str));
    }

    public static final byte[] toUtf8(final char[] str) {
        return toUtf8(CharBuffer.wrap(str));
    }


    public static final byte[] toUtf8(byte prefix, final String str) {
        //TODO see if this needs trimmed
        return ByteBuf.create(prefix + str.length()).add((byte) prefix).add(str).toBytes();
    }

    /**
     * ordinary array equals comparison with some conditions removed
     */
    public static boolean equals2(final byte[] a, final byte[] a2) {
        /*if (a==null || a2==null)
            return false;*/

        final int length = a.length;
        if (a2.length != length)
            return false;

        if (a == a2) return true;

        //backwards
        for (int i = length - 1; i >= 0; i--)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    @Override
    public int compareTo(final Utf8 that) {
        int lDiff = that.bytes.length - bytes.length;
        if (lDiff != 0) return lDiff;
        for (int n = 0; n < bytes.length; n++) {
            final int bDiff = that.bytes[n] - bytes[n];
            if (bDiff != 0) return bDiff;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Arrays.hashCode(bytes);
        }
        return hash;
    }

    @Override
    public int length() {
        if (length == -1) {
            length = computeLength(bytes, start, end);
        }
        return length;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Utf8))
            return false;

        Utf8 u = (Utf8) obj;
        if (hashCode() != u.hashCode())
            return false;

        return equals2(bytes, u.bytes);
    }

    public void commit() {
        length = -1;
        hash = 0;
    }

    /*@Override
     public char charAt(int index) {
     return (char) bytes[index];
     }*/
    @Override
    public char charAt(int index) {
        return toChars()[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        //TODO use toChars
        return toString().subSequence(start, end);
    }

    public static int computeLength(final byte[] bytes, final int start, final int end) {
        return fromUtf8ToChars(bytes).length;
    }

    @Override
    public String toString() {
        if (this.length == 0) {
            return "";
        }
        return fromUtf8(bytes, length);
    }

    /** removes any trailing \0 chars from the array, creating a new array if necessar y*/
    public static char[] trim(final char[] c) {
        int i = c.length-1;
        if (c[i]=='\0') {
            //scan from end
            for (; i >=0; i--) {
                if (c[i]!='\0') break;
            }
            return Arrays.copyOf(c, i+1);
        }
        return c;
    }

    public static String trimString(final char[] c) {
        int firstZero = Chars.indexOf(c, (char)0);
        if (firstZero==-1)
            firstZero = c.length;
        return new String(c, 0, firstZero);
    }

}

//class Utf8_apache implements Comparable<Utf8>, CharSequence {
//
//    private static final byte[] EMPTY = new byte[0];
//
//    private byte[] bytes = EMPTY;
//    private int length;
//    private String string;
//
//    public Utf8_apache() {
//    }
//
//    public Utf8_apache(String string) {
//        this.bytes = Utf8.toUtf8(string);
//        this.length = bytes.length;
//        this.string = string;
//    }
//
//    public Utf8_apache(Utf8_apache other) {
//        this.length = other.length;
//        this.bytes = new byte[other.length];
//        System.arraycopy(other.bytes, 0, this.bytes, 0, this.length);
//        this.string = other.string;
//    }
//
//    public Utf8_apache(byte[] bytes) {
//        this.bytes = bytes;
//        this.length = bytes.length;
//    }
//
//    /**
//     * Return UTF-8 encoded bytes. Only valid through {@link #getByteLength()}.
//     */
//    public byte[] getBytes() {
//        return bytes;
//    }
//
//    /**
//     * Return length in bytes.
//     *
//     * @deprecated call {@link #getByteLength()} instead.
//     */
//    public int getLength() {
//        return length;
//    }
//
//    /**
//     * Return length in bytes.
//     */
//    public int getByteLength() {
//        return length;
//    }
//
//    /**
//     * Set length in bytes. Should called whenever byte content changes, even if
//     * the length does not change, as this also clears the cached String.
//     *
//     * @deprecated call {@link #setByteLength(int)} instead.
//     */
//    public Utf8_apache setLength(int newLength) {
//        return setByteLength(newLength);
//    }
//
//    /**
//     * Set length in bytes. Should called whenever byte content changes, even if
//     * the length does not change, as this also clears the cached String.
//     */
//    public Utf8_apache setByteLength(int newLength) {
//        if (this.bytes.length < newLength) {
//            byte[] newBytes = new byte[newLength];
//            System.arraycopy(bytes, 0, newBytes, 0, this.length);
//            this.bytes = newBytes;
//        }
//        this.length = newLength;
//        this.string = null;
//        return this;
//    }
//
//    /**
//     * Set to the contents of a String.
//     */
//    public Utf8_apache set(String string) {
//        this.bytes = Utf8.toUtf8(string);
//        this.length = bytes.length;
//        this.string = string;
//        return this;
//    }
//
//    private abstract static class Utf8Converter {
//
//        public abstract String fromUtf8(byte[] bytes, int length);
//
//        public abstract byte[] toUtf8(String str);
//    }
//
////    private static final Utf8Converter UTF8_CONVERTER
////            = System.getProperty("java.version").startsWith("1.6.")
////                    ? new Utf8Converter() {                       // optimized for Java 6
////                @Override
////                public String fromUtf8(byte[] bytes, int length) {
////                    try {
////                        return new String(bytes, 0, length, "UTF-8");
////                    } catch (UnsupportedEncodingException e) {
////                        throw new RuntimeException(e);
////                    }
////                }
////
////                @Override
////                public byte[] toUtf8(String str) {
////                    try {
////                        return str.getBytes("UTF-8");
////                    } catch (UnsupportedEncodingException e) {
////                        throw new RuntimeException(e);
////                    }
////                }
////            }
////            : new Utf8Converter() {                       // faster in Java 7 & 8
////                @Override
////                public String fromUtf8(byte[] bytes, int length) {
////                    return new String(bytes, 0, length, Utf8.utf8Charset);
////                }
////
////                @Override
////                public byte[] toUtf8(String str) {
////                    return str.getBytes(Utf8.utf8Charset);
////                }
////            };
//
//    @Override
//    public String toString() {
//        if (this.length == 0) {
//            return "";
//        }
//        if (this.string == null) {
//            this.string = Utf8.fromUtf8(bytes, length);
//        }
//        return this.string;
//    }
//
//
//    final static char[] emptyChars = new char[0];
//
//    public char[] toChars() {
//        if (this.length == 0) {
//            return emptyChars;
//        }
//        return Utf8.fromUtf8ToChars(bytes, length);
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (o == this) {
//            return true;
//        }
//        if (!(o instanceof Utf8)) {
//            return false;
//        }
//        Utf8 that = (Utf8) o;
//        if (!(this.length == that.length)) {
//            return false;
//        }
//        byte[] thatBytes = that.bytes;
//        for (int i = 0; i < this.length; i++) {
//            if (bytes[i] != thatBytes[i]) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 0;
//        for (int i = 0; i < this.length; i++) {
//            hash = hash * 31 + bytes[i];
//        }
//        return hash;
//    }
//
//    @Override
//    public int compareTo(Utf8 that) {
//        throw new RuntimeException("_");
//        //return BinaryData.compareBytes(this.bytes, 0, this.length,
//        //                             that.bytes, 0, that.length);
//    }
//
//    // CharSequence implementation
//    @Override
//    public char charAt(int index) {
//        return toChars()[index];
//    }
//
//    @Override
//    public int length() {
//        return toChars().length;
//    }
//
//
//    @Override
//    public CharSequence subSequence(int start, int end) {
//        return toString().subSequence(start, end);
//    }
//
//}
