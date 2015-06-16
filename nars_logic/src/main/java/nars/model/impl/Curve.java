package nars.model.impl;

import nars.Memory;
import nars.bag.Bag;
import nars.bag.impl.CurveBag;
import nars.bag.impl.CurveBag.FairPriorityProbabilityCurve;
import nars.budget.Budget;
import nars.model.ControlCycle;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.concept.Concept;
import nars.nal.concept.DefaultConcept;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.util.data.id.Identifier;


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
    public ControlCycle newControlCycle() {
        //return new BalancedSequentialMemoryCycle(newConceptBag(p), c);
        //return new DefaultCore(newConceptBag(), newSubconceptBag(), getConceptBuilder(), newNovelTaskBag());
        return super.newControlCycle();
    }
    
    @Override
    public Concept newConcept(final Term t, Budget b, final Memory m) {


        Bag<Sentence, TaskLink> taskLinks = new CurveBag<>(rng, getConceptTaskLinks(), curve, randomRemoval);
        Bag<Identifier, TermLink> termLinks = new CurveBag<>(rng, getConceptTermLinks(), curve, randomRemoval);
        
        return new DefaultConcept(t, b, taskLinks, termLinks, m);
    }
    
}
