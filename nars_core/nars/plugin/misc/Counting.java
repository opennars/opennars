package nars.plugin.misc;

import nars.util.EventEmitter.EventObserver;
import nars.util.Events;
import nars.storage.Memory;
import nars.NAR;
import nars.util.Plugin;
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
import nars.plugin.mental.InternalExperience;

/**
 * Counting and Cardinality
 */
public class Counting implements Plugin {

    public EventObserver obs;
    
    final static Term CARDINALITY = Term.get("CARDINALITY");
    
    @Override public boolean setEnabled(NAR n, boolean enabled) {
        Memory memory = n.memory;
        
        if(obs==null) {
            obs=new EventObserver() {

                @Override
                public void event(Class event, Object[] a) {

                    if ((event!=Events.TaskDerive.class && event!=Events.TaskAdd.class))
                        return;

                    Task task = (Task)a[0];
                    if(task.budget.summary() < InternalExperience.MINIMUM_BUDGET_SUMMARY_TO_CREATE) {
                        return;
                    }

                    if(task.sentence.punctuation==Symbols.JUDGMENT_MARK) { 
                        //lets say we have <{...} --> M>.
                        if(task.sentence.term instanceof Inheritance) {

                            Inheritance inh=(Inheritance) task.sentence.term;

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
                                Sentence j = new Sentence(
                                    new_term,
                                    Symbols.JUDGMENT_MARK,
                                    truth,
                                    stampi);
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
