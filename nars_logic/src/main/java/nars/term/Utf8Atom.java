package nars.term;


import nars.Op;
import nars.util.utf8.Byted;
import nars.util.utf8.Utf8;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

abstract public class Utf8Atom extends AbstractAtomic implements Byted {

    private final byte[] id;
    private final int hash;

    public Utf8Atom(String id, Op op) {
        this(Utf8.toUtf8(id), op);
    }

    public Utf8Atom(String id) {
        this(Utf8.toUtf8(id));
    }
    public Utf8Atom(byte[] id) {
        this.id = id;
        this.hash = Atom.hash(
                this.id, op().ordinal()
        );
    }
    public Utf8Atom(byte[] id, Op op) {
        this(id, Atom.hash(id, op.ordinal() ));
    }
    public Utf8Atom(byte[] id, int hash) {
        this.id = id;
        this.hash = hash;
    }

    @Override
    public byte[] bytes() {
        return id;
    }


    /**
     * @param that The Term to be compared with the current Term
     */
    @Override
    public final int compareTo(final Object that) {
        if (that==this) return 0;

        Term t = (Term)that;
        int d = Integer.compare(op().ordinal(), t.op().ordinal());
        if (d!=0) return d;

        //if the op is the same, it will be a subclass of atom
        //which should have an ordering determined by its byte[]
        return Byted.compare(this, (Utf8Atom)that);
    }

    @Override
    public boolean equals(final Object x) {
        if (this == x) return true;

        if (x instanceof Utf8Atom) {
            Utf8Atom ax = (Utf8Atom)x;
            return Byted.equals(this, ax) && (op() == ax.op());
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public final void setBytes(byte[] b) {
        throw new RuntimeException("immutable");
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
