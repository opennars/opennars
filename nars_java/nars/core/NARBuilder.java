package nars.core;

/**
 * NAR design parameters which define a NAR at initialization.  These do not change at runtime.
 * For runtime parameters, @see NARParams
 * @author me
 */
abstract public class NARBuilder extends Parameters implements NARConfiguration {
    
    /** determines maximum number of concepts */
    private int conceptBagSize;
    
    @Override public int getConceptBagSize() { return conceptBagSize; }    
    public NARBuilder setConceptBagSize(int conceptBagSize) { this.conceptBagSize = conceptBagSize; return this;   }

    /** Level granularity in Bag, usually 100 (two digits) */    
    private int conceptBagLevels;
    @Override public int getConceptBagLevels() { return conceptBagLevels; }    
    public NARBuilder setConceptBagLevels(int bagLevels) { this.conceptBagLevels = bagLevels; return this;  }
    
    public int taskLinkBagLevels;
    
    /** Size of TaskLinkBag */
    public int taskLinkBagSize;
    
    public int termLinkBagLevels;
    
    /** Size of TermLinkBag */
    public int termLinkBagSize;
    
  
    
    /** initial runtime parameters */
    abstract public NARParams newInitialParams();
    
    public NAR build() {
        return new NAR(this/*, newInitialParams()*/);
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
