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

package nars.operate.mental;

import nars.Memory;
import nars.budget.Budget;
import nars.nal.Item;
import nars.nal.concept.Concept;
import nars.nal.Task;
import nars.nal.tlink.TaskLink;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.ConceptProcess;
import nars.nal.term.Term;

import java.util.ArrayList;

/**
 * Operator that activates a concept
 */
public class Consider extends Operator implements Mental {

    public static Budget budgetMentalConcept(final Operation o) {
        return o.getTask().clone();
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
        Term term = args[0];
        
        Concept concept = memory.conceptualize(Consider.budgetMentalConcept(operation), term);

        TaskLink taskLink = concept.taskLinks.peekNext();
        if (taskLink!=null) {
            new ConceptProcess(concept, taskLink) {

                @Override
                public void beforeFinish() {
                }

            }.run();
        }
        
        return null;
    }

}
