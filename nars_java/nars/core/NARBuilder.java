package nars.core;

import java.util.concurrent.atomic.AtomicInteger;
import nars.entity.ConceptBuilder;
import nars.storage.ConceptBag;
import nars.storage.Memory;
import nars.storage.NovelTaskBag;

/**
 * NAR design parameters which define a NAR at initialization.  These do not change at runtime.
 * For runtime parameters, @see NARParams
 * @author me
 */
abstract public class NARBuilder extends Parameters implements NARConfiguration, ConceptBuilder {

    @Deprecated public final AtomicInteger conceptForgettingRate = new AtomicInteger(Parameters.CONCEPT_FORGETTING_CYCLE);

    public int taskLinkBagLevels;
    
    /** Size of TaskLinkBag */
    public int taskLinkBagSize;
    
    public int termLinkBagLevels;
    
    /** Size of TermLinkBag */
    public int termLinkBagSize;
    
  
    
    /** initial runtime parameters */
    abstract public NARParams newInitialParams();
    abstract public ConceptBag newConceptBag();
    abstract public NovelTaskBag newNovelTaskBag();
    
    public NAR build() {
        NARParams p = newInitialParams();
        Memory m = new Memory(p, newConceptBag(), newNovelTaskBag(), this);
        return new NAR(m);
    }

    /**
     * @return the taskLinkBagLevels
     */
    @Override
    public int getTaskLinkBagLevels() {
        return taskLinkBagLevels;
    }

    public NARBuilder setTaskLinkBagLevels(int taskLinkBagLevels) {
        this.taskLinkBagLevels = taskLinkBagLevels;
        return this;
    }

    @Override
    public int getTaskLinkBagSize() {
        return taskLinkBagSize;
    }

    public NARBuilder setTaskLinkBagSize(int taskLinkBagSize) {
        this.taskLinkBagSize = taskLinkBagSize;
        return this;
    }

    @Override
    public int getTermLinkBagLevels() {
        return termLinkBagLevels;
    }

    public NARBuilder setTermLinkBagLevels(int termLinkBagLevels) {
        this.termLinkBagLevels = termLinkBagLevels;
        return this;
    }

    @Override
    public int getTermLinkBagSize() {
        return termLinkBagSize;
    }

    public NARBuilder setTermLinkBagSize(int termLinkBagSize) {
        this.termLinkBagSize = termLinkBagSize;
        return this;
    }
}
