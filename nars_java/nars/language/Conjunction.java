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

import static java.lang.System.arraycopy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import nars.core.Memory;
import nars.inference.TemporalRules;
import nars.io.Symbols.NativeOperator;

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
    private Conjunction(final CharSequence name, final Term[] arg, final int order) {
        super(name, arg);
        temporalOrder = order;
    }

    @Override
    final public int getMinimumRequiredComponents() {
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
    private Conjunction(final CharSequence n, final Term[] arg, final boolean con, final short i, final int order) {
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
    
    public Conjunction cloneReplacingTerms(final Term[] replacementTerms) {
        return new Conjunction(makeCompoundName(operator(), replacementTerms), replacementTerms, isConstant(), complexity, temporalOrder);
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
     * Try to make a new compound from a list of term. Called by StringParser.
     *
     * @return the Term generated from the arguments
     * @param argList the list of arguments
     * @param memory Reference to the memory
     */
    final public static Term make(final Term[] argList) {
        return make(argList, TemporalRules.ORDER_NONE);
    }

    /**
     * Try to make a new compound from a list of term. Called by StringParser.
     *
     * @param temporalOrder The temporal order among term
     * @param argList the list of arguments
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    final public static Term make(final Term[] argList, final int temporalOrder) {
        if (argList.length == 0) {
            return null;
        }                         // special case: single component
        if (argList.length == 1) {
            return argList[0];
        }                         // special case: single component
        if (temporalOrder == TemporalRules.ORDER_FORWARD) {
            return new Conjunction(makeCompoundName(NativeOperator.SEQUENCE, argList), argList, temporalOrder);
        } else {
            // sort/merge arguments
            final TreeSet<Term> set = new TreeSet<>(Arrays.asList(argList));             
            return make(set, temporalOrder);
        }
    }

    /**
     * Try to make a new Disjunction from a set of term. Called by the public
     * make methods.
     *
     * @param set a set of Term as term
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    final private static Term make(final TreeSet<Term> set, int temporalOrder) {
        Term[] argument = set.toArray(new Term[set.size()]);
        final CharSequence name;
        if (temporalOrder == TemporalRules.ORDER_NONE) {
            name = makeCompoundName(NativeOperator.CONJUNCTION, argument);
        } else {
            name = makeCompoundName(NativeOperator.PARALLEL, argument);
        }
        return new Conjunction(name, argument, temporalOrder);
    }

    // overload this method by term type?
    /**
     * Try to make a new compound from two term. Called by the inference rules.
     *
     * @param term1 The first component
     * @param term2 The second component
     * @param memory Reference to the memory
     * @return A compound generated or a term it reduced to
     */
    final public static Term make(final Term term1, final Term term2) {
        return make(term1, term2, TemporalRules.ORDER_NONE);
    }

    final public static Term make(final Term term1, final Term term2, int temporalOrder) {
        if (temporalOrder == TemporalRules.ORDER_FORWARD) {
            
            final Term[] components;
            
            if ((term1 instanceof Conjunction) && (term1.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                
                CompoundTerm cterm1 = (CompoundTerm) term1;
                
                ArrayList<Term> list = new ArrayList<>(cterm1.size());
                cterm1.addTermsTo(list);
                        
                if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) { 
                    // (&/,(&/,P,Q),(&/,R,S)) = (&/,P,Q,R,S)
                    ((CompoundTerm) term2).addTermsTo(list);
                } 
                else {
                    // (&,(&,P,Q),R) = (&,P,Q,R)
                    list.add(term2);
                }
                
                components = list.toArray(new Term[list.size()]);
                
            } else if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                CompoundTerm cterm2 = (CompoundTerm) term2;
                components = new Term[((CompoundTerm) term2).size() + 1];
                components[0] = term1;
                arraycopy(cterm2.term, 0, components, 1, cterm2.size());
            } else {
                components = new Term[] { term1, term2 };
            }
            return make(components, temporalOrder);
            
        } else {
            
            final TreeSet<Term> set;
            if (term1 instanceof Conjunction) {                
                set = ((CompoundTerm) term1).getTermTreeSet();
                if (term2 instanceof Conjunction) {                    
                    // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                    ((CompoundTerm) term2).addTermsTo(set);
                } 
                else {
                    // (&,(&,P,Q),R) = (&,P,Q,R)
                    set.add(term2);
                }                          
                
            } else if (term2 instanceof Conjunction) {
                set = ((CompoundTerm) term2).getTermTreeSet();
                set.add(term1);                              // (&,R,(&,P,Q)) = (&,P,Q,R)
            } else {
                set = new TreeSet<>();
                set.add(term1);
                set.add(term2);
            }
            return make(set, temporalOrder);
        }
    }

    @Override
    public int getTemporalOrder() {
        return temporalOrder;
    }
}
