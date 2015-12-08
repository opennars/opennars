/*
 * Negation.java
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
package nars.nal.nal1;

import nars.Op;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;

/**
 * A negation of a statement.
 */
public interface Negation {

    static Term negation(final Term t) {
        if (t.op() == Op.NEGATION) {
            // (--,(--,P)) = P
            return ((Compound) t).term(0);
        }
        return new GenericCompound(Op.NEGATION, t);
    }

}
