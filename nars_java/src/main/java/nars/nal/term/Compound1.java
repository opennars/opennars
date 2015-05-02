package nars.nal.term;

/** an optimized compound implementation for use when only 1 subterm */
abstract public class Compound1 extends Compound {

    private CharSequence cachedName = null;

    public Compound1(Term the) {
        super(the);
    }

    public Term the() {
        return term[0];
    }

    @Override
    public int hashCode() {
        return the().hashCode() + operator().hashCode() * 31;
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null) return false;

        if (getClass()!=that.getClass()) return false;
        nars.nal.term.Compound1 c = (nars.nal.term.Compound1)that;
        //if (operator()!=c.operator()) return false;
        if (the().equals(c.the())) {
            share(c);
            return true;
        }
        return false;
    }

    @Override
    public void invalidate() {
        if (hasVar()) {
            Term n = the();
            cachedName = null;
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
        return the().compareTo(((nars.nal.term.Compound1) otherCompoundOfEqualType).the());
    }



    @Override
    public CharSequence name() {
        if (cachedName == null)
            cachedName = makeName();
        return cachedName;
    }

    @Override
    public CharSequence nameCached() {
        return cachedName;
    }
}
