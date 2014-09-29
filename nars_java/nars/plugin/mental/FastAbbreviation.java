package nars.plugin.mental;

import java.util.concurrent.atomic.AtomicInteger;
import nars.core.EventEmitter.Observer;
import nars.core.Events.TaskDerived;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Plugin;
import nars.entity.Task;
import static nars.language.CompoundTerm.termArray;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.plugin.mental.Abbreviation.Abbreviate;
import nars.util.meter.util.AtomicDouble;

/**
 * 1-step abbreviation, which calls ^abbreviate directly and not through an added Task.
 * Experimental alternative to Abbreviation plugin.
 */
public class FastAbbreviation implements Plugin {


    
    public AtomicInteger abbreviationComplexityMin = new AtomicInteger();
    public AtomicDouble abbreviationQualityMin = new AtomicDouble();
    
    //TODO different parameters for priorities and budgets of both the abbreviation process and the resulting abbreviation judgment
    //public AtomicDouble priorityFactor = new AtomicDouble(1.0);
    
    public boolean canAbbreviate(Task task) {
        return !(task.sentence.content instanceof Operation) && 
                (task.sentence.content.getComplexity() > abbreviationComplexityMin.get()) &&
                (task.budget.getQuality() > abbreviationQualityMin.get());
    }
    
    @Override
    public boolean setEnabled(final NAR n, final boolean enabled) {
        final Memory memory = n.memory;
        
        final Operator abbreviate = memory.getOperator("^abbreviate");
        if (abbreviate == null) {
            memory.addOperator(new Abbreviate());
        }
        
        memory.event.set(new Observer() {            
            
            @Override public void event(Class event, Object[] a) {
                if (event != TaskDerived.class)
                    return;                    

                Task task = (Task)a[0];

                //is it complex and also important? then give it a name:
                if (canAbbreviate(task)) {

                    Operation operation = Operation.make(
                            abbreviate, termArray( task.sentence.content ), 
                            false, memory);

                    abbreviate.call(operation, memory);
                }
                                
            }

        }, enabled, TaskDerived.class);
        
        return true;
    }
    
}
