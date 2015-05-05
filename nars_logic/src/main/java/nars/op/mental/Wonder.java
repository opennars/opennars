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
import nars.budget.Budget;
import nars.Symbols;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.stamp.Stamp;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Term;

import java.util.ArrayList;

/**
 * Operator that creates a question with a given statement
 */
public class Wonder extends Operator implements Mental {

    public Wonder() {
        super("^wonder");
    }

    /**
     * To create a question with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args) {
        Term content = args[0];
        
        
        Sentence sentence = new Sentence(content, Symbols.QUESTION, null, new Stamp(nar.memory, Stamp.ETERNAL));
        Budget budget = new Budget(Global.DEFAULT_QUESTION_PRIORITY, Global.DEFAULT_QUESTION_DURABILITY, 1);
        return Lists.newArrayList( new Task(sentence, budget, operation.getTask()) );
    }
        
}
