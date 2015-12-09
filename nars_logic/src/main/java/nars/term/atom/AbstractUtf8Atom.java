package nars.term.atom;


import nars.Op;
import nars.term.Term;
import nars.util.utf8.Byted;
import nars.util.utf8.Utf8;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class AbstractUtf8Atom extends Atomic implements Byted, Externalizable {

    private final byte[] id;
    private final int hash;


    public AbstractUtf8Atom(String id) {
        this.id = Utf8.toUtf8(id);
        hash = Atom.hash(
            id.hashCode(), op().ordinal()
        );
    }

    public AbstractUtf8Atom(byte[] id) {
        this.id = id;
        hash = Atom.hash(
            id, op().ordinal()
        );
    }

    public AbstractUtf8Atom(byte[] id, Op specificOp) {
        this.id = id;
        hash = Atom.hash(id, specificOp.ordinal());
    }


    @Override
    public byte[] bytes() {
        return id;
    }

    @Override
    public String toString() {
        return Utf8.fromUtf8toString(bytes());
    }

    @Override
    public boolean equals(Object x) {
        if (this == x) return true;

        if (x instanceof Atomic) {
            Atomic ax = (Atomic)x;
            return Byted.equals(this, ax) && (op() == ax.op());
        }
        return false;
    }

    /**
     * default implementation that uses bytes() as lowest common
     * denominator of comparison
     */
    @Override public int compareTo(Object that) {
        if (that==this) return 0;

        Term t = (Term)that;
        int d = Integer.compare(op().ordinal(), t.op().ordinal());
        if (d!=0) return d;

        //if the op is the same, it will be a subclass of atom
        //which should have an ordering determined by its byte[]
        return Byted.compare(this, (Atomic)that);
    }


    @Override
    public final int hashCode() {
        return hash;
    }



    @Override
    public final void writeExternal(ObjectOutput out) throws IOException {
        byte[] name = bytes();
        //out.writeByte((byte)op().ordinal());
        out.writeShort(name.length);
        out.write(bytes());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        int nameLen = in.readShort();
        byte[] name = new byte[nameLen];

        in.read(name);

        setBytes(name);
    }
}
