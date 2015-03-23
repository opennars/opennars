package nars.logic.nal3;

import nars.logic.entity.Compound;
import nars.logic.entity.Term;

/**
 * Common parent class for IntersectInt and IntersectExt
 */
abstract public class Intersect extends Compound {

    public Intersect(Term[] arg) {
        super(arg);
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
