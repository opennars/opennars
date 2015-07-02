package nars.nal.concept;

import nars.Memory;
import nars.budget.Budget;
import nars.nal.term.Term;


public interface ConceptBuilder {

    public Concept newConcept(Term t, Budget b, Memory m);

}
