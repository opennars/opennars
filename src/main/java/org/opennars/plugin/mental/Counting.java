/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.plugin.mental;

import org.opennars.entity.*;
import org.opennars.io.Symbols;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events;
import org.opennars.language.Inheritance;
import org.opennars.language.Product;
import org.opennars.language.SetExt;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.plugin.Plugin;
import org.opennars.storage.Memory;

/**
 * Counting and Cardinality
 */
public class Counting implements Plugin {

    public EventObserver obs;
    
    final static Term CARDINALITY = Term.get("CARDINALITY");
    
    @Override public boolean setEnabled(final Nar n, final boolean enabled) {
        final Memory memory = n.memory;
        
        if(obs==null) {
            obs= (event, a) -> {

                if ((event!=Events.TaskDerive.class && event!=Events.TaskAdd.class))
                    return;

                final Task task = (Task)a[0];
                if(task.getPriority() < InternalExperience.MINIMUM_PRIORITY_TO_CREATE_WANT_BELIEVE_ETC) {
                    return;
                }

                if(task.sentence.punctuation==Symbols.JUDGMENT_MARK) {
                    //lets say we have <{...} --> M>.
                    if(task.sentence.term instanceof Inheritance) {

                        final Inheritance inh=(Inheritance) task.sentence.term;

                        if(inh.getSubject() instanceof SetExt) {

                            final SetExt set_term=(SetExt) inh.getSubject();

                            //this gets the cardinality of M
                            final int cardinality=set_term.size();

                            //now create term <(*,M,cardinality) --> CARDINALITY>.
                            final Term[] product_args = new Term[] {
                                inh.getPredicate(),
                                Term.get(cardinality)
                            };

                            //TODO CARDINATLITY can be a static final instance shared by all
                            final Term new_term=Inheritance.make(new Product(product_args), /* --> */ CARDINALITY);
                            if (new_term == null) {
                                //this usually happens when product_args contains the term CARDINALITY in which case it is an invalid Inheritance statement
                                return;
                            }

                            final TruthValue truth = task.sentence.truth.clone();
                            final Stamp stampi = task.sentence.stamp.clone();
                            final Sentence j = new Sentence(
                                new_term,
                                Symbols.JUDGMENT_MARK,
                                truth,
                                stampi);
                            final BudgetValue budg = task.budget.clone();
                            final Task newTask = new Task(j, budg, true);

                            memory.addNewTask(newTask, "Derived (Cardinality)");
                        }
                    }
                }
            };
        }
        
        memory.event.set(obs, enabled, Events.TaskDerive.class);
        return true;
    }
    
}
