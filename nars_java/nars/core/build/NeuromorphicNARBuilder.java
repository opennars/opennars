package nars.core.build;

import nars.core.Attention;
import nars.core.Memory;
import nars.core.Param;
import nars.core.control.AntAttention;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
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
public class NeuromorphicNARBuilder extends CurveBagNARBuilder {

    public NeuromorphicNARBuilder() {
        super();        
    }

    @Override
    public Attention newAttention(Param p, ConceptBuilder c) {
        //return new WaveAttention(1000, c);
        return new AntAttention(1000, c);
    }

    
    @Override
    public Bag<Concept, Term> newConceptBag(Param p) {
        //return new DelayBag(getConceptBagSize());
        return null;
    }

    @Override
    public Concept newConcept(BudgetValue b, Term t, Memory m) {
        
        Bag<TaskLink,Task> taskLinks = new CurveBag<>(getTaskLinkBagSize(), true);
        Bag<TermLink,TermLink> termLinks = new CurveBag<>(getTermLinkBagSize(), true);
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }

    
    
}