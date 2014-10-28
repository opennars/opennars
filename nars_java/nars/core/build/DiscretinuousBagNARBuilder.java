package nars.core.build;

import nars.core.Memory;
import nars.core.Param;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.CurveBag;
import nars.storage.LevelBag;

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
        return new CurveBag<>(getNovelTaskBagSize(), randomRemoval);
    }

    @Override
    public Bag<Concept,Term> newConceptBag(Param p) {
        return new LevelBag(getConceptBagLevels(), getConceptBagSize());
        
        //NOT READY yet
        //return new GearBag(getConceptBagLevels(), getConceptBagSize());
    }


    @Override
    public Concept newConcept(BudgetValue b, final Term t, final Memory m) {
        
        /*AbstractBag<TaskLink> taskLinks = new ContinuousBag2<>(getTaskLinkBagSize(), m.param.taskCyclesToForget, curve, randomRemoval);
        AbstractBag<TermLink> termLinks = new ContinuousBag2<>(getTermLinkBagSize(), m.param.beliefCyclesToForget, curve, randomRemoval);*/
        
        Bag<TaskLink,Task> taskLinks = new CurveBag<>(getTaskLinkBagSize(), randomRemoval);
        Bag<TermLink,TermLink> termLinks = new CurveBag<>(getTermLinkBagSize(), randomRemoval);
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }
    
}
