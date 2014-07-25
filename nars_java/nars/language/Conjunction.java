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

import nars.io.Symbols.Operator;
import nars.storage.Memory;
import nars.inference.TemporalRules;

/**
 * Conjunction of statements
 */
public class Conjunction extends CompoundTerm {

    private int temporalOrder;
    /**
     * Constructor with partial values, called by make
     *
     * @param arg The component list of the term
     */
    private Conjunction(ArrayList<Term> arg, int order) {
        super(arg);
        temporalOrder = order;
        setName(makeName());
    }

    /**
     * Constructor with full values, called by clone
     *
     * @param n The name of the term
     * @param cs Component list
     * @param con Whether the term is a constant
     * @param i Syntactic complexity of the compound
     */
    private Conjunction(String n, ArrayList<Term> arg, boolean con, short i, int order) {
        super(n, arg, con, i);
        temporalOrder = order;
    }

    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Object clone() {
        return new Conjunction(name, cloneList(components), isConstant(), complexity, temporalOrder);
    }

    /**
     * Get the operator of the term.
     *
     * @return the operator of the term
     */
    @Override
    public Operator operator() {
        switch (temporalOrder) {
            case TemporalRules.ORDER_FORWARD:
                return Operator.SEQUENCE;
            case TemporalRules.ORDER_CONCURRENT:
                return Operator.PARALLEL;
            default:
        return Operator.CONJUNCTION;
    	}
    }

    /**
     * Check if the compound is commutative.
     *
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        if (temporalOrder == TemporalRules.ORDER_FORWARD) {
            return false;
        } else {
        return true;
    	}
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

        return make(argList, TemporalRules.ORDER_NONE, memory);
    }
        
    /**
     * Try to make a new compound from a list of components. Called by
     * StringParser.
     *
     * @param temporalOrder The temporal order among components
     * @param argList the list of arguments
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    public static Term make(ArrayList<Term> argList, int temporalOrder, final Memory memory) {
        if (argList.isEmpty()) {
            return null;
        }                         // special case: single component
        if (argList.size() == 1) {
            return argList.get(0);
        }                         // special case: single component
        if (temporalOrder == TemporalRules.ORDER_FORWARD) {
            final String name = makeCompoundName(Operator.SEQUENCE, argList);
            final Term t = memory.nameToTerm(name);
            return (t != null) ? t : new Conjunction(argList, temporalOrder);
        } else {
        final TreeSet<Term> set = new TreeSet<>(argList); // sort/merge arguments
            return make(set, temporalOrder, memory);
        }
    }

    /**
     * Try to make a new Disjunction from a set of components. Called by the
     * public make methods.
     *
     * @param set a set of Term as components
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    private static Term make(final TreeSet<Term> set, int temporalOrder, final Memory memory) {
        final ArrayList<Term> argument = new ArrayList<>(set);
        final String name;
        if (temporalOrder == TemporalRules.ORDER_NONE) {
            name = makeCompoundName(Operator.CONJUNCTION, argument);
        } else {
            name = makeCompoundName(Operator.PARALLEL, argument);
        }
        final Term t = memory.nameToTerm(name);
        return (t != null) ? t : new Conjunction(argument, temporalOrder);
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
        return make(term1, term2, TemporalRules.ORDER_NONE, memory);
    }

    public static Term make(final Term term1, final Term term2, int temporalOrder, final Memory memory) {
        if (temporalOrder == TemporalRules.ORDER_FORWARD) {
            final ArrayList<Term> list;
            if ((term1 instanceof Conjunction) && (((Conjunction) term1).getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                list = new ArrayList<>(((CompoundTerm) term1).cloneComponents());
                if ((term2 instanceof Conjunction) && (((Conjunction) term2).getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                    list.addAll(((CompoundTerm) term2).cloneComponents());
                } // (&/,(&/,P,Q),(&/,R,S)) = (&/,P,Q,R,S)
                else {
                    list.add((Term) term2.clone());
                }                          // (&,(&,P,Q),R) = (&,P,Q,R)
            } else if ((term2 instanceof Conjunction) && (((Conjunction) term2).getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                list = new ArrayList<>(((CompoundTerm) term2).size() + 1);
                list.add((Term) term1.clone());
                list.addAll(((CompoundTerm) term2).cloneComponents()); // (&,R,(&,P,Q)) = (&,P,Q,R)
            } else {
                list = new ArrayList<>(2);
                list.add((Term) term1.clone());
                list.add((Term) term2.clone());
            }
            return make(list, temporalOrder, memory);
        } else {
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
            return make(set, temporalOrder, memory);
        }
    }

    public int getTemporalOrder() {
        return temporalOrder;
    }
}
