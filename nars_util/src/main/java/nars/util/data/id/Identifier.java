package nars.util.data.id;

import nars.util.data.rope.impl.CharArrayRope;
import nars.util.utf8.Utf8;

import java.io.*;

/**
 * Generic abstract identifier for symbols, tags, and other identifiables.
 * It is responsible for maintaining efficient time and space equality,
 * lexicographic ordering, and hashing of representations of
 * tokens / symbols / identifiers.
 *
 * can generate the following representations:
 *      --pretty String output ( default behavior of .toString() )
 *      --compact String output (ex: does not include spaces for readability, newlines, etc.)
 *      --internal byte[] - most compact and does not need to be human readable
 *
 * reducing the size of internal representations does not only save
 * memory but it is faster to compare and hash, and may hash with
 * better entropy.
 */
abstract public class Identifier<E extends Identifier> implements Comparable, Serializable {

    transient private Identified host = null;

    public char[] toChars(boolean pretty) {
        CharArrayWriter caw = new EfficientCharArrayWriter(getStringSizeEstimate());
        try {
            append(caw, pretty);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return caw.toCharArray();
    }


    public interface Identified extends Named<Identifier> {
        /**
         * allows a host of an identifier to replace its identifier
         * with an instance known to be equal, effectively
         * removing duplicates from the system.
         */
        void identifierEquals(Identifier other);
    }



    public void append(OutputStream o, boolean pretty) throws IOException {
        append(new PrintWriter(o), pretty);
    }

    /**
     * implementations should call Writer.append() sometimes instead of Writer.write()
     * to avoid allocating a temporary buffer
     */
    public abstract void append(Writer p, boolean pretty) throws IOException;

    public void set(Identified h) {
        this.host = h;
    }


    @Override
    public boolean equals(final Object x) {
        if (x == this) return true;

        /*if (equalOnlyToSameClass())
            if (!(x.getClass().equals(getClass()))) return false;
        else*/
        if (!(x instanceof Identifier)) return false;

        Identifier ix = (Identifier)x;
        if (equalTo(ix)) {
            share(ix);
            return true;
        }

        return false;
    }

    public void share(Identifier ix) {
        Identified localHost = host;
        Identified remoteHost = ix.host;
        if (localHost==null) {
            if (remoteHost != null)
                remoteHost.identifierEquals(this);
        }
        else {
            //modify localHost
            localHost.identifierEquals(ix);
        }

        //interesting but identityHashCode is slow
//        else {
//            //use the lower of the system codes to avoid circular assignments
//            int l = System.identityHashCode( localHost );
//            int r = System.identityHashCode( remoteHost );
//            Identified target;
//            Identifier source;
//            if (l < r) {
//                target = localHost;
//                source = ix;
//            }
//            else {
//                target = remoteHost;
//                source = this;
//            }
//
//            target.identifierEquals(source);
//        }
    }

    /*protected boolean equalOnlyToSameClass() {
        return false;
    }*/

    /** this method needs to test value equality and should not involve hashcode or instance equality tests */
    public abstract boolean equalTo(Identifier x);

    /** this method needs to test value equality and should not involve hashcode or instance equality tests */
    public abstract int compare(Identifier x);

    @Override
    public int compareTo(Object o) {
        if (this == o) return 0;

        /*
        Class ca = getClass();
        Class cb = o.getClass();
        if (ca!=cb //&& equalOnlyToSameClass()) {
            //sort by the hashcode of the class names, not natural ordering
            String sa = ca.getName();
            String sb = cb.getName();
            int a = sa.hashCode();
            int b = sb.hashCode();
            if (a!=b)
                return Integer.compare(a, b);
            return sa.compareTo(sb); //rare case if the hashcodes are equal but different classes
        }
        else {*/
            //same class as this
        Identifier i = (Identifier) o;
        int x = compare(i);
        if ( x== 0) {
            share(i);
        }
        return x;
        //}
    }

    /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    public String toString(final boolean pretty) {
        char[] c = toChars(pretty);
        return new String(c);
    }

    /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    public StringBuilder toStringBuilder(final boolean pretty) {
        char[] c = toChars(pretty);
        return new StringBuilder(c.length).append(c);
    }

    public CharSequence toCharSequence(final boolean pretty) {
        char[] c = toChars(pretty, true);
        return new CharArrayRope(c);
    }

    private char[] toChars(boolean pretty, boolean trim) {
        char[] c = toChars(pretty);
        if (trim)
            c = Utf8.trim(c);
        return c;
    }

    public byte[] bytes() {
        /** inefficient, override in subclasses please */
        System.err.println(this + " wasteful String generation");

        return Utf8.toUtf8(toChars(false));
    }

    public byte byteAt(final int i) {
        byte[] b = bytes();
        if (b == null) return 0;
        if (b.length <= i) return 0;
        return b[i];
    }


    @Override
    public String toString() {
        return toString(true);
    }

    abstract int getStringSizeEstimate();

    /** frees all associated memory */
    abstract public void delete();

    /** WARNING the toCharArray() result may need trimmed for trailing zero's */
    private static class EfficientCharArrayWriter extends CharArrayWriter {

        public EfficientCharArrayWriter(int initialSize) {
            super(initialSize);
        }

        @Override
        public char[] toCharArray() {
            if (size() == buf.length)
                return buf;
            return super.toCharArray();
        }
    }
}
