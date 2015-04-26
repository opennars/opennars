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

package nars.nal.nal4;

import nars.nal.NALOperator;
import nars.nal.term.Compound;
import nars.nal.term.DefaultCompound;
import nars.nal.term.Term;

import java.util.List;

/**
 * A Product is a sequence of 1 or more terms.
 */
public class Product extends DefaultCompound {
    
    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    public Product(final Term... arg) {
        super(arg);
        
        init(arg);
    }

    public Product(final List<Term> x) {
        this(x.toArray(new Term[x.size()]));
    }

    public static Product make(final Term[] pre, final Term... suf) {
        final int pLen = pre.length;
        final int sLen = suf.length;
        Term[] x = new Term[pLen + suf.length];
        System.arraycopy(pre, 0, x, 0, pLen);
        System.arraycopy(suf, 0, x, pLen, sLen);
        return new Product(x);
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
    public Compound clone(Term[] replaced) {
        return new Product(replaced);
    }

    

        
    /**
     * Try to make a Product from an ImageExt/ImageInt and a component. Called by the logic rules.
     * @param image The existing Image
     * @param component The component to be added into the component list
     * @param index The index of the place-holder in the new Image -- optional parameter
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Compound image, final Term component, final int index) {
        Term[] argument = image.cloneTerms();
        argument[index] = component;
        return new Product(argument);
    }
    
    /**
     * Get the operate of the term.
     * @return the operate of the term
     */
    @Override
    public NALOperator operator() {
        return NALOperator.PRODUCT;
    }


    @Override
    public Object first() {
        return term[0];
    }


}
