package nars.nal.term;

import nars.nal.Terms;

/** implementation of a compound which generates and stores its name as a CharSequence, which is used for equality and hash*/
abstract public class DefaultCompound extends Compound {

    public DefaultCompound(Term... components) {
        super(components);
    }


    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;

        if (that == null) return false;

        //if the other class is not a DefaultCompound it is considered non-equal, so the use of implementations must be consistent
        //if (!(that instanceof nars.nal.term.DefaultCompound)) return false;

        if (getClass() != that.getClass()) return false;


        final nars.nal.term.DefaultCompound t = (nars.nal.term.DefaultCompound)that;
//        if ((name == null) || (t.name == null)) {
//            //check operate first because name() may to avoid potential construction of name()
//            if (/*operator()!=t.operator()|| */ getComplexity() != t.getComplexity() )
//                return false;
//        }

        return equalTo(t);
//        if (equalID(t)) {
//            share(t);
//            return true;
//        }
//        return false;
    }

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
