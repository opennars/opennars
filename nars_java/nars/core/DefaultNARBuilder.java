package nars.core;

import nars.entity.Concept;
import nars.language.Term;
import nars.storage.ConceptBag;
import nars.storage.Memory;
import nars.storage.NovelTaskBag;
import nars.storage.TaskLinkBag;
import nars.storage.TermLinkBag;

/**
 * Default set of NAR parameters.
 * @author me
 */
public class DefaultNARBuilder extends NARBuilder {

    
    final static int DefaultLevels = 100;
    
    public DefaultNARBuilder() {
        super();
        
        setConceptBagLevels(DefaultLevels);
        setConceptBagSize(1000);        
        
        setTaskLinkBagLevels(DefaultLevels);        
        setTaskLinkBagSize(20);

        setTermLinkBagLevels(DefaultLevels);
        setTermLinkBagSize(100);
    }

    @Override
    public NARParams newInitialParams() {
        NARParams p = new NARParams();
        p.setSilenceLevel(0);
        return p;
    }

    @Override
    public Concept newConcept(final Term t, final Memory m) {
        
        TaskLinkBag taskLinks = new TaskLinkBag(getTaskLinkBagLevels(), getTaskLinkBagSize(), m.taskForgettingRate);
        TermLinkBag termLinks = new TermLinkBag(getTermLinkBagLevels(), getTermLinkBagSize(), m.beliefForgettingRate);
        
        return new Concept(t, taskLinks, termLinks, m);        
    }

    
    @Override
    public ConceptBag newConceptBag() {
        return new ConceptBag(getConceptBagLevels(), getConceptBagSize(), conceptForgettingRate);
    }

    @Override
    public NovelTaskBag newNovelTaskBag() {
        return new NovelTaskBag(getConceptBagLevels(), Parameters.TASK_BUFFER_SIZE); 
    }
 
    
    /** determines maximum number of concepts */
    private int conceptBagSize;    
    @Override public int getConceptBagSize() { return conceptBagSize; }    
    public NARBuilder setConceptBagSize(int conceptBagSize) { this.conceptBagSize = conceptBagSize; return this;   }

    /** Level granularity in Bag, usually 100 (two digits) */    
    private int conceptBagLevels;
    @Override public int getConceptBagLevels() { return conceptBagLevels; }    
    public NARBuilder setConceptBagLevels(int bagLevels) { this.conceptBagLevels = bagLevels; return this;  }
        
    
}
