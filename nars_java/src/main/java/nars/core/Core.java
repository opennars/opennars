package nars.core;

import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.inference.BudgetFunctions.Activating;
import nars.language.Term;


/** Core implements a central reasoning component which references a set of Concepts and activates them during a memory cycle.*/
public interface Core extends Iterable<Concept> /* TODO: implements Plugin */ {


    public interface AttentionAware {
        public void setAttention(Core a);
    }


    /** called during main memory cycle */
    public void cycle();

    /** how many input tasks to process per cycle.  this allows Attention to regulate
     *  input relative to other kinds of mental activity
     * @return 
     */
    public int getInputPriority();


    /** Invoked during a memory reset to empty all concepts */
    public void reset();

    /** Maps Term to associated Concept. May also be called 'recognize'
     * as it can be used to determine if a symbolic pattern (term) is known */
    public Concept concept(Term term);

    /**
     * Creates and adds new concept to the memory.  May also be called 'cognize' because
     * it is like a request to process a symbolic pattern (term).
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

    /** used by the bag to explicitly forget an item asynchronously */
    public void conceptRemoved(Concept c);
    
    public Memory getMemory();
    
}
