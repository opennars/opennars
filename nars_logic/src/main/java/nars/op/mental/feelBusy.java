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

import nars.Memory;
import nars.nal.Task;
import nars.nal.nal8.Operation;

import java.util.ArrayList;

/**
 * Feeling busy value
 */
public class feelBusy extends feel implements Mental {


    /**
     * To get the current value of an internal sensor
     * @param args Arguments, a set and a variable
     * @param memory
     * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Memory memory) {
        return feeling(nar.memory.emotion.busy(), nar.memory);
    }    
}
