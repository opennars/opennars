package nars.meta.pre;

import nars.nal.nal4.ProductN;
import nars.task.Task;
import nars.term.Term;
import nars.term.Variable;

/**
 * Created by me on 8/29/15.
 */
public class PairMatchingProduct extends ProductN {

    public int volA, volB;
    public int structureA, structureB; //should use the long stuctureHash?

    final static Variable empty = new Variable("%z");

    public PairMatchingProduct() {
        this(empty, empty);
    }

    public PairMatchingProduct(Term a, Term b) {
        super(a, b);
    }

    @Override
    protected void init(final Term... term) {
        super.init(term);
        final Term a = term[0];
        final Term b = term[1];
        volA = a.volume();
        structureA = a.structure();
        volB = b.volume();
        structureB = b.structure();
    }

    public void set(final Task a, final Task b) {
        set(a.getTerm(), b == null ? empty : b.getTerm());
    }

    public void set(final Term a, final Term b) {
        this.term[0] = a;
        this.term[1] = b;
        rehash();
    }

    @Override
    protected int getStructureBase() {
        return 0;
    }

    public boolean substitutesMayExist(final PairMatchingProduct pattern) {
        if (impossibleStructure(pattern.structure()))
            return false;
        if (volume() < pattern.volume())
            return false;

        return substitutesMayExistPart2(pattern);
    }

    /** separated into its own method to assist inlining */
    protected boolean substitutesMayExistPart2(final PairMatchingProduct pattern) {
        final Term c = term[0];
        if (c.impossibleStructure(pattern.structureA)) return false;

        final Term d = term[1];
        if (d.impossibleStructure(pattern.structureB)) return false;
        if (volA < pattern.volA) return false;
        if (volB < pattern.volB) return false;

        return true;
    }



//    @Override
//    protected <T extends Term> T normalized(boolean destructive) {
//        return (T) this;
//    }

}
