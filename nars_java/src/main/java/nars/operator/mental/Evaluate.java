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

import com.google.common.collect.Lists;
import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.BudgetValue;
import nars.logic.entity.Sentence;
import nars.logic.entity.Stamp;
import nars.logic.entity.Task;
import nars.io.Symbols;
import nars.logic.entity.Term;
import nars.logic.nal8.Operation;
import nars.logic.nal8.Operator;

import java.util.ArrayList;

/**
 * Operator that creates a quest with a given statement
 */
public class Evaluate extends Operator implements Mental {

    public Evaluate() {
        super("^evaluate");
    }

    /**
     * To create a quest with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        Term content = args[0];
        
        Sentence sentence = new Sentence(content, Symbols.QUEST_MARK, null, new Stamp(memory));
        BudgetValue budget = new BudgetValue(Parameters.DEFAULT_QUEST_PRIORITY, Parameters.DEFAULT_QUESTION_DURABILITY, 1);
        
        return Lists.newArrayList( new Task(sentence, budget) );        

    }
        
}
