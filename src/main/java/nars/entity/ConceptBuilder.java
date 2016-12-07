package nars.entity;

import nars.language.Term;
import nars.storage.Memory;



public interface ConceptBuilder {
    public Concept newConcept(BudgetValue b, Term t, Memory m);    
}
