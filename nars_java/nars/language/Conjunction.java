/*
 * Conjunction.java
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
 * Conjunction of statements
 */
public class Conjunction extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     *
     * @param arg The component list of the term
     */
    protected Conjunction(ArrayList<Term> arg) {
        super(arg);
    }

    /**
     * Constructor with full values, called by clone
     *
     * @param n The name of the term
     * @param cs Component list
     * @param con Whether the term is a constant
     * @param i Syntactic complexity of the compound
     */
    private Conjunction(String n, ArrayList<Term> cs, boolean con, short i) {
        super(n, cs, con, i);
    }

    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Object clone() {
        return new Conjunction(name, (ArrayList<Term>) cloneList(components), isConstant(), complexity);
    }

    /**
     * Get the operator of the term.
     *
     * @return the operator of the term
     */
    @Override
    public String operator() {
        return Symbols.CONJUNCTION_OPERATOR;
    }

    /**
     * Check if the compound is commutative.
     *
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }

    /**
     * Try to make a new compound from a list of components. Called by
     * StringParser.
     *
     * @return the Term generated from the arguments
     * @param argList the list of arguments
     * @param memory Reference to the memory
     */
    public static Term make(ArrayList<Term> argList, final Memory memory) {
        
        //SETH asks: why is a term in the argList null
        /*argList.removeIf(new Predicate<Term>() {
            @Override public boolean test(Term t) {
                return t==null;
            }            
        });*/
         for (final Iterator<Term> itr = argList.iterator(); itr.hasNext();) {
            if (itr.next() == null) { itr.remove(); }
        }
        
        final TreeSet<Term> set = new TreeSet<>(argList); // sort/merge arguments
        return make(set, memory);
    }

    /**
     * Try to make a new Disjunction from a set of components. Called by the
     * public make methods.
     *
     * @param set a set of Term as components
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    private static Term make(final TreeSet<Term> set, final Memory memory) {
        if (set.isEmpty()) {
            return null;
        }                         // special case: single component
        if (set.size() == 1) {
            return set.first();
        }                         // special case: single component
        final ArrayList<Term> argument = new ArrayList<>(set);
        final String name = makeCompoundName(Symbols.CONJUNCTION_OPERATOR, argument);
        final Term t = memory.nameToListedTerm(name);
        return (t != null) ? t : new Conjunction(argument);
    }

    // overload this method by term type?
    /**
     * Try to make a new compound from two components. Called by the inference
     * rules.
     *
     * @param term1 The first component
     * @param term2 The second component
     * @param memory Reference to the memory
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term term1, final Term term2, final Memory memory) {
        final TreeSet<Term> set;
        if (term1 instanceof Conjunction) {
            set = new TreeSet<>(((CompoundTerm) term1).cloneComponents());
            if (term2 instanceof Conjunction) {
                set.addAll(((CompoundTerm) term2).cloneComponents());
            } // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
            else {
                set.add((Term) term2.clone());
            }                          // (&,(&,P,Q),R) = (&,P,Q,R)
        } else if (term2 instanceof Conjunction) {
            set = new TreeSet<>(((CompoundTerm) term2).cloneComponents());
            set.add((Term) term1.clone());                              // (&,R,(&,P,Q)) = (&,P,Q,R)
        } else {
            set = new TreeSet<>();
            set.add((Term) term1.clone());
            set.add((Term) term2.clone());
        }
        return make(set, memory);
    }
}
