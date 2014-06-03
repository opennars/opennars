/*
 * IntersectionInt.java
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
 * A compound term whose intension is the intersection of the extensions of its components
 */
public class IntersectionInt extends CompoundTerm {
    
    /**
     * constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private IntersectionInt(String n, ArrayList<Term> arg) {
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
    private IntersectionInt(String n, ArrayList<Term> cs, ArrayList<Variable> open, ArrayList<Variable> closed, short i) {
        super(n, cs, open, closed, i);
    }
    
    /**
     * override the cloning methed in Object
     * @return A new object, to be casted into a Conjunction
     */
    public Object clone() {
        return new IntersectionInt(name, (ArrayList<Term>) cloneList(components), 
                (ArrayList<Variable>) cloneList(openVariables), (ArrayList<Variable>) cloneList(closedVariables), complexity);
    }
    
    /**
     * Try to make a new compound from two components. Called by the inference rules.
     * @param term1 The first compoment
     * @param term2 The first compoment
     * @return A compound generated or a term it reduced to
     */    
    public static Term make(Term term1, Term term2, Memory memory) {
        TreeSet set;
        if ((term1 instanceof SetExt) && (term2 instanceof SetExt)) {
            set = new TreeSet(((CompoundTerm) term1).cloneComponents());
            set.addAll(((CompoundTerm) term2).cloneComponents());           // set union
            return SetExt.make(set, memory);
        }
        if ((term1 instanceof SetInt) && (term2 instanceof SetInt)) {
            set = new TreeSet(((CompoundTerm) term1).cloneComponents());
            set.retainAll(((CompoundTerm) term2).cloneComponents());        // set intersection
            return SetInt.make(set, memory);
        }
        if (term1 instanceof IntersectionInt) {
            set = new TreeSet(((CompoundTerm) term1).cloneComponents());
            if (term2 instanceof IntersectionInt)
                set.addAll(((CompoundTerm) term2).cloneComponents());   // (|,(|,P,Q),(|,R,S)) = (|,P,Q,R,S)
            else
                set.add((Term) term2.clone());                          // (|,(|,P,Q),R) = (|,P,Q,R)
        } else if (term2 instanceof IntersectionInt) {
            set = new TreeSet(((CompoundTerm) term2).cloneComponents());
            set.add((Term) term1.clone());                              // (|,R,(|,P,Q)) = (|,P,Q,R)
        } else {
            set = new TreeSet();
            set.add((Term) term1.clone());
            set.add((Term) term2.clone());
        }
        return make(set, memory);
    }
    
    /**
     * Try to make a new IntersectionExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param argList The list of components
     */
    public static Term make(ArrayList<Term> argList, Memory memory) {
        TreeSet<Term> set = new TreeSet<Term>(argList); // sort/merge arguments
        return make(set, memory);
    }
    
    /**
     * Try to make a new compound from a set of components. Called by the public make methods.
     * @param set a set of Term as compoments
     * @return the Term generated from the arguments
     */
    public static Term make(TreeSet<Term> set, Memory memory) {
        if (set.size() == 1)
            return set.first();                         // special case: single component
        ArrayList<Term> argument = new ArrayList<Term>(set);
        String name = makeCompoundName(Symbols.INTERSECTION_INT_OPERATOR, argument);
        Term t = memory.nameToListedTerm(name);
        return (t != null) ? t : new IntersectionInt(name, argument);
    }
    
    /**
     * get the operator of the term.
     * @return the operator of the term
     */
    public String operator() {
        return Symbols.INTERSECTION_INT_OPERATOR;
    }

    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */
    public boolean isCommutative() {
        return true;
    }
}
