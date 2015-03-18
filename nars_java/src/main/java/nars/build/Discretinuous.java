package nars.build;

import nars.core.Memory;
import nars.logic.entity.*;
import nars.logic.entity.tlink.TermLinkKey;
import nars.util.bag.Bag;
import nars.util.bag.impl.LevelBag;
import nars.util.bag.impl.experimental.ChainBag;

import static nars.util.bag.impl.LevelBag.NextNonEmptyLevelMode.Fast;

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
    public Bag<Sentence<CompoundTerm>, Task<CompoundTerm>> newNovelTaskBag() {
        return new ChainBag(getNovelTaskBagSize());
    }

    @Override
    public Bag<Term, Concept> newConceptBag() {
        return new LevelBag(getConceptBagLevels(), getConceptBagSize()).setNextNonEmptyMode(Fast);
    }


    @Override
    public Concept newConcept(BudgetValue b, final Term t, final Memory m) {
        
        /*AbstractBag<TaskLink> taskLinks = new ContinuousBag2<>(getTaskLinkBagSize(), m.param.taskCyclesToForget, curve, randomRemoval);
        AbstractBag<TermLink> termLinks = new ContinuousBag2<>(getTermLinkBagSize(), m.param.beliefCyclesToForget, curve, randomRemoval);*/
        
        //Bag<Sentence, TaskLink> taskLinks = new CurveBag<>(getConceptTaskLinks(), randomRemoval);
        //Bag<TermLinkKey, TermLink> termLinks = new CurveBag<>(getConceptTermLinks(), randomRemoval);

        Bag<String, TaskLink> taskLinks = new ChainBag<>(getConceptTaskLinks());
        Bag<TermLinkKey, TermLink> termLinks = new ChainBag<>(getConceptTermLinks());

        return new Concept(b, t, taskLinks, termLinks, m);        
    }
    
}
