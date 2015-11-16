/*
 * Instance.java
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

package nars.nal.nal2;

import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.term.Term;

/**
 * A Statement about an Instance relation, which is used only in Narsese for I/O, 
 * and translated into Inheritance for internal use.
 */
public abstract class Instance /*extends Statement*/ {
    
    /**
     * Try to make a new compound from two components. Called by the logic rules.
     * <p>
     *  A {-- B becomes {A} --> B
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or null
     */
    public static
        <P extends Term, S extends Term> Inheritance<SetExt<S>, P> make(final S subject, final P predicate) {
            return Inheritance.make(SetExt.make(subject), predicate);
    }
}
