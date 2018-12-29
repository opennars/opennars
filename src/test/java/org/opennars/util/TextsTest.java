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
package org.opennars.util;

import org.junit.Test;
import org.opennars.io.Texts;

import static org.junit.Assert.assertEquals;


public class TextsTest {
 

    @Test
    public void testN2() {
        assertEquals("1.00", Texts.n2(1.00f).toString());
        assertEquals("0.50", Texts.n2(0.5f).toString());
        assertEquals("0.09", Texts.n2(0.09f).toString());
        assertEquals("0.10", Texts.n2(0.1f).toString());
        assertEquals("0.01", Texts.n2(0.009f).toString());
        assertEquals("0.00", Texts.n2(0.001f).toString());
        assertEquals("0.01", Texts.n2(0.01f).toString());
        assertEquals("0.00", Texts.n2(0f).toString());
    }
}
