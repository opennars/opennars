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
package nars.nal.nal5;

import nars.Global;
import nars.Op;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;

import java.util.Collections;
import java.util.List;

/** 
 * A disjunction of Statements.
 */
public class Disjunction extends Junction {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private Disjunction(final Term[] arg) {
        super(arg);
        
        if (Global.DEBUG) { Terms.verifySortedAndUnique(arg, false);         }
        
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
    public Term clone(Term[] x) {
        return make(x);
    }
    
    
    /**
     * Try to make a new Disjunction from two term. Called by the logic rules.
     * @param term1 The first component
     * @param term2 The first component
     * @return A Disjunction generated or a Term it reduced to
     */
    public static Term make(Term term1, Term term2) {

        if (term1 instanceof Disjunction) {
            Compound ct1 = ((Compound) term1);
            List<Term> l = Global.newArrayList(ct1.length());
            Collections.addAll(l, ((Compound)term1).term);
            if (term2 instanceof Disjunction) {
                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                Collections.addAll(l, ((Compound)term2).term);
            }
            else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                l.add(term2);
            }
            return make(l);
        } else if (term2 instanceof Disjunction) {
            Compound ct2 = ((Compound) term2);
            // (&,R,(&,P,Q)) = (&,P,Q,R)
            List<Term> l = Global.newArrayList(ct2.length());
            Collections.addAll(l, ct2.term);
            l.add(term1);
            return make(l);
        } else {
            //the two terms by themselves, unreducable
            return make(new Term[] { term1, term2 });
        }
    }

    protected static Term make(List<Term> l) {
        return make(l.toArray(new Term[l.size()]));
    }

    public static Term make(Term[] t) {
        if (t.length == 0) return null;

        if ((t.length == 2) && ((t[0] instanceof Disjunction) || (t[1] instanceof Disjunction))) {
            //will call this method recursively
            return make(t[0], t[1]);
        }

        t = Terms.toSortedSetArray(t);

        if (t.length == 1) {
            // special case: single component
            return t[0];
        }

        return new Disjunction(t);
    }
    
    /**
     * Get the operate of the term.
     * @return the operate of the term
     */
    @Override
    public Op op() {
        return Op.DISJUNCTION;
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
