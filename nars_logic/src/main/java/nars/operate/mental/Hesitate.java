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

import nars.nal.concept.Concept;
import nars.nal.Task;
import nars.nal.term.Term;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;

import java.util.ArrayList;

/**
 * Operator that activates a concept
 */
public class Hesitate extends Operator implements Mental {

    public Hesitate() {
        super("^hesitate");
    }

    /**
     * To activate a concept as if a question has been asked about it
     *
     * @param args Arguments, a Statement followed by an optional tense
     * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args) {
        Term term = args[0];
        Concept concept = nar.memory.conceptualize(Consider.budgetMentalConcept(operation), term);
        concept.discountConfidence(false);
        return null;
    }
    
}
