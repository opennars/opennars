/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
    public volatile float MINIMUM_PRIORITY=0.3f;
    public void setMINIMUM_PRIORITY(double val) {
        this.MINIMUM_PRIORITY = (float) val;
    }
    public double getMINIMUM_PRIORITY() {
        return MINIMUM_PRIORITY;
    }
    
    public Counting(){}
    public Counting(float MINIMUM_PRIORITY) {
        this.MINIMUM_PRIORITY = MINIMUM_PRIORITY;
    }
    
    @Override public boolean setEnabled(final Nar n, final boolean enabled) {
        final Memory memory = n.memory;
        
        if(obs==null) {
            obs= (event, a) -> {

                if ((event!=Events.TaskDerive.class && event!=Events.TaskAdd.class))
                    return;

                final Task task = (Task)a[0];
                if(task.getPriority() < MINIMUM_PRIORITY) {
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

                            final Task newTask = new Task(j, budg, Task.EnumType.INPUT);

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
