package nars.nal.nal3;

import nars.term.Term;
import nars.term.compound.CompoundN;

/**
 * Common parent class for IntersectInt and IntersectExt
 */
public abstract class Intersect extends CompoundN {

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
