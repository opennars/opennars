package nars.util;

import nars.core.build.DefaultNARBuilder;
import nars.core.Param;
import nars.core.Parameters;
import nars.entity.Concept;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.storage.Memory;


public class ContinuousBagNARBuilder extends DefaultNARBuilder {
    private final boolean randomRemoval;

    public ContinuousBagNARBuilder() {
        this(true);
    }
    public ContinuousBagNARBuilder(boolean randomRemoval) {
        super();
        this.randomRemoval = randomRemoval;
    }

    @Override
    public AbstractBag<Task> newNovelTaskBag(Param p) {
        return new ContinuousBag<>(Parameters.TASK_BUFFER_SIZE, Parameters.NEW_TASK_FORGETTING_CYCLE, randomRemoval);
    }

    @Override
    public AbstractBag<Concept> newConceptBag(Param p) {
        return new ContinuousBag<>(getConceptBagSize(), Parameters.CONCEPT_FORGETTING_CYCLE, randomRemoval);
    }
    
    @Override
    public Concept newConcept(final Term t, final Memory m) {
        
        AbstractBag<TaskLink> taskLinks = new ContinuousBag<>(getTaskLinkBagSize(), m.param.taskForgettingRate, randomRemoval);
        AbstractBag<TermLink> termLinks = new ContinuousBag<>(getTermLinkBagSize(), m.param.beliefForgettingRate, randomRemoval);
        
        return new Concept(t, taskLinks, termLinks, m);        
    }
    
}
