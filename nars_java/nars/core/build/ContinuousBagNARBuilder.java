package nars.core.build;

import nars.core.ConceptProcessor;
import nars.core.Memory;
import nars.core.Param;
import nars.core.control.SequentialMemoryCycle;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.storage.ContinuousBag2;
import nars.storage.ContinuousBag2.BagCurve;


public class ContinuousBagNARBuilder extends DefaultNARBuilder {
    private final boolean randomRemoval;

    public ContinuousBagNARBuilder() {
        this(true);
    }
    public ContinuousBagNARBuilder(boolean randomRemoval) {
        super();
        this.randomRemoval = randomRemoval;
    }
    
    //final static BagCurve curve = new ContinuousBag2.DefaultBagCurve();
    final static BagCurve curve = new ContinuousBag2.CubicBagCurve();

    @Override
    public AbstractBag<Task> newNovelTaskBag(Param p) {
        return new ContinuousBag2<>(getTaskBufferSize(), p.taskCycleForgetDurations, curve, randomRemoval);
    }

    @Override
    public AbstractBag<Concept> newConceptBag(Param p) {
        return new ContinuousBag2<>(getConceptBagSize(), p.conceptForgetDurations, curve, randomRemoval);
    }

    @Override
    public ConceptProcessor newConceptProcessor(Param p, ConceptBuilder c) {
        //return new BalancedSequentialMemoryCycle(newConceptBag(p), c);
        return new SequentialMemoryCycle(newConceptBag(p), c);
    }
    
    @Override
    public Concept newConcept(final Term t, final Memory m) {
        
        AbstractBag<TaskLink> taskLinks = new ContinuousBag2<>(getTaskLinkBagSize(), m.param.taskCycleForgetDurations, curve, randomRemoval);
        AbstractBag<TermLink> termLinks = new ContinuousBag2<>(getTermLinkBagSize(), m.param.beliefForgetDurations, curve, randomRemoval);
        
        return new Concept(t, taskLinks, termLinks, m);        
    }
    
}
