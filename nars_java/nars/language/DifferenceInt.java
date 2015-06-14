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
package nars.language;

import java.util.TreeSet;
import nars.io.Symbols.NativeOperator;
import nars.storage.Memory;

/**
 * A compound term whose extension is the difference of the intensions of its term
 */
public class DifferenceInt extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private DifferenceInt(final CharSequence name, final Term[] arg) {
        super(name, arg);
    }

    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param i Syntactic complexity of the compound
     */
    private DifferenceInt(CharSequence n, Term[] cs, boolean con, short i) {
        super(n, cs, con, i);
    }

    /**
     * Clone an object
     * @return A new object, to be casted into a DifferenceInt
     */
    @Override
    public DifferenceInt clone() {
        return new DifferenceInt(name(), cloneTerms(), isConstant(), complexity);
    }

    /**
     * Try to make a new DifferenceExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param argList The list of term
     * @param memory Reference to the memory
     */
    public static Term make(Term[] argList, Memory memory) {
        if (argList.length == 1) { // special case from CompoundTerm.reduceComponent
            return argList[0];
        }
        if (argList.length != 2) {
            return null;
        }
        
        if ((argList[0] instanceof SetInt) && (argList[1] instanceof SetInt)) {
            TreeSet<Term> set = new TreeSet<>(((CompoundTerm) argList[0]).cloneTermsList());
            set.removeAll(((CompoundTerm) argList[1]).cloneTermsList());           // set difference
            return SetInt.make(set, memory);
        }
        
        CharSequence name = makeCompoundName(NativeOperator.DIFFERENCE_INT, argList);
        Term t = memory.conceptTerm(name);
        return (t != null) ? t : new DifferenceInt(name, argList);
    }

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param t1 The first component
     * @param t2 The second component
     * @param memory Reference to the memory
     * @return A compound generated or a term it reduced to
     */
    public static Term make(Term t1, Term t2, Memory memory) {
        if (t1.equals(t2)) {
            return null;
        }

        return make(new Term[] { t1, t2 }, memory);
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.DIFFERENCE_INT;
    }
}


