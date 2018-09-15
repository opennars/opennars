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

import java.util.Arrays;

/**
 * An extension image as defined in the NARS-theory
 *
 * <p>
 * B --&gt; (/,P,A,_)) iff (*,A,B) --$gt; P
 * <p>
 * Internally, it is actually (/,A,P)_1, with an index.
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class ImageExt extends Image {


    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     * @param index The index of relation in the component list
     */
    public ImageExt(final Term[] arg, final short index) {
        super(arg, index);
    }


    /**
     * Clone an object
     * @return A new object, to be casted into an ImageExt
     */
    @Override
    public ImageExt clone() {
        return new ImageExt(term, relationIndex);
    }
    @Override
    public Term clone(final Term[] replaced) {
        if(replaced == null) {
            return null;
        }
        if (replaced.length != term.length)
            throw new IllegalStateException("Replaced terms not the same amount as existing terms (" + term.length + "): " + Arrays.toString(replaced));
        
        return new ImageExt(replaced, relationIndex);
    }
    

    
    /**
     * Try to make a new ImageExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param argList The list of term
     */
    public static Term make(final Term[] argList) {
        if (argList.length < 2) {
            return argList[0];
        }
        final Term relation = argList[0];
        final Term[] argument = new Term[argList.length-1];
        int index = 0, n = 0;
        for (int j = 1; j < argList.length; j++) {
            if (isPlaceHolder(argList[j])) {
                index = j - 1;
                argument[n] = relation;
            } else {
                argument[n] =  argList[j];
            }
            n++;
        }
        return new ImageExt(argument, (short) index);
    }

    /**
     * Try to make an Image from a Product and a relation. Called by the inference rules.
     * @param product The product
     * @param relation The relation
     * @param index The index of the place-holder
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Product product, final Term relation, final short index) {
        if (relation instanceof Product) {
            final Product p2 = (Product) relation;
            if ((product.size() == 2) && (p2.size() == 2)) {
                if ((index == 0) && product.term[1].equals(p2.term[1])) { // (/,_,(*,a,b),b) is reduced to a
                    return p2.term[0];
                }
                if ((index == 1) && product.term[0].equals(p2.term[0])) { // (/,(*,a,b),a,_) is reduced to b
                    return p2.term[1];
                }
            }
        }
        final Term[] argument = product.cloneTerms(); //TODO is this clone needed?
        argument[index] = relation;
        return new ImageExt(argument, index);
    }

    /**
     * Try to make an Image from an existing Image and a component. Called by the inference rules.
     * @param oldImage The existing Image
     * @param component The component to be added into the component list
     * @param index The index of the place-holder in the new Image
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final ImageExt oldImage, final Term component, final short index) {
        final Term[] argList = oldImage.cloneTerms();
        final int oldIndex = oldImage.relationIndex;
        final Term relation = argList[oldIndex];
        argList[oldIndex] = component;
        argList[index] = relation;
        return new ImageExt(argList, index);
    }



    /**
     * get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.IMAGE_EXT;
    }
}
