package nars.nal.term;

import nars.nal.Terms;

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

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null) return false;

        if (getClass() != that.getClass()) return false;


        Compound2 c = (Compound2) that;

//        if (name().hasName() && c.name().hasName()) {
//            return equalID(c);
//        }

        //if names have not been generated then compare by content, avoiding construction of a name string
        return equalsByContent(c);
    }

    public boolean equalsByContent(final Compound2 c) {

        //compare components without generating name

        //if (operator()!=c.operator()) return false;
        if (getComplexity() != c.getComplexity()) return false;
        if (getTemporalOrder() != c.getTemporalOrder()) return false;

        if (a().equals(c.a()) && b().equals(c.b())) {
            //must be equal so share the identifier
            setName(c.name());
            return true;
        }
        return false;

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
