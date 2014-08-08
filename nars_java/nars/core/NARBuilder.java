package nars.core;

import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Task;
import nars.operator.DefaultOperators;
import nars.operator.ExampleOperators;
import nars.operator.Operator;
import nars.storage.AbstractBag;
import nars.storage.Memory;

/**
 * NAR design parameters which define a NAR at initialization.  These do not change at runtime.
 For runtime parameters, @see Param
 * @author me
 */
abstract public class NARBuilder extends Parameters implements ConceptBuilder {

 
    
    /** initial runtime parameters */
    abstract public Param newParam();
    abstract public AbstractBag<Concept> newConceptBag(Param p);
    abstract public AbstractBag<Task> newNovelTaskBag(Param p);
    
    public NAR build() {
        Param p = newParam();
        Operator[] operators = DefaultOperators.get();
        Memory m = new Memory(p, newConceptBag(p), newNovelTaskBag(p), this, operators);
        
        for (Operator o : ExampleOperators.get()) {
            m.addOperator(o);
        }
        
        return new NAR(m);
    }

    
}
