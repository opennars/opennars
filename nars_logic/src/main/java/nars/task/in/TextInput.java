/*
 * TextInput.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.task.in;

import nars.NAR;
import nars.Narsese;
import nars.task.Task;
import nars.task.flow.TaskQueue;

import java.util.Collection;

/**
 * Process experience from a string into zero or more input tasks
 */
public class TextInput extends TaskQueue {

	public TextInput(NAR n, String input) {
		process(n, input);
	}

	protected int process(NAR nar, String input) {
        //..

        return Narsese.the().tasks(input,
                (Collection<Task>) this, nar.memory);
    }
}
