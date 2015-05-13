package nars.nal.term;

/** an optimized compound implementation for use when only 1 subterm */
abstract public class Compound1 extends Compound {

    byte[] name = null;
    transient int hash;

    public Compound1(final Term the) {
        super(the);
    }

    public Term the() {
        return term[0];
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
    protected void init(Term[] term) {
        super.init(term);

        if (!hasVar()) //only do this here if not hasVar, because if it does have var it will calculate it in invalidate()
            updateHash();
    }


    protected void updateHash() {
        int h = getTemporalOrder();
        h = h * 31 + the().hashCode();
        h = h * 31 + operator().hashCode();
        this.hash = h;
    }

    @Override
    public void invalidate() {
        if (hasVar()) {
            name = null;
            Term n = the();
            if (n instanceof Compound) {
                ((Compound)n).invalidate();
            }

            updateHash();
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
    public byte[] name() {
        if (name == null) {
            name = makeKey();
        }
        return name;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public byte[] nameCached() {
        return name;
    }
}
