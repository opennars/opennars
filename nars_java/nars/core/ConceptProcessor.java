package nars.core;

import java.util.Collection;
import nars.core.control.FireConcept;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.inference.BudgetFunctions.Activating;
import nars.language.Term;


/** A ConceptProcessor implements a model for storing Concepts and
 *  activating them during a memory cycle.  In essence it forms the very core of the memory,
 *  responsible for efficiently storing all NAR Concept's and which of those will activate
 *  at the beginning of each cycle.*/
public interface ConceptProcessor extends Iterable<Concept> {



    /** An iteration of the main loop, called during each memory cycle. */
    public FireConcept next();

    /** All known concepts */
    public Collection<? extends Concept> getConcepts();

    /** Invoked during a memory reset to empty all concepts */
    public void clear();

    /** Maps Term to associated Concept */
    public Concept concept(Term term);

    /**
     * Creates and adds new concept to the memory
     * @return the new concept, or null if the memory is full
     */
    public Concept conceptualize(BudgetValue budget, Term term, boolean createIfMissing);

    /** Activates a concept, adjusting its budget.  
     *  May be invoked by the concept processor or at certain points in the reasoning process.
     */
    public void activate(Concept c, BudgetValue b, Activating mode);

    //public void forget(Concept c);
    
    /**
     * Provides a "next" concept for sampling during inference. 
     */
    public Concept sampleNextConcept();

    public void init(Memory m);

    
}
