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
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.nal.nal8.operator.SynchOperator;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Term;
import nars.truth.DefaultTruth;

import java.util.ArrayList;

/**
 * Feeling common operations
 */
public abstract class feel extends SynchOperator implements Mental {
    private final Term feelingTerm = Atom.the("feel");


    
    /**
     * To get the current value of an internal sensor
     *
     * @param value The value to be checked, in [0, 1]
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    protected ArrayList<Task> feeling(float value, Memory memory) {

        Term predicate = SetInt.make(feelingTerm);

        final Term self = memory.self();
        final Term selfSubject = SetExt.make(self);
        Inheritance content = Inheritance.make(selfSubject, predicate);

        return Lists.newArrayList(memory.newTask(content)
                        .judgment().truth(new DefaultTruth(value, 0.999f))
                        .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
                        .occurrNow()
        );


    }
}
