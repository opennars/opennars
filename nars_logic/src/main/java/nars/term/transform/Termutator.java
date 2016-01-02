package nars.term.transform;

import nars.term.Termlike;

/**
 * Created by me on 12/22/15.
 */
public abstract class Termutator /* implements BooleanIterator */ {

    public final Termlike resultKey;

    protected Termutator(Termlike resultKey) {
        this.resultKey = resultKey;
    }

    ///** string representing the conditions necessary for match. used for comparison when resultKey are equal to know if there is conflict */
    //abstract String getConditionsKey();

    /**
     * applies test, returns the determined validity
     */
    public abstract boolean next();

    public abstract void reset();

    public abstract int getEstimatedPermutations();


    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        return resultKey.equals(((Termutator)obj).resultKey);
    }

    @Override
    public final int hashCode() {
        return resultKey.hashCode();
    }

    public abstract boolean hasNext();

}
