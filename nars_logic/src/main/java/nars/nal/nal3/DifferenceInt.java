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

import nars.Global;
import nars.Op;
import nars.term.Compound;
import nars.term.Term;

import java.util.Set;

/**
 * A compound term whose extension is the difference of the intensions of its term
 */
public class DifferenceInt extends Difference {

    /**
     * Constructor with partial values, called by make
     */
    private DifferenceInt(final Term a, final Term b) {
        super();

        init(a, b);
    }


    /**
     * Clone an object
     * @return A new object, to be casted into a DifferenceInt
     */
    @Override
    public final DifferenceInt clone() {
        return new DifferenceInt(term(0), term(1));
    }
    
    @Override public final Term clone(Term[] replaced) {
        return make(replaced);
    }

    /**
     * Try to make a new DifferenceExt
     * @return the Term generated from the arguments
     * @param arg The list of term
     */
    public static Term make(Term[] arg) {
        ensureValidDifferenceSubterms(arg);

        return make(arg[0], arg[1]);
    }

    /**
     * Try to make a new compound from two term. Called by the logic rules.
     * @param a The first component
     * @param b The second component
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term a, final Term b) {
        if ((a instanceof SetInt) && (b instanceof SetInt)) {
            //TODO maybe a faster way to calculate:
            Set<Term> set = Global.newHashSet(a.volume());
            ((Compound<?>) a).forEach(set::add);
            ((Compound<?>) b).forEach(set::remove);
            return SetInt.make(set);
        }

        return new DifferenceInt(a, b);
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


