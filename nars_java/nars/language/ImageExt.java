/*
 * ImageExt.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.language;

import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import nars.storage.Memory;

/**
 * An extension image.
 * <p>
 * B --> (/,P,A,_)) iff (*,A,B) --> P
 * <p>
 * Internally, it is actually (/,A,P)_1, with an index.
 */
public class ImageExt extends Image {


    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     * @param index The index of relation in the component list
     */
    private ImageExt(final CharSequence n, final Term[] arg, final short index) {
        super(n, arg, index);
    }

    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param complexity Syntactic complexity of the compound
     * @param index The index of relation in the component list
     */
    protected ImageExt(final CharSequence n, final Term[] cs, final boolean con, final short complexity, final short index) {
        super(n, cs, con, complexity, index);
    }

    /**
     * Clone an object
     * @return A new object, to be casted into an ImageExt
     */
    @Override
    public ImageExt clone() {
        return new ImageExt(name(), cloneTerms(), isConstant(), complexity, relationIndex);
    }

    

    
    
    /**
     * Try to make a new ImageExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param argList The list of term
     * @param memory Reference to the memory
     */
    public static Term make(Term[] argList, Memory memory) {
        if (argList.length < 2) {
            return null;
        }
        Term relation = argList[0];
        Term[] argument = new Term[argList.length-1];
        int index = 0, n = 0;
        for (int j = 1; j < argList.length; j++) {
            if (argList[j].name().charAt(0) == Symbols.IMAGE_PLACE_HOLDER) {
                index = j - 1;
                argument[n] = relation;
            } else {
                argument[n] =  argList[j];
            }
            n++;
        }
        return make(argument, (short) index, memory);
    }

    /**
     * Try to make an Image from a Product and a relation. Called by the inference rules.
     * @param product The product
     * @param relation The relation
     * @param index The index of the place-holder
     * @return A compound generated or a term it reduced to
     */
    public static Term make(Product product, Term relation, short index, Memory memory) {
        if (relation instanceof Product) {
            Product p2 = (Product) relation;
            if ((product.size() == 2) && (p2.size() == 2)) {
                if ((index == 0) && product.term[1].equals(p2.term[1])) { // (/,_,(*,a,b),b) is reduced to a
                    return p2.term[0];
                }
                if ((index == 1) && product.term[0].equals(p2.term[0])) { // (/,(*,a,b),a,_) is reduced to b
                    return p2.term[1];
                }
            }
        }
        Term[] argument = product.cloneTerms();
        argument[index] = relation;
        return make(argument, index, memory);
    }

    /**
     * Try to make an Image from an existing Image and a component. Called by the inference rules.
     * @param oldImage The existing Image
     * @param component The component to be added into the component list
     * @param index The index of the place-holder in the new Image
     * @return A compound generated or a term it reduced to
     */
    public static Term make(ImageExt oldImage, Term component, short index, Memory memory) {
        Term[] argList = oldImage.cloneTerms();
        int oldIndex = oldImage.relationIndex;
        Term relation = argList[oldIndex];
        argList[oldIndex] = component;
        argList[index] = relation;
        return make(argList, index, memory);
    }

    /**
     * Try to make a new compound from a set of term. Called by the public make methods.
     * @param argument The argument list
     * @param index The index of the place-holder in the new Image
     * @return the Term generated from the arguments
     */
    public static Term make(final Term[] argument, final short index, final Memory memory) {
        CharSequence name = makeImageName(NativeOperator.IMAGE_EXT, argument, index);
        Term t = memory.conceptTerm(name);
        return (t != null) ? t : new ImageExt(name, argument, index);
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
