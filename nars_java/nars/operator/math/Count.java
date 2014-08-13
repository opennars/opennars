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

package nars.operators.math;

import java.util.ArrayList;
import nars.entity.*;
import nars.language.*;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.storage.Memory;

/**
 * Count the number of elements in a set
 */
public class Count extends Operator {

    public Count() {
        super("^count");
    }

    /**
     * To count the number of elements of a set
     * @param args Arguments, a set and a variable
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        if (args.length!=2) {
            //TODO report error
            return null;
        }
                
        Term content = args[0];
        if (!(content instanceof SetExt) && !(content instanceof SetInt)) {
            //TODO report error
            return null;
        }
        if (!(args[1] instanceof Variable)){
            //TODO report error
            return null;
        }
        int n = ((CompoundTerm) content).size();
        Term numberTerm = new Term("" + n);
        args[1] = numberTerm;
        return null;
    }
    
    
}
