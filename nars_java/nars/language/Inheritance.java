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

import java.util.ArrayList;

import nars.io.Symbols.InnateOperator;
import nars.operation.Operation;
import nars.operation.Operator;
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
    protected Inheritance(final ArrayList<Term> arg) {
        super(arg);
    }

    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param i Syntactic complexity of the compound
     */
    protected Inheritance(final String n, final ArrayList<Term> cs, final boolean con, final boolean hasVar, final short i, final int nameHash) {
        super(n, cs, con, hasVar, i, nameHash);
    }

    /**
     * Clone an object
     * @return A new object, to be casted into a SetExt
     */
    @Override public Object clone() {
        return new Inheritance(getName(), cloneList(components), isConstant(), containVar(), complexity, hashCode());
    }

    /**
     * Try to make a new compound from two components. Called by the inference rules.
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
        
        String name;
        if ((subject instanceof Product) && (predicate instanceof Operator)) {
            name = Operation.makeName(predicate.getName(), ((CompoundTerm) subject).getComponents(), memory);
        } else {
            name = makeStatementName(subject, InnateOperator.INHERITANCE, predicate);
        }
 
        Term t = memory.nameToTerm(name);
        if (t != null) {
            return (Inheritance) t;
        }
        
        ArrayList<Term> argument = argumentsToList(subject, predicate);
        
        if ((subject instanceof Product) && (predicate instanceof Operator)) {
            return new Operation(argument);
        } else {
            return new Inheritance(argument);
        }
         
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    public InnateOperator operator() {
        return InnateOperator.INHERITANCE;
    }

}

