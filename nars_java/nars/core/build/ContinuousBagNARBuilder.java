package nars.core.build;

import nars.core.ConceptProcessor;
import nars.core.Memory;
import nars.core.Param;
import nars.core.control.SequentialMemoryCycle;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.storage.AdaptiveContinuousBag;
import nars.storage.ContinuousBag2;
import nars.storage.ContinuousBag2.BagCurve;


public class ContinuousBagNARBuilder extends DefaultNARBuilder {
    public final boolean randomRemoval;
    public final BagCurve curve;

    public ContinuousBagNARBuilder() {
        this(true);
    }
    
    public ContinuousBagNARBuilder(boolean randomRemoval) {
        this(new ContinuousBag2.QuadraticBagCurve(), randomRemoval);        
    }
    
    public ContinuousBagNARBuilder(BagCurve curve, boolean randomRemoval) {
        super();
        this.randomRemoval = randomRemoval;
        this.curve = curve;
    }
    

    @Override
    public AbstractBag<Task,Sentence> newNovelTaskBag(Param p) {
        return new ContinuousBag2<>(getNovelTaskBagSize(), curve, randomRemoval);
    }

    @Override
    public AbstractBag<Concept,Term> newConceptBag(Param p) {
        //return new ContinuousBag2<>(getConceptBagSize(), curve, randomRemoval);
        return new AdaptiveContinuousBag<>(getConceptBagSize());
    }

    @Override
    public ConceptProcessor newConceptProcessor(Param p, ConceptBuilder c) {
        //return new BalancedSequentialMemoryCycle(newConceptBag(p), c);
        return new SequentialMemoryCycle(newConceptBag(p), c);
    }
    
    @Override
    public Concept newConcept(final Term t, final Memory m) {
        
        AbstractBag<TaskLink,TermLink> taskLinks = new ContinuousBag2<>(getTaskLinkBagSize(), curve, randomRemoval);
        AbstractBag<TermLink,TermLink> termLinks = new ContinuousBag2<>(getTermLinkBagSize(), curve, randomRemoval);
        
        return new Concept(t, taskLinks, termLinks, m);        
    }
    
}
