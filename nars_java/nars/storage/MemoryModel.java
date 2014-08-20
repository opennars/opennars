package nars.storage;

import java.util.Collection;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.language.Term;


/** defines a policy for top-level control of a memory cycle */


public interface MemoryModel {

    public void cycle(Memory m);

    public Collection<? extends Concept> getConcepts();

    public void clear();

    public Concept concept(CharSequence name);

    /**
     * Creates and adds new concept to the memory
     * @return the new concept, or null if the memory is full
     */
    public Concept addConcept(Term term, Memory memory);

    public void conceptActivate(Concept c, BudgetValue b);

    public Concept sampleNextConcept();
    
}
