package nars.util.data.id;

import nars.util.utf8.FastByteComparisons;
import nars.util.utf8.Utf8;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;

/**
 * Created by me on 6/1/15.
 */
abstract public class UTF8Identifier extends Identifier {

    protected byte[] name = null;
    protected int hash = 0;

    /** should set the name byte[]  */
    abstract public byte[] makeName();

    /** should set the hashCode, but this may need to call makeName */
    public void makeHash() {
        ensureNamed();
        hash = Arrays.hashCode(name) * 31;
    }

    public boolean hasName() { return name!=null;  }

    public boolean hasHash() {
        /** assumes the hash is generated when name is  */
        if (!hasName())
            return false;
        return true;
    }

    public byte[] name() {
        ensureNamed();
        return name;
    }

    private synchronized void ensureNamed() {
        if (!hasName()) {
            name = makeName();
        }
    }

    @Override
    public int hashCode() {
        if (!hasHash()) {
            makeHash();
        }
        return hashCode();
    }

    @Override
    public boolean equalTo(Identifier x) {
        return Utf8.equals2(name, ((UTF8Identifier)x).name);
    }

    @Override
    public int compare(Identifier o) {
        int i = Integer.compare(hashCode(), o.hashCode());
        if (i == 0) {
            //rare case that hash is equal, do a value comparison
            return FastByteComparisons.compare(name(), ((UTF8Identifier)o).name());
        }
        return i;
    }

    @Override
    void write(OutputStream o) throws IOException {
        o.write(name);
    }

    @Override
    public void print(Writer p, boolean pretty) throws IOException {
        //default behavior: string representation of name
        ensureNamed();
        p.write(Utf8.fromUtf8(name()));
    }


    @Override
    public void delete() {
        name = null;
        hash = 0;
    }
}
