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

import java.util.Collection;

import static org.opennars.io.Symbols.NativeOperator.SET_INT_CLOSER;
import static org.opennars.io.Symbols.NativeOperator.SET_INT_OPENER;

/**
 * An intensionally defined set, which contains one or more instances defining the Term.
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class SetInt extends SetTensional {

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term - args must be unique and sorted
     */
    public SetInt(final Term... arg) {
        super(arg);
    }
 



    /**
     * Clone a SetInt
     * @return A new object, to be casted into a SetInt
     */
    @Override
    public SetInt clone() {
        return new SetInt(term);
    }

    @Override public SetInt clone(final Term[] replaced) {
        if(replaced == null) {
            return null;
        }
        return make(replaced);
    }

    public static SetInt make(final Collection<Term> l) {
        return make(l.toArray(new Term[0]));
    }    
    
    public static SetInt make(Term... t) {
        t = Term.toSortedSetArray(t);
        if (t.length == 0) return null;
        return new SetInt(t);
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.SET_INT_OPENER;
    }


    /**
     * Make a String representation of the set, override the default.
     * @return true for communitative
     */
    @Override
    public CharSequence makeName() {
        return makeSetName(SET_INT_OPENER.ch, term, SET_INT_CLOSER.ch);
    }
    
}

