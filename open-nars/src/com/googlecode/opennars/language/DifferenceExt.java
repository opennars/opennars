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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.language;

import java.util.*;

import com.googlecode.opennars.entity.TermLink;
import com.googlecode.opennars.main.Memory;
import com.googlecode.opennars.parser.Symbols;

/**
 * A compound term whose extension is the difference of the extensions of its components
 */
public class DifferenceExt extends CompoundTerm {

    /**
     * constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private DifferenceExt(String n, ArrayList<Term> arg) {
        super(n, arg);
    }
    
    /**
     * constructor with full values, called by clone
     * @param cs component list
     * @param open open variable list
     * @param closed closed variable list
     * @param i syntactic complexity of the compound
     * @param n The name of the term
     */
    private DifferenceExt(String n, ArrayList<Term> cs, ArrayList<Variable> open, ArrayList<Variable> closed, short i) {
        super(n, cs, open, closed, i);
    }
    
    /**
     * override the cloning methed in Object
     * @return A new object, to be casted into a DifferenceExt
     */
    public Object clone() {
        return new DifferenceExt(name, (ArrayList<Term>) cloneList(components),
                (ArrayList<Variable>) cloneList(openVariables), (ArrayList<Variable>) cloneList(closedVariables), complexity);
    }
    
    /**
     * Try to make a new DifferenceExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param argList The list of components
     */     
    public static Term make(ArrayList<Term> argList, Memory memory) {
        if (argList.size() == 1)    // special case from CompoundTerm.reduceComponent
            return argList.get(0);
        if (argList.size() != 2)
            return null;
        String name = makeCompoundName(Symbols.DIFFERENCE_EXT_OPERATOR, argList);
        Term t = memory.nameToListedTerm(name);
        return (t != null) ? t : new DifferenceExt(name, argList);
    }

    /**
     * Try to make a new compound from two components. Called by the inference rules.
     * @param t1 The first compoment
     * @param t2 The second compoment
     * @return A compound generated or a term it reduced to
     */
    public static Term make(Term t1, Term t2, Memory memory) {
        if (t1.equals(t2))
            return null;
        if ((t1 instanceof SetExt) && (t2 instanceof SetExt)) {
            TreeSet set = new TreeSet(((CompoundTerm) t1).cloneComponents());
            set.removeAll(((CompoundTerm) t2).cloneComponents());           // set difference
            return SetExt.make(set, memory);
        }
        ArrayList<Term> list = argumentsToList(t1, t2);
        return make(list, memory);
    }
    
    /**
     * get the operator of the term.
     * @return the operator of the term
     */
    public String operator() {
        return Symbols.DIFFERENCE_EXT_OPERATOR;
    }
}
