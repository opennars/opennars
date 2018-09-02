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

import org.opennars.io.Symbols.NativeOperator;
import org.opennars.main.MiscFlags;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;

import java.util.Arrays;

/**
 * A Statement about an Inheritance relation as defined in the NARS-theory
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class Inheritance extends Statement {

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    protected Inheritance(final Term[] arg) {
        super(arg);  
        
        init(arg);
    }
    
    protected Inheritance(final Term subj, final Term pred) {
        this(new Term[] { subj, pred} );
    }


    /**
     * Clone an object
     * @return A new object, to be casted into a SetExt
     */
    @Override public Inheritance clone() {
        return make(getSubject(), getPredicate());
    }

    @Override public Inheritance clone(final Term[] t) {
        if(t == null) {
            return null;
        }
        if (t.length!=2)
            throw new IllegalArgumentException("Invalid terms for " + getClass().getSimpleName() + ": " + Arrays.toString(t));
                
        return make(t[0], t[1]);
    }

    /** alternate version of Inheritance.make that allows equivalent subject and predicate
     * to be reduced to the common term.      */
    public static Term makeTerm(final Term subject, final Term predicate) {            
        return make(subject, predicate);        
    }

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or null
     */
    public static Inheritance make(final Term subject, final Term predicate) {
                
        if (subject==null || predicate==null || invalidStatement(subject, predicate)) {
            return null;
        }
        
        final boolean subjectProduct = subject instanceof Product;
        final boolean predicateOperator = predicate instanceof Operator;
        
        if (MiscFlags.DEBUG) {
            if (!predicateOperator && predicate.toString().startsWith("^")) {
                throw new IllegalStateException("operator term detected but is not an operator: " + predicate);
            }
        }
        
        if (subjectProduct && predicateOperator) {
            //name = Operation.makeName(predicate.name(), ((CompoundTerm) subject).term);
            return Operation.make((Operator)predicate, ((CompoundTerm)subject).term, true);
        } else {            
            return new Inheritance(subject, predicate);
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

