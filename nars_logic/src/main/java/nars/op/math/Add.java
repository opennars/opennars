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
package nars.op.math;

import nars.nal.nal8.TermFunction;
import nars.nal.term.Atom;
import nars.nal.term.Term;
import nars.op.mental.Mental;

/**
 * Count the number of elements in a set
 */
public class Add extends TermFunction implements Mental {

    public Add() {
        super("^add");
    }

    @Override
    public Term function(Term[] x) {
        if (x.length < 2) {
            throw new RuntimeException("Requires 2 arguments");
        }
        
        int n1, n2;
        
        try {
            n1 = Integer.parseInt(String.valueOf(x[0].name()));
        } catch (NumberFormatException e) {
            throw new RuntimeException("1st parameter not an integer: " + x[0]);
        }
        
        try {
            n2 = Integer.parseInt(String.valueOf(x[1].name()));
        } catch (NumberFormatException e) {
            throw new RuntimeException("2nd parameter not an integer: " + x[1]);
        }
        
        return Atom.get(String.valueOf(n1 + n2));
    }

}
