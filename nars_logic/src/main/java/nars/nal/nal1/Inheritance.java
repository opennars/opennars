/*
 * Inheritance.java
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
import nars.term.Statement;
import nars.term.Term;
import nars.term.compound.GenericCompound;

/**
 * A Statement about an Inheritance relation.
 */
public interface Inheritance {


    /** alternate version of Inheritance.make that allows equivalent subject and predicate
     * to be reduced to the common term.      */
    static Term inheritance(final Term subject, final Term predicate) {
        if (subject.equals(predicate))
            return subject;

        if (Statement.invalidStatement(subject, predicate)) {
            return null;
        }

//        if ((predicate instanceof Operator) && if (subject instanceof Product))
//            return new GenericCompound(Op.INHERITANCE, (Operator)predicate, (Product)subject);
//        else
            return new GenericCompound(Op.INHERITANCE, subject, predicate);
         
    }

}

