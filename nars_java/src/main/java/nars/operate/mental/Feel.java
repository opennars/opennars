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
package nars.operate.mental;

import com.google.common.collect.Lists;
import nars.Memory;
import nars.Global;
import nars.budget.Budget;
import nars.io.Symbols;
import nars.nal.BudgetFunctions;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.TruthValue;
import nars.nal.stamp.Stamp;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operator;
import nars.nal.term.Term;

import java.util.ArrayList;

/**
 * Feeling common operations
 */
public abstract class Feel extends Operator implements Mental {
    private final Term feelingTerm;

    public Feel(String name) {
        super(name);
        
        // remove the "^feel" prefix from name
        this.feelingTerm = get(((String) name()).substring(5));
    }

    final static Term self = get(Symbols.SELF);
    final static Term selfSubject = SetExt.make(self);
    
    /**
     * To get the current value of an internal sensor
     *
     * @param value The value to be checked, in [0, 1]
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    protected ArrayList<Task> feeling(float value, Memory memory) {
        Stamp stamp = new Stamp(memory, Tense.Present);
        TruthValue truth = new TruthValue(value, 0.999f);
                
        Term predicate = new SetInt(feelingTerm);
        
        Term content = Inheritance.make(selfSubject, predicate);
        Sentence sentence = new Sentence(content, Symbols.JUDGMENT, truth, stamp);
        float quality = BudgetFunctions.truthToQuality(truth);
        Budget budget = new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, quality);
        
        return Lists.newArrayList( new Task(sentence, budget) );        

    }
}
