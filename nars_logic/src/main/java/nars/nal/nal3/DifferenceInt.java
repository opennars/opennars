/*
 * DifferenceInt.java
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
package nars.nal.nal3;

import nars.Op;
import nars.term.Term;
import nars.term.compound.Compound;

/**
 * A compound term whose extension is the difference of the intensions of its term
 */
public class DifferenceInt extends Difference {

    /**
     * Constructor with partial values, called by make
     */
    private DifferenceInt(final Term a, final Term b) {
        super(a, b);
    }


    /**
     * Clone an object
     * @return A new object, to be casted into a DifferenceInt
     */
    @Override
    public final DifferenceInt clone() {
        Term[] t = terms.term;
        return new DifferenceInt(t[0], t[1]);
    }
    
    @Override public final Term clone(Term[] replaced) {
        return make(replaced);
    }

    /**
     * Try to make a new DifferenceExt
     * @return the Term generated from the arguments
     */
    public static Term make(Term a, Term b) {
        if ((a instanceof Compound) && (b instanceof Compound)) {
            return SetInt.subtractInt((Compound)a, (Compound)b);
        }

        return new DifferenceInt(a, b);
    }

    /**
     * Try to make a new compound from two term. Called by the logic rules.
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term... arg) {
        if (arg.length != 2) return null;

        Term a = arg[0];
        Term b = arg[1];
        return DifferenceInt.make(a,b);
    }

    /**
     * Get the operate of the term.
     * @return the operate of the term
     */
    @Override
    public final Op op() {
        return Op.DIFFERENCE_INT;
    }
}


