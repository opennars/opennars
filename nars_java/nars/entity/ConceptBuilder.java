package nars.entity;

import nars.language.Term;
import nars.storage.Memory;

/**
 *
 * @author me
 */


public interface ConceptBuilder {
    public Concept newConcept(Term t, Memory m);
}
