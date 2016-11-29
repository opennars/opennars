package nars.entity;

import nars.storage.Memory;
import nars.language.Term;



public interface ConceptBuilder {
    public Concept newConcept(BudgetValue b, Term t, Memory m);    
}
