/*
 * Disjunction.java
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

import java.util.Collection;
import java.util.TreeSet;
import nars.core.Parameters;
import nars.io.Symbols.NativeOperator;

/** 
 * A disjunction of Statements.
 */
public class Disjunction extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private Disjunction(final Term[] arg) {
        super(arg);
        
        if (Parameters.DEBUG) { Terms.verifySortedAndUnique(arg, false);         }        
        
        init(arg);
    }

    
    @Override
    public int getMinimumRequiredComponents() {
        return 1;
    }
    
    /**
     * Clone an object
     * @return A new object
     */
    @Override
    public Disjunction clone() {
        return new Disjunction(term);
    }

    @Override
    public Term clone(Term[] x) {
        return make(Term.toSortedSet(x));
    }
    
    
    /**
     * Try to make a new Disjunction from two term. Called by the inference rules.
     * @param term1 The first component
     * @param term2 The first component
     * @param memory Reference to the memory
     * @return A Disjunction generated or a Term it reduced to
     */
    public static Term make(Term term1, Term term2) {
        TreeSet<Term> set;
        if (term1 instanceof Disjunction) {
            set = new TreeSet<>(((CompoundTerm) term1).getTermList());
            if (term2 instanceof Disjunction) {
                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                set.addAll(((CompoundTerm) term2).getTermList());
            } 
            else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                set.add(term2);
            }                          
        } else if (term2 instanceof Disjunction) {
            // (&,R,(&,P,Q)) = (&,P,Q,R)
            set = new TreeSet<>(((CompoundTerm) term2).getTermList());
            set.add(term1);                              
        } else {
            set = new TreeSet<>();
            set.add(term1);
            set.add(term2);
        }
        return make(set);
    }

    /**
     * Try to make a new IntersectionExt. Called by StringParser.
     * @param argList a list of Term as term
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    public static Term make(Collection<Term> argList) {
        TreeSet<Term> set = new TreeSet<>(argList); // sort/merge arguments
        return make(set);
    }

    /**
     * Try to make a new Disjunction from a set of term. Called by the public make methods.
     * @param set a set of Term as term
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    public static Term make(TreeSet<Term> set) {
        if (set.size() == 1) {
            // special case: single component
            return set.first();
        }                         
        Term[] argument = set.toArray(new Term[set.size()]);
        return new Disjunction(argument);
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.DISJUNCTION;
    }

    /**
     * Disjunction is commutative.
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }
}
