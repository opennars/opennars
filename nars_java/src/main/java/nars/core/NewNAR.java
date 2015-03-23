package nars.core;

import nars.logic.entity.Compound;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;
import nars.util.bag.Bag;

/**
 * NAR design parameters which define a NAR at initialization.  
 * These do not change after initialization.
 * For runtime parameters, @see Param
 */
abstract public class NewNAR extends Parameters  {

    public final Param param = new Param();
    protected int level;

    abstract public Bag<Sentence<Compound>, Task<Compound>> newNovelTaskBag();
    abstract public Core newCore();

    public NewNAR() {
    }
            
    
//    /** initial runtime parameters */
//    public Param getParam() {
//        return param;
//    }
//    
//    public Bag<Task<Term>,Sentence<Term>> getNovelTaskBag() {
//        return novelTaskBag;
//    }
//    
//    public Attention getAttention() {
//        return attention;
//    }
//    
//    public ConceptBuilder getConceptBuilder() {
//        return conceptBuilder;
//    }

    protected Memory newMemory(Param p) {
        return new Memory(level, p, newCore());
    }

    /** called after NAR created, for initializing it */
    public void init(NAR nar) {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public String toJSON() {
        return Param.json.toJson(this);
    }
    
}
