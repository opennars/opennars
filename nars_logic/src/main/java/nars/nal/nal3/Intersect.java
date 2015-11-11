package nars.nal.nal3;

import nars.term.DefaultCompound;

/**
 * Common parent class for IntersectInt and IntersectExt
 */
abstract public class Intersect extends DefaultCompound {

    protected Intersect() {
        super();
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
