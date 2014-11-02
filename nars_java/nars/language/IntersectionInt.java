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

import java.util.Arrays;
import java.util.TreeSet;
import nars.core.Parameters;
import nars.io.Symbols.NativeOperator;

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
    public static Term make(final Term term1, final Term term2) {
        TreeSet<Term> set;
        if ((term1 instanceof SetExt) && (term2 instanceof SetExt)) {
            set = new TreeSet<>(((CompoundTerm) term1).getTermList());
            set.addAll(((CompoundTerm) term2).getTermList());           // set union
            return SetExt.make(set);
        }
        if ((term1 instanceof SetInt) && (term2 instanceof SetInt)) {
            set = new TreeSet<>(((CompoundTerm) term1).getTermList());
            set.retainAll(((CompoundTerm) term2).getTermList());        // set intersection
            return SetInt.make(set);
        }
        if (term1 instanceof IntersectionInt) {
            set = new TreeSet<>(((CompoundTerm) term1).getTermList());
            if (term2 instanceof IntersectionInt) {
                // (|,(|,P,Q),(|,R,S)) = (|,P,Q,R,S)
                set.addAll(((CompoundTerm) term2).getTermList());
            } 
            else {
                // (|,(|,P,Q),R) = (|,P,Q,R)
                set.add(term2);
            }                          
        } else if (term2 instanceof IntersectionInt) {
            // (|,R,(|,P,Q)) = (|,P,Q,R)
            set = new TreeSet<>(((CompoundTerm) term2).getTermList());
            set.add(term1);   
        } else {
            set = new TreeSet<>();
            set.add(term1);
            set.add(term2);
        }
        return make(set);
    }

    public static Term make(TreeSet<Term> t) {
        if (t.size() == 0) return null;        
        if (t.size() == 1) return t.first(); // special case: single component        
                
        Term[] a = t.toArray(new Term[t.size()]);
        return new IntersectionInt(a);
    }
    
    public static Term make(Term[] replaced) {
        if (replaced.length == 1)
            return replaced[0];
        else if (replaced.length > 1)
            return make(Term.toSortedSet(replaced));
        else
            throw new RuntimeException("Invalid # of terms for Intersection: " + Arrays.toString(replaced));
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
