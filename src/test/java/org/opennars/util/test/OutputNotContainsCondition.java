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
package org.opennars.util.test;

import org.opennars.main.Nar;

/**
 *
 * @author me
 */
public class OutputNotContainsCondition extends OutputContainsCondition {

    public OutputNotContainsCondition(final Nar nar, final String containing) {
        super(nar, containing, -1);
        succeeded = true;
    }

    @Override
    public String getFalseReason() {
        return "incorrect output: " + containing;
    }

    @Override
    public boolean condition(final Class channel, final Object signal) {
        if (!succeeded) {
            return false;
        }
        if (cond(channel, signal)) {
            onFailure(channel, signal);
            succeeded = false;
            return false;
        }
        return true;
    }

    public boolean isInverse() {
        return true;
    }

    protected void onFailure(final Class channel, final Object signal) {
    }
    
}
