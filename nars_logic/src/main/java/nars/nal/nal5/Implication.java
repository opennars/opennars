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
import nars.nal.nal7.Tense;
import nars.term.Statement;
import nars.term.Term;

/**
 * A Statement about an Inheritance copula.
 */
public class Implication<A extends Term, B extends Term> extends Statement<A,B> {

    //TODO use subclasses like Conjunction
    @Deprecated protected final int temporalOrder;


    protected Implication(final A subject, final B predicate, final int order) {
        super();

        if (order == Tense.ORDER_INVALID) {
            throw new RuntimeException("Invalid temporal order; args=" + subject + ',' + predicate);
        }

        temporalOrder = order;

        init(subject, predicate);
    }


    @Override public final boolean isCommutative() {
        return false;
    }

    public static Term makeImplication(final Term subject, final Term predicate) {
        if (subject.equals(predicate))
            return subject;
        return make(subject, predicate, Tense.ORDER_NONE);
    }

    /**
     * Try to make a new compound from two term. Called by the logic rules.
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or a term it reduced to
     */
    public static <A extends Term> Implication<A,A> make(final A subject, final A predicate) {
        return make(subject, predicate, Tense.ORDER_NONE);
    }

//    public static CharSequence makeName(final Term subject, final int temporalOrder, final Term predicate) {
//        Op copula;
//        switch (temporalOrder) {
//            case TemporalRules.ORDER_FORWARD:
//                copula = Op.IMPLICATION_AFTER;
//                break;
//            case TemporalRules.ORDER_CONCURRENT:
//                copula = Op.IMPLICATION_WHEN;
//                break;
//            case TemporalRules.ORDER_BACKWARD:
//                copula = Op.IMPLICATION_BEFORE;
//                break;
//            default:
//                copula = Op.IMPLICATION;
//        }
//        return makeStatementName(subject, copula, predicate);
//    }
    
    public static Implication make(final Term subject, final Term predicate, int temporalOrder) {
        if (invalidStatement(subject, predicate)) {
            return null;
        }
        
        if ((subject instanceof Implication) || (subject instanceof Equivalence) || (predicate instanceof Equivalence) ||
                (subject instanceof CyclesInterval) || (predicate instanceof CyclesInterval)) {
            return null;
        }
        
        if (predicate instanceof Implication) {
            final Term oldCondition = ((Statement) predicate).getSubject();
            if ((oldCondition instanceof Conjunction) && oldCondition.containsTerm(subject)) {
                return null;
            }
            final Term newCondition = Conjunctive.make(subject, oldCondition, temporalOrder);
            return make(newCondition, ((Statement) predicate).getPredicate(), temporalOrder);
        } else {
            return new Implication(subject, predicate, temporalOrder);
        }
    }

    /**
     * Get the operate of the term.
     * @return the operate of the term
     */
    @Override
    public Op op() {
        switch (temporalOrder) {
            case Tense.ORDER_FORWARD:
                return Op.IMPLICATION_AFTER;
            case Tense.ORDER_CONCURRENT:
                return Op.IMPLICATION_WHEN;
            case Tense.ORDER_BACKWARD:
                return Op.IMPLICATION_BEFORE;
        }
        return Op.IMPLICATION;
    }
    
    @Override
    public int getTemporalOrder() {
        return temporalOrder;
    }

    public boolean isForward() {
        return getTemporalOrder()== Tense.ORDER_FORWARD;
    }
    public boolean isBackward() {
        return getTemporalOrder()== Tense.ORDER_BACKWARD;
    }
    public boolean isConcurrent() {
        return getTemporalOrder()== Tense.ORDER_CONCURRENT;
    }
    
}
