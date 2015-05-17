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

import com.google.common.collect.Lists;
import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.Symbols;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.stamp.Stamp;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Compound;

import java.util.ArrayList;

/**
 * Operator that creates a quest with a given statement
 */
public class Evaluate extends Operator implements Mental {

    public Evaluate() {
        super();
    }

    /**
     * To create a quest with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory
     * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Memory memory) {
        Compound content = Sentence.termOrException(operation.arg(0));

        Sentence sentence = new Sentence(content, Symbols.QUEST, null, new Stamp(operation, nar.memory, Tense.Present));
        Budget budget = new Budget(Global.DEFAULT_QUEST_PRIORITY, Global.DEFAULT_QUESTION_DURABILITY, 1);
        
        return Lists.newArrayList( new Task(sentence, budget, operation.getTask()) );

    }
        
}
