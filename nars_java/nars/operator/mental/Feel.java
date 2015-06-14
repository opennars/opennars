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
package nars.operator.mental;

import java.util.ArrayList;
import nars.core.Parameters;
import nars.entity.*;
import nars.inference.BudgetFunctions;
import nars.io.Symbols;
import nars.language.*;
import nars.operator.Operator;
import nars.storage.Memory;

/**
 * Feeling common operations
 */
public abstract class Feel extends Operator {

    public Feel(String name) {
        super(name);
    }

    /**
     * To get the current value of an internal sensor
     *
     * @param value The value to be checked, in [0, 1]
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    protected ArrayList<Task> feeling(float value, Memory memory) {
        Stamp stamp = new Stamp(memory, Tense.Present);
        TruthValue truth = new TruthValue(value, 0.999f);
        Term self = new Term(Symbols.SELF);
        Term subject = SetExt.make(self, memory);
                
        Term predicate = SetInt.make(
                // remove the "^feel" prefix from name
                new Term( ((String)name()).substring(5) ), memory); 
        
        Term content = Inheritance.make(subject, predicate, memory);
        Sentence sentence = new Sentence(content, Symbols.JUDGMENT_MARK, truth, stamp);
        float quality = BudgetFunctions.truthToQuality(truth);
        BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, quality);
        Task task = new Task(sentence, budget);
        ArrayList<Task> feedback = new ArrayList<>(1);
        feedback.add(task);
        return feedback;
    }
}
