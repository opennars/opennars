/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.language;

import org.opennars.inference.TemporalRules;
import org.opennars.io.Symbols.NativeOperator;

import java.util.Arrays;

/**
 * A Statement about an Equivalence relation as defined in the NARS-theory
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class Equivalence extends Statement {

    private int temporalOrder = TemporalRules.ORDER_NONE;

    /**
     * Constructor with partial values, called by make
     *
     * @param components The component list of the term
     */
    private Equivalence(final Term[] components, final int order) {
        super(components);
        
        temporalOrder = order;
        
        init(components);
    }

    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Equivalence clone() {
        return new Equivalence(term, temporalOrder);
    }
    
    @Override public Equivalence clone(final Term[] t) {  
        if(t == null) {
            return null;
        }
        if (t.length!=2)
            throw new IllegalStateException("Equivalence requires 2 components: " + Arrays.toString(t));
        
        return make(t[0], t[1], temporalOrder);
    }
    
    /** alternate version of Inheritance.make that allows equivalent subject and predicate
     * to be reduced to the common term.      */
    public static Term makeTerm(final Term subject, final Term predicate, final int temporalOrder) {
        if (subject.equals(predicate))
            return subject;                
        return make(subject, predicate, temporalOrder);        
    }    


    /**
     * Try to make a new compound from two term. Called by the inference
     * rules.
     *
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or null
     */
    public static Equivalence make(final Term subject, final Term predicate) {  // to be extended to check if subject is Conjunction
        return make(subject, predicate, TemporalRules.ORDER_NONE);
    }

    public static Equivalence make(Term subject, Term predicate, int temporalOrder) {  // to be extended to check if subject is Conjunction
        if (invalidStatement(subject, predicate) && temporalOrder != TemporalRules.ORDER_FORWARD && temporalOrder != TemporalRules.ORDER_CONCURRENT) {
            return null;
        }
        
        if ((subject instanceof Implication) || (subject instanceof Equivalence)
                || (predicate instanceof Implication) || (predicate instanceof Equivalence) ||
                (subject instanceof Interval) || (predicate instanceof Interval)) {
            return null;
        }
                
        if ((temporalOrder == TemporalRules.ORDER_BACKWARD)
                || ((subject.compareTo(predicate) > 0) && (temporalOrder != TemporalRules.ORDER_FORWARD))) {
            final Term interm = subject;
            subject = predicate;
            predicate = interm;
        }
        
        final NativeOperator copula;
        switch (temporalOrder) {
            case TemporalRules.ORDER_BACKWARD:
                temporalOrder = TemporalRules.ORDER_FORWARD;
                //TODO determine if this missing break is intended
            case TemporalRules.ORDER_FORWARD:
                copula = NativeOperator.EQUIVALENCE_AFTER;
                break;
            case TemporalRules.ORDER_CONCURRENT:
                copula = NativeOperator.EQUIVALENCE_WHEN;
                break;
            default:
                copula = NativeOperator.EQUIVALENCE;
        }
        final Term[] t;
        if (temporalOrder==TemporalRules.ORDER_FORWARD)
            t = new Term[] { subject, predicate };
        else
            t = Term.toSortedSetArray(subject, predicate);
       
        if (t.length != 2)
            return null;        
        return new Equivalence(t, temporalOrder);
    }

    /**
     * Get the operator of the term.
     *
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        switch (temporalOrder) {
            case TemporalRules.ORDER_FORWARD:
                return NativeOperator.EQUIVALENCE_AFTER;
            case TemporalRules.ORDER_CONCURRENT:
                return NativeOperator.EQUIVALENCE_WHEN;
        }
        return NativeOperator.EQUIVALENCE;
    }

    /**
     * Check if the compound is commutative.
     *
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return (temporalOrder != TemporalRules.ORDER_FORWARD);
    }

    @Override
    public int getTemporalOrder() {
        return temporalOrder;
    }
}
