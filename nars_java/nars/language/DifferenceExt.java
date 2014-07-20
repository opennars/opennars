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
package nars.language;

import java.util.*;

import nars.io.Symbols;
import nars.io.Symbols.Operator;
import nars.storage.Memory;

/**
 * A compound term whose extension is the difference of the extensions of its components
 */
public class DifferenceExt extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private DifferenceExt(ArrayList<Term> arg) {
        super(arg);
    }

    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param i Syntactic complexity of the compound
     */
    private DifferenceExt(String n, ArrayList<Term> cs, boolean con, short i) {
        super(n, cs, con, i);
    }

    /**
     * Clone an object
     * @return A new object, to be casted into a DifferenceExt
     */
    public Object clone() {
        return new DifferenceExt(name, (ArrayList<Term>) cloneList(components), isConstant(), complexity);
    }

    /**
     * Try to make a new DifferenceExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param argList The list of components
     * @param memory Reference to the memory
     */
    public static Term make(ArrayList<Term> argList, Memory memory) {
        if (argList.size() == 1) { // special case from CompoundTerm.reduceComponent
            return argList.get(0);
        }
        if (argList.size() != 2) {
            return null;
        }
        if ((argList.get(0) instanceof SetExt) && (argList.get(1) instanceof SetExt)) {
            TreeSet<Term> set = new TreeSet<Term>(((CompoundTerm) argList.get(0)).cloneComponents());
            set.removeAll(((CompoundTerm) argList.get(1)).cloneComponents());           // set difference
            return SetExt.make(set, memory);
        }
        
        String name = makeCompoundName(Operator.DIFFERENCE_EXT, argList);
        Term t = memory.nameToTerm(name);
        return (t != null) ? t : new DifferenceExt(argList);
    }

    /**
     * Try to make a new compound from two components. Called by the inference rules.
     * @param t1 The first component
     * @param t2 The second component
     * @param memory Reference to the memory
     * @return A compound generated or a term it reduced to
     */
    public static Term make(Term t1, Term t2, Memory memory) {
        if (t1.equals(t2)) {
            return null;
        }
        ArrayList<Term> list = argumentsToList(t1, t2);
        return make(list, memory);
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    public Operator operator() {
        return Operator.DIFFERENCE_EXT;
    }
}
