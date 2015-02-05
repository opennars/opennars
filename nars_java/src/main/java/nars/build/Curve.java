package nars.build;

import nars.core.Core;
import nars.core.Memory;
import nars.logic.entity.*;
import nars.util.bag.Bag;
import nars.util.bag.impl.CurveBag;
import nars.util.bag.impl.CurveBag.FairPriorityProbabilityCurve;


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
    public Bag<Sentence<CompoundTerm>, Task<CompoundTerm>> newNovelTaskBag() {
        return new CurveBag<Task<CompoundTerm>,Sentence<CompoundTerm>>(getNovelTaskBagSize(), curve, randomRemoval);
    }

    @Override
    public Bag<Term, Concept> newConceptBag() {
        return new CurveBag<>(getConceptBagSize(), curve, randomRemoval);
        //return new AdaptiveContinuousBag<>(getConceptBagSize());
    }

    

    @Override
    public Core newCore() {
        //return new BalancedSequentialMemoryCycle(newConceptBag(p), c);
        //return new DefaultCore(newConceptBag(), newSubconceptBag(), getConceptBuilder(), newNovelTaskBag());
        return super.newCore();
    }
    
    @Override
    public Concept newConcept(BudgetValue b, final Term t, final Memory m) {
        
        Bag<Sentence, TaskLink> taskLinks = new CurveBag<>(getConceptTaskLinks(), curve, randomRemoval);
        Bag<String, TermLink> termLinks = new CurveBag<>(getConceptTermLinks(), curve, randomRemoval);
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }
    
}
