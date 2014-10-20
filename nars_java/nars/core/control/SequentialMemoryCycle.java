package nars.core.control;

import java.util.Collection;
import java.util.Iterator;
import nars.core.ConceptProcessor;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.inference.BudgetFunctions;
import nars.language.Term;
import nars.storage.Bag;

/**
 * The original deterministic memory cycle implementation that is currently used as a standard
 * for development and testing.
 */
public class SequentialMemoryCycle implements ConceptProcessor {


    /* ---------- Long-term storage for multiple cycles ---------- */
    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final Bag<Concept,Term> concepts;
    private final ConceptBuilder conceptBuilder;
    Memory memory;
    

    public SequentialMemoryCycle(Bag<Concept,Term> concepts, ConceptBuilder conceptBuilder) {
        this.concepts = concepts;
        this.conceptBuilder = conceptBuilder;        
    }
    
    
    @Override
    public void cycle(final Memory m) {
        this.memory = m;
        
        m.processNewTasks();
        
        if (m.getNewTaskCount() == 0) {       // necessary?
            m.processNovelTask();
        }

        if (m.getNewTaskCount() == 0) {       // necessary?
            processConcept();
        }

    }
    
    
    /**
     * Select and fire the next concept.
     */
    public void processConcept() {
        float forgetCycles = memory.param.conceptForgetDurations.getCycles();

        Concept currentConcept = concepts.processNext(forgetCycles, memory);
        if (currentConcept != null) {            
            currentConcept.fire();
        }
    }

    @Override
    public Collection<Concept> getConcepts() {
         return concepts.values();
    }

    @Override
    public void clear() {
        concepts.clear();
    }

    @Override
    public Concept concept(final Term term) {
        return concepts.get(term);
    }

    @Override
    public Concept addConcept(final Term term, final Memory memory) {
        Concept concept = conceptBuilder.newConcept(term, memory);
        
        boolean added = concepts.putIn(concept);
        if (!added)
            return null;
        
        return concept;
    }
    

    @Override
    public void activate(final Concept c, final BudgetValue b) {
        concepts.pickOut(c.name());
        BudgetFunctions.activate(c, b);
        concepts.putBack(c, memory.param.conceptForgetDurations.getCycles(), memory);
    }
    
    @Override
    public void forget(Concept c) {
        concepts.pickOut(c.name());        
        concepts.putBack(c, memory.param.conceptForgetDurations.getCycles(), memory);    
    }

    @Override
    public Concept sampleNextConcept() {
        return concepts.peekNext();
    }

    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }
    
    
}
