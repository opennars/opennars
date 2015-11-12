package nars.term;


import nars.Op;

public class TermSet extends TermVector {

    @Override
    public void init(Term[] term, @Deprecated int hashSeed, @Deprecated Op containerOp) {
        super.init(
            Terms.toSortedSetArray(term),
            hashSeed, containerOp);
    }
}
