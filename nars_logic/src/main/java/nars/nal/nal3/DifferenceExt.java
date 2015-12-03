/*
 * DifferenceExt.java
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


/**
 * A compound term whose extension is the difference of the extensions of its term
 */
public class DifferenceExt extends Difference {

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    private DifferenceExt(Term a, Term b) {
        super();

        init(a, b);
    }


    /**
     * Clone an object
     * @return A new object, to be casted into a DifferenceExt
     */
    @Override
    public DifferenceExt clone() {
        Term[] t = terms.term;
        return new DifferenceExt(t[0], t[1]);
    }

    @Override public Term clone(Term[] replaced) {
        return make(replaced);
    }

    
    /**
     * Try to make a new DifferenceExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param arg The list of term
     */
    public static Term make(Term[] arg) {
        if (arg.length!=2) return null;

        Term A = arg[0];
        Term B = arg[1];

        return DifferenceExt.make(A, B);
    }


    /**
     * Try to make a new compound from two term. Called by the logic rules.
     * @param t1 The first component
     * @param t2 The second component
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term A, final Term B) {

        if (A.equals(B)) return null;

        if ((A instanceof SetExt) && (B instanceof SetExt)) {
            return SetExt.subtract((SetExt)A, (SetExt)B);
        }

        return new DifferenceExt(A, B);
    }

    /**
     * Get the operate of the term.
     * @return the operate of the term
     */
    @Override
    public final Op op() {
        return Op.DIFFERENCE_EXT;
    }
}
