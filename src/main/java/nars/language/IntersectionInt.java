/*
 * IntersectionInt.java
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

import com.google.common.collect.ObjectArrays;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import nars.main.Parameters;
import nars.io.Symbols.NativeOperator;
import static nars.language.IntersectionExt.make;
import static nars.language.IntersectionExt.make;
import static nars.language.IntersectionExt.make;
import static nars.language.IntersectionExt.make;
import static nars.language.IntersectionExt.make;
import static nars.language.IntersectionExt.make;
import static nars.language.IntersectionExt.make;
import static nars.language.IntersectionExt.make;

/**
 * A compound term whose intension is the intersection of the extensions of its term
 */
public class IntersectionInt extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private IntersectionInt(final Term[] arg) {
        super( arg );
        
        if (Parameters.DEBUG) { Terms.verifySortedAndUnique(arg, false); }        
        
        init(arg);
    }


    /**
     * Clone an object
     * @return A new object, to be casted into a Conjunction
     */
    @Override
    public IntersectionInt clone() {
        return new IntersectionInt(term);
    }

  @Override
    public Term clone(Term[] replaced) {
        return make(replaced);
    }
        

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param term1 The first compoment
     * @param term2 The first compoment
     * @param memory Reference to the memory
     * @return A compound generated or a term it reduced to
     */
    public static Term make(Term term1, Term term2) {
        
        if ((term1 instanceof SetExt) && (term2 instanceof SetExt)) {
            // set union
            Term[] both = ObjectArrays.concat(
                    ((CompoundTerm) term1).term, 
                    ((CompoundTerm) term2).term, Term.class);
            return SetExt.make(both);
        }
        if ((term1 instanceof SetInt) && (term2 instanceof SetInt)) {
            // set intersection
            TreeSet<Term> set = Term.toSortedSet(((CompoundTerm) term1).term);
            
            set.retainAll(((CompoundTerm) term2).asTermList());     
            
            //technically this can be used directly if it can be converted to array
            //but wait until we can verify that TreeSet.toarray does it or write a helper function like existed previously
            return SetInt.make(set.toArray(new Term[set.size()]));
        }
        
        List<Term> se = new ArrayList();
        if (term1 instanceof IntersectionInt) {
            ((CompoundTerm) term1).addTermsTo(se);
            if (term2 instanceof IntersectionInt) {
                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)                
                ((CompoundTerm) term2).addTermsTo(se);
            }               
            else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                se.add(term2);
            }               
        } else if (term2 instanceof IntersectionInt) {
            // (&,R,(&,P,Q)) = (&,P,Q,R)
            ((CompoundTerm) term2).addTermsTo(se);
            se.add(term1);
        } else {
            se.add(term1);
            se.add(term2);
        }
        return make(se.toArray(new Term[se.size()]));
    }

    
    public static Term make(Term[] t) {
        t = Term.toSortedSetArray(t);
        switch (t.length) {
            case 0: return null;
            case 1: return t[0];
            default:
               return new IntersectionInt(t); 
        }
    }
    
    
    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.INTERSECTION_INT;
    }

    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }
}
