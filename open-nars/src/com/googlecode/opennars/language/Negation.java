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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.googlecode.opennars.language;

import java.util.*;

import com.googlecode.opennars.entity.TermLink;
import com.googlecode.opennars.main.Memory;
import com.googlecode.opennars.parser.Symbols;

/**
 * A negation of a Statement.
 */
public class Negation extends CompoundTerm {
    
    /**
     * constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private Negation(String n, ArrayList<Term> arg) {
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
    private Negation(String n, ArrayList<Term> cs, ArrayList<Variable> open, ArrayList<Variable> closed, short i) {
        super(n, cs, open, closed, i);
    }
    
    /**
     * override the cloning methed in Object
     * @return A new object, to be casted into a SetExt
     */
    public Object clone() {
        return new Negation(name, (ArrayList<Term>) cloneList(components),
                (ArrayList<Variable>) cloneList(openVariables), (ArrayList<Variable>) cloneList(closedVariables), complexity);
    }
    
    /**
     * Try to make a Negation of one component. Called by the inference rules.
     * @param t The compoment
     * @return A compound generated or a term it reduced to
     */
    public static Term make(Term t, Memory memory) {
        if (t instanceof Negation)
            return (Term) ((CompoundTerm) t).cloneComponents().get(0);         // (--,(--,P)) = P
        ArrayList<Term> argument = new ArrayList<Term>();
        argument.add(t);
        return make(argument, memory);
    }
    
    /**
     * Try to make a new SetExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param argument The list of components
     */
    public static Term make(ArrayList<Term> argument, Memory memory) {
        if (argument.size() != 1)
            return null;
        String name = makeCompoundName(Symbols.NEGATION_OPERATOR, argument);
        Term t = memory.nameToListedTerm(name);
        return (t != null) ? t : new Negation(name, argument);
    }
    
    /**
     * get the operator of the term.
     * @return the operator of the term
     */
    public String operator() {
        return Symbols.NEGATION_OPERATOR;
    }
}
