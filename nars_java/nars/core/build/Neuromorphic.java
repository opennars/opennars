package nars.core.build;

import nars.core.Attention;
import nars.core.Memory;
import nars.core.control.AntAttention;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.CurveBag;

/**
 *
 * https://en.wikipedia.org/wiki/Neuromorphic_engineering
 */
public class Neuromorphic extends Curve {
    private int numAnts;

    public Neuromorphic() {
        this(-1);
    }
    
    public Neuromorphic(int numAnts) {
        super();        
        this.type = "neuromorphic";
        this.numAnts = numAnts;
    }

    @Override
    public Attention newAttention() {
        //return new WaveAttention(1000, c);
        if (numAnts == -1)
            numAnts = param.conceptsFiredPerCycle.get();
        return new AntAttention(numAnts, 1.0f, getConceptBagSize(), getConceptBuilder());
    }

    
    @Override
    public Bag<Concept, Term> newConceptBag() {
        //return new DelayBag(getConceptBagSize());
        return null;
    }

    @Override
    public Concept newConcept(BudgetValue b, Term t, Memory m) {
        
        Bag<TaskLink,Task> taskLinks = new CurveBag<>(getConceptTaskLinks(), true);
        Bag<TermLink,TermLink> termLinks = new CurveBag<>(getConceptTermLinks(), true);
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }

    
    
}