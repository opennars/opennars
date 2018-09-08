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

import java.util.NavigableSet;
import java.util.TreeSet;

import static org.opennars.language.DifferenceInt.ensureValidDifferenceArguments;

/**
 * A compound term whose extension is the difference of the extensions of its term as defined in the NARS-theory
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class DifferenceExt extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    private DifferenceExt(final Term[] arg) {
        super(arg);
        
        ensureValidDifferenceArguments(arg);        
        
        init(arg);
    }


    /**
     * Clone an object
     * @return A new object, to be casted into a DifferenceExt
     */
    @Override
    public DifferenceExt clone() {
        return new DifferenceExt(term);
    }

    @Override public Term clone(final Term[] replaced) {
        if(replaced == null) {
            return null;
        }
        return make(replaced);
    }

    
    /**
     * Try to make a new DifferenceExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param arg The list of term
     */
    public static Term make(final Term[] arg) {
        if (arg.length == 1) { // special case from CompoundTerm.reduceComponent
            return arg[0];
        }
        if (arg.length  != 2) {
            return null;
        }
        if ((arg[0] instanceof SetExt) && (arg[1] instanceof SetExt)) {
            //TODO maybe a faster way to do this operation:
            final NavigableSet<Term> set = new TreeSet<>(((CompoundTerm) arg[0]).asTermList());
            set.removeAll(((CompoundTerm) arg[1]).asTermList());           // set difference
            return SetExt.make(set);
        }                
        
        if (arg[0].equals(arg[1])) {
            return null;
        }
        
        return new DifferenceExt(arg);
    }

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param t1 The first component
     * @param t2 The second component
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term t1, final Term t2) {
        if (t1.equals(t2))
            return null;

        return make(new Term[]{t1,t2});
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.DIFFERENCE_EXT;
    }
}
