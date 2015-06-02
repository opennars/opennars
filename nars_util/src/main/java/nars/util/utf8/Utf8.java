package nars.util.utf8;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 *
 * @author https://github.com/squito/jutf8
 * http://svn.apache.org/viewvc/avro/trunk/lang/java/avro/src/main/java/org/apache/avro/other/Utf8.java?revision=1552418&view=co
 */
public class Utf8 implements CharSequence, Comparable<Utf8> {

    final byte[] bytes;
    final int start;
    final int end;
    int length = -1;
    int hash = 0;

    public static final Charset utf8Charset = Charset.forName("UTF-8");

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
        this(s.getBytes(utf8Charset));
    }

    public static final String fromUtf8(final byte[] bytes, final int length) {
        return new String(bytes, 0, length, utf8Charset);
    }

    public static final String fromUtf8(final byte[] bytes) {
        return new String(bytes, utf8Charset);
    }

    public static final byte[] toUtf8(final String str) {
        return str.getBytes(utf8Charset);
    }
    public static final byte[] toUtf8(final char[] str) {

        return utf8Charset.encode(CharBuffer.wrap(str)).array();
    }
    public static final byte[] toUtf8(byte prefix, final String str) {
        return ByteBuf.create(prefix + str.length()).add((byte)prefix).add(str).toBytes();
    }

    /** ordinary array equals comparison with some conditions removed */
    public static boolean equals2(final byte[] a, final byte[] a2) {
        /*if (a==null || a2==null)
            return false;*/


        final int length = a.length;
        if (a2.length != length)
            return false;

        if (a == a2) return true;

        //backwards
        for (int i=length-1; i>=0; i--)
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
            if (bDiff!=0) return bDiff;
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
        return toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    public static int computeLength(final byte[] bytes, final int start, final int end) {
        return utf8Charset.decode(ByteBuffer.wrap(bytes, start, end - start)).length();
    }

    @Override
    public String toString() {
        if (this.length == 0) {
            return "";
        }
        return fromUtf8(bytes, length);
    }

}

class Utf8_apache implements Comparable<Utf8>, CharSequence {

    private static final byte[] EMPTY = new byte[0];

    private byte[] bytes = EMPTY;
    private int length;
    private String string;

    public Utf8_apache() {
    }

    public Utf8_apache(String string) {
        this.bytes = getBytesFor(string);
        this.length = bytes.length;
        this.string = string;
    }

    public Utf8_apache(Utf8_apache other) {
        this.length = other.length;
        this.bytes = new byte[other.length];
        System.arraycopy(other.bytes, 0, this.bytes, 0, this.length);
        this.string = other.string;
    }

    public Utf8_apache(byte[] bytes) {
        this.bytes = bytes;
        this.length = bytes.length;
    }

    /**
     * Return UTF-8 encoded bytes. Only valid through {@link #getByteLength()}.
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Return length in bytes.
     *
     * @deprecated call {@link #getByteLength()} instead.
     */
    public int getLength() {
        return length;
    }

    /**
     * Return length in bytes.
     */
    public int getByteLength() {
        return length;
    }

    /**
     * Set length in bytes. Should called whenever byte content changes, even if
     * the length does not change, as this also clears the cached String.
     *
     * @deprecated call {@link #setByteLength(int)} instead.
     */
    public Utf8_apache setLength(int newLength) {
        return setByteLength(newLength);
    }

    /**
     * Set length in bytes. Should called whenever byte content changes, even if
     * the length does not change, as this also clears the cached String.
     */
    public Utf8_apache setByteLength(int newLength) {
        if (this.bytes.length < newLength) {
            byte[] newBytes = new byte[newLength];
            System.arraycopy(bytes, 0, newBytes, 0, this.length);
            this.bytes = newBytes;
        }
        this.length = newLength;
        this.string = null;
        return this;
    }

    /**
     * Set to the contents of a String.
     */
    public Utf8_apache set(String string) {
        this.bytes = getBytesFor(string);
        this.length = bytes.length;
        this.string = string;
        return this;
    }

    private abstract static class Utf8Converter {

        public abstract String fromUtf8(byte[] bytes, int length);

        public abstract byte[] toUtf8(String str);
    }

    private static final Utf8Converter UTF8_CONVERTER
            = System.getProperty("java.version").startsWith("1.6.")
                    ? new Utf8Converter() {                       // optimized for Java 6
                @Override
                public String fromUtf8(byte[] bytes, int length) {
                    try {
                        return new String(bytes, 0, length, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public byte[] toUtf8(String str) {
                    try {
                        return str.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            : new Utf8Converter() {                       // faster in Java 7 & 8
                @Override
                public String fromUtf8(byte[] bytes, int length) {
                    return new String(bytes, 0, length, Utf8.utf8Charset);
                }

                @Override
                public byte[] toUtf8(String str) {
                    return str.getBytes(Utf8.utf8Charset);
                }
            };

    @Override
    public String toString() {
        if (this.length == 0) {
            return "";
        }
        if (this.string == null) {
            this.string = UTF8_CONVERTER.fromUtf8(bytes, length);
        }
        return this.string;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Utf8)) {
            return false;
        }
        Utf8 that = (Utf8) o;
        if (!(this.length == that.length)) {
            return false;
        }
        byte[] thatBytes = that.bytes;
        for (int i = 0; i < this.length; i++) {
            if (bytes[i] != thatBytes[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < this.length; i++) {
            hash = hash * 31 + bytes[i];
        }
        return hash;
    }

    @Override
    public int compareTo(Utf8 that) {        
        throw new RuntimeException("_");
    //return BinaryData.compareBytes(this.bytes, 0, this.length,
        //                             that.bytes, 0, that.length);
    }

    // CharSequence implementation
    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    /**
     * Gets the UTF-8 bytes for a String
     */
    public static final byte[] getBytesFor(String str) {
        return UTF8_CONVERTER.toUtf8(str);
    }

}
