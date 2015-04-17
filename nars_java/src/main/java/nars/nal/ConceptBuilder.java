package nars.nal;

import nars.Memory;
import nars.budget.Budget;
import nars.nal.concept.Concept;
import nars.nal.term.Term;


public interface ConceptBuilder {
    public Concept newConcept(Budget b, Term t, Memory m);
}
