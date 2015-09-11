/*
 * Copyright (C) 2014 peiwang
 *
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
package nars.op.mental;

import nars.Global;
import nars.NAR;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.nal8.operator.SynchOperator;
import nars.task.Task;
import nars.term.Term;

import java.util.List;

/**
 * Operator that activates a concept
 */
public class remind extends SynchOperator implements Mental {

    public static Operator remind = Operator.the("remind");

    /**
     * To activate a concept as if a question has been asked about it
     *
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory
     * @return Immediate results as Tasks
     */
    @Override
    public List<Task> apply(Operation operation) {
        Term term = operation.arg(0);

        remind(term, nar);

        return null;
    }

    public static Concept remind(Term term, NAR n) {
        //Concept concept = nar.memory.conceptualize(consider.budgetMentalConcept(operation), term);
        Budget budget = new Budget(Global.DEFAULT_QUESTION_PRIORITY, Global.DEFAULT_QUESTION_DURABILITY, 1);
        //nar.memory.concepts.activate(concept, budget, Activating.TaskLink);
        return n.conceptualize(term, budget);
    }

}
