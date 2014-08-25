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
package nars.operator;

import nars.io.Symbols;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_OPENER;
import nars.language.CompoundTerm;
import nars.language.Inheritance;
import nars.language.Product;
import nars.language.Term;
import nars.storage.Memory;

/**
 * An operation is interpreted as an Inheritance relation.
 */
public class Operation extends Inheritance {

    /**
     * Constructor with partial values, called by make
     *
     * @param n The name of the term
     * @param arg The component list of the term
     */
    public Operation(CharSequence name, Term[] arg) {
        super(name, arg);
    }

    /**
     * Constructor with full values, called by clone
     *
     * @param n The name of the term
     * @param cs Component list
     * @param con Whether the term is a constant
     * @param complexity Syntactic complexity of the compound
     */
    protected Operation(final CharSequence n, final Term[] cs, final boolean con, final boolean hasVar, final short complexity) {
        super(n, cs, con, hasVar, complexity);
    }

    /**
     * Clone an object
     *
     * @return A new object, to be casted into a SetExt
     */
    @Override
    public Operation clone() {        
        return new Operation(name(), cloneTerms(), isConstant(), containVar(), getComplexity());
    }

    
    
    
 
   
    /**
     * Try to make a new compound from two components. Called by the inference
     * rules.
     *
     * @param memory Reference to the memory
     * @param addSelf include SELF term at end of product terms
     * @return A compound generated or null
     */
    public static Operation make(Operator oper, final Term[] arg, boolean addSelf, final Memory memory) {        
        
        addSelf = false; //FALSE untli understand when SELF should apply
        
        
        if (oper == null) {
            return null;
        }
        
        CharSequence name = makeName(oper.name(), arg);
        Term t = memory.conceptTerm(name);
        if (t != null) {
            return (Operation) t;
        }
        
        Term productArg[];
        if (addSelf) {            
            productArg = new Term[arg.length+1];
            System.arraycopy(arg, 0, productArg, 0, arg.length);
            productArg[arg.length] = memory.self;
        }
        else {
            productArg = arg;
        }        
                
        return new Operation(name, 
                termArray(Product.make(productArg, memory), oper)
        );
        
    }

    @Override
    protected CharSequence makeName() {
        if(getSubject() instanceof Product && getPredicate() instanceof Operator)
            return makeName(getPredicate().name(), ((Product)getSubject()).term);
        return makeStatementName(getSubject(), Symbols.NativeOperator.INHERITANCE, getPredicate());
    }

    
    public static CharSequence makeName(final CharSequence op, Term[] arg) {
        final StringBuilder nameBuilder = new StringBuilder(16) //estimate
                .append(COMPOUND_TERM_OPENER.ch).append(op);
        
        for (final Term t : arg) {
            nameBuilder.append(Symbols.ARGUMENT_SEPARATOR);
            nameBuilder.append(t.name());
        }
        
        nameBuilder.append(COMPOUND_TERM_CLOSER.ch);
        return nameBuilder.toString();
    }
    
    
    /*public Operator getOperator() {
        return (Operator) getPredicate();
    }
    
    public Term[] getArguments() {
        return ((CompoundTerm) getSubject()).term;
    }*/
    
}
