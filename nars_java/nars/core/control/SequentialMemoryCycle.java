package nars.core.control;

import java.util.Collection;
import java.util.Iterator;
import nars.core.ConceptProcessor;
import nars.core.Memory;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.inference.BudgetFunctions;
import nars.language.Term;
import nars.storage.AbstractBag;

/**
 * The original deterministic memory cycle implementation that is currently used as a standard
 * for development and testing.
 */
public class SequentialMemoryCycle implements ConceptProcessor {


    /* ---------- Long-term storage for multiple cycles ---------- */
    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final AbstractBag<Concept> concepts;
    private final ConceptBuilder conceptBuilder;
    Memory memory;
    

    public SequentialMemoryCycle(AbstractBag<Concept> concepts, ConceptBuilder conceptBuilder) {
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
        Concept currentConcept = concepts.processNext(true, memory);
        if (currentConcept != null) {            
            
            if (memory.getRecorder().isActive()) {
                memory.getRecorder().append("Concept Select", currentConcept.term.toString());
            }
                        
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
    public Concept concept(CharSequence name) {
        return concepts.get(name);
    }

    @Override
    public Concept addConcept(Term term, Memory memory) {
        Concept concept = conceptBuilder.newConcept(term, memory);
        
        boolean added = concepts.putIn(concept);
        if (!added)
            return null;
        
        return concept;
    }
    

    @Override
    public void activate(Concept c, BudgetValue b) {
        concepts.pickOut(c.name());
        BudgetFunctions.activate(c, b);
        concepts.putBack(c, memory);
    }
    
    @Override
    public void forget(Concept c) {
        concepts.pickOut(c.name());        
        concepts.putBack(c, memory);    
    }

    @Override
    public Concept sampleNextConcept() {
        return concepts.processNext(false, memory);
    }

    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }
    
    
}
