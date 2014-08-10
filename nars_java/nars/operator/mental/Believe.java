/*
 * Believe.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.operator.mental;

import java.util.ArrayList;
import nars.core.Parameters;
import nars.entity.*;
import nars.inference.BudgetFunctions;
import nars.language.*;
import nars.io.Symbols;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.storage.Memory;

/**
 * Operator that creates a judgment with a given statement
 */
public class Believe extends Operator {

    public Believe() {
        super("^believe");
    }

    /**
     * To create a judgment with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
+    * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        Term content = args[0];
        
        TruthValue truth = new TruthValue(1, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
        Sentence sentence = new Sentence(content, Symbols.JUDGMENT_MARK, truth, new Stamp(memory));
        float quality = BudgetFunctions.truthToQuality(truth);
        BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, quality);
        Task task = new Task(sentence, budget);
        ArrayList<Task> feedback = new ArrayList<>(1);
        feedback.add(task);
        return feedback;
    }
}