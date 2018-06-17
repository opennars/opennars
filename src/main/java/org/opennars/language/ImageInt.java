/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.language;

import org.opennars.io.Symbols.NativeOperator;

import java.util.Arrays;

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
    protected ImageInt(final Term[] arg, final short index) {
        super(arg, index);
    }

    
    /**
     * Clone an object
     * @return A new object, to be casted into an ImageInt
     */
    @Override
    public ImageInt clone() {
        return new ImageInt(term, relationIndex);
    }

    @Override
    public Term clone(final Term[] replaced) {
        if (replaced.length != term.length)
            throw new IllegalStateException("Replaced terms not the same amount as existing terms (" + term.length + "): " + Arrays.toString(replaced));
        
        return new ImageInt(replaced, relationIndex);
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
                argument[n] = argList[j];
            }
            n++;
        }
        return make(argument, (short) index);
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
                if ((index == 0) && product.term[1].equals(p2.term[1])) {// (\,_,(*,a,b),b) is reduced to a
                    return p2.term[0];
                }
                if ((index == 1) && product.term[0].equals(p2.term[0])) {// (\,(*,a,b),a,_) is reduced to b
                    return p2.term[1];
                }
            }
        }
        final Term[] argument = product.cloneTerms(); //TODO is this clone needed?
        argument[index] = relation;
        return make(argument, index);
    }

    /**
     * Try to make an Image from an existing Image and a component. Called by the inference rules.
     * @param oldImage The existing Image
     * @param component The component to be added into the component list
     * @param index The index of the place-holder in the new Image
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final ImageInt oldImage, final Term component, final short index) {
        final Term[] argList = oldImage.cloneTerms();
        final int oldIndex = oldImage.relationIndex;
        final Term relation = argList[oldIndex];
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
    public static ImageInt make(final Term[] argument, final short index) {        
        return new ImageInt(argument, index);
    }
    

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.IMAGE_INT;
    }
}
