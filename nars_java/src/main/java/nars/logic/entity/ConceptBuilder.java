package nars.logic.entity;

import nars.core.Memory;


public interface ConceptBuilder {
    public Concept newConcept(BudgetValue b, Term t, Memory m);    
}
