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

import nars.nal.NALOperator;
import nars.nal.nal7.AbstractInterval;
import nars.term.Statement;
import nars.term.Term;
import nars.nal.nal7.TemporalRules;

import java.util.Arrays;

/**
 * A Statement about an Inheritance copula.
 */
public class Implication extends Statement {

    private final int temporalOrder;

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    public Implication(Term[] arg, int order) {
        super(arg[0], arg[1]);

        if (order == TemporalRules.ORDER_INVALID) {
            throw new RuntimeException("Invalid temporal order=" + order + "; args=" + Arrays.toString(arg));
        }

        temporalOrder = order;
        
        init(arg);
    }
    
    public Implication(Term subject, Term predicate, int order) {
        this(new Term[] { subject, predicate }, order);
    }


    
    
    /**
     * Clone an object
     * @return A new object
     */
    @Override
    public Implication clone() {
        return new Implication(term, getTemporalOrder());
    }
    
    @Override public Implication clone(final Term[] t) {
        if (t.length!=2)
            throw new RuntimeException("Implication requires 2 components: " + Arrays.toString(t));
        
        return make(t[0], t[1], temporalOrder);
    }


    public static Term makeTerm(final Term subject, final Term predicate) {
        if (subject.equals(predicate))
            return subject;
        return make(subject, predicate, TemporalRules.ORDER_NONE);
    }

    /**
     * Try to make a new compound from two term. Called by the logic rules.
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or a term it reduced to
     */
    public static Implication make(final Term subject, final Term predicate) {
        return make(subject, predicate, TemporalRules.ORDER_NONE);
    }

    public static CharSequence makeName(final Term subject, final int temporalOrder, final Term predicate) {
        NALOperator copula;
        switch (temporalOrder) {
            case TemporalRules.ORDER_FORWARD:
                copula = NALOperator.IMPLICATION_AFTER;
                break;
            case TemporalRules.ORDER_CONCURRENT:
                copula = NALOperator.IMPLICATION_WHEN;
                break;
            case TemporalRules.ORDER_BACKWARD:
                copula = NALOperator.IMPLICATION_BEFORE;
                break;
            default:
                copula = NALOperator.IMPLICATION;
        }                
        return makeStatementName(subject, copula, predicate);
    }
    
    public static Implication make(final Term subject, final Term predicate, int temporalOrder) {
        if (invalidStatement(subject, predicate)) {
            return null;
        }
        
        if ((subject instanceof Implication) || (subject instanceof Equivalence) || (predicate instanceof Equivalence) ||
                (subject instanceof AbstractInterval) || (predicate instanceof AbstractInterval)) {
            return null;
        }
        
        //final CharSequence name = makeName(subject, temporalOrder, predicate);         
        if (predicate instanceof Implication) {
            final Term oldCondition = ((Statement) predicate).getSubject();
            if ((oldCondition instanceof Conjunction) && oldCondition.containsTerm(subject)) {
                return null;
            }
            final Term newCondition = Conjunction.make(subject, oldCondition, temporalOrder);
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
    public NALOperator operator() {
        switch (temporalOrder) {
            case TemporalRules.ORDER_FORWARD:
                return NALOperator.IMPLICATION_AFTER;
            case TemporalRules.ORDER_CONCURRENT:
                return NALOperator.IMPLICATION_WHEN;
            case TemporalRules.ORDER_BACKWARD:
                return NALOperator.IMPLICATION_BEFORE;
        }
        return NALOperator.IMPLICATION;
    }
    
    @Override
    public int getTemporalOrder() {
        return temporalOrder;
    }

    public boolean isForward() {
        return getTemporalOrder()==TemporalRules.ORDER_FORWARD;
    }
    public boolean isBackward() {
        return getTemporalOrder()==TemporalRules.ORDER_BACKWARD;
    }
    public boolean isConcurrent() {
        return getTemporalOrder()==TemporalRules.ORDER_CONCURRENT;
    }
    
}
