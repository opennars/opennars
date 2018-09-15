/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.language;

import org.opennars.io.Symbols.NativeOperator;
import org.opennars.main.MiscFlags;

import java.util.ArrayList;
import java.util.List;

/** 
 * A disjunction of Statements as defined in the NARS-theory
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class Disjunction extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    private Disjunction(final Term[] arg) {
        super(arg);
        
        if (MiscFlags.DEBUG) { Terms.verifySortedAndUnique(arg, false);         }        
        
        init(arg);
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
    public Term clone(final Term[] x) {
        if(x == null) {
            return null;
        }
        return make(x);
    }
    
    
    /**
     * Try to make a new Disjunction from two term. Called by the inference rules.
     * @param term1 The first component
     * @param term2 The first component
     * @return A Disjunction generated or a Term it reduced to
     */
    public static Term make(final Term term1, final Term term2) {
        final List<Term> set = new ArrayList();
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
        return make(set.toArray(new Term[0]));
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
