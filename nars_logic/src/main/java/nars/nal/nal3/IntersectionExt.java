/*
 * IntersectionExt.java
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
package nars.nal.nal3;

import com.google.common.collect.ObjectArrays;
import nars.Global;
import nars.Op;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A compound term whose extension is the intersection of the extensions of its term
 */
public class IntersectionExt extends Intersect {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private IntersectionExt(Term[] arg) {
        super(arg);
        
        if (Global.DEBUG) { Terms.verifySortedAndUnique(arg, false); }
        
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
    public Term clone(Term[] replaced) {
        return make(replaced);
    }
    
    /**
     * Try to make a new compound from two term. Called by the logic rules.
     * @param term1 The first compoment
     * @param term2 The first compoment
     * @return A compound generated or a term it reduced to
     */
    public static Term make(Term term1, Term term2) {
        
        if ((term1 instanceof SetInt) && (term2 instanceof SetInt)) {
            // set union
            Term[] both = ObjectArrays.concat(
                    ((SetInt) term1).terms(),
                    ((SetInt) term2).terms(), Term.class);
            return SetInt.make(both);
        }
        if ((term1 instanceof SetExt) && (term2 instanceof SetExt)) {
            // set intersection
            Set<Term> set = ((Compound) term1).asTermSortedSet();
            
            set.retainAll(((Compound) term2).asTermSet());
            
            //technically this can be used directly if it can be converted to array
            //but wait until we can verify that TreeSet.toarray does it or write a helper function like existed previously
            return SetExt.make(set.toArray(new Term[set.size()]));
        }

        if (term1 instanceof IntersectionExt) {
            List<Term> se = Global.newArrayList();
            ((Compound) term1).addTermsTo(se);
            if (term2 instanceof IntersectionExt) {
                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)                
                ((Compound) term2).addTermsTo(se);
            }               
            else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                se.add(term2);
            }
            return make(se.toArray(new Term[se.size()]));
        } else if (term2 instanceof IntersectionExt) {
            List<Term> se = Global.newArrayList();
            // (&,R,(&,P,Q)) = (&,P,Q,R)
            ((Compound) term2).addTermsTo(se);
            se.add(term1);
            return make(se.toArray(new Term[se.size()]));
        } else {
            return make(new Term[] { term1, term2 } );
        }

    }


    
    public static Term make(Term[] t) {
        t = Terms.toSortedSetArray(t);
        switch (t.length) {
            case 0: throw new RuntimeException("zero arguments invalid for set");
            case 1: return t[0];
            /*case 2:
                //flatten
                if ((t[0] instanceof SetTensional) &&
                    (t[1] instanceof SetTensional) && t[0].op() == t[1].op())
                        return IntersectionExt.make(t[0], t[1]);
                else {
                    //fall-through to default
                }
            } */
            //TODO 3: make(t[0], make(t[1], t[2]) ... etc??

        }
        return new IntersectionExt(t);
    }

    public static Term make(Collection<Term> unsorted) {
        return make(unsorted.toArray(new Term[unsorted.size()]));
    }
    
    /**
     * Get the operate of the term.
     * @return the operate of the term
     */
    @Override
    public Op op() {
        return Op.INTERSECTION_EXT;
    }



}
