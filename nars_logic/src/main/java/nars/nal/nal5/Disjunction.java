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
import nars.term.Term;
import nars.term.Terms;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;

import java.util.List;

import static nars.Op.DISJUNCTION;

/** 
 * A disjunction of Statements.
 */
public interface Disjunction  {

    /**
     * Try to make a new Disjunction from two term. Called by the logic rules.
     * @param term1 The first component
     * @param term2 The first component
     * @return A Disjunction generated or a Term it reduced to
     */
    static Term disjunction(Term term1, Term term2) {

        if (term1.op() == DISJUNCTION) {
            Compound ct1 = ((Compound) term1);
            List<Term> l = Global.newArrayList(ct1.size());
            ct1.addAllTo(l);
            if (term2.op() == DISJUNCTION) {
                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                ((Compound)term2).addAllTo(l);
            }
            else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                l.add(term2);
            }
            return disjunction(l);
        } else if (term2.op() == DISJUNCTION) {
            Compound ct2 = ((Compound) term2);
            // (&,R,(&,P,Q)) = (&,P,Q,R)
            List<Term> l = Global.newArrayList(ct2.size());
            ct2.addAllTo(l);
            l.add(term1);
            return disjunction(l);
        } else {
            //the two terms by themselves, unreducable
            return disjunction(new Term[] { term1, term2 });
        }
    }

    static Term disjunction(List<Term> l) {
        return disjunction(l.toArray(new Term[l.size()]));
    }

    static Term disjunction(Term[] t) {
        if (t.length == 0) return null;

        if ((t.length == 2) && ((t[0] instanceof Disjunction) || (t[1] instanceof Disjunction))) {
            //will call this method recursively
            return disjunction(t[0], t[1]);
        }

        t = Terms.toSortedSetArray(t);

        if (t.length == 1) {
            // special case: single component
            return t[0];
        }

        return new GenericCompound<>(DISJUNCTION, t);
    }


}
