package nars.nal.nal3;

import nars.term.DefaultCompound2;
import nars.term.Term;

/**
 * Common parent class for IntersectInt and IntersectExt
 */
abstract public class Intersect extends DefaultCompound2 {

    protected Intersect(Term[] args) {
        super(args);
    }

    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */
    @Override
    public final boolean isCommutative() {
        return true;
    }

}
