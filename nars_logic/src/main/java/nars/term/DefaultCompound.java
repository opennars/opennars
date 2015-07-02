package nars.term;

/** implementation of a compound which generates and stores its name as a CharSequence, which is used for equality and hash*/
abstract public class DefaultCompound extends Compound {

    public DefaultCompound(Term... components) {
        super(components);
    }


//    @Override
//    public boolean equals(final Object that) {
//        if (this == that) return true;
//
//        if (that == null) return false;
//
//        //if the other class is not a DefaultCompound it is considered non-equal, so the use of implementations must be consistent
//        //if (!(that instanceof nars.nal.term.DefaultCompound)) return false;
//
//        if (getClass() != that.getClass()) return false;
//
//
//        final nars.nal.term.DefaultCompound t = (nars.nal.term.DefaultCompound)that;
//
//
//        final int m = getMass();
//        if (m != t.getMass()) return false;
//
//        if ((m > 12) && (length() < 3)) {
//            if (structuralHash()!=t.structuralHash()) return false;
//            if (getTemporalOrder()!=t.getTemporalOrder()) return false;
//            return equalSubterms(term, t.term);
//        }
//        else {
//            return equalTo(t);
//        }
//
//    }

    @Override
    public int compareSubterms(final Compound otherCompoundOfEqualType) {
        DefaultCompound o = ((DefaultCompound) otherCompoundOfEqualType);
        return Terms.compareSubterms(term, o.term);
    }



    @Override
    public void invalidate() {
        if (hasVar()) {
            super.invalidate(); //invalidate name so it will be (re-)created lazily

            for (final Term t : term) {
                if (t instanceof Compound)
                    ((Compound) t).invalidate();
            }
        }
        else {
            setNormalized();
        }
    }


    final static int maxSubTermsForNameCompare = 2; //tunable

    @Override
    protected int compare(final Compound otherCompoundOfEqualType) {

        int l = length();

        if ((l != otherCompoundOfEqualType.length()) || (l < maxSubTermsForNameCompare))
            return compareSubterms(otherCompoundOfEqualType);

        return compareName(otherCompoundOfEqualType);
    }
}
