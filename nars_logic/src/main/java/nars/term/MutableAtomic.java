package nars.term;

import nars.util.utf8.Utf8;

/**
 * Base class for terms which contain no subterms
 */
public abstract class MutableAtomic extends AbstractAtomic {

    protected byte[] data;

    protected transient int hash;

    protected MutableAtomic(String id) {
        this(Utf8.toUtf8(id));
    }

    protected MutableAtomic(byte[] id) {
        setBytes(id);
    }

    protected MutableAtomic() {

    }


    @Override
    public int hashCode() {
        final int h = this.hash;
        if (h == 0) {
            throw new RuntimeException("should have hashed: " + this);
        }
        return h;
    }


    @Override public byte[] bytes() {
        return data;
    }

    @Override
    public final void setBytes(final byte[] id) {
        if (id!=this.data) {
            byte[] data = this.data = id;
            this.hash = Atom.hash(data, op().ordinal());
        }
    }


}
