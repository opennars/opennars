/*
 * The MIT License
 *
 * Copyright 2019 The OpenNARS authors.
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
package org.opennars.inference;

import org.opennars.language.Conjunction;
import org.opennars.language.Implication;
import org.opennars.language.Term;

/**
 * helper methods for the deriver
 */
public class DeriverHelpers {
    /**
     *
     * @param termCode code of the type of the term, can be copula if it is a statement
     * @param left
     * @param right
     * @return
     */
    public static Term makeBinary(String termCode, Term left, Term right) {
        if (termCode.equals("&/")) {
            return Conjunction.make(new Term[]{left, right}, TemporalRules.ORDER_FORWARD);
        }
        else if(termCode.equals("=/>")) {
            return new Implication(new Term[]{left, right}, TemporalRules.ORDER_FORWARD);
        }
        else {
            throw new RuntimeException("NOT IMPLEMENTED!"); // TODO< chose an exception which we can throw >
        }
    }

    public static Term make(String termCode, Term... args) {
        if (termCode.equals("&/")) {
            return Conjunction.make(args, TemporalRules.ORDER_FORWARD);
        }
        else {
            throw new RuntimeException("NOT IMPLEMENTED!"); // TODO< chose an exception which we can throw >
        }
    }
}
