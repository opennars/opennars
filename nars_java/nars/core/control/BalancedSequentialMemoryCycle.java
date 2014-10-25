package nars.core.control;

import nars.core.Memory;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.language.Term;
import nars.storage.Bag;

/**
 * Implements a cycle with fair 1:1:1 policy for processing a) New tasks, b) Novel tasks, and c) Concepts
 */
public class BalancedSequentialMemoryCycle extends SequentialMemoryCycle {

    public BalancedSequentialMemoryCycle(Bag<Concept,Term> concepts, Bag<Concept,Term> subcon, ConceptBuilder conceptBuilder) {
        super(concepts, subcon, conceptBuilder);
    }

    @Override
    public void cycle(Memory m) {
        this.memory = m;
        
        m.processNewTasks(1);

        m.processNovelTask();

        processConcept();

    }
    
    
}
