package nars.entity;

import nars.language.Term;
import nars.core.Memory;

/**
 *
 * @author me
 */


public interface ConceptBuilder {
    public Concept newConcept(Term t, Memory m);
}
