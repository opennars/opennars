package nars.core;

import nars.entity.ConceptBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.operator.DefaultOperators;
import nars.operator.ExampleOperators;
import nars.operator.Operator;
import nars.storage.AbstractBag;

/**
 * NAR design parameters which define a NAR at initialization.  These do not change at runtime.
 For runtime parameters, @see Param
 * @author me
 */
abstract public class NARBuilder extends Parameters  {
     
    /** initial runtime parameters */
    abstract public Param newParam();
    abstract public AbstractBag<Task,Sentence> newNovelTaskBag(Param p);
    abstract public ConceptProcessor newConceptProcessor(Param p, ConceptBuilder c);
    abstract public ConceptBuilder getConceptBuilder();
    
    public NAR build() {
        Param p = newParam();
        Operator[] operators = DefaultOperators.get();
        Memory m = new Memory(p, newConceptProcessor(p, getConceptBuilder()), newNovelTaskBag(p), operators);
        
        for (Operator o : ExampleOperators.get()) {
            m.addOperator(o);
        }
        
        return new NAR(m, new Perception());
    }
    
}
