package nars.term;

import nars.Op;
import nars.util.utf8.Utf8;

/**
 * Created by me on 12/4/15.
 */
public abstract class AbstractStringAtom extends AbstractStringAtomRaw {

    final int hash;

    public AbstractStringAtom(byte[] id) {
        this(id, null);
    }
    public AbstractStringAtom(String id) {
        this(id, null);
    }

    public AbstractStringAtom(byte[] id, Op specificOp) {
        this( new String(id), specificOp);
    }

    public AbstractStringAtom(String id, Op specificOp) {
        super(id);
        this.hash = Atom.hash(
            id.hashCode(),
            specificOp!=null ? specificOp : op()
        );
    }


    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public byte[] bytes() {
        return Utf8.toUtf8(id);
    }

}
