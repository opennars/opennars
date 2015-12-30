package nars.term.atom;

import nars.Op;

/** implemented with a native Java string.
 *  this should be the ideal choice for JDK9
 *  since it does Utf8 internally and many
 *  string operations are intrinsics.  */
public abstract class AbstractStringAtomRaw extends Atomic  {

    public final String id;

    protected AbstractStringAtomRaw(String id) {
        this.id = id;
    }


    @Override
    public int hashCode() {
        /** for Op.ATOM, we use String hashCode() as-is, avoiding need to calculate or store a hash mutated by the Op */
        return id.hashCode();
    }

    @Override
    public abstract Op op();

    @Override public String toString() {
        return id;
    }

}
