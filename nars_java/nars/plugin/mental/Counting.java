package nars.plugin.mental;

import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Plugin;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.Symbols;
import nars.language.Inheritance;
import nars.language.Product;
import nars.language.SetExt;
import nars.language.Term;

/**
 * Counting and Cardinality
 */
public class Counting implements Plugin {

    @Override public boolean setEnabled(NAR n, boolean enabled) {
        Memory memory = n.memory;
        memory.event.set(new Observer() {

            @Override
            public void event(Class event, Object[] a) {
                
                if (event!=Events.TaskDerived.class)
                    return;
                
                Task task = (Task)a[0];
                
                if(task.sentence.punctuation==Symbols.JUDGMENT_MARK) { 
                    //lets say we have <{...} --> M>.
                    if(task.sentence.content instanceof Inheritance) {
                        
                        Inheritance inh=(Inheritance) task.sentence.content;
                        
                        if(inh.getSubject() instanceof SetExt) {
                            
                            SetExt set_term=(SetExt) inh.getSubject();
                            
                            //this gets the cardinality of M
                            int cardinality=set_term.size();   
                            
                            //now create term <(*,M,cardinality) --> CARDINALITY>.
                            Term[] product_args = new Term[] { 
                                inh.getPredicate(),
                                new Term(String.valueOf(cardinality)) 
                            };
                            
                            //TODO CARDINATLITY can be a static final instance shared by all
                            Term new_term=Inheritance.make(
                                Product.make(product_args, memory), /* --> */ new Term("CARDINALITY"), 
                                memory);

                            TruthValue truth = task.sentence.truth.clone();
                            Stamp stampi = task.sentence.stamp.clone();
                            Sentence j = new Sentence(new_term, Symbols.JUDGMENT_MARK, truth, stampi);
                            BudgetValue budg = task.budget.clone();
                            Task newTask = new Task(j, budg,task);
                            
                            memory.output(newTask);
                            
                            memory.addNewTask(newTask, "Derived (Cardinality)");
                        }
                    }
                }
            }
            
        }, enabled, Events.TaskDerived.class);
        return true;
    }
    
}
