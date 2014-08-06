package nars.core;

import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.operator.DefaultOperators;
import nars.operator.Operator;
import nars.storage.AbstractBag;
import nars.storage.Memory;
import nars.storage.NovelTaskBag;

/**
 * NAR design parameters which define a NAR at initialization.  These do not change at runtime.
 For runtime parameters, @see Param
 * @author me
 */
abstract public class NARBuilder extends Parameters implements ConceptBuilder {

 

    
  
    
    /** initial runtime parameters */
    abstract public Param newParam();
    abstract public AbstractBag<Concept> newConceptBag(Param p);
    abstract public NovelTaskBag newNovelTaskBag(Param p);
    
    public NAR build() {
        Param p = newParam();
        Operator[] operators = DefaultOperators.get();
        Memory m = new Memory(p, newConceptBag(p), newNovelTaskBag(p), this, operators);
        return new NAR(m);
    }

    
}
