package nars.term;

import nars.Op;

/**
 * Marker class for Term types which store instance-specific metadata
 * that should not be overwritten.
 */
public interface TermMetadata {

    int metadataBits = Op.or(Op.PARALLEL, Op.SEQUENCE, Op.INTERVAL);

    static boolean hasMetadata(Termlike term) {
        return term.hasAny(metadataBits);
    }

}
