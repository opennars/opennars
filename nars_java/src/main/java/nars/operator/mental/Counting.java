package nars.operator.mental;

import nars.core.Events;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Plugin;
import nars.event.Reaction;
import nars.io.Symbols;
import nars.logic.entity.*;
import nars.logic.entity.stamp.Stamp;
import nars.logic.nal1.Inheritance;
import nars.logic.nal3.SetExt;
import nars.logic.nal4.Product;

/**
 * Counting and Cardinality
 * TODO extends AbstractPlugin
 */
public class Counting implements Plugin {

    public Reaction obs;
    
    final static Term CARDINALITY = Term.get("CARDINALITY");
    
    @Override public boolean setEnabled(NAR n, boolean enabled) {
        Memory memory = n.memory;
        
        if(obs==null) {
            obs=new Reaction() {

                @Override
                public void event(Class event, Object[] a) {

                    if ((event!=Events.TaskDerive.class && event!=Events.TaskAdd.class))
                        return;

                    Task task = (Task)a[0];
                    if(task.budget.summary() < InternalExperience.MINIMUM_BUDGET_SUMMARY_TO_CREATE) {
                        return;
                    }

                    if(task.sentence.punctuation==Symbols.JUDGMENT) {
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

                                //TODO CARDINALITY can be a static final instance shared by all
                                Term new_term=Inheritance.make(new Product(product_args), /* --> */ CARDINALITY);
                                if (new_term == null) {
                                    //this usually happens when product_args contains the term CARDINALITY in which case it is an invalid Inheritance statement
                                    return;
                                }
                                
                                TruthValue truth = task.sentence.truth.clone();
                                Stamp stampi = task.sentence.stamp.clone();
                                Sentence j = new Sentence(new_term, Symbols.JUDGMENT, truth, stampi);
                                BudgetValue budg = task.budget.clone();
                                Task newTask = new Task(j, budg,task);                               

                                memory.taskAdd(newTask, "Derived (Cardinality)");
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
