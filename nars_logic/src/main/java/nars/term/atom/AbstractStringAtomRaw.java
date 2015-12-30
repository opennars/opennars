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
    public abstract Op op();

    @Override public String toString() {
        return id;
    }

}
