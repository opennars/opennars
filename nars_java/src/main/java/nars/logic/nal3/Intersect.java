package nars.logic.nal3;

import nars.logic.entity.CompoundTerm;
import nars.logic.entity.Term;

/**
 * Common parent class for IntersectInt and IntersectExt
 */
abstract public class Intersect extends CompoundTerm {

    public Intersect(Term[] arg) {
        super(arg);
    }
}
