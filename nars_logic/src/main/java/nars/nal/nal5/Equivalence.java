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
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.TemporalRules;
import nars.term.Statement;
import nars.term.Term;

import java.util.Arrays;

/**
 * A Statement about an Equivalence relation.
 */
public class Equivalence extends Statement {

    private final int temporalOrder;

    /**
     * Constructor with partial values, called by make
     *
     * @param arg The component list of the term
     */
    private Equivalence(Term subject, Term predicate, int order) {
        super(subject, predicate);

        if ((order == TemporalRules.ORDER_BACKWARD) ||
                (order == TemporalRules.ORDER_INVALID)) {
            throw new RuntimeException("Invalid temporal order=" + order + "; args=" + subject + " , " + predicate);
        }

        temporalOrder = order;


        
        init(term);
    }

    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Equivalence clone() {
        return new Equivalence(getSubject(), getPredicate(), temporalOrder);
    }
    
    @Override public Equivalence clone(final Term[] t) {        
        if (t.length!=2)
            throw new RuntimeException("Equivalence requires 2 components: " + Arrays.toString(t));
        
        return make(t[0], t[1], temporalOrder);
    }
    
    /** alternate version of Inheritance.make that allows equivalent subject and predicate
     * to be reduced to the common term.      */
    public static Term makeTerm(final Term subject, final Term predicate, int temporalOrder) {
        if (subject.equals(predicate))
            return subject;                
        return make(subject, predicate, temporalOrder);        
    }
    public static Term makeTerm(Term subject, Term predicate) {
        return makeTerm(subject, predicate, TemporalRules.ORDER_NONE);
    }


    /**
     * Try to make a new compound from two term. Called by the logic
     * rules.
     *
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or null
     */
    public static Equivalence make(Term subject, Term predicate) {  // to be extended to check if subject is Conjunction
        return make(subject, predicate, TemporalRules.ORDER_NONE);
    }

    public static Equivalence make(Term subject, Term predicate, int temporalOrder) {  // to be extended to check if subject is Conjunction

        if (invalidStatement(subject, predicate)) {
            return null;
        }
        
        if ((subject instanceof Implication) || (subject instanceof Equivalence)
                || (predicate instanceof Implication) || (predicate instanceof Equivalence) ||
                (subject instanceof AbstractInterval) || (predicate instanceof AbstractInterval)) {
            return null;
        }
                
        if ((temporalOrder == TemporalRules.ORDER_BACKWARD)
                || ((subject.compareTo(predicate) > 0) && (temporalOrder != TemporalRules.ORDER_FORWARD))) {
            //swap
            Term interm = subject;
            subject = predicate;
            predicate = interm;
        }

        if (temporalOrder == TemporalRules.ORDER_BACKWARD)
            temporalOrder = TemporalRules.ORDER_FORWARD;

        //Term[] t;
        if (temporalOrder==TemporalRules.ORDER_FORWARD) {
            //already in final order
        }
        else {
            int c = subject.compareTo(predicate);
            if (c > 0) {
                //swap
                Term interm = subject;
                subject = predicate;
                predicate = interm;
            }
        }


        return new Equivalence(subject, predicate, temporalOrder);
    }

    /**
     * Get the operate of the term.
     *
     * @return the operate of the term
     */
    @Override
    public Op op() {
        switch (temporalOrder) {
            case TemporalRules.ORDER_FORWARD:
                return Op.EQUIVALENCE_AFTER;
            case TemporalRules.ORDER_CONCURRENT:
                return Op.EQUIVALENCE_WHEN;
        }
        return Op.EQUIVALENCE;
    }

    /**
     * Check if the compound is commutative.
     *
     * @return true for commutative
     */
    @Override
    public final boolean isCommutative() {
        return (temporalOrder != TemporalRules.ORDER_FORWARD);
    }

    @Override
    public final int getTemporalOrder() {
        return temporalOrder;
    }
}
