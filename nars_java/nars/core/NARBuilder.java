package nars.core;

import nars.entity.ConceptBuilder;
import nars.entity.Task;
import nars.io.DefaultTextPerception;
import nars.operator.DefaultOperators;
import nars.operator.ExampleOperators;
import nars.operator.Operator;
import nars.storage.AbstractBag;
import nars.storage.Memory;
import nars.storage.MemoryModel;

/**
 * NAR design parameters which define a NAR at initialization.  These do not change at runtime.
 For runtime parameters, @see Param
 * @author me
 */
abstract public class NARBuilder extends Parameters  {

 
    
    /** initial runtime parameters */
    abstract public Param newParam();
    abstract public AbstractBag<Task> newNovelTaskBag(Param p);
    abstract public MemoryModel newMemoryModel(Param p, ConceptBuilder c);
    
    public NAR build() {
        Param p = newParam();
        Operator[] operators = DefaultOperators.get();
        Memory m = new Memory(p, newMemoryModel(p, getConceptBuilder()), newNovelTaskBag(p), operators);
        
        for (Operator o : ExampleOperators.get()) {
            m.addOperator(o);
        }
        
        
        
        return new NAR(m,                 
                new Perception( new DefaultTextPerception(m) ));
    }

    abstract public ConceptBuilder getConceptBuilder();

    
}
