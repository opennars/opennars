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
 * A Statement about an Inheritance copula.
 */
public class Implication extends Statement {
    private int temporalOrder = TemporalRules.ORDER_NONE;

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    public Implication(final Term[] arg, final int order) {
        super(arg);
                
        temporalOrder = order;
        
        init(arg);
    }
    
    public Implication(final Term subject, final Term predicate, final int order) {
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
            throw new IllegalStateException("Implication requires 2 components: " + Arrays.toString(t));
        
        return make(t[0], t[1], temporalOrder);
    }
    

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or a term it reduced to
     */
    public static Implication make(final Term subject, final Term predicate) {
        return make(subject, predicate, TemporalRules.ORDER_NONE);
    }

    public static CharSequence makeName(final Term subject, final int temporalOrder, final Term predicate) {
        final NativeOperator copula;
        switch (temporalOrder) {
            case TemporalRules.ORDER_FORWARD:
                copula = NativeOperator.IMPLICATION_AFTER;
                break;
            case TemporalRules.ORDER_CONCURRENT:
                copula = NativeOperator.IMPLICATION_WHEN;
                break;
            case TemporalRules.ORDER_BACKWARD:
                copula = NativeOperator.IMPLICATION_BEFORE;
                break;
            default:
                copula = NativeOperator.IMPLICATION;
        }                
        return makeStatementName(subject, copula, predicate);
    }
    
    public static Implication make(final Term subject, final Term predicate, final int temporalOrder) {
        if (invalidStatement(subject, predicate, temporalOrder != TemporalRules.ORDER_FORWARD && temporalOrder != TemporalRules.ORDER_CONCURRENT)) {
            return null;
        }
        
        if ((subject instanceof Implication) || (subject instanceof Equivalence) || (predicate instanceof Equivalence) ||
                (subject instanceof Interval) || (predicate instanceof Interval)) {
            return null;
        }
        
        //final CharSequence name = makeName(subject, temporalOrder, predicate);         
        if (predicate instanceof Implication) {
            final Term oldCondition = ((Statement) predicate).getSubject();
            if ((oldCondition instanceof Conjunction) && oldCondition.containsTerm(subject)) {
                return null;
            }
            int order = temporalOrder;
            boolean spatial = false;
            if(subject instanceof Conjunction) {
                final Conjunction conj = (Conjunction) subject;
                order = conj.getTemporalOrder();
                spatial = conj.getIsSpatial();
            }
            final Term newCondition = Conjunction.make(subject, oldCondition, order, spatial);
            return make(newCondition, ((Statement) predicate).getPredicate(), temporalOrder);
        } else {
            return new Implication(new Term[] { subject, predicate }, temporalOrder);
        }
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        switch (temporalOrder) {
            case TemporalRules.ORDER_FORWARD:
                return NativeOperator.IMPLICATION_AFTER;
            case TemporalRules.ORDER_CONCURRENT:
                return NativeOperator.IMPLICATION_WHEN;
            case TemporalRules.ORDER_BACKWARD:
                return NativeOperator.IMPLICATION_BEFORE;
        }
        return NativeOperator.IMPLICATION;
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
