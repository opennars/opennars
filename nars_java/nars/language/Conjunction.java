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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import nars.inference.TemporalRules;
import nars.io.Symbols.NativeOperator;
import nars.storage.Memory;

/**
 * Conjunction of statements
 */
public class Conjunction extends CompoundTerm {

    public final int temporalOrder;
    
    /**
     * Constructor with partial values, called by make
     *
     * @param arg The component list of the term
     */
    private Conjunction(CharSequence name, Term[] arg, int order) {
        super(name, arg);
        temporalOrder = order;
    }

    @Override
    public int getMinimumRequiredComponents() {
        return 1;
    }

    
    /**
     * Constructor with full values, called by clone
     *
     * @param n The name of the term
     * @param cs Component list
     * @param con Whether the term is a constant
     * @param i Syntactic complexity of the compound
     */
    private Conjunction(CharSequence n, Term[] arg, boolean con, short i, int order) {
        super(n, arg, con, i);
        temporalOrder = order;
    }

    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Conjunction clone() {
        return new Conjunction(name(), cloneTerms(), isConstant(), complexity, temporalOrder);
    }

    /**
     * Get the operator of the term.
     *
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        switch (temporalOrder) {
            case TemporalRules.ORDER_FORWARD:
                return NativeOperator.SEQUENCE;
            case TemporalRules.ORDER_CONCURRENT:
                return NativeOperator.PARALLEL;
            default:
        return NativeOperator.CONJUNCTION;
    	}
    }

    /**
     * Check if the compound is commutative.
     *
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return temporalOrder != TemporalRules.ORDER_FORWARD;
    }

    /**
     * Try to make a new compound from a list of term. Called by
     * StringParser.
     *
     * @return the Term generated from the arguments
     * @param argList the list of arguments
     * @param memory Reference to the memory
     */
    public static Term make(Term[] argList, final Memory memory) {
        return make(argList, TemporalRules.ORDER_NONE, memory);
    }
        
    /**
     * Try to make a new compound from a list of term. Called by
     * StringParser.
     *
     * @param temporalOrder The temporal order among term
     * @param argList the list of arguments
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    public static Term make(Term[] argList, int temporalOrder, final Memory memory) {
        if (argList.length == 0) {
            return null;
        }                         // special case: single component
        if (argList.length == 1) {
            return argList[0];
        }                         // special case: single component
        if (temporalOrder == TemporalRules.ORDER_FORWARD) {
            final CharSequence name = makeCompoundName(NativeOperator.SEQUENCE, argList);
            final Term t = memory.conceptTerm(name);
            return (t != null) ? t : new Conjunction(name, argList, temporalOrder);
        } else {
            final TreeSet<Term> set = new TreeSet<>(); // sort/merge arguments
            set.addAll(Arrays.asList(argList));
            return make(set, temporalOrder, memory);
        }
    }

    /**
     * Try to make a new Disjunction from a set of term. Called by the
     * public make methods.
     *
     * @param set a set of Term as term
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    private static Term make(final TreeSet<Term> set, int temporalOrder, final Memory memory) {
        Term[] argument = set.toArray(new Term[set.size()]);
        final CharSequence name;
        if (temporalOrder == TemporalRules.ORDER_NONE) {
            name = makeCompoundName(NativeOperator.CONJUNCTION, argument);
        } else {
            name = makeCompoundName(NativeOperator.PARALLEL, argument);
        }
        final Term t = memory.conceptTerm(name);
        return (t != null) ? t : new Conjunction(name, argument, temporalOrder);
    }

    // overload this method by term type?
    /**
     * Try to make a new compound from two term. Called by the inference
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
            if ((term1 instanceof Conjunction) && (term1.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                list = new ArrayList<>(((CompoundTerm) term1).cloneTermsList());
                if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                    list.addAll(((CompoundTerm) term2).cloneTermsList());
                } // (&/,(&/,P,Q),(&/,R,S)) = (&/,P,Q,R,S)
                else {
                    list.add(term2.clone());
                }                          // (&,(&,P,Q),R) = (&,P,Q,R)
            } else if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                list = new ArrayList<>(((CompoundTerm) term2).size() + 1);
                list.add(term1.clone());
                list.addAll(((CompoundTerm) term2).cloneTermsList()); // (&,R,(&,P,Q)) = (&,P,Q,R)
            } else {
                list = new ArrayList<>(2);
                list.add(term1.clone());
                list.add(term2.clone());
            }
            return make(list.toArray(new Term[list.size()]), temporalOrder, memory);
        } else {
        final TreeSet<Term> set;
        if (term1 instanceof Conjunction) {
            set = new TreeSet<>(((CompoundTerm) term1).cloneTermsList());
            if (term2 instanceof Conjunction) {
                set.addAll(((CompoundTerm) term2).cloneTermsList());
            } // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
            else {
                set.add(term2.clone());
            }                          // (&,(&,P,Q),R) = (&,P,Q,R)
        } else if (term2 instanceof Conjunction) {
            set = new TreeSet<>(((CompoundTerm) term2).cloneTermsList());
            set.add(term1.clone());                              // (&,R,(&,P,Q)) = (&,P,Q,R)
        } else {
            set = new TreeSet<>();
            set.add(term1.clone());
            set.add(term2.clone());
        }
            return make(set, temporalOrder, memory);
        }
    }

    @Override
    public int getTemporalOrder() {
        return temporalOrder;
    }
}
