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

import org.apache.commons.lang3.StringUtils;
import org.opennars.language.Term;
import org.opennars.operator.FunctionOperator;
import org.opennars.storage.Memory;

/**
 * Count the number of elements in a set
 */
public class Add extends FunctionOperator {

    public Add() {
        super("^add");
    }

    @Override
    protected Term function(final Memory memory, final Term[] x) {
        if (x.length!= 2) {
            throw new IllegalStateException("Requires 2 arguments");
        }
        
        final int n1;
        final int n2;

        if(StringUtils.isNumeric(x[0].name())) {
            n1 = Integer.parseInt(String.valueOf(x[0].name()));
        } else {
            throw new IllegalArgumentException("1st parameter not an integer");
        }
        
        if( StringUtils.isNumeric((x[1].name()))) {
            n2 = Integer.parseInt(String.valueOf(x[1].name()));
        } else {
            throw new IllegalArgumentException("2nd parameter not an integer");
        }
        
        return new Term(String.valueOf(n1 + n2));            
    }

    @Override
    protected Term getRange() {
        return Term.get("added");
    }
    
}
