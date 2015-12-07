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
import nars.nal.nal2.Similarity;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SyncOperator;
import nars.task.Task;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.util.List;

/**
 * Operator that give a CompoundTerm a new name
 */
public class name extends SyncOperator implements Mental {


    /**
     * To create a judgment with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @return Immediate results as Tasks
     */
    @Override
    public List<Task> apply(Task<Operation> t) {
        Operation operation = t.getTerm();
        Term compound = operation.arg(0);
        Term atomic = operation.arg(1);
        Compound content = Task.taskable( Similarity.make(compound, atomic) );
        if (content == null) return null;

        final Truth truth;
        final Memory memory = nar.memory;

        return Lists.newArrayList( operation.newSubTask(t, memory,
                content, Symbols.JUDGMENT, truth = new DefaultTruth(1, 0.9999f),
                memory.time(),
                new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, truth)) );
    }
}
