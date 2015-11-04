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
import nars.Symbols;
import nars.budget.Budget;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SyncOperator;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.Stamp;

import java.util.List;

/**
 * Operator that creates a question with a given statement
 */
public class wonder extends SyncOperator implements Mental {

    /**
     * To create a question with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @return Immediate results as Tasks
     */
    @Override
    public List<Task> apply(Task<Operation> t) {
        final Operation operation = t.getTerm();
        Term content = operation.arg(0);


        Budget budget = new Budget(Global.DEFAULT_QUESTION_PRIORITY, Global.DEFAULT_QUESTION_DURABILITY, 1);

        return Lists.newArrayList(
                operation.newSubTask(t, nar.memory, (Compound)content, Symbols.QUESTION, null, Stamp.ETERNAL, budget)
        );
    }
        
}
