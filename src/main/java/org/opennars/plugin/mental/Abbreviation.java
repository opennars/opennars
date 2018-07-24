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

import com.google.common.collect.Lists;
import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events.TaskDerive;
import org.opennars.language.Similarity;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.main.Nar.PortableInteger;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.plugin.Plugin;
import org.opennars.storage.Memory;

import java.util.List;

import static org.opennars.language.CompoundTerm.termArray;

/**
 * 1-step abbreviation, which calls ^abbreviate directly and not through an added Task.
 * Experimental alternative to Abbreviation plugin.
 */
public class Abbreviation implements Plugin {

    private final double abbreviationProbability = 0.0001f;
    
    /**
    * Operator that give a CompoundTerm an atomic name
    */
    public static class Abbreviate extends Operator {

        public Abbreviate() {
            super("^abbreviate");
        }

        private static final PortableInteger currentTermSerial = new PortableInteger(1);

        public Term newSerialTerm(final char prefix) {
            return new Term(prefix + String.valueOf(currentTermSerial.incrementAndGet()));
        }


        /**
         * To create a judgment with a given statement
         * @param args Arguments, a Statement followed by an optional tense
         * @param memory The memory in which the operation is executed
         * @return Immediate results as Tasks
         */
        @Override
        protected List<Task> execute(final Operation operation, final Term[] args, final Memory memory, final Timable time) {
            
            final Term compound = args[0];
            
            final Term atomic = newSerialTerm(Symbols.TERM_PREFIX);
                        
            final Sentence sentence = new Sentence(
                    Similarity.make(compound, atomic), 
                    Symbols.JUDGMENT_MARK, 
                    new TruthValue(1, memory.narParameters.DEFAULT_JUDGMENT_CONFIDENCE, memory.narParameters),  // a naming convension
                    new Stamp(time, memory));
            
            final float quality = BudgetFunctions.truthToQuality(sentence.truth);
            
            final BudgetValue budget = new BudgetValue(
                    memory.narParameters.DEFAULT_JUDGMENT_PRIORITY, 
                    memory.narParameters.DEFAULT_JUDGMENT_DURABILITY, 
                    quality, memory.narParameters);

            final Task newTask = new Task(sentence, budget, Task.EnumType.INPUT);
            return Lists.newArrayList(newTask);

        }

    }
    
    public int abbreviationComplexityMin = 20;
    public double abbreviationQualityMin = 0.95f;
    public EventObserver obs;
    
    //TODO different parameters for priorities and budgets of both the abbreviation process and the resulting abbreviation judgment
    //public PortableDouble priorityFactor = new PortableDouble(1.0);
    
    public boolean canAbbreviate(final Task task) {
        return !(task.sentence.term instanceof Operation) && 
                (task.sentence.term.getComplexity() > abbreviationComplexityMin) &&
                (task.budget.getQuality() > abbreviationQualityMin);
    }
    
    @Override
    public boolean setEnabled(final Nar n, final boolean enabled) {
        final Memory memory = n.memory;
        
        Operator _abbreviate = memory.getOperator("^abbreviate");
        if (_abbreviate == null) {
            _abbreviate = memory.addOperator(new Abbreviate());
        }
        final Operator abbreviate = _abbreviate;
        
        if(obs==null) {
            obs= (event, a) -> {
                if (event != TaskDerive.class)
                    return;

                if ((abbreviationProbability < 1.0) && (Memory.randomNumber.nextDouble() > abbreviationProbability))
                    return;

                final Task task = (Task)a[0];

                //is it complex and also important? then give it a name:
                if (canAbbreviate(task)) {

                    final Operation operation = Operation.make(
                            abbreviate, termArray(task.sentence.term ),
                            false);

                    operation.setTask(task);

                    abbreviate.call(operation, memory, n);
                }

            };
        }
        
        memory.event.set(obs, enabled, TaskDerive.class);
        
        return true;
    }
    
}
