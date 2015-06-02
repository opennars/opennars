package nars.util.data.id;

import com.sun.org.apache.xml.internal.utils.StringComparable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Generic abstract identifier for symbols, tags, and other identifiables
 */
abstract public class Identifier<E extends Identifier> implements Comparable {

    private IdentifierHost host = null;

    public interface Identified<I extends Identifier> extends Named<I> {

    }

    public interface IdentifierHost<I extends Identifier> extends Identified<I> {
        /**
         * allows a host of an identifier to replace its identifier
         * with an instance known to be equal, effectively
         * removing duplicates from the system.
         */
        void identifierEquals(I other);
    }

    abstract void write(OutputStream o) throws IOException;

    public abstract void print(Writer p, boolean pretty) throws IOException;

    public void set(IdentifierHost h) {
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
            //if both hosts are non-null, this object's host will be replaced
            IdentifierHost localHost = host;
            IdentifierHost remoteHost = ix.host;
            if (localHost==null) {
                if (remoteHost != null)
                    remoteHost.identifierEquals(this);
            }
            else {
                localHost.identifierEquals(ix);
            }
            return true;
        }

        return false;
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
            return compare((Identifier) o);
        //}
    }

    public StringBuffer toString(boolean pretty) {
        StringWriter w = new StringWriter(getStringSizeEstimate());
        try {
            print(w, pretty);
        } catch (IOException e) {
            e.printStackTrace();
            w.write(super.toString());
        }
        return w.getBuffer();
    }

    @Override
    public String toString() {
        return toString(true).toString();
    }

    abstract int getStringSizeEstimate();

    /** frees all associated memory */
    abstract public void delete();
}
