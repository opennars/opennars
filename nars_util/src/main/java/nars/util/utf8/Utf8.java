package nars.util.utf8;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
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


    public static char[] fromUtf8ToChars(byte[] bytes) {
        return fromUtf8ToChars(bytes, bytes.length);
    }





    /**
     * use fromUtf8ToChars when possible, because fromUtf8 creates a String
     * which creates an unnecessary duplicate of the decode buffer
     */
    public static String fromUtf8toString(byte[] bytes) {
        return fromUtf8toString(bytes, bytes.length);
    }
    static final char[] emptyChars = new char[0];


    public char[] toChars() {
        if (length == 0) {
            return emptyChars;
        }
        return fromUtf8ToChars(bytes, length);
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


    public static String fromUtf8toString(byte[] bytes, int length) {
        return sbbd.get().newString(bytes, 0, length);
    }

    public static char[] fromUtf8ToChars(byte[] bytes, int length) {
        return sbbd.get().newChars(bytes, 0, length);
    }



    @Override
    public final byte[] bytes() {
        return bytes;
    }

    @Override
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
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
        if (c[i] == '\0') {
            for (; i >= 0; i--) if (c[i] != '\0') break;
            return Arrays.copyOf(c, i + 1);
        }
        return c;
    }
}
