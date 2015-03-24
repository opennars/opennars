package nars.nal.entity;

import nars.Memory;
import nars.energy.Budget;


public interface ConceptBuilder {
    public Concept newConcept(Budget b, Term t, Memory m);
}
