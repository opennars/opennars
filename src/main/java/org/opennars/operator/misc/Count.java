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
package org.opennars.operator.misc;

import org.opennars.language.CompoundTerm;
import org.opennars.language.SetExt;
import org.opennars.language.SetInt;
import org.opennars.language.Term;
import org.opennars.operator.FunctionOperator;
import org.opennars.storage.Memory;

/**
 * Count the number of elements in a set
 * 

'INVALID
(^count,a)!
(^count,a,b)!
(^count,a,#b)!

'VALID: 
(^count,[a,b],#b)!

 * 
 */
public class Count extends FunctionOperator {

    public Count() {
        super("^count");
    }

    final static String requireMessage = "Requires 1 SetExt or SetInt argument";
    
    final static Term counted = Term.get("counted");
    
    
    @Override
    protected Term function(final Memory memory, final Term[] x) {
        if (x.length!=1) {
            throw new IllegalStateException(requireMessage);
        }

        final Term content = x[0];
        if (!(content instanceof SetExt) && !(content instanceof SetInt)) {
            throw new IllegalStateException(requireMessage);
        }       
        
        final int n = ((CompoundTerm) content).size();
        return Term.get(n);
    }

    @Override
    protected Term getRange() {
        return counted;
    }


    
}
