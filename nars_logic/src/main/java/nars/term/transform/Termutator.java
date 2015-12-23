package nars.term.transform;

/**
 * Created by me on 12/22/15.
 */
public abstract class Termutator /* implements BooleanIterator */ {

    /**
     * applies test, returns the determined validity
     */
    public abstract boolean next();

    public abstract void reset();

    public abstract int getEstimatedPermutations();


    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public abstract boolean hasNext();

}
