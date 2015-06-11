package nars.nal.concept;

import nars.Memory;
import nars.budget.Budget;
import nars.nal.Truth;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.op.math.count;


public interface ConceptBuilder {

    public Concept newConcept(Term t, Budget b, Memory m);

}
