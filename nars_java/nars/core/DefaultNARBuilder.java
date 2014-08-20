package nars.core;

import java.util.concurrent.atomic.AtomicInteger;
import nars.core.control.SequentialMemoryCycle;
import nars.entity.Concept;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.storage.Bag;
import nars.storage.Memory;

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
        p.cycleInputTasks.set(1);
                
        
        //Experimental parameters - adjust at your own risk
        p.cycleMemory.set(1);                
        return p;
    }

    @Override
    public Memory.MemoryModel newMemoryModel(Param p) {
        return new SequentialMemoryCycle(newConceptBag(p));
    }
    
    

    @Override
    public Concept newConcept(final Term t, final Memory m) {
        
        AbstractBag<TaskLink> taskLinks = new Bag<>(getTaskLinkBagLevels(), getTaskLinkBagSize(), m.param.taskForgettingRate);
        AbstractBag<TermLink> termLinks = new Bag<>(getTermLinkBagLevels(), getTermLinkBagSize(), m.param.beliefForgettingRate);
        
        return new Concept(t, taskLinks, termLinks, m);        
    }

    
    protected AbstractBag<Concept> newConceptBag(Param p) {
        return new Bag(getConceptBagLevels(), getConceptBagSize(), p.conceptForgettingRate);
    }

    @Override
    public AbstractBag<Task> newNovelTaskBag(Param p) {
        return new Bag<>(getConceptBagLevels(), Parameters.TASK_BUFFER_SIZE, new AtomicInteger(Parameters.NEW_TASK_FORGETTING_CYCLE));
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
