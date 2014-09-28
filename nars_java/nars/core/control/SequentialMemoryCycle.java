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

    public SequentialMemoryCycle(AbstractBag<Concept> concepts, ConceptBuilder conceptBuilder) {
        this.concepts = concepts;
        this.conceptBuilder = conceptBuilder;
    }
    
    
    @Override
    public void cycle(Memory m) {
        m.processNewTasks();
        
        if (m.getNewTaskCount() == 0) {       // necessary?
            m.processNovelTask();
        }

        if (m.getNewTaskCount() == 0) {       // necessary?
            processConcept(m);
        }

    }

    
    
    
    /**
     * Select and fire the next concept.
     */
    public void processConcept(Memory m) {
        Concept currentConcept = concepts.processNext(true);
        if (currentConcept != null) {            
            
            if (m.getRecorder().isActive()) {
                m.getRecorder().append("Concept Select", currentConcept.term.toString());
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
        concepts.pickOut(c.getKey());
        BudgetFunctions.activate(c, b);
        concepts.putBack(c);
    }
    
    @Override
    public void forget(Concept c) {
        concepts.pickOut(c.getKey());
        concepts.forget(c);
        concepts.putBack(c);    
    }

    @Override
    public Concept sampleNextConcept() {
        return concepts.processNext(false);
    }

    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }
    
    
}
