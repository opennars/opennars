/*
 * ImplicationAfter.java
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

import com.googlecode.opennars.main.Memory;
import com.googlecode.opennars.parser.Symbols;

/**
 * Temporal Implication relation, predicate after subject.
 */
public class ImplicationAfter extends Implication {
    
    /**
     * constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private ImplicationAfter(String n, ArrayList<Term> arg) {
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
    private ImplicationAfter(String n, ArrayList<Term> cs, ArrayList<Variable> open, ArrayList<Variable> closed, short i) {
        super(n, cs, open, closed, i);
    }
    
    /**
     * override the cloning methed in Object
     * @return A new object, to be casted into a SetExt
     */
    public Object clone() {
        return new ImplicationAfter(name, (ArrayList<Term>) cloneList(components),
                (ArrayList<Variable>) cloneList(openVariables), (ArrayList<Variable>) cloneList(closedVariables), complexity);
    }
     
    /**
     * Try to make a new compound from two components. Called by the inference rules.
     * @param subject The first compoment
     * @param predicate The second compoment
     * @return A compound generated or a term it reduced to
     */
    public static ImplicationAfter make(Term subject, Term predicate, Memory memory) {  // to be extended to check if subject is Conjunction
        if (invalidStatement(subject, predicate))
            return null;
        String name = makeStatementName(subject, Symbols.IMPLICATION_AFTER_RELATION, predicate);
        Term t = memory.nameToListedTerm(name);
        if (t != null)
            return (ImplicationAfter) t;
        if (predicate instanceof ImplicationAfter) {
            Term oldCondition = ((ImplicationAfter) predicate).getSubject();
            Term newCondition = ConjunctionSequence.make(subject, oldCondition, memory);
            return make(newCondition, ((ImplicationAfter) predicate).getPredicate(), memory);
        } else {
            ArrayList<Term> argument = argumentsToList(subject, predicate);
            return new ImplicationAfter(name, argument);
        }
    }
    
    /**
     * get the operator of the term.
     * @return the operator of the term
     */
    public String operator() {
        return Symbols.IMPLICATION_AFTER_RELATION;
    }

    // overwrite default
    public CompoundTerm.TemporalOrder getTemporalOrder() {
        return CompoundTerm.TemporalOrder.AFTER;
    }
}
