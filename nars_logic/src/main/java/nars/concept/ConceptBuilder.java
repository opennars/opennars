package nars.concept;

import nars.Memory;
import nars.budget.Budget;
import nars.term.Term;


@FunctionalInterface public interface ConceptBuilder {

    Concept newConcept(Term t, Budget b, Memory m);

}
