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
import nars.inference.BudgetFunctions;
import nars.io.Symbols;
import nars.language.*;
import nars.operator.Operator;
import nars.storage.Memory;

import java.util.ArrayList;

/**
 * Feeling common operations
 */
public abstract class Feel extends Operator {
    private final Term feelingTerm;

    public Feel(String name) {
        super(name);
        
        // remove the "^feel" prefix from name
        this.feelingTerm = Term.get(((String)name()).substring(5));
    }

    final static Term self = Term.get(Symbols.SELF);
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
        Sentence sentence = new Sentence(content, Symbols.JUDGMENT_MARK, truth, stamp);
        float quality = BudgetFunctions.truthToQuality(truth);
        BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, quality);
        
        return Lists.newArrayList( new Task(sentence, budget) );        

    }
}
