package nars.core;

/**
 * NAR design parameters which define a NAR at initialization.  These do not change at runtime.
 * For runtime parameters, @see NARParams
 * @author me
 */
abstract public class NARBuilder extends Parameters {
    
    /** determines maximum number of concepts */
    private int conceptBagSize;
    public int getConceptBagSize() { return conceptBagSize; }    
    public NARBuilder setConceptBagSize(int conceptBagSize) { this.conceptBagSize = conceptBagSize; return this;   }

    /** Level granularity in Bag, usually 100 (two digits) */    
    private int bagLevels;
    public int getBagLevels() { return bagLevels; }    
    public NARBuilder setBagLevels(int bagLevels) { this.bagLevels = bagLevels; return this;  }
    
    
    
    /** initial runtime parameters */
    abstract public NARParams newInitialParams();
    
    public NAR build() {
        return new NAR(this/*, newInitialParams()*/);
    }
}
