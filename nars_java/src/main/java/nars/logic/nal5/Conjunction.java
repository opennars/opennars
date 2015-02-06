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
package nars.logic.nal5;

import nars.core.Parameters;
import nars.logic.NALOperator;
import nars.logic.Terms;
import nars.logic.entity.CompoundTerm;
import nars.logic.entity.Term;
import nars.logic.nal7.TemporalRules;

import java.util.*;

import static java.lang.System.arraycopy;

/**
 * Conjunction of statements
 */
public class Conjunction extends Junction {

    public final int temporalOrder;

    /**
     * Constructor with partial values, called by make
     *
     * @param arg The component list of the term
     */
    protected Conjunction(final Term[] arg, final int order) {
        super(arg);

        if (order == TemporalRules.ORDER_BACKWARD) {
            throw new RuntimeException("Conjunction does not allow reverse order; args=" + Arrays.toString(arg));
            //although, we could reverse the arg terms..
        }

        temporalOrder = order;
        
        init(arg);

    }

    @Override
    final public int getMinimumRequiredComponents() {
        return 1;
    }


    @Override public Term clone(Term[] t) {        
        return make(t, temporalOrder);
    }

    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Conjunction clone() {
        return new Conjunction(term, temporalOrder);
    }
    
    
    /**
     * Get the operator of the term.
     *
     * @return the operator of the term
     */
    @Override
    public NALOperator operator() {
        switch (temporalOrder) {
            case TemporalRules.ORDER_FORWARD:
                return NALOperator.SEQUENCE;
            case TemporalRules.ORDER_CONCURRENT:
                return NALOperator.PARALLEL;
            default:
                return NALOperator.CONJUNCTION;
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
     * Try to make a new compound from a list of term
     *
     * @param temporalOrder The temporal order among term
     * @param argList the list of arguments
     * @param memory Reference to the memory
     * @return the Term generated from the arguments, or null if not possible
     */
    final public static Term make(final Term[] argList, final int temporalOrder) {
        if (Parameters.DEBUG) {  Terms.verifyNonNull(argList);}
        
        if (argList.length == 0) {
            return null;
        }                         // special case: single component
        if (argList.length == 1) {
            return argList[0];
        }                         // special case: single component
        
        if (temporalOrder == TemporalRules.ORDER_FORWARD) {
            
            return new Conjunction(argList, temporalOrder);
            
        } else {
            Term[] a = Term.toSortedSetArray(argList);
            if (a.length == 1) return a[0];
            return new Conjunction(a, temporalOrder);
        }
    }

    final public static Term make(final Term prefix, final Collection<? extends Term> suffix, final int temporalOrder) {
        Term[] t = new Term[suffix.size()+1];
        int i = 0;
        t[i++] = prefix;
        for (Term x : suffix)
            t[i++] = x;
        return make(t, temporalOrder);        
    }
    
    
    /**    
     *
     * @param c a set of Term as term
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    final private static Term make(final Collection<Term> c, int temporalOrder) {
        Term[] argument = c.toArray(new Term[c.size()]);
        return make(argument, temporalOrder);
    }

    @Override
    protected CharSequence makeName() {
        return makeCompoundName( operator(),  term);
    }

    
    // overload this method by term type?
    /**
     * Try to make a new compound from two term. Called by the logic rules.
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

            return makeForward(term1, term2);

        } else {
            

            if (term1 instanceof Conjunction) {
                CompoundTerm ct1 = ((CompoundTerm) term1);
                final List<Term> set = Parameters.newArrayList(ct1.size() + 1);
                Collections.addAll(set, ct1.term);
                if (term2 instanceof Conjunction) {
                    // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                    Collections.addAll(set, ((CompoundTerm) term2).term);
                }
                else {
                    // (&,(&,P,Q),R) = (&,P,Q,R)
                    set.add(term2);
                }                          
                return make(set, temporalOrder);
            } else if (term2 instanceof Conjunction) {
                CompoundTerm ct2 = ((CompoundTerm) term2);
                final List<Term> set = Parameters.newArrayList(ct2.size() + 1);
                Collections.addAll(set, ct2.term);
                set.add(term1);                              // (&,R,(&,P,Q)) = (&,P,Q,R)
                return make(set, temporalOrder);
            } else {
                return make(new Term[] { term1, term2 }, temporalOrder);
            }
            
        }
    }


    protected static Term makeForward(Term term1, Term term2) {
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

        return make(components, TemporalRules.ORDER_FORWARD);
    }

    @Override
    public int getTemporalOrder() {
        return temporalOrder;
    }

}
