package nars.core.build;

import nars.core.Memory;
import nars.logic.entity.*;
import nars.util.bag.Bag;
import nars.util.bag.CurveBag;
import nars.util.bag.LevelBag;

import static nars.util.bag.LevelBag.NextNonEmptyLevelMode.Fast;

/** Uses discrete bag for concepts, and continuousbag for termlink and tasklink bags. */
public class Discretinuous extends Default {
    private final boolean randomRemoval;

    public Discretinuous() {
        this(true);
    }
    public Discretinuous(boolean randomRemoval) {
        super();
        this.randomRemoval = randomRemoval;
    }

    
    @Override
    public Bag<Task<Term>,Sentence<Term>> newNovelTaskBag() {        
        return new LevelBag(getNovelTaskBagLevels(), getNovelTaskBagSize()).setNextNonEmptyMode(Fast);        
    }

    @Override
    public Bag<Concept,Term> newConceptBag() {
        return new LevelBag(getConceptBagLevels(), getConceptBagSize()).setNextNonEmptyMode(Fast);
    }


    @Override
    public Concept newConcept(BudgetValue b, final Term t, final Memory m) {
        
        /*AbstractBag<TaskLink> taskLinks = new ContinuousBag2<>(getTaskLinkBagSize(), m.param.taskCyclesToForget, curve, randomRemoval);
        AbstractBag<TermLink> termLinks = new ContinuousBag2<>(getTermLinkBagSize(), m.param.beliefCyclesToForget, curve, randomRemoval);*/
        
        Bag<TaskLink,Task> taskLinks = new CurveBag<>(getConceptTaskLinks(), randomRemoval);
        Bag<TermLink,TermLink> termLinks = new CurveBag<>(getConceptTermLinks(), randomRemoval);
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }
    
}
