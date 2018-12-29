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

import com.google.common.collect.ObjectArrays;
import org.opennars.io.Symbols.NativeOperator;
import org.opennars.main.MiscFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

/**
 * A compound term whose extension is the intersection of the extensions of its term as defined in the NARS-theory
 *
 * @author Patrick Hammer
 */
public class IntersectionExt extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    private IntersectionExt(final Term[] arg) {
        super(arg);
        
        if (MiscFlags.DEBUG) { Terms.verifySortedAndUnique(arg, false); }
        
        init(arg);
        
    }


    /**
     * Clone an object
     * @return A new object, to be casted into a IntersectionExt
     */
    @Override
    public IntersectionExt clone() {
        return new IntersectionExt(term);
    }
    
    @Override
    public Term clone(final Term[] replaced) {
        if(replaced == null) {
            return null;
        }
        return make(replaced);
    }
    
    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param term1 The first component
     * @param term2 The first component
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term term1, final Term term2) {
        
        if ((term1 instanceof SetInt) && (term2 instanceof SetInt)) {
            // set union
            final Term[] both = ObjectArrays.concat(
                    ((CompoundTerm) term1).term, 
                    ((CompoundTerm) term2).term, Term.class);
            return SetInt.make(both);
        }
        if ((term1 instanceof SetExt) && (term2 instanceof SetExt)) {
            // set intersection
            final NavigableSet<Term> set = Term.toSortedSet(((CompoundTerm) term1).term);
            
            set.retainAll(((CompoundTerm) term2).asTermList());     
            
            //technically this can be used directly if it can be converted to array
            //but wait until we can verify that NavigableSet.toarray does it or write a helper function like existed previously
            return SetExt.make(set.toArray(new Term[0]));
        }
        final List<Term> se = new ArrayList();
        if (term1 instanceof IntersectionExt) {
            ((CompoundTerm) term1).addTermsTo(se);
            if (term2 instanceof IntersectionExt) {
                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)                
                ((CompoundTerm) term2).addTermsTo(se);
            }               
            else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                se.add(term2);
            }               
        } else if (term2 instanceof IntersectionExt) {
            // (&,R,(&,P,Q)) = (&,P,Q,R)
            ((CompoundTerm) term2).addTermsTo(se);
            se.add(term1);
        } else {
            se.add(term1);
            se.add(term2);
        }
        return make(se.toArray(new Term[0]));
    }


    
    public static Term make(Term[] t) {
        t = Term.toSortedSetArray(t);
        switch (t.length) {
            case 0: return null;
            case 1: return t[0];
            default:
               return new IntersectionExt(t); 
        }
    }
    



    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.INTERSECTION_EXT;
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
