package nars.core;

import nars.entity.Sentence;
import nars.entity.Task;
import nars.language.Term;
import nars.storage.Bag;

/**
 * NAR design parameters which define a NAR at initialization.  
 * These do not change after initialization.
 * For runtime parameters, @see Param
 */
abstract public class Build extends Parameters  {
     
    public String type = "abstract";
       
    public Param param = new Param();
    
    abstract public Bag<Task<Term>,Sentence<Term>> newNovelTaskBag();
    abstract public Attention newAttention();

    public Build() {
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
    
    @Deprecated public NAR build() {
        return NAR.build(this);
    }

    protected Memory newMemory(Param p) {        
        return new Memory(p, newAttention(), newNovelTaskBag());
    }

    /** called after NAR created, for initializing it */
    public NAR init(NAR nar) {
        return nar;
    }
    
    @Override
    public String toString() {
        return Param.json.toJson(this).toString();
    }
    
}
