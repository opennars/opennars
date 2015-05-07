package nars.util.index;

import nars.nal.concept.Concept;
import nars.nal.term.Term;

/**
 * base type for cell entries in the matrix. subclass to add additional information per cell
 */
abstract public class ConceptMatrixEntry<R extends Term, C extends Term, E extends Term, V extends ConceptMatrixEntry> implements Concept.ConceptMeta {

    private ConceptMatrix matrix;
    public final Concept concept; //may be null

    public ConceptMatrixEntry(ConceptMatrix<R, C, E, V> matrix, Concept c) {
        this.matrix = matrix;
        this.concept = c;
    }

    @Override
    public void onState(Concept.State nextState) {
        if (nextState == Concept.State.Deleted) {
            this.matrix.deleteEntry(this);
        }
    }

}
