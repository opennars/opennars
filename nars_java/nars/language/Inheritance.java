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

import nars.io.Symbols.NativeOperator;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.storage.Memory;

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
        return new Inheritance(name(), cloneTerms(), isConstant(), containVar(), complexity);
    }

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param subject The first compoment
     * @param predicate The second compoment
     * @param memory Reference to the memory
     * @return A compound generated or null
     */
    public static Inheritance make(final Term subject, final Term predicate, final Memory memory) {
        
        if (invalidStatement(subject, predicate)) {
            //throw new RuntimeException("Inheritance.make: Invalid Inheritance statement: subj=" + subject + ", pred=" + predicate);
            return null;
        }
        
        CharSequence name;
        if ((subject instanceof Product) && (predicate instanceof Operator)) {
            name = Operation.makeName(predicate.name(), ((CompoundTerm) subject).term);
        } else {
            name = makeStatementName(subject, NativeOperator.INHERITANCE, predicate);
        }
 
        Term t = memory.conceptTerm(name);
        if (t != null) {
            return (Inheritance) t;
        }        
        
        Term[] arguments = termArray( subject, predicate );
        
        if ((subject instanceof Product) && (predicate instanceof Operator)) {
            //return new Operation(name, arguments);
            return Operation.make((Operator)predicate, ((CompoundTerm)subject).term, true, memory);
        } else {
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

