package nars.nar;

import nars.Memory;
import nars.bag.Bag;
import nars.bag.impl.CurveBag;
import nars.bag.impl.CurveBag.FairPriorityProbabilityCurve;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.process.CycleProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;


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
    public Bag<Sentence<Compound>, Task<Compound>> newNovelTaskBag() {
        return new CurveBag(rng, getNovelTaskBagSize(), curve, randomRemoval);
    }

    @Override
    public Bag<Term, Concept> newConceptBag() {
        return new CurveBag(rng, getActiveConcepts(), curve, randomRemoval);
        //return new AdaptiveContinuousBag<>(getConceptBagSize());
    }

    

    @Override
    public CycleProcess newControlCycle() {
        //return new BalancedSequentialMemoryCycle(newConceptBag(p), c);
        //return new DefaultCore(newConceptBag(), newSubconceptBag(), getConceptBuilder(), newNovelTaskBag());
        return super.newControlCycle();
    }
    
    @Override
    public Concept newConcept(final Term t, Budget b, final Memory m) {


        Bag<Sentence, TaskLink> taskLinks = new CurveBag<>(rng, getConceptTaskLinks(), curve, randomRemoval);
        Bag<TermLinkKey, TermLink> termLinks = new CurveBag<>(rng, getConceptTermLinks(), curve, randomRemoval);
        
        return new DefaultConcept(t, b, taskLinks, termLinks, m);
    }
    
}
