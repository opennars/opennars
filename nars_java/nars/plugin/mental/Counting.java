package nars.plugin.mental;

import java.util.Arrays;
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

    public Observer obs;
    
    final static Term CARDINALITY = Term.get("CARDINALITY");
    
    @Override public boolean setEnabled(NAR n, boolean enabled) {
        Memory memory = n.memory;
        
        if(obs==null) {
            obs=new Observer() {

                @Override
                public void event(Class event, Object[] a) {

                    if ((event!=Events.TaskDerive.class && event!=Events.TaskAdd.class))
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
                                    Term.get(cardinality) 
                                };

                                //TODO CARDINATLITY can be a static final instance shared by all
                                Term new_term=Inheritance.make(new Product(product_args), /* --> */ CARDINALITY);
                                if (new_term == null) {
                                    //this usually happens when product_args contains the term CARDINALITY in which case it is an invalid Inheritance statement
                                    return;
                                }
                                
                                TruthValue truth = task.sentence.truth.clone();
                                Stamp stampi = task.sentence.stamp.clone();
                                Sentence j = new Sentence(new_term, Symbols.JUDGMENT_MARK, truth, stampi);
                                BudgetValue budg = task.budget.clone();
                                Task newTask = new Task(j, budg,task);                               

                                memory.addNewTask(newTask, "Derived (Cardinality)");
                            }
                        }
                    }
                }
            };
        }
        
        memory.event.set(obs, enabled, Events.TaskDerive.class);
        return true;
    }
    
}
