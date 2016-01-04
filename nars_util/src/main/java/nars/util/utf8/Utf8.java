package nars.util.utf8;

import com.google.common.primitives.Chars;
import sun.nio.cs.ThreadLocalCoders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;

/**
 * http://www.ascii-code.com/ we can use the 0..31 control characters
 * for 1 byte representation of NAL operators
 * which will be interpreted as such when not between double quotes
 *
 * @author https://github.com/squito/jutf8
 *         http://svn.apache.org/viewvc/avro/trunk/lang/java/avro/src/main/java/org/apache/avro/other/Utf8.java?revision=1552418&view=co
 */
public class Utf8 implements CharSequence, Comparable<Utf8>, Byted {

    public static final Charset utf8Charset = Charset.forName("UTF-8");
//    final static CharsetEncoder utf8Encoder = ThreadLocalCoders.encoderFor(utf8Charset)
//            .onMalformedInput(CodingErrorAction.REPLACE)
//            .onUnmappableCharacter(CodingErrorAction.IGNORE);
//    final static CharsetDecoder utf8Decoder = ThreadLocalCoders.decoderFor(utf8Charset)
//            .onMalformedInput(CodingErrorAction.REPLACE)
//            .onUnmappableCharacter(CodingErrorAction.REPLACE);

    public static CharsetEncoder utf8Encoder() {
        return ThreadLocalCoders.encoderFor(utf8Charset);
    }
    public static CharsetDecoder utf8Decoder() {
        return ThreadLocalCoders.decoderFor(utf8Charset);
    }

    byte[] bytes;
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

    public static char[] fromUtf8ToChars(byte[] bytes) {
        return fromUtf8ToChars(bytes, bytes.length);
    }


    /**
     * use this method when possible, because fromUtf8 creates a String
     * which creates an unnecessary duplicate of the decode buffer
     */
    public static char[] fromUtf8ToCharsJDK(byte[] bytes, int length) {
        CharsetDecoder uu = utf8Decoder();
        uu.reset();
        try {
            int n = (int)(length * uu.averageCharsPerByte());
            CharBuffer d = CharBuffer.allocate(n);
            uu.decode(ByteBuffer.wrap(bytes, 0, length), d, true);
            return trim(d);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }





    /**
     * use fromUtf8ToChars when possible, because fromUtf8 creates a String
     * which creates an unnecessary duplicate of the decode buffer
     */
    public static String fromUtf8toString(byte[] bytes) {
        return fromUtf8toString(bytes, bytes.length);
    }
    public static String fromUtf8toString(char prefix, byte[] bytes) {
        return fromUtf8toString(prefix, bytes, bytes.length);
    }

    static final char[] emptyChars = new char[0];

    public char[] toChars() {
        if (length == 0) {
            return emptyChars;
        }
        return Utf8.fromUtf8ToChars(bytes, length);
    }

    public static byte[] trim(ByteBuffer c) {
        byte[] x = c.array();
        int l = c.limit();
        if (l!=c.capacity()) {
            x = Arrays.copyOf(x, l);
        }
        return x;
    }

    public static char[] trim(CharBuffer c) {
        char[] x = c.array();
        int l = c.limit();

        boolean needsCopy = false;

        //skip suffix zero charcter
        int il = l;
        while (l > 0 && x[l-1] == '\u0000') {
            l--;
        }
        if (il!=l) needsCopy = true;

        if (l!=c.capacity()) {
            needsCopy = true;
        }

        if (needsCopy) {
            x = Arrays.copyOf(x, l);
        }
        return x;
    }

    public static byte[] toUtf8JDK(CharBuffer c) {
        CharsetEncoder uu = utf8Encoder();
        uu.reset();
        try {
            ByteBuffer e = uu.encode(c);
            return trim(e);
        } catch (CharacterCodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static final ThreadLocal<StringEncoder> sbbe =
            ThreadLocal.withInitial(StringEncoder::new);
    private static final ThreadLocal<StringDecoder> sbbd =
            ThreadLocal.withInitial(StringDecoder::new);

    public static byte[] toUtf8(String s) {
        return sbbe.get().toNewArray(s);
    }

    /** # bytes must be even */
    public static char[] bytesToChars(byte[] b) {
        int blen = b.length;
        //boolean odd = blen % 2 == 1;
        //int slen = odd ? (blen /2)+1 : (blen/2);
        int slen = blen/2;
        char[] c = new char[slen];
        int j = 0;
        for (int i = 0; i < slen; i++) {
            int d = (b[j++]) << 8;
            //if (j < slen)
                d |= b[j++];
            c[i] = (char) d;
        }
        return c;
    }

    public static byte[] charsToBytes(CharSequence s) {
        int slen = s.length();

        //boolean odd = slen%2==1 && (s.charAt(slen-1) & 0x00FF) == 0; //is last byte 0?

        int bb = slen * 2;// - (odd ? 1 : 0);
        byte[] b = new byte[bb];
        int j = 0;
        for (int i = 0; i< slen; i++) {
            int c = s.charAt(i);
            b[j++] = (byte)((c & 0xFF00)>>8);
            if (j < bb)
                b[j++] = (byte)((c & 0x00FF));
        }
        return b;
    }

    /** packs a String as UTF8 into a new String */
    public static String fromStringtoStringUtf8(String s) {
        return new String(bytesToChars(toUtf8(s)));
    }

    public static String fromStringUtf8(String s) {
        return Utf8.fromUtf8toString(charsToBytes(s));
    }

    public static String fromUtf8toString(byte[] bytes, int length) {
        return sbbd.get().newString(bytes, 0, length);
    }
    public static String fromUtf8toString(char prefix, byte[] bytes, int length) {
        return sbbd.get().newString(prefix, bytes, 0, length);
    }

    public static char[] fromUtf8ToChars(byte[] bytes, int length) {
        return sbbd.get().newChars(bytes, 0, length);
    }
    public static void fromUtf8ToAppendable(byte[] bytes, int length, Appendable a) throws IOException {
        sbbd.get().appendChars(bytes, 0, length, a);
    }
    public static void fromUtf8ToAppendable(byte[] bytes, Appendable a) throws IOException {
        fromUtf8ToAppendable(bytes, bytes.length, a);
    }
    public static void fromUtf8ToStringBuilder(byte[] bytes, int length, StringBuilder a)  {
        sbbd.get().appendChars(bytes, 0, length, a);
    }
    public static void fromUtf8ToStringBuilder(byte[] bytes, StringBuilder a)  {
        fromUtf8ToStringBuilder(bytes, bytes.length, a);
    }

    public static byte[] toUtf8JDK(String str) {
        return toUtf8JDK(CharBuffer.wrap(str));

        //unsafe version;
        //return toUtf8(StringHack.chars(str));
    }


//    public static final byte[] toUtf8(byte prefix, final String str) {
//        //TODO see if this needs trimmed
//        return ByteBuf.create(prefix + str.length()).add((byte) prefix).add(str).toBytes();
//    }

    @Override
    public final byte[] bytes() {
        return bytes;
    }

    @Override
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * ordinary array equals comparison with some conditions removed
     */
    public static boolean equals2(byte[] a, byte[] a2) {
        /*if (a==null || a2==null)
            return false;*/

        int length = a.length;
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
    public int compareTo(Utf8 that) {
        int lDiff = that.bytes.length - bytes.length;
        if (lDiff != 0) return lDiff;
        byte[] bytes = this.bytes;
        byte[] tbytes = that.bytes;
        for (int n = 0; n < bytes.length; n++) {
            int bDiff = tbytes[n] - bytes[n];
            if (bDiff != 0) return bDiff;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h != 0) return h;

        int newHash = Arrays.hashCode(bytes);
        if (newHash == 0) newHash = 1; //reserve 0
        return hash = newHash;
    }

    @Override
    public int length() {
        if (length == -1) {
            length = computeLength(bytes, start, end);
        }
        return length;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Utf8)) return false;
        return Byted.equals(this, (Utf8)obj);
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

    public static int computeLength(byte[] bytes, int start, int end) {
        return fromUtf8ToChars(bytes).length;
    }

    @Override
    public String toString() {
        if (length == 0) {
            return "";
        }
        return fromUtf8toString(bytes, length);
    }

    /** removes any trailing \0 chars from the array, creating a new array if necessar y*/
    public static char[] trim(char[] c) {
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

    public static String trimString(char[] c) {
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
