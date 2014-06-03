/*
 * ConjunctionSequence.java
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

import com.googlecode.opennars.inference.*;
import com.googlecode.opennars.main.Memory;
import com.googlecode.opennars.parser.*;

/**
 * A sequential conjunction of Statements.
 */
public class ConjunctionSequence extends Conjunction {
    
    /**
     * constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private ConjunctionSequence(String n, ArrayList<Term> arg) {
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
    private ConjunctionSequence(String n, ArrayList<Term> cs, ArrayList<Variable> open, ArrayList<Variable> closed, short i) {
        super(n, cs, open, closed, i);
    }
    
    /**
     * override the cloning methed in Object
     * @return A new object, to be casted into a Conjunction
     */
    public Object clone() {
        return new ConjunctionSequence(name, (ArrayList<Term>) cloneList(components),
                (ArrayList<Variable>) cloneList(openVariables), (ArrayList<Variable>) cloneList(closedVariables), complexity);
    }
    
    /**
     * Try to make a new compound from a list of components. Called by StringParser.
     * @param argument the list of arguments
     * @return the Term generated from the arguments
     */
    public static Term make(ArrayList<Term> argument, Memory memory) {
        if (argument.size() == 1)
            return argument.get(0);
        String name = makeCompoundName(Symbols.PRODUCT_OPERATOR, argument);
        Term t = memory.nameToListedTerm(name);
        return (t != null) ? t : new ConjunctionSequence(name, argument);
    }
    
    /**
     * get the operator of the term.
     * @return the operator of the term
     */
    public String operator() {
        return Symbols.SEQUENCE_OPERATOR;
    }
    
    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */
    public boolean isCommutative() {
        return false;
    }

    public CompoundTerm.TemporalOrder getTemporalOrder() {
        return CompoundTerm.TemporalOrder.AFTER;
    }
}
