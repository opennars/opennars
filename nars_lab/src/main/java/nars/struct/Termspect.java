package nars.struct;

import nars.Op;
import nars.term.Term;

import java.nio.ByteBuffer;

/** represents one particular Term "view" of a TermCept, at a time.
 *  a view is parameterized by an Op type
 */
abstract public class Termspect extends TermCept implements Term {

    private Op op = null;

    public Termspect() {
        super();
    }

    public Termspect(TermCept t, Op o) {
        this();
        set(t, o);
    }

    @Override
    public Term clone() {
        return null;
    }

    public nars.struct.Termspect set(ByteBuffer b, int offset, Op o) {
        set(b, offset);
        this.op = o;
        return this;
    }

    public nars.struct.Termspect set(TermCept t, Op o) {
        set(t.getByteBuffer(), t.outerOffset);
        this.op = o;
        return this;
    }



}
