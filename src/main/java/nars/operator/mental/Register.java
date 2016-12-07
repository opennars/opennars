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

import nars.entity.Task;
import nars.language.Term;
import nars.operator.NullOperator;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.storage.Memory;

import java.util.ArrayList;

/**
 * Register a new operator when the system is running
 */
public class Register extends Operator {

    public Register() {
        super("^register");
    }

    /**
     * To register a new operator
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        Operator op=new NullOperator(args[0].toString());
        memory.addOperator(op);  // add error checking
        return null;
    }
    
}
