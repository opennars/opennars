package nars.concept;

import nars.Memory;
import nars.budget.Budget;
import nars.term.Term;


public interface ConceptBuilder {

    public Concept newConcept(Term t, Budget b, Memory m);

}
