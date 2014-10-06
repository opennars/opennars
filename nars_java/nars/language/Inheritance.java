/*
 * Inheritance.java
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

import nars.core.Memory;
import nars.io.Symbols.NativeOperator;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * A Statement about an Inheritance relation.
 */
public class Inheritance extends Statement {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    protected Inheritance(final CharSequence name, final Term[] arg) {
        super(name, arg);       
    }

    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param arg Component list
     * @param open Open variable list
     * @param i Syntactic complexity of the compound
     */
    protected Inheritance(final CharSequence n, final Term[] arg, final boolean con, final boolean hasVar, final short i) {
        super(n, arg, con, hasVar, i);
    }

    /**
     * Clone an object
     * @return A new object, to be casted into a SetExt
     */
    @Override public Inheritance clone() {
        return new Inheritance(name(), cloneTerms(), isConstant(), containVar(), getComplexity());
    }

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param subject The first compoment
     * @param predicate The second compoment
     * @param memory Reference to the memory
     * @return A compound generated or null
     */
    public static Inheritance make(final Term subject, final Term predicate) {
        
        if (invalidStatement(subject, predicate)) {
            //throw new RuntimeException("Inheritance.make: Invalid Inheritance statement: subj=" + subject + ", pred=" + predicate);
            return null;
        }
        
        boolean subjectProduct = subject instanceof Product;
        boolean predicateOperator = predicate instanceof Operator;
        
        CharSequence name;
        if (subjectProduct && predicateOperator) {
            //name = Operation.makeName(predicate.name(), ((CompoundTerm) subject).term);
            return Operation.make((Operator)predicate, ((CompoundTerm)subject).term, true);
        } else {
            name = makeStatementName(subject, NativeOperator.INHERITANCE, predicate);
            Term[] arguments = termArray( subject, predicate );
            return new Inheritance(name, arguments);
        }
         
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.INHERITANCE;
    }

}

