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
package org.opennars.operator.mental;

import org.opennars.entity.BudgetValue;
import org.opennars.entity.Concept;
import org.opennars.entity.Task;
import org.opennars.inference.BudgetFunctions;
import org.opennars.inference.BudgetFunctions.Activating;
import org.opennars.interfaces.Timable;
import org.opennars.language.Term;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.storage.Memory;

import java.util.List;

/**
 * Operator that activates a concept
 */
public class Remind extends Operator {

    public Remind() {
        super("^remind");
    }
    
    public void activate(final Memory memory, final Concept c, final BudgetValue b, final Activating mode) {
        memory.concepts.take(c.name());
        BudgetFunctions.activate(c.budget, b, mode);
        memory.concepts.putBack(c, memory.cycles(memory.narParameters.CONCEPT_FORGET_DURATIONS), memory);
    }

    /**
     * To activate a concept as if a question has been asked about it
     *
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    @Override    
    protected List<Task> execute(final Operation operation, final Term[] args, final Memory memory, final Timable time) {
        final Term term = args[1];
        final Concept concept = memory.conceptualize(Consider.budgetMentalConcept(operation), term);
        final BudgetValue budget = new BudgetValue(memory.narParameters.DEFAULT_QUESTION_PRIORITY, memory.narParameters.DEFAULT_QUESTION_DURABILITY, 1, memory.narParameters);
        activate(memory, concept, budget, Activating.TaskLink);
        return null;
    }

}
