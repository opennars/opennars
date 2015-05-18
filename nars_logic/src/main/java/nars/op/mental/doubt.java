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

import nars.Memory;
import nars.nal.Task;
import nars.nal.concept.Concept;
import nars.nal.nal8.Operation;
import nars.nal.nal8.SynchOperator;
import nars.nal.term.Term;

import java.util.ArrayList;

/**
 * Operator that activates a concept
 */
public class doubt extends SynchOperator implements Mental {



    /**
     * To activate a concept as if a question has been asked about it
     *
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory
     * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Memory memory) {
        Term term = operation.arg();
        Concept concept = nar.memory.conceptualize(consider.budgetMentalConcept(operation), term);
        concept.discountConfidence(true);
        return null;
    }

}
