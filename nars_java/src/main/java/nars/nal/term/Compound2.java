package nars.nal.term;

/** an optimized compound implementation for use when only 1 subterm */
abstract public class Compound2 extends Compound {

    private CharSequence cachedName = null;
    private int cachedHash = 0;

    protected Compound2(Term a, Term b) {
        super(a, b);
    }

    public Term a() {
        return term[0];
    }
    public Term b() {
        return term[1];
    }

    @Override
    public int hashCode() {
        int h = this.cachedHash;
        if (h == 0) {
            h = a().hashCode();
            h = h * 31 + b().hashCode();
            h = h * 31 + operator().hashCode();
            this.cachedHash = h;
        }
        return h;
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null) return false;

        if (getClass()!=that.getClass()) return false;
        Compound2 c = (Compound2)that;
        //if (operator()!=c.operator()) return false;
        if (a().equals(c.a()) && b().equals(c.b())) {
            share(c);
            return true;
        }
        return false;

    }



    /** compares only the contents of the subterms; assume that the other term is of the same operator type */
    @Override
    public int compareSubterms(final Compound otherCompoundOfEqualType) {
        //this is what we want to avoid - generating string names
        //override in subclasses where a different non-string comparison can be made


        final Compound2 other = ((Compound2) otherCompoundOfEqualType);

        int ca = a().compareTo(other.a());
        if (ca != 0) return ca;

        int cb = b().compareTo(other.b());

//        if (Global.DEBUG) {
//            if ((cb == 0) && (!otherCompoundOfEqualType.equals(this))) {
//                throw new RuntimeException("inconsistent compareTo: " + ca + ", " + cb + " =?" + otherCompoundOfEqualType.equals(this));
//            }
//        }
        return cb;
    }

    @Override
    public void invalidate() {
        if (hasVar()) {
            this.cachedName = null; //invalidate name so it will be (re-)created lazily
            this.cachedHash = 0;
            for (final Term t : term) {
                if (t instanceof Compound)
                    ((Compound) t).invalidate();
            }
        }
        else {
            setNormalized(true);
        }



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
