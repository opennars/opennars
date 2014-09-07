package nars.core.build;

import nars.core.ConceptProcessor;
import nars.core.Memory;
import nars.core.Param;
import nars.core.control.BalancedSequentialMemoryCycle;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.storage.ContinuousBag;


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
        return new ContinuousBag<>(getTaskBufferSize(), p.taskCyclesToForget, randomRemoval);
    }

    @Override
    public AbstractBag<Concept> newConceptBag(Param p) {
        return new ContinuousBag<>(getConceptBagSize(), p.conceptCyclesToForget, randomRemoval);
    }

    @Override
    public ConceptProcessor newConceptProcessor(Param p, ConceptBuilder c) {
        return new BalancedSequentialMemoryCycle(newConceptBag(p), c);
    }
    
    @Override
    public Concept newConcept(final Term t, final Memory m) {
        
        AbstractBag<TaskLink> taskLinks = new ContinuousBag<>(getTaskLinkBagSize(), m.param.taskCyclesToForget, randomRemoval);
        AbstractBag<TermLink> termLinks = new ContinuousBag<>(getTermLinkBagSize(), m.param.beliefCyclesToForget, randomRemoval);
        
        return new Concept(t, taskLinks, termLinks, m);        
    }
    
}
