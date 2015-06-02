package nars.nal.term;

import nars.nal.Terms;

/** an optimized compound implementation for use when only 1 subterm */
abstract public class Compound1<T extends Term> extends Compound {



    public Compound1(final T the) {
        super(the);
    }

    public T the() {
        return (T)term[0];
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null) return false;

        if (getClass()!=that.getClass()) return false;

        nars.nal.term.Compound1 c = (nars.nal.term.Compound1)that;
        if (operator()!=c.operator()) return false;
        if (the().equals(c.the())) {
            setName(c.name());
            return true;
        }
        return false;
    }


    @Override final public int length() {
        return 1;
    }

    @Override
    protected void init(Term[] term) {
        super.init(term);

//        if (!hasVar()) //only do this here if not hasVar, because if it does have var it will calculate it in invalidate()
//            updateHash();
    }


//    protected void updateHash() {
//        int h = getTemporalOrder();
//        h = h * 31 + the().hashCode();
//        h = h * 31 + operator().hashCode();
//        this.hash = h;
//    }

    @Override
    public void invalidate() {
        if (hasVar()) {

            super.invalidate();

            T n = the();
            if (n instanceof Compound) {
                ((Compound)n).invalidate();
            }

        }
        else {
            setNormalized();
        }
    }

    /** compares only the contents of the subterms; assume that the other term is of the same operator type */
    @Override
    public int compareSubterms(final Compound otherCompoundOfEqualType) {
        //this is what we want to avoid - generating string names
        //override in subclasses where a different non-string comparison can be made
        return Terms.compareSubterms(term, otherCompoundOfEqualType.term);
    }

    @Override
    protected int compare(Compound otherCompoundOfEqualType) {
        return compareSubterms(otherCompoundOfEqualType);
    }
}
