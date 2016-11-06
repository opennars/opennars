package nars.core;

import nars.core.control.DefaultAttention;
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
    abstract public DefaultAttention newAttention();

    public Build() {
    }
            
    @Deprecated public NAR build() {
        return new NAR(this)
        //return build(g, g.param);
        ;
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
