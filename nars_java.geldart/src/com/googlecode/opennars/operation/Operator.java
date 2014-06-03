/*
 * Operator.java
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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.operation;

import java.util.*;
import java.io.*;

import com.googlecode.opennars.entity.Task;
import com.googlecode.opennars.language.Term;

/**
 * An individual operator that can be execute by the system.
 * The only file to modify when adding a new operator into NARS
 */
public abstract class Operator extends Term {
    public Operator(String name) {
        super(name);
    }
    
    // required method for every operation
    public abstract Object execute(Task task);

    // register the operators in the memory
    // the only method to modify when adding a new operator into NARS
    // an operator should contain at least two characters after "^""
    public static HashMap<String, Operator> setOperators() {
        HashMap<String, Operator> table = new HashMap<String, Operator>();
        table.put("^go-to", new GoTo("^go-to"));
        table.put("^pick", new Pick("^pick"));
        table.put("^open", new Open("^open"));
        return table;
    }
}

