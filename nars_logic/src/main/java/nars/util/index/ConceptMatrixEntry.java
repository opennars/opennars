package nars.util.index;

import nars.concept.Concept;
import nars.term.Term;

/**
 * base type for cell entries in the matrix. subclass to add additional information per cell
 */
abstract public class ConceptMatrixEntry<R extends Term, C extends Term, E extends Term, V extends ConceptMatrixEntry> implements Concept.Meta {

    private final ConceptMatrix matrix;
    public final Concept concept; //may be null

    public ConceptMatrixEntry(ConceptMatrix<R, C, E, V> matrix, Concept c) {
        this.matrix = matrix;
        this.concept = c;
    }

    @Override
    public void onState(Concept c, Concept.State nextState) {
        if (nextState == Concept.State.Deleted) {
            this.matrix.deleteEntry(this);
        }
    }

}
