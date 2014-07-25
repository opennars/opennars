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

import java.util.*;

import nars.io.Symbols.Operator;
import nars.storage.Memory;

/**
 * A Product is a sequence of 1 or more terms.
 */
public class Product extends CompoundTerm {
    
    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private Product(final ArrayList<Term> arg) {
        super(arg);
    }
    
    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param complexity Syntactic complexity of the compound
     */
    private Product(final String n, final ArrayList<Term> cs, final boolean con, final short complexity) {
        super(n, cs, con, complexity);
    }
    
    @Override
    public int getMinimumRequiredComponents() {
        return 1;
    }    
    
    /**
     * Clone a Product
     * @return A new object, to be casted into an ImageExt
     */
    public Object clone() {
        return new Product(name, cloneList(components), isConstant(), complexity);
    }

     /**
     * Try to make a new compound. Called by StringParser.
     * @return the Term generated from the arguments
     * @param argument The list of components
     * @param memory Reference to the memeory
     */
    public static Term make(final ArrayList<Term> argument, final Memory memory) {
        final String name = makeCompoundName(Operator.PRODUCT, argument);
        final Term t = memory.nameToTerm(name);
        return (t != null) ? t : new Product(argument);
    }
        
    /**
     * Try to make a Product from an ImageExt/ImageInt and a component. Called by the inference rules.
     * @param image The existing Image
     * @param component The component to be added into the component list
     * @param index The index of the place-holder in the new Image -- optional parameter
     * @param memory Reference to the memeory
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final CompoundTerm image, final Term component, final int index, final Memory memory) {
        final ArrayList<Term> argument = image.cloneComponents();
        argument.set(index, component);
        return make(argument, memory);
    }
    
    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public Operator operator() {
        return Operator.PRODUCT;
    }
}
