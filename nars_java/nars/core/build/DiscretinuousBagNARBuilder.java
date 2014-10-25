package nars.core.build;

import nars.core.ConceptProcessor;
import nars.core.Memory;
import nars.core.Param;
import nars.core.control.BalancedSequentialMemoryCycle;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.LevelBag;
import nars.storage.ContinuousBag;

/** Uses discrete bag for concepts, and continuousbag for termlink and tasklink bags. */
public class DiscretinuousBagNARBuilder extends DefaultNARBuilder {
    private final boolean randomRemoval;

    public DiscretinuousBagNARBuilder() {
        this(true);
    }
    public DiscretinuousBagNARBuilder(boolean randomRemoval) {
        super();
        this.randomRemoval = randomRemoval;
    }

    
    @Override
    public Bag<Task,Sentence> newNovelTaskBag(Param p) {
        //return new ContinuousBag2<>(getTaskBufferSize(), p.taskCyclesToForget, curve, randomRemoval);
        return new ContinuousBag<>(getNovelTaskBagSize(), randomRemoval);
    }

    @Override
    public Bag<Concept,Term> newConceptBag(Param p) {
        return new LevelBag(getConceptBagLevels(), getConceptBagSize());
    }

    @Override
    public ConceptProcessor newConceptProcessor(Param p, ConceptBuilder c) {
        //return new SequentialMemoryCycle(newConceptBag(p), c);
        return new BalancedSequentialMemoryCycle(newConceptBag(p), c);
    }
    
    @Override
    public Concept newConcept(BudgetValue b, final Term t, final Memory m) {
        
        /*AbstractBag<TaskLink> taskLinks = new ContinuousBag2<>(getTaskLinkBagSize(), m.param.taskCyclesToForget, curve, randomRemoval);
        AbstractBag<TermLink> termLinks = new ContinuousBag2<>(getTermLinkBagSize(), m.param.beliefCyclesToForget, curve, randomRemoval);*/
        
        Bag<TaskLink,Task> taskLinks = new ContinuousBag<>(getTaskLinkBagSize(), randomRemoval);
        Bag<TermLink,TermLink> termLinks = new ContinuousBag<>(getTermLinkBagSize(), randomRemoval);
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }
    
}
