/*
 * ImageInt.java
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
package nars.nal.nal4;

import nars.Op;
import nars.term.Term;


/**
 * An intension image.
 * <p>
 * (\,P,A,_)) --> B iff P --> (*,A,B)
 * <p>
 * Internally, it is actually (\,A,P)_1, with an index.
 */
public class ImageInt extends Image {

    /**
     * constructor with partial values, called by make
     * @param arg The component list of the term
     * @param index The index of relation in the component list
     */
    public ImageInt(Term[] arg, int index) {
        super(arg, index);
    }


    /**
     * Clone an object
     * @return A new object, to be casted into an ImageInt
     */
    @Override
    public ImageInt clone() {
        return new ImageInt(terms.term, relationIndex);
    }

    @Override
    public Term clone(Term[] replaced) {
        if ((replaced.length != size())
                || Image.hasPlaceHolder(replaced)) //TODO indexOfPlaceHolder
            return Image.makeInt(replaced);

//        if (replaced.length != size())
//            //return null;
//            throw new RuntimeException("Replaced terms not the same amount as existing terms (" + terms().length + "): " + Arrays.toString(replaced));


        return new ImageInt(replaced, relationIndex);
    }

    /**
     * Try to make an Image from a Product and a relation. Called by the logic rules.
     * @param product The product
     * @param relation The relation
     * @param index The index of the place-holder
     * @return A compound generated or a term it reduced to
     */
    public static Term make(Product product, Term relation, short index) {
        if (relation instanceof Product) {
            Product p2 = (Product) relation;
            if ((product.size() == 2) && (p2.size() == 2)) {
                if ((index == 0) && product.term(1).equals(p2.term(1))) {// (\,_,(*,a,b),b) is reduced to a
                    return p2.term(0);
                }
                if ((index == 1) && product.term(0).equals(p2.term(0))) {// (\,(*,a,b),a,_) is reduced to b
                    return p2.term(1);
                }
            }
        }

        Term[] argument = product.termsCopy(); //shallow clone necessary because the index argument is replaced
        argument[index] = relation;
        return make(argument, index);
    }

    /**
     * Try to make an Image from an existing Image and a component. Called by the logic rules.
     * @param oldImage The existing Image
     * @param component The component to be added into the component list
     * @param index The index of the place-holder in the new Image
     * @return A compound generated or a term it reduced to
     */
    public static Term make(ImageInt oldImage, Term component, short index) {
        Term[] argList = oldImage.termsCopy();
        int oldIndex = oldImage.relationIndex;
        Term relation = argList[oldIndex];
        argList[oldIndex] = component;
        argList[index] = relation;
        return make(argList, index);
    }

    /**
     * Try to make a new compound from a set of term. Called by the public make methods.
     * @param argument The argument list
     * @param index The index of the place-holder in the new Image
     * @return the Term generated from the arguments
     */
    public static ImageInt make(Term[] argument, int index) {
        return new ImageInt(argument, index);
    }
    

    /**
     * Get the operate of the term.
     * @return the operate of the term
     */
    @Override
    public Op op() {
        return Op.IMAGE_INT;
    }
}
