package nars.core.control;

import nars.core.Memory;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.storage.AbstractBag;

/**
 *
 * @author me
 */


public class BalancedSequentialMemoryCycle extends SequentialMemoryCycle {

    public BalancedSequentialMemoryCycle(AbstractBag<Concept> concepts, ConceptBuilder conceptBuilder) {
        super(concepts, conceptBuilder);
    }

    @Override
    public void cycle(Memory m) {
        
        m.processNewTasks(1);

        m.processNovelTask();

        processConcept(m);

    }
    
    
}
