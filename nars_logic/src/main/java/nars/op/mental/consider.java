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

import nars.budget.Budget;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.nal.Deriver;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.nal8.operator.SynchOperator;
import nars.process.ConceptTaskLinkProcess;
import nars.task.Task;
import nars.term.Term;

import java.util.ArrayList;

/**
 * Operator that activates a concept
 */
public class consider extends SynchOperator implements Mental {

    public static Operator consider = Operator.the("consider");

    public static Budget budgetMentalConcept(final Operation o) {
        return o.getTask().getBudget();
    }

    final Deriver deriver = null; //new SimpleDeriver();

    /**
     * To activate a concept as if a question has been asked about it
     *
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory
     * @return Immediate results as Tasks
     */
    @Override
    public ArrayList<Task> apply(Operation operation) {
        Term term = operation.arg(0);
        
        Concept concept = nar.conceptualize(term, budgetMentalConcept(operation));

        TaskLink taskLink = concept.getTaskLinks().peekNext();
        if (taskLink!=null) {
            new ConceptTaskLinkProcess(nar, concept, taskLink).input(nar, deriver);
        }
        
        return null;
    }

}
