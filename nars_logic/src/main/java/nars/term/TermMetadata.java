package nars.term;

import nars.Op;

/**
 * Marker class for Term types which store instance-specific metadata
 * that should not be overwritten.
 */
public interface TermMetadata {

    int metadataStruture = Op.or(Op.PARALLEL, Op.SEQUENCE);

    static boolean has(Compound term) {
        return term.has(metadataStruture);
    }

}
