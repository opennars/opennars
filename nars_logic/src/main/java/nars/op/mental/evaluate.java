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
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SynchOperator;
import nars.task.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Operator that creates a quest with a given statement
 */
public class evaluate extends SynchOperator implements Mental {

    /**
     * To create a quest with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory
     * @return Immediate results as Tasks
     */
    @Override
    public List<Task> apply(Operation operation) {
        throw new RuntimeException("TODO convert this to new API");

//        Compound content = Sentence.termOrException(operation.arg(0));
//
//        Sentence sentence = new Sentence(content, Symbols.QUEST, null, new Stamper(operation, nar.memory, Tense.Present));
//        Budget budget = new Budget(Global.DEFAULT_QUEST_PRIORITY, Global.DEFAULT_QUESTION_DURABILITY, 1);
//
//        return Lists.newArrayList( new Task(sentence, budget, operation.getTask()) );

    }
        
}
