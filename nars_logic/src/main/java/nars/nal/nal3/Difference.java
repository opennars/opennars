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
import nars.term.compound.GenericCompound;

/**
 * A compound term whose extension is the difference of the intensions of its term
 */
public interface Difference {


    /**
     * Try to make a new DifferenceExt
     * @return the Term generated from the arguments
     */
    static Term diffInt(Term a, Term b) {
        if ((a instanceof Compound) && (b instanceof Compound)) {
            return SetInt.subtractInt((Compound)a, (Compound)b);
        }

        return GenericCompound.COMPOUND(Op.DIFFERENCE_INT, a, b);
    }
    /**
     * Try to make a new DifferenceExt
     * @return the Term generated from the arguments
     */
    static Term diffExt(Term a, Term b) {
        if ((a instanceof Compound) && (b instanceof Compound)) {
            return SetExt.subtractExt((Compound)a, (Compound)b);
        }

        return GenericCompound.COMPOUND(Op.DIFFERENCE_EXT, a, b);
    }


}


