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
import nars.storage.CurveBag;
import nars.storage.CurveBag.FairPriorityProbabilityCurve;


public class CurveBagNARBuilder extends DefaultNARBuilder {
    public final boolean randomRemoval;
    public final CurveBag.BagCurve curve;

    public CurveBagNARBuilder() {
        this(true);
    }
    
    public CurveBagNARBuilder(boolean randomRemoval) {
        this(new FairPriorityProbabilityCurve(), randomRemoval);        
    }
    
    public CurveBagNARBuilder(CurveBag.BagCurve curve, boolean randomRemoval) {
        super();
        this.randomRemoval = randomRemoval;
        this.curve = curve;
    }
    

    @Override
    public Bag<Task,Sentence> newNovelTaskBag(Param p) {
        return new CurveBag<Task,Sentence>(getNovelTaskBagSize(), curve, randomRemoval);
    }

    @Override
    public Bag<Concept,Term> newConceptBag(Param p) {
        return new CurveBag<>(getConceptBagSize(), curve, randomRemoval);
        //return new AdaptiveContinuousBag<>(getConceptBagSize());
    }

    @Override
    protected Bag<Concept, Term> newSubconceptBag(Param p) {
        if (getSubconceptBagSize() == 0) return null;
        return new CurveBag<>(getSubconceptBagSize(), curve, randomRemoval);
    }
    
    

    @Override
    public ConceptProcessor newConceptProcessor(Param p, ConceptBuilder c) {
        //return new BalancedSequentialMemoryCycle(newConceptBag(p), c);
        return new SequentialMemoryCycle(newConceptBag(p), newSubconceptBag(p), c);
    }
    
    @Override
    public Concept newConcept(BudgetValue b, final Term t, final Memory m) {
        
        Bag<TaskLink,Task> taskLinks = new CurveBag<>(getTaskLinkBagSize(), curve, randomRemoval);
        Bag<TermLink,TermLink> termLinks = new CurveBag<>(getTermLinkBagSize(), curve, randomRemoval);
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }
    
}
