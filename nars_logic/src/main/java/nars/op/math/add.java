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

import nars.nal.nal7.Tense;
import nars.nal.nal8.operator.TermFunction;
import nars.op.mental.Mental;
import nars.term.Term;
import nars.term.compile.TermBuilder;
import nars.term.compound.Compound;

/**
 * Count the number of elements in a set
 */
public class add extends TermFunction<Integer> implements Mental {

    @Override
    public Integer function(Compound o, TermBuilder i) {

        Term[] x = o.terms();

        if (x.length < 2) {
            throw new RuntimeException("Requires 2 arguments");
        }
        
        int n1;

        try {
            n1 = integer(x[0]);
        } catch (NumberFormatException e) {
            throw new RuntimeException("1st parameter not an integer: " + x[0]);
        }

        int n2;
        try {
            n2 = integer(x[1]);
        } catch (NumberFormatException e) {
            throw new RuntimeException("2nd parameter not an integer: " + x[1]);
        }
        
        return n1 + n2;
    }

    @Override
    public Tense getResultTense() {
        return Tense.Eternal;
    }


}
