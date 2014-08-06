package nars.core;

import nars.entity.Concept;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.storage.Bag;
import nars.storage.Memory;
import nars.storage.NovelTaskBag;
import nars.storage.TaskLinkBag;
import nars.storage.TermLinkBag;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class DefaultNARBuilder extends NARBuilder {

    
    public DefaultNARBuilder() {
        super();
        
        setConceptBagLevels(100);
        setConceptBagSize(1000);        
        
        setTaskLinkBagLevels(100);        
        setTaskLinkBagSize(20);

        setTermLinkBagLevels(100);
        setTermLinkBagSize(100);
    }

    @Override
    public Param newParam() {
        Param p = new Param();
        p.noiseLevel.set(100);
        p.conceptForgettingRate.set(10);             
        p.taskForgettingRate.set(20);
        p.beliefForgettingRate.set(50);
    
        return p;
    }

    @Override
    public Concept newConcept(final Term t, final Memory m) {
        
        TaskLinkBag taskLinks = new TaskLinkBag(getTaskLinkBagLevels(), getTaskLinkBagSize(), m.param.taskForgettingRate);
        TermLinkBag termLinks = new TermLinkBag(getTermLinkBagLevels(), getTermLinkBagSize(), m.param.beliefForgettingRate);
        
        return new Concept(t, taskLinks, termLinks, m);        
    }

    
    @Override
    public AbstractBag<Concept> newConceptBag(Param p) {
        return new Bag(getConceptBagLevels(), getConceptBagSize(), p.conceptForgettingRate);
    }

    @Override
    public NovelTaskBag newNovelTaskBag(Param p) {
        return new NovelTaskBag(getConceptBagLevels(), Parameters.TASK_BUFFER_SIZE); 
    }
 
    public int taskLinkBagLevels;
    
    /** Size of TaskLinkBag */
    public int taskLinkBagSize;
    
    public int termLinkBagLevels;
    
    /** Size of TermLinkBag */
    public int termLinkBagSize;
    
    /** determines maximum number of concepts */
    private int conceptBagSize;    
    public int getConceptBagSize() { return conceptBagSize; }    
    public DefaultNARBuilder setConceptBagSize(int conceptBagSize) { this.conceptBagSize = conceptBagSize; return this;   }

    /** Level granularity in Bag, usually 100 (two digits) */    
    private int conceptBagLevels;
    public int getConceptBagLevels() { return conceptBagLevels; }    
    public DefaultNARBuilder setConceptBagLevels(int bagLevels) { this.conceptBagLevels = bagLevels; return this;  }
        
    /**
     * @return the taskLinkBagLevels
     */
    public int getTaskLinkBagLevels() {
        return taskLinkBagLevels;
    }

    public DefaultNARBuilder setTaskLinkBagLevels(int taskLinkBagLevels) {
        this.taskLinkBagLevels = taskLinkBagLevels;
        return this;
    }

    public int getTaskLinkBagSize() {
        return taskLinkBagSize;
    }

    public DefaultNARBuilder setTaskLinkBagSize(int taskLinkBagSize) {
        this.taskLinkBagSize = taskLinkBagSize;
        return this;
    }

    public int getTermLinkBagLevels() {
        return termLinkBagLevels;
    }

    public DefaultNARBuilder setTermLinkBagLevels(int termLinkBagLevels) {
        this.termLinkBagLevels = termLinkBagLevels;
        return this;
    }

    public int getTermLinkBagSize() {
        return termLinkBagSize;
    }

    public DefaultNARBuilder setTermLinkBagSize(int termLinkBagSize) {
        this.termLinkBagSize = termLinkBagSize;
        return this;
    }
    
}
