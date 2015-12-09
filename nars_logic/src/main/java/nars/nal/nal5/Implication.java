/*
 * Implication.java
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
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;

import static nars.term.Statement.*;

/**
 * A Statement about an Inheritance copula.
 */
public interface Implication {

    @Deprecated static Op op(Order order) {
        switch (order) {
            case Backward:
                return Op.IMPLICATION_BEFORE;
            case Forward:
                return Op.IMPLICATION_AFTER;
            case Concurrent:
                return Op.IMPLICATION_WHEN;
        }
        return Op.IMPLICATION;
    }



    static Term implication(Term subject, Term predicate) {
        if (subject.equals(predicate))
            return subject;
        return implication(subject, predicate, Tense.ORDER_NONE);
    }

//    /**
//     * Try to make a new compound from two term. Called by the logic rules.
//     * @param subject The first component
//     * @param predicate The second component
//     * @return A compound generated or a term it reduced to
//     */
//    static <A extends Term> Compound implies(A subject, A predicate) {
//        return implication(subject, predicate, Tense.ORDER_NONE);
//    }

    static Compound implication(Term subject, Term predicate, Order temporalOrder) {
        if (invalidStatement(subject, predicate)) {
            return null;
        }
        
        if ((subject instanceof Implication) || (subject instanceof Equivalence) || (predicate instanceof Equivalence) ||
                (subject instanceof CyclesInterval) || (predicate instanceof CyclesInterval)) {
            return null;
        }
        
        if (predicate instanceof Implication) {
            Term oldCondition = subj(predicate);
            if ((oldCondition instanceof Conjunction) && oldCondition.containsTerm(subject)) {
                return null;
            }
            Term newCondition = Conjunctive.make(subject, oldCondition, temporalOrder);
            return implication(newCondition, pred(predicate), temporalOrder);
        } else {
            return (Compound) GenericCompound.COMPOUND(op(temporalOrder), subject, predicate);
        }
    }


}
