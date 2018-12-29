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
import org.opennars.io.Symbols.NativeOperator;

import java.util.Objects;

import static org.opennars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static org.opennars.io.Symbols.NativeOperator.COMPOUND_TERM_OPENER;

/**
 * Generalization of Images as defined in the NARS-theory
 *
 */
abstract public class Image extends CompoundTerm {
    /** The index of relation in the component list */
    public final short relationIndex;

    protected Image(final Term[] components, final short relationIndex) {
        super(components);
        
        this.relationIndex = relationIndex;
                
        init(components);
    }

    @Override
    protected void init(final Term[] components) {
        super.init(components);
        this.hash = Objects.hash(super.hashCode(), relationIndex); 
    }
    
    @Override
    public int compareTo(final AbstractTerm that) {
        if (that instanceof Image) {
            final int r = relationIndex - ((Image)that).relationIndex;
            if (r!=0)
                return r;            
        }
        return super.compareTo(that);
    }

    //TODO replace with a special Term type
    public static boolean isPlaceHolder(final Term t) {
        if (t.getClass() != Term.class) return false;
        final CharSequence n = t.name();
        if (n.length() != 1) return false;
        return n.charAt(0) == Symbols.IMAGE_PLACE_HOLDER;
    }    
    
   /**
     * default method to make the oldName of an image term from given fields
     *
     * @param op the term operator
     * @param arg the list of term
     * @param relationIndex the location of the place holder
     * @return the oldName of the term
     */
    protected static String makeImageName(final NativeOperator op, final Term[] arg, final int relationIndex) {
        final int sizeEstimate = 12 * arg.length + 2;
        
        final StringBuilder name = new StringBuilder(sizeEstimate)
            .append(COMPOUND_TERM_OPENER.ch)
            .append(op)
            .append(Symbols.ARGUMENT_SEPARATOR)
            .append(arg[relationIndex].name());
        
        for (int i = 0; i < arg.length; i++) {
            name.append(Symbols.ARGUMENT_SEPARATOR);
            if (i == relationIndex) {
                name.append(Symbols.IMAGE_PLACE_HOLDER);                
            } else {
                name.append(arg[i].name());
            }
        }
        name.append(COMPOUND_TERM_CLOSER.ch);
        return name.toString();
    }    
    
    /**
     * Get the other term in the Image
     * @return The term relaterom existing fields
     * @return the name of the term
     */
    @Override
    public CharSequence makeName() {
        return makeImageName(operator(), term, relationIndex);
    }

    /**
     * Get the relation term in the Image
     * @return The term representing a relation
     */
    public Term getRelation() {
        return term[relationIndex];
    }

    /**
     * Get the other term in the Image
     * @return The term related
     */
    public Term getTheOtherComponent() {
        if (term.length != 2) {
            return null;
        }
        return (relationIndex == 0) ? term[1] : term[0];
    }
}

