/*
 * Product.java
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

import nars.io.Symbols.NativeOperator;

import java.util.List;

/**
 * A Product is a sequence of 1 or more terms.
 */
public class Product extends CompoundTerm {
    
    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    public Product(final Term... arg) {
        super(arg);
        
        init(arg);
    }

    public Product(final List<Term> x) {
        this(x.toArray(new Term[x.size()]));
    }
    
    public static Product make(final Term... arg) {
        return new Product(arg);
    }
    

    
    @Override
    public int getMinimumRequiredComponents() {
        return 1;
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
    public CompoundTerm clone(Term[] replaced) {
        return new Product(replaced);
    }

    

        
    /**
     * Try to make a Product from an ImageExt/ImageInt and a component. Called by the inference rules.
     * @param image The existing Image
     * @param component The component to be added into the component list
     * @param index The index of the place-holder in the new Image -- optional parameter
     * @param memory Reference to the memeory
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final CompoundTerm image, final Term component, final int index) {
        Term[] argument = image.cloneTerms();
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
