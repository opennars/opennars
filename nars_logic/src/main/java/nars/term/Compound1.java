package nars.term;

/** an optimized compound implementation for use when only 1 subterm */
abstract public class Compound1<T extends Term> extends Compound<T> {



    public Compound1() {
        super();
    }

    public final T the() {
        return term[0];
    }

//    @Override
//    public boolean equals(final Object that) {
//        if (this == that) return true;
//        if (that == null) return false;
//
//        if (getClass()!=that.getClass()) return false;
//
//        nars.nal.term.Compound1 c = (nars.nal.term.Compound1)that;
//        if (operator()!=c.operator()) return false;
//        if (the().equals(c.the())) {
//            share(c);
//            return true;
//        }
//        return false;
//    }


//    protected void updateHash() {
//        int h = getTemporalOrder();
//        h = h * 31 + the().hashCode();
//        h = h * 31 + operator().hashCode();
//        this.hash = h;
//    }



//    /** compares only the contents of the subterms; assume that the other term is of the same operator type */
//    @Override
//    public int compareSubterms(final Compound otherCompoundOfEqualType) {
//        //this is what we want to avoid - generating string names
//        //override in subclasses where a different non-string comparison can be made
//        return Terms.compareSubterms(term, otherCompoundOfEqualType.term);
//    }

//    @Override
//    protected int compare(Compound otherCompoundOfEqualType) {
//        return compareSubterms(otherCompoundOfEqualType);
//    }
}
