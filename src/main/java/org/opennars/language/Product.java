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

import java.util.List;

/**
 * A Product is a sequence of 1 or more terms.
 */
public class Product extends CompoundTerm {
    
    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    public Product(final Term... arg) {
        super(arg);
        
        init(arg);
    }

    public Product(final List<Term> x) {
        this(x.toArray(new Term[0]));
    }
    
    public static Product make(final Term... arg) {
        return new Product(arg);
    }   
    
    /**
     * Clone a Product
     * @return A new object, to be casted into an ImageExt
     */
    @Override
    public Product clone() {
        return new Product(term);
    }

    @Override
    public CompoundTerm clone(final Term[] replaced) {
        return new Product(replaced);
    }

    

        
    /**
     * Try to make a Product from an ImageExt/ImageInt and a component. Called by the inference rules.
     * @param image The existing Image
     * @param component The component to be added into the component list
     * @param index The index of the place-holder in the new Image -- optional parameter
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final CompoundTerm image, final Term component, final int index) {
        final Term[] argument = image.cloneTerms();
        argument[index] = component;
        return new Product(argument);
    }
    
    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.PRODUCT;
    }



    
}
