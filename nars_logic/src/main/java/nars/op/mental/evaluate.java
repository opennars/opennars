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
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SyncOperator;
import nars.task.Task;
import nars.term.Compound;

import java.util.List;

/**
 * Operator that creates a quest with a given statement
 */
public class evaluate extends SyncOperator implements Mental {

    /**
     * To create a quest with a given statement
     * Arguments, a Statement followed by an optional tense
     * @return Immediate results as Tasks
     */
    @Override
    public List<Task> apply(Task<Operation> op) {


        Compound content = Task.termOrNull(op.getTerm().arg(0));
        if (content == null)
            return null;

        //Sentence sentence = new Sentence(content, Symbols.QUEST, null, new Stamper(op, nar.memory, Tense.Present));

        return Lists.newArrayList( op.getTerm().newSubTask(op,
                nar.memory,
                content, Symbols.QUEST, null,
                nar.time(),
                Global.DEFAULT_QUEST_PRIORITY, Global.DEFAULT_QUESTION_DURABILITY, 1
        ) );


//        return Lists.newArrayList( new DefaultTask<>(content, Symbols.QUEST, null,
//                Global.DEFAULT_QUEST_PRIORITY, Global.DEFAULT_QUESTION_DURABILITY, 1,
//                op.getTask(), null, null
//        ) );

    }
        
}
