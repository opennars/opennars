package nars.core.build;

import nars.core.ConceptProcessor;
import nars.core.Memory;
import nars.core.Param;
import nars.core.control.SequentialMemoryCycle;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.ContinuousBag;
import nars.storage.ContinuousBag2.BagCurve;
import nars.storage.ContinuousBag2.PriorityProbabilityApproximateCurve;


public class ContinuousBagNARBuilder extends DefaultNARBuilder {
    public final boolean randomRemoval;
    public final BagCurve curve;

    public ContinuousBagNARBuilder() {
        this(true);
    }
    
    public ContinuousBagNARBuilder(boolean randomRemoval) {
        this(new PriorityProbabilityApproximateCurve(), randomRemoval);        
    }
    
    public ContinuousBagNARBuilder(BagCurve curve, boolean randomRemoval) {
        super();
        this.randomRemoval = randomRemoval;
        this.curve = curve;
    }
    

    @Override
    public Bag<Task,Sentence> newNovelTaskBag(Param p) {
        return new ContinuousBag<>(getNovelTaskBagSize(), curve, randomRemoval);
    }

    @Override
    public Bag<Concept,Term> newConceptBag(Param p) {
        return new ContinuousBag<>(getConceptBagSize(), curve, randomRemoval);
        //return new AdaptiveContinuousBag<>(getConceptBagSize());
    }

    @Override
    public ConceptProcessor newConceptProcessor(Param p, ConceptBuilder c) {
        //return new BalancedSequentialMemoryCycle(newConceptBag(p), c);
        return new SequentialMemoryCycle(newConceptBag(p), newSubconceptBag(p), c);
    }
    
    @Override
    public Concept newConcept(BudgetValue b, final Term t, final Memory m) {
        
        Bag<TaskLink,Task> taskLinks = new ContinuousBag<>(getTaskLinkBagSize(), curve, randomRemoval);
        Bag<TermLink,TermLink> termLinks = new ContinuousBag<>(getTermLinkBagSize(), curve, randomRemoval);
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }
    
}
