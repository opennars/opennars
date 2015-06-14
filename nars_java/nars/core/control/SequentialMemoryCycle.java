package nars.core.control;

import java.util.Collection;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.inference.BudgetFunctions;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.storage.Memory;
import nars.storage.MemoryModel;

/**
 * A deterministic memory cycle implementation that is used for development and testing.
 */
public class SequentialMemoryCycle implements MemoryModel {


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
    public void processConcepts(Memory m) {
        Concept currentConcept = concepts.processNext();
        if (currentConcept != null) {
            
            
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
    public Concept addConcept(Term term, Memory memory) {
        Concept concept = conceptBuilder.newConcept(term, memory);
        
        boolean added = concepts.putIn(concept);
        if (!added)
            return null;
        
        return concept;
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
