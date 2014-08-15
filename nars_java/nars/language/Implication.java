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
package nars.language;

import nars.inference.TemporalRules;
import nars.io.Symbols.NativeOperator;
import nars.storage.Memory;

/**
 * A Statement about an Inheritance copula.
 */
public class Implication extends Statement {
    private int temporalOrder = TemporalRules.ORDER_NONE;

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    private Implication(CharSequence name, Term[] arg, int order) {
        super(name, arg);
        temporalOrder = order;
    }

    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param con Whether it is a constant term
     * @param i Syntactic complexity of the compound
     */
    private Implication(CharSequence n, Term[] cs, boolean con, boolean hasVar, short i, int order) {
        super(n, cs, con, hasVar, i);
        temporalOrder = order;
    }

    
    
    /**
     * Clone an object
     * @return A new object
     */
    @Override
    public Implication clone() {
        return new Implication(name(), cloneTerms(), isConstant(), containVar(), complexity, temporalOrder);
    }

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param subject The first component
     * @param predicate The second component
     * @param memory Reference to the memory
     * @return A compound generated or a term it reduced to
     */
    public static Implication make(final Term subject, final Term predicate, final Memory memory) {
        return make(subject, predicate, TemporalRules.ORDER_NONE, memory);
    }
    
    public static Implication make(final Term subject, final Term predicate, int temporalOrder, final Memory memory) {
        if ((subject == null) || (predicate == null)) {
            return null;
        }
        if ((subject instanceof Implication) || (subject instanceof Equivalence) || (predicate instanceof Equivalence)) {
            return null;
        }
        if (invalidStatement(subject, predicate)) {
            return null;
        }
        NativeOperator copula;
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
        final CharSequence name = makeStatementName(subject, copula, predicate);
        final Term t = memory.conceptTerm(name);
        if (t != null) {            
            if (t.getClass()!=Implication.class) {                
                throw new RuntimeException("Implication.make"  + ": "+ name + " is not Implication; it is " + t.getClass().getSimpleName() + " = " + t.toString() );
            }
            return (Implication) t;
        }
        if (predicate instanceof Implication) {
            final Term oldCondition = ((Statement) predicate).getSubject();
            if ((oldCondition instanceof Conjunction) && oldCondition.containsTerm(subject)) {
                return null;
            }
            final Term newCondition = Conjunction.make(subject, oldCondition, temporalOrder, memory);
            return make(newCondition, ((Statement) predicate).getPredicate(), temporalOrder, memory);
        } else {
            return new Implication(name, new Term[] { subject, predicate }, temporalOrder);
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
}
