/*
 * SetInt.java
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

import java.util.Collection;
import java.util.TreeSet;
import nars.io.Symbols.NativeOperator;
import static nars.io.Symbols.NativeOperator.SET_INT_CLOSER;
import static nars.io.Symbols.NativeOperator.SET_INT_OPENER;

/**
 * An intensionally defined set, which contains one or more instances defining the Term.
 */
public class SetInt extends SetTensional {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private SetInt(final Term[] arg) {
        super(arg);
    }
 



    /**
     * Clone a SetInt
     * @return A new object, to be casted into a SetInt
     */
    @Override
    public SetInt clone() {
        return new SetInt(term);
    }

    @Override public SetInt clone(Term[] replaced) {
        return make(Term.toSortedSet(replaced));
    }

    
    
    /**
     * Try to make a new set from one component. Called by the inference rules.
     * @param t The compoment
     * @param memory Reference to the memeory
     * @return A compound generated or a term it reduced to
     */
    public static SetInt make(final TreeSet<Term> t) {        
        if (t.isEmpty()) return null;
        Term[] x = t.toArray(new Term[t.size()]);
        return new SetInt(x);
    }
    
    public static SetInt make(Term t) {
        return new SetInt(new Term[] { t });
    }


    /**
     * Try to make a new compound from a set of term. Called by the public make methods.
     * @param t a set of Term as compoments
     * @param memory Reference to the memeory
     * @return the Term generated from the arguments
     */
    public static Term make(Collection<Term> t) {
        int s = t.size();
        if (s == 0) return null;         
        else if (s == 1) return make(t.iterator().next());
        else return make(new TreeSet<>(t));
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.SET_INT_OPENER;
    }


    /**
     * Make a String representation of the set, override the default.
     * @return true for communitative
     */
    @Override
    public CharSequence makeName() {
        return makeSetName(SET_INT_OPENER.ch, term, SET_INT_CLOSER.ch);
    }
    
}

