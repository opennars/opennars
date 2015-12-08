package nars.term;

import nars.Op;
import nars.term.compound.Compound;

/**
 * Marker class for Term types which store instance-specific metadata
 * that should not be overwritten.
 */
public interface TermMetadata {

    int temporalBits = Op.or(Op.PARALLEL, Op.SEQUENCE);

    static boolean hasTemporals(Compound term) {
        return term.hasAny(temporalBits);
    }

}
