package nars.util.data.id;

import nars.util.utf8.Utf8;

import java.io.*;

/**
 * Generic abstract identifier for symbols, tags, and other identifiables
 */
abstract public class Identifier<E extends Identifier> implements Comparable {

    private Identified host = null;

    public char[] toChars(boolean pretty) {
        CharArrayWriter caw = new EfficientCharArrayWriter();
        try {
            write(caw, pretty);
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


    public abstract void write(Writer p, boolean pretty) throws IOException;

    public void set(Identified h) {
        this.host = h;
    }

    public void write(OutputStream o, boolean pretty) throws IOException {
        write(new PrintWriter(o), pretty);
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

    public String toString(final boolean pretty) {
        char[] c = toChars(pretty);
        return new String(c);
    }
    public StringBuilder toStringBuilder(final boolean pretty) {
        char[] c = toChars(pretty);
        return new StringBuilder(c.length).append(c);
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
        return toString(true).toString();
    }

    abstract int getStringSizeEstimate();

    /** frees all associated memory */
    abstract public void delete();

    private static class EfficientCharArrayWriter extends CharArrayWriter {
        @Override
        public char[] toCharArray() {
            if (size() == buf.length)
                return buf;
            return super.toCharArray();
        }
    }
}
