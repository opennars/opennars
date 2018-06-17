/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.language;

import org.opennars.inference.TemporalRules;
import org.opennars.io.Symbols.NativeOperator;

import java.util.Arrays;

/**
 * A Statement about an Equivalence relation.
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
