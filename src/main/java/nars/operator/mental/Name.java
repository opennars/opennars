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
import nars.config.Parameters;
import nars.entity.*;
import nars.io.Symbols;
import nars.language.Similarity;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.storage.Memory;

import java.util.ArrayList;

/**
 * Operator that give a CompoundTerm a new name
 */
public class Name extends Operator {

    public Name() {
        super("^name");
    }

    /**
     * To create a judgment with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        Term compound = args[0];
        Term atomic = args[1];
        Similarity content = Similarity.make(compound, atomic);
        
        TruthValue truth = new TruthValue(1, 0.9999f);  // a naming convension
        Sentence sentence = new Sentence(content, Symbols.JUDGMENT_MARK, truth, new Stamp(memory));
        
        BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, truth);
        
        return Lists.newArrayList( new Task(sentence, budget) );        
    }
}
