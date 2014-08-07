package nars.util;

import nars.core.DefaultNARBuilder;
import nars.core.Param;
import nars.core.Parameters;
import nars.entity.Concept;
import nars.entity.Task;
import nars.storage.AbstractBag;
import nars.util.ContinuousBag;


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
        return new ContinuousBag<Task>(Parameters.TASK_BUFFER_SIZE, Parameters.NEW_TASK_FORGETTING_CYCLE, randomRemoval);
    }

    @Override
    public AbstractBag<Concept> newConceptBag(Param p) {
        return new ContinuousBag<Concept>(getConceptBagSize(), Parameters.CONCEPT_FORGETTING_CYCLE, randomRemoval);
    }
}
