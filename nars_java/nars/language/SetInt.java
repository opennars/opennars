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
public class SetInt extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    public SetInt(final Term[] arg) {
        super(arg);
    }

    /**
     * constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param complex Syntactic complexity of the compound
     */
    private SetInt(Term[] cs, int torder, boolean con, boolean hasVar, short complex, int hash) {
        super(cs, torder, con, hasVar, complex, hash);
    }

    @Override
    public boolean validSize(int num) {
        return num >= 1;
    }
    
    
    /**
     * Clone a SetInt
     * @return A new object, to be casted into a SetInt
     */
    @Override
    public SetInt clone() {
        return new SetInt(cloneTerms(), getTemporalOrder(), isConstant(), containsVar(), getComplexity(), hashCode());
    }

 
    public static Term make(Term t) {
        return new SetInt(new Term[] { t });
    }

    public static SetInt make(Collection<Term> argList) {
        return make ((TreeSet)new TreeSet<Term>(argList)); // sort/merge arguments
    }

    /**
     * Try to make a new compound from a set of term. Called by the public make methods.
     * @param set a set of Term as compoments
     * @param memory Reference to the memeory
     * @return the Term generated from the arguments
     */
    public static SetInt make(TreeSet<Term> set) {
        if (set.isEmpty())
            return null;
        return new SetInt( set.toArray(new Term[set.size()]) );
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
     * Check if the compound is communitative.
     * @return true for communitative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }

    /**
     * Make a String representation of the set, override the default.
     * @return true for communitative
     */
    @Override
    public String makeName() {
        return makeSetName(SET_INT_OPENER.ch, term, SET_INT_CLOSER.ch);
    }
}

