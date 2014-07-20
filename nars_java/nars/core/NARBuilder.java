package nars.core;

/**
 *
 * @author me
 */


abstract public class NARBuilder extends Parameters {
    
    /** Silent threshold for task reporting, in [0, 100]. */
    private int silenceLevel;
    public int getSilenceLevel() { return silenceLevel;    }
    public NARBuilder setSilenceLevel(int silenceLevel) { this.silenceLevel = silenceLevel; return this; }

    /** determines maximum number of concepts */
    private int conceptBagSize;
    public int getConceptBagSize() { return conceptBagSize; }    
    public NARBuilder setConceptBagSize(int conceptBagSize) { this.conceptBagSize = conceptBagSize; return this;   }

    /** Level granularity in Bag, usually 100 (two digits) */    
    private int bagLevels;
    public int getBagLevels() { return bagLevels; }    
    public NARBuilder setBagLevels(int bagLevels) { this.bagLevels = bagLevels; return this;  }
    
    public static class NARParams {
        
    }
    
    /** initial runtime parameters */
    public NARParams newInitialParams() {
        return null;
    }
    
    public NAR build() {
        return new NAR(this/*, newInitialParams()*/);
    }
}
