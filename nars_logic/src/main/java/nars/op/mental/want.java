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
import nars.Symbols;
import nars.budget.Budget;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SyncOperator;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.util.List;

/**
 * Operator that creates a goal with a given statement
 */
public class want extends SyncOperator implements Mental {


    /**
     * To create a goal with a given statement
     *
     * @param args Arguments, a Statement followed by an optional tense
     * @return Immediate results as Tasks
     */
    @Override
    public List<Task> apply(Task<Operation> t) {
        final Operation operation = t.getTerm();

        Term content = operation.arg(0);

        Truth truth = new DefaultTruth(1, Global.DEFAULT_JUDGMENT_CONFIDENCE);

        Budget budget = new Budget(Global.DEFAULT_GOAL_PRIORITY, Global.DEFAULT_GOAL_DURABILITY, truth);

        final Memory m = nar.memory;
        return Lists.newArrayList(
                operation.newSubTask(t, m, (Compound) content, Symbols.GOAL, truth, m.time(), budget)
        );
    }

//    @Override
//    public boolean isExecutable(Memory mem) {
//        return false;
//    }
}
