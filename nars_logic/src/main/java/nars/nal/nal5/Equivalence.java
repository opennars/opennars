/*
 * Equivalence.java
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

import nars.Op;
import nars.nal.nal7.CyclesInterval;
import nars.nal.nal7.Order;
import nars.nal.nal7.Tense;
import nars.term.Statement;
import nars.term.Term;
import nars.term.compound.GenericCompound;

/**
 * A Statement about an Equivalence relation.
 */
public interface Equivalence  {

    @Deprecated static Op equivOp(Order order) {
        switch (order) {
            case Forward:
                return Op.EQUIV_AFTER;
            case Concurrent:
                return Op.EQUIV_WHEN;
            case Backward:
                throw new RuntimeException("invalid order");
        }
        return Op.EQUIV;
    }



    /**
     * Try to make a new compound from two term. Called by the logic
     * rules.
     *
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or null
     */
    public static Term make(Term subject, Term predicate) {  // to be extended to check if subject is Conjunction
        return make(subject, predicate, Tense.ORDER_NONE);
    }

    public static Term make(Term subject, Term predicate, Order temporalOrder) {  // to be extended to check if subject is Conjunction

        if ((subject instanceof Implication) || (subject instanceof Equivalence)
                || (predicate instanceof Implication) || (predicate instanceof Equivalence) ||
                (subject instanceof CyclesInterval) || (predicate instanceof CyclesInterval)) {
            return null;
        }

        if (Statement.invalidStatement(subject, predicate)) {
            return null;
        }

        //swap terms for commutivity, or to reverse a backward order
        boolean reverse;
        if (temporalOrder == Tense.ORDER_BACKWARD) {
            temporalOrder = Tense.ORDER_FORWARD;
            reverse = true;
        }
        else if (temporalOrder != Tense.ORDER_FORWARD)
            reverse = subject.compareTo(predicate) > 0;
        else
            reverse = false;

        if (reverse) {
            //swap
            Term interm = subject;
            subject = predicate;
            predicate = interm;
        }

        return GenericCompound.COMPOUND(
            equivOp(temporalOrder),
            subject, predicate
        );
    }

}
