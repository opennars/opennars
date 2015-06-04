package nars.util.data.id;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;
import nars.util.utf8.FastByteComparisons;
import nars.util.utf8.Utf8;
import sun.misc.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

/**
 * Constant-value UTF8 identifier, populated by String or byte[] on construction
 */
public class UTF8Identifier extends Identifier {

    protected byte[] name = null;
    transient protected int hash = 0;


    /** do nothing, used by subclass */
    protected UTF8Identifier() { }

    public UTF8Identifier(byte[] b) {
        this.name = b;
    }

    public UTF8Identifier(byte[] b, int start, int stop) {
        int len = stop - start;
        this.name = new byte[len];
        System.arraycopy(b, start, name, 0, len);
    }

    public UTF8Identifier(String s) {
        this(Utf8.toUtf8(s));
    }

    public UTF8Identifier(char[] s) {
        this(Utf8.toUtf8(s));
    }

    /** should set the hashCode, but this may need to call makeName */
    public int makeHash() {
        return Arrays.hashCode(name) * 31;
    }

    public boolean hasName() { return name!=null;  }

    public boolean hasHash() {
        return hash!=0;
    }

    public byte[] name() {
        ensureNamed();
        return name;
    }

    public char[] toChars(final boolean pretty) {
        /** if UTF8Identifier it is a constant (until we balance the dynamic with a concrete constant subclass and make this abstract)
         * otherwise call the write routine
         * */
        if (getClass() == UTF8Identifier.class)
            return Utf8.fromUtf8ToChars(name());
        else
            return super.toChars(pretty);
    }

    @Override
    int getStringSizeEstimate() {
        if (hasName())
            return name.length;
        return 16;
    }

    @Override
    public int hashCode() {
        ensureHashed();
        return hash;
    }

    private void ensureHashed() {
        if (!hasHash()) {
            this.hash = makeHash();
        }
    }


    protected void ensureNamed() {
        //should not need to do anything here in the constant/static impl
    }


    @Override
    public boolean equalTo(Identifier x) {
        if (x instanceof UTF8Identifier) {

            UTF8Identifier u = (UTF8Identifier) x;
            if (unequalHash(u))
                return false;

            return Utf8.equals2(name(), u.name());
        }

        //this case should be avoided, it is wasteful
        System.err.println(this + " wasteful String comparison");
        return toString().equals(x.toString());
    }

    /*8 returns true only if the hashes both exist and are different */
    public boolean unequalHash(final UTF8Identifier u) {
        if (!hasHash()) return false;
        if (!u.hasHash()) return false;
        return hashCode()!=u.hashCode();
    }

    public byte[] bytes() {
        return name();
    }

    @Override
    public int compare(Identifier o) {
        int i = Integer.compare(hashCode(), o.hashCode());
        if (i == 0) {
            if (o instanceof UTF8Identifier)
                //rare case that hash is equal, do a value comparison
                return FastByteComparisons.compare(name(), ((UTF8Identifier)o).name());
            else {
                //this case should be avoided
                System.err.println(this + " wasteful String comparison");
                return toString().compareTo(o.toString());
            }
        }

        return i;
    }



    @Override
    public void write(Writer p, boolean pretty) throws IOException {
        //default behavior: string representation of name
        ensureNamed();
//
//        ByteArrayOutputStream boas = new ByteArrayOutputStream();
//        boas.write(name());
//
//        ByteStreams.newDataOutput()
//        p.write(new StringWriter(boas, Utf8.utf8Charset);, pretty);


        p.write(Utf8.fromUtf8ToChars(name()));
    }


    @Override
    public void delete() {
        name = null;
        hash = 0;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** string representation formed by the UTF8 byte[] name() */
    public String nameString() {
        return Utf8.fromUtf8(name());
    }

}
