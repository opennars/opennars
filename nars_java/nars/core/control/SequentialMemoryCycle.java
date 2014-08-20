package nars.core.control;

import java.util.Collection;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.inference.BudgetFunctions;
import nars.storage.AbstractBag;
import nars.storage.Memory;

/**
 * A deterministic memory cycle implementation that is used for development and testing.
 */
public class SequentialMemoryCycle implements Memory.MemoryModel {


    /* ---------- Long-term storage for multiple cycles ---------- */
    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final AbstractBag<Concept> concepts;

    public SequentialMemoryCycle(AbstractBag<Concept> concepts) {
        this.concepts = concepts;
    }
    
    
    @Override
    public void cycle(Memory m) {
        m.processNewTask();

        if (m.noResult()) {       // necessary?
            m.processNovelTask();
        }

        if (m.noResult()) {       // necessary?
            processConcepts(m);
        }

    }
    
    /**
     * Select a concept to fire.
     */
    @Override
    public void processConcepts(Memory m) {
        Concept currentConcept = concepts.processNext();
        if (currentConcept != null) {
            m.setCurrentTerm(currentConcept.term);
            
            if (m.getRecorder().isActive()) {
                m.getRecorder().append("Concept Selected: " + currentConcept.term);
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
    public boolean addConcept(Concept concept) {
        return concepts.putIn(concept);
    }

    @Override
    public void conceptActivate(Concept c, BudgetValue b) {
        concepts.pickOut(c.getKey());
        BudgetFunctions.activate(c, b);
        concepts.putBack(c);
    }

    @Override
    public Concept sampleNextConcept() {
        return concepts.processNext();
    }
    
    
    
    
    
}
