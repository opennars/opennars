package nars.term;

/**
 * an optimized compound implementation for use when only 1 subterm
 */
abstract public class Compound2<A extends Term, B extends Term> extends Compound {

    protected Compound2(A a, B b) {
        super(a, b);
    }

    public A a() {
        return (A) term[0];
    }

    public B b() {
        return (B) term[1];
    }


    @Override
    final public int length() {
        return 2;
    }

    @Override
    public void invalidate() {
        if (hasVar()) {
            super.invalidate(); //invalidate name so it will be (re-)created lazily

            for (final Term t : term) {
                if (t instanceof Compound)
                    ((Compound) t).invalidate();
            }

        } else {
            setNormalized();
        }
    }

//    @Override
//    public boolean equals(final Object that) {
//        if (this == that) return true;
//        if (that == null) return false;
//
//        if (getClass() != that.getClass()) return false;
//
//
//        Compound2 c = (Compound2) that;
//
//
//        final int m = getMass();
//        if (c.getMass()!=m) return false;
//
//        if (m < 4) {
//            return equalsByName(c);
//        }
//        else {
//            //avoids generating a byte[] ident
//            return equalsByContent(c);
//        }
//    }

//    public boolean equalsByName(final Compound2 c) {
//        return equalTo(c);
//    }
//
//    public boolean equalsByContent(final Compound2 c) {
//
//        //compare components without generating name
//
//        //if (operator()!=c.operator()) return false;
//        if (structuralHash() != c.structuralHash()) return false;
//        if (a().getMass() != c.a().getMass()) return false;
//        if (b().getMass() != c.b().getMass()) return false;
//
//        if (getTemporalOrder() != c.getTemporalOrder()) return false;
//
//        if (a().equals(c.a()) && b().equals(c.b())) {
//            //must be equal so share the identifier
//            share(c);
//            return true;
//        }
//        return false;
//
//    }

    @Override
    public boolean hasVar() {
        return getTotalVariables() > 0;
    }

    @Override
    protected int compare(Compound otherCompoundOfEqualType) {
        return compareSubterms(otherCompoundOfEqualType);
    }


    //    @Override
//    public int hashCode2() {
//        //return hashCode();
//        return Util.hash(operator().ordinal(), getTemporalOrder(), b(), a() );
//    }

    /**
     * compares only the contents of the subterms; assume that the other term is of the same operator type
     */
    @Override
    public int compareSubterms(final Compound otherCompoundOfEqualType) {
        //this is what we want to avoid - generating string names
        //override in subclasses where a different non-string comparison can be made


        final Compound2 other = ((Compound2) otherCompoundOfEqualType);

        return Terms.compareSubterms(term, other.term);
    }


}
