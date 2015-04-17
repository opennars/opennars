package nars.prototype;

import nars.Memory;
import nars.budget.Bag;
import nars.budget.Budget;
import nars.budget.bag.LevelBag;
import nars.budget.bag.experimental.ChainBag;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.concept.Concept;
import nars.nal.concept.DefaultConcept;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkKey;

import static nars.budget.bag.LevelBag.NextNonEmptyLevelMode.Fast;

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
    public Bag<Sentence<Compound>, Task<Compound>> newNovelTaskBag() {

        return new ChainBag(getNovelTaskBagSize());
    }

    @Override
    public Bag<Term, Concept> newConceptBag() {
        return new LevelBag(getConceptBagLevels(), getConceptBagSize()).setNextNonEmptyMode(Fast);
    }


    @Override
    public Concept newConcept(Budget b, final Term t, final Memory m) {
        
        /*AbstractBag<TaskLink> taskLinks = new ContinuousBag2<>(getTaskLinkBagSize(), m.param.taskCyclesToForget, curve, randomRemoval);
        AbstractBag<TermLink> termLinks = new ContinuousBag2<>(getTermLinkBagSize(), m.param.beliefCyclesToForget, curve, randomRemoval);*/
        
        //Bag<Sentence, TaskLink> taskLinks = new CurveBag<>(getConceptTaskLinks(), randomRemoval);
        //Bag<TermLinkKey, TermLink> termLinks = new CurveBag<>(getConceptTermLinks(), randomRemoval);

        Bag<String, TaskLink> taskLinks = new ChainBag<>(getConceptTaskLinks());
        Bag<TermLinkKey, TermLink> termLinks = new ChainBag<>(getConceptTermLinks());

        return new DefaultConcept(t, b, taskLinks, termLinks, m);
    }
    
}
