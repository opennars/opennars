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
package nars.operator.misc;

import nars.language.Term;
import nars.operator.SynchronousFunctionOperator;
import nars.storage.Memory;

/**
 * Count the number of elements in a set
 */
public class Add extends SynchronousFunctionOperator {

    public Add() {
        super("^add");
    }

    @Override
    protected Term function(Memory memory, Term[] x) {
        if (x.length!= 2) {
            throw new RuntimeException("Requires 2 arguments");
        }
        
        int n1, n2;
        
        try {
            n1 = Integer.parseInt(String.valueOf(x[0].name()));
        } catch (NumberFormatException e) {
            throw new RuntimeException("1st parameter not an integer");
        }
        
        try {
            n2 = Integer.parseInt(String.valueOf(x[1].name()));
        } catch (NumberFormatException e) {
            throw new RuntimeException("2nd parameter not an integer");
        }
        
        return new Term(String.valueOf(n1 + n2));            
    }

    @Override
    protected Term getRange() {
        return Term.get("added");
    }
    
}
