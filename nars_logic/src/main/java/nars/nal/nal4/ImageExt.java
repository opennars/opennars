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
package nars.nal.nal4;

import nars.Op;
import nars.term.Term;


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
     * @param arg The component list of the term
     * @param index The index of relation in the component list
     */
    public ImageExt(Term[] arg, int index) {
        super(arg, index);
    }




    /**
     * Clone an object
     * @return A new object, to be casted into an ImageExt
     */
    @Override
    public ImageExt clone() {
        return new ImageExt(terms.term, relationIndex);
    }
    @Override
    public Term clone(Term[] replaced) {
        if ((replaced.length != size())
                || Image.hasPlaceHolder(replaced)) //TODO indexOfPlaceHolder
            return Image.makeExt(replaced);

//        if (replaced.length != size())
//            //return null;
//            throw new RuntimeException("Replaced terms not the same amount as existing terms (" + terms().length + "): " + Arrays.toString(replaced));

        return new ImageExt(replaced, relationIndex);
    }
    


    /**
     * Try to make an Image from an existing Image and a component. Called by the logic rules.
     * @param oldImage The existing Image
     * @param component The component to be added into the component list
     * @param index The index of the place-holder in the new Image
     * @return A compound generated or a term it reduced to
     */
    public static Term make(ImageExt oldImage, Term component, short index) {
        Term[] argList = oldImage.termsCopy();
        int oldIndex = oldImage.relationIndex;
        Term relation = argList[oldIndex];
        argList[oldIndex] = component;
        argList[index] = relation;
        return new ImageExt(argList, index);
    }



    /**
     * get the operate of the term.
     * @return the operate of the term
     */
    @Override
    public final Op op() {
        return Op.IMAGE_EXT;
    }
}
