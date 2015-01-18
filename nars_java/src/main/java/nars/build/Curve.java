package nars.build;

import nars.core.Core;
import nars.core.Memory;
import nars.control.DefaultCore;
import nars.logic.entity.*;
import nars.util.bag.Bag;
import nars.util.bag.CurveBag;
import nars.util.bag.CurveBag.FairPriorityProbabilityCurve;


public class Curve extends Default {
    public final boolean randomRemoval;
    public final CurveBag.BagCurve curve;

    public Curve() {
        this(true);
    }
    
    public Curve(boolean randomRemoval) {
        this(new FairPriorityProbabilityCurve(), randomRemoval);        
    }
    
    public Curve(CurveBag.BagCurve curve, boolean randomRemoval) {
        super();
        this.randomRemoval = randomRemoval;
        this.curve = curve;
    }
    

    @Override
    public Bag<Task<CompoundTerm>,Sentence<CompoundTerm>> newNovelTaskBag() {
        return new CurveBag<Task<CompoundTerm>,Sentence<CompoundTerm>>(getNovelTaskBagSize(), curve, randomRemoval);
    }

    @Override
    public Bag<Concept,Term> newConceptBag() {
        return new CurveBag<>(getConceptBagSize(), curve, randomRemoval);
        //return new AdaptiveContinuousBag<>(getConceptBagSize());
    }

    

    @Override
    public Core newAttention() {
        //return new BalancedSequentialMemoryCycle(newConceptBag(p), c);
        return new DefaultCore(newConceptBag(), newSubconceptBag(), getConceptBuilder());
    }
    
    @Override
    public Concept newConcept(BudgetValue b, final Term t, final Memory m) {
        
        Bag<TaskLink,Sentence> taskLinks = new CurveBag<>(getConceptTaskLinks(), curve, randomRemoval);
        Bag<TermLink,String> termLinks = new CurveBag<>(getConceptTermLinks(), curve, randomRemoval);
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }
    
}
