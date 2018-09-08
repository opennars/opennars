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
package org.opennars.core;

import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import org.opennars.entity.Stamp.BaseEntry;
import static org.opennars.entity.Stamp.toSetArray;

/**
 * Tests the correct functionality of stamps
 *
 */
public class TestStamp {
    private long narid = 0;
    BaseEntry entry(long inputId) {
        return new BaseEntry(narid, inputId);
    }
    @Test 
    public void testStampToSetArray() {
        
        assertTrue(toSetArray(new BaseEntry[] { entry(1), entry(2), entry(3) }).length == 3);        
        assertTrue(toSetArray(new BaseEntry[] { entry(1), entry(1), entry(3) }).length == 2);
        assertTrue(toSetArray(new BaseEntry[] { entry(1) }).length == 1);
        assertTrue(toSetArray(new BaseEntry[] {  }).length == 0);
        assertTrue(
                Arrays.hashCode(toSetArray(new BaseEntry[] { entry(3),entry(2),entry(1) }))
                ==
                Arrays.hashCode(toSetArray(new BaseEntry[] { entry(2),entry(3),entry(1) }))
        );
        assertTrue(
                Arrays.hashCode(toSetArray(new BaseEntry[] { entry(1),entry(2),entry(3) }))
                !=
                Arrays.hashCode(toSetArray(new BaseEntry[] { entry(1),entry(1),entry(3) }))
        );    
    }
}
