package nars.term.atom;

import nars.Op;
import nars.util.utf8.Utf8;

/**
 * Created by me on 12/4/15.
 */
public abstract class AbstractStringAtom extends AbstractStringAtomRaw {

    final int hash;

    protected AbstractStringAtom() {
        super(null);
        this.hash = 0;
    }

    protected AbstractStringAtom(byte[] id) {
        this(id, null);
    }
    protected AbstractStringAtom(String id) {
        this(id, null);
    }

    protected AbstractStringAtom(byte[] id, Op specificOp) {
        this( new String(id), specificOp);
    }

    protected AbstractStringAtom(String id, Op specificOp) {
        super(id);
        hash = Atom.hash(
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
