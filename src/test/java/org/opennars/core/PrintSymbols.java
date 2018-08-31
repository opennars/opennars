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

import org.opennars.io.Symbols;
import org.opennars.io.Symbols.NativeOperator;/**
 *
 * @author me
 */


public class PrintSymbols {

    public static void main(final String[] args) {
        int relations = 0;
        int innates = 0;
        int symbols = 0;
        
        System.out.println("string" + "\t\t" + "rel?" + "\t\t" + "innate?" + "\t\t" + "opener?" + "\t\t" + "closer?");
        for (final NativeOperator i : Symbols.NativeOperator.values()) {
            System.out.println(i.symbol + "\t\t" + i.relation + "\t\t" + i.isNative + "\t\t" + i.opener + "\t\t" + i.closer); 
            if (i.relation) relations++;
            if (i.isNative) innates++;
            symbols++;
        }
        System.out.println("symbols=" + symbols + ", relations=" + relations + ", innates=" + innates);
    }
}
