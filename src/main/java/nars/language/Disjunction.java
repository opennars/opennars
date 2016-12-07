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

import nars.config.Parameters;
import nars.io.Symbols.NativeOperator;

import java.util.ArrayList;
import java.util.List;

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
        return make(x);
    }
    
    
    /**
     * Try to make a new Disjunction from two term. Called by the inference rules.
     * @param term1 The first component
     * @param term2 The first component
     * @param memory Reference to the memory
     * @return A Disjunction generated or a Term it reduced to
     */
    public static Term make(Term term1, Term term2) {
        List<Term> set = new ArrayList();
        if (term1 instanceof Disjunction) {
            set.addAll(((CompoundTerm) term1).asTermList());
            if (term2 instanceof Disjunction) {
                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                set.addAll(((CompoundTerm) term2).asTermList());
            } 
            else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                set.add(term2);
            }                          
        } else if (term2 instanceof Disjunction) {
            // (&,R,(&,P,Q)) = (&,P,Q,R)
            set.addAll(((CompoundTerm) term2).asTermList());
            set.add(term1);                              
        } else {
            set.add(term1);
            set.add(term2);
        }
        return make(set.toArray(new Term[set.size()]));
    }


    public static Term make(Term[] t) {
        t = Term.toSortedSetArray(t);
        
        if (t.length == 0) return null;
        if (t.length == 1) {
            // special case: single component
            return t[0];
        }                         
        
        return new Disjunction(t);
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
