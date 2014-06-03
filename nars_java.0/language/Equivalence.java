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
import nars.storage.Memory;

/**
 * A Statement about an Equivalence relation.
 */
public class Equivalence extends //	Implication ???
									Statement
{

    /**
     * Constructor with partial values, called by make
     * @param components The component list of the term
     */
    protected Equivalence(ArrayList<Term> components) {
        super(components);
    }

    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param components Component list
     * @param constant Whether the statement contains open variable
     * @param complexity Syntactic complexity of the compound
     */
    protected Equivalence(String n, ArrayList<Term> components, boolean constant, short complexity) {
        super(n, components, constant, complexity);
    }

    /**
     * Clone an object
     * @return A new object
     */
    public Object clone() {
        return new Equivalence(name, (ArrayList<Term>) cloneList(components), isConstant(), complexity);
    }

    /**
     * Try to make a new compound from two components. Called by the inference rules.
     * @param subject The first component
     * @param predicate The second component
     * @param memory Reference to the memory
     * @return A compound generated or null
     */
    public static Equivalence make(Term subject, Term predicate, Memory memory) {  // to be extended to check if subject is Conjunction
        if ((subject instanceof Implication) || (subject instanceof Equivalence)) {
            return null;
        }
        if ((predicate instanceof Implication) || (predicate instanceof Equivalence)) {
            return null;
        }
        if (invalidStatement(subject, predicate)) {
            return null;
        }
        if (subject.compareTo(predicate) > 0) {
            Term interm = subject;
            subject = predicate;
            predicate = interm;
        }
        String name = makeStatementName(subject, Symbols.EQUIVALENCE_RELATION, predicate);
        Term t = memory.nameToListedTerm(name);
        if (t != null) {
            return (Equivalence) t;
        }
        ArrayList<Term> argument = argumentsToList(subject, predicate);
        return new Equivalence(argument);
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    public String operator() {
        return Symbols.EQUIVALENCE_RELATION;
    }

    /**
     * Check if the compound is commutative.
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }
}
