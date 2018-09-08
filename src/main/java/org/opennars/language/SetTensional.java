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

import org.opennars.io.Symbols;
import org.opennars.main.MiscFlags;

import java.nio.CharBuffer;

/**
 * Base class for SetInt (intensional set) and SetExt (extensional set)
 *
 * @author Patrick Hammer
 */
abstract public class SetTensional extends CompoundTerm {
    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    protected SetTensional(final Term[] arg) {
        super(arg);
        
        if (arg.length == 0)
            throw new IllegalStateException("0-arg empty set");
        
        if (MiscFlags.DEBUG) { Terms.verifySortedAndUnique(arg, true); }
        
        init(arg);
    }
    
    
    /**
     * make the oldName of an ExtensionSet or IntensionSet
     *
     * @param opener the set opener
     * @param closer the set closer
     * @param arg the list of term
     * @return the oldName of the term
     */
    protected static CharSequence makeSetName(final char opener, final Term[] arg, final char closer) {
        int size = 1 + 1 - 1; //opener + closer - 1 [no preceding separator for first element]
        
        for (final Term t : arg) 
            size += 1 + t.name().length();
        
        
        final CharBuffer n = CharBuffer.allocate(size);
        
        n.append(opener);                    
        for (int i = 0; i < arg.length; i++) {
            if (i!=0) n.append(Symbols.ARGUMENT_SEPARATOR);
            n.append(arg[i].name());
        }        
        n.append(closer);
               
        
        return n.compact().toString();
    }
    

    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }    
}
