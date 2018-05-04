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

import java.util.ArrayList;
import org.opennars.control.DerivationContext;
import org.opennars.storage.Memory;
import org.opennars.control.GeneralInferenceControl;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Concept;
import org.opennars.entity.Task;
import org.opennars.language.Term;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;

/**
 * Operator that activates a concept
 */
public class Consider extends Operator {

    public static BudgetValue budgetMentalConcept(final Operation o) {
        return o.getTask().budget.clone();
    }
    
    public Consider() {
        super("^consider");
    }

    /**
     * To activate a concept as if a question has been asked about it
     *
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        Term term = args[1];
        
        Concept concept = memory.conceptualize(Consider.budgetMentalConcept(operation), term);
        
        DerivationContext cont = new DerivationContext(memory);
        cont.setCurrentConcept(concept);
        GeneralInferenceControl.fireConcept(cont, 1);
        
        return null;
    }

}
