/*
 * Equivalence.java
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
 * A Statement about an Equivalence relation.
 */
public class Equivalence extends Statement {

    private int temporalOrder = CompoundTerm.ORDER_NONE;
    /**
     * Constructor with partial values, called by make
     *
     * @param components The component list of the term
     */
    private Equivalence(ArrayList<Term> components, int order) {
        super(components);
        temporalOrder = order;
    }

    /**
     * Constructor with full values, called by clone
     *
     * @param n The name of the term
     * @param components Component list
     * @param constant Whether the statement contains open variable
     * @param complexity Syntactic complexity of the compound
     */
    private Equivalence(String n, ArrayList<Term> components, boolean constant, short complexity, int order) {
        super(n, components, constant, complexity);
        temporalOrder = order;
    }

    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Object clone() {
        return new Equivalence(name, cloneList(components), isConstant(), complexity, temporalOrder);
    }

    /**
     * Try to make a new compound from two components. Called by the inference
     * rules.
     *
     * @param subject The first component
     * @param predicate The second component
     * @param memory Reference to the memory
     * @return A compound generated or null
     */
    public static Equivalence make(Term subject, Term predicate, Memory memory) {  // to be extended to check if subject is Conjunction
        return make(subject, predicate, CompoundTerm.ORDER_NONE, memory);
    }

    public static Equivalence make(Term subject, Term predicate, int temporalOrder, Memory memory) {  // to be extended to check if subject is Conjunction
        if ((subject instanceof Implication) || (subject instanceof Equivalence)) {
            return null;
        }
        if ((predicate instanceof Implication) || (predicate instanceof Equivalence)) {
            return null;
        }
        if (invalidStatement(subject, predicate)) {
            return null;
        }
        if ((subject.compareTo(predicate) > 0) && (temporalOrder != CompoundTerm.ORDER_FORWARD)){
            Term interm = subject;
            subject = predicate;
            predicate = interm;
        }
        Operator copula;
        switch (temporalOrder) {
            case CompoundTerm.ORDER_FORWARD:
                copula = Operator.EQUIVALENCE_AFTER;
                break;
            case CompoundTerm.ORDER_CONCURRENT:
                copula = Operator.EQUIVALENCE_WHEN;
                break;
            default:
                copula = Operator.EQUIVALENCE;
        }                
        String name = makeStatementName(subject, copula, predicate);
        Term t = memory.nameToTerm(name);
        if (t != null) {
            return (Equivalence) t;
        }
        ArrayList<Term> argument = argumentsToList(subject, predicate);
        return new Equivalence(argument, temporalOrder);
    }

    /**
     * Get the operator of the term.
     *
     * @return the operator of the term
     */
    @Override
    public Operator operator() {
        switch (temporalOrder) {
            case CompoundTerm.ORDER_FORWARD:
                return Operator.EQUIVALENCE_AFTER;
            case CompoundTerm.ORDER_CONCURRENT:
                return Operator.EQUIVALENCE_WHEN;
    	}
        return Operator.EQUIVALENCE;
    }

    /**
     * Check if the compound is commutative.
     *
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return (temporalOrder != CompoundTerm.ORDER_FORWARD);
    }

    public int getTemporalOrder() {
        return temporalOrder;
    }
}
