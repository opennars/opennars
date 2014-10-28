/*
 * SetExt.java
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
package nars.language;

import java.util.Arrays;
import java.util.Collection;
import nars.io.Symbols.NativeOperator;
import static nars.io.Symbols.NativeOperator.SET_EXT_CLOSER;
import static nars.io.Symbols.NativeOperator.SET_EXT_OPENER;

/**
 * An extensionally defined set, which contains one or more instances.
 */
public class SetExt extends SetTensional {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private SetExt(final Term[] arg) {
        super(arg);
    }

    
    /**
     * Clone a SetExt
     * @return A new object, to be casted into a SetExt
     */
    @Override
    public SetExt clone() {
        return new SetExt(term);
    }


    /**
     * Try to make a new compound from a set of term. Called by the public make methods.
     * @param t a set of Term as compoments
     * @param memory Reference to the memeory
     * @return the Term generated from the arguments
     */
    public static Term make(final Collection<Term> t) {
        if (t.isEmpty())
            return null;
        
        Term[] argument = t.toArray(new Term[t.size()]);
        Arrays.sort(argument);
        return make(argument);
    }

    public static Term make(final Term... t) {        
        if (t.length == 0) return null;
        
        return new SetExt(t);
    }
    
    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.SET_EXT_OPENER;
    }


    /**
     * Make a String representation of the set, override the default.
     * @return true for communitative
     */
    @Override
    public CharSequence makeName() {
        return makeSetName(SET_EXT_OPENER.ch, term, SET_EXT_CLOSER.ch);
    }
}

