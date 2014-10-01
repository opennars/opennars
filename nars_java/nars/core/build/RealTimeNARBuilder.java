package nars.core.build;

import nars.core.ConceptProcessor;
import nars.core.Memory;
import nars.core.Param;
import nars.core.control.RealTimeFloodCycle;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Task;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.storage.ContinuousBag;

/**
 *
 * @author me
 */
public class RealTimeNARBuilder extends DefaultNARBuilder {
    private final boolean randomRemoval;

    public RealTimeNARBuilder() {
        this(true);
    }
    public RealTimeNARBuilder(boolean randomRemoval) {
        super();
        this.randomRemoval = randomRemoval;
    }

    @Override
    public AbstractBag<Task> newNovelTaskBag(Param p) {
        return new ContinuousBag<>(getTaskBufferSize(), p.newTaskForgetDurations, randomRemoval);
    }

    @Override
    public AbstractBag<Concept> newConceptBag(Param p) {
        return new ContinuousBag<>(getConceptBagSize(), p.conceptForgetDurations, randomRemoval);
    }
    
    @Override
    public Concept newConcept(final Term t, final Memory m) {        
        //NOT USED, remove this abstract method because it doesnt apply to all types
        return null; 
    }

    @Override
    public ConceptProcessor newConceptProcessor(Param p, ConceptBuilder c) {
        return new RealTimeFloodCycle();
    }
    
    
}