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

/**
 * A negation of a statement as defined in the NARS-theory
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class Negation extends CompoundTerm {



    /** avoid using this externally, because double-negatives can be unwrapped to the 
     * original term using Negation.make */
    protected Negation(final Term t) {
        super(new Term[] { t });
        
        init(term);
    }

    @Override
    protected CharSequence makeName() {
        return makeCompoundName(NativeOperator.NEGATION, term[0]);
    }
    
    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Negation clone() {
        return new Negation(term[0]);
    }

    @Override
    public Term clone(final Term[] replaced) {
        if(replaced == null) {
            return null;
        }
        if (replaced.length!=1)
            return null;
        return make(replaced[0]);
    }
    
    

    /**
     * Try to make a Negation of one component. Called by the inference rules.
     *
     * @param t The component
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term t) {
        if (t instanceof Negation) {
            // (--,(--,P)) = P
            return ((Negation) t).term[0];
        }         
        return new Negation(t);
    }

    /**
     * Try to make a new Negation. Called by StringParser.
     *
     * @return the Term generated from the arguments
     * @param argument The list of term
     */
    public static Term make(final Term[] argument) {
        if (argument.length != 1)
            return null;        
        return make(argument[0]);
    }

    /**
     * Get the operator of the term.
     *
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.NEGATION;
    }

    
    public static boolean areMutuallyInverse(final Term tc, final Term ptc) {
        //doesnt seem necessary to check both, one seems sufficient.
        //incurs cost of creating a Negation and its id
        return (ptc.equals(Negation.make(tc)) /* || tc.equals(Negation.make(ptc))*/ );        
    }

}
