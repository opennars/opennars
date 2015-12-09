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

import nars.$;
import nars.Op;
import nars.Symbols;
import nars.term.Term;
import nars.term.Terms;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;

import java.util.Collection;

/**
 * A Product is a sequence of 1 or more terms.
 */
public interface Product {

    /**
     * universal zero-length product
     */
    Compound Empty = (Compound) GenericCompound.COMPOUND(Op.PRODUCT, Terms.Empty);


    /**
     * Try to make a Product from an ImageExt/ImageInt and a component. Called by the logic rules.
     *
     * @param image     The existing Image
     * @param component The component to be added into the component list
     * @param index     The index of the place-holder in the new Image -- optional parameter
     * @return A compound generated or a term it reduced to
     */
    static Term make(Compound<Term> image, Term component, int index) {
        Term[] argument = image.termsCopy();
        argument[index] = component;
        return $.p(argument);
    }

//    static Product make(final Term[] pre, final Term... suf) {
//        final int pLen = pre.length;
//        final int sLen = suf.length;
//        Term[] x = new Term[pLen + suf.length];
//        System.arraycopy(pre, 0, x, 0, pLen);
//        System.arraycopy(suf, 0, x, pLen, sLen);
//        return make(x);
//    }

    static <T extends Term> Compound<T> make(Collection<T> t) {
        return $.p(t.toArray((T[]) new Term[t.size()]));
    }
//    static Product makeFromIterable(final Iterable<Term> t) {
//        return make(Iterables.toArray(t, Term.class));
//    }

    static <T extends Term> Compound<T> only(T the) {
        return $.p(the);
    }

    /**
     * 2 term constructor
     */
    static <T extends Term> Compound<T> make(T a, T b) {
        return $.p(new Term[]{a, b});
    }


    static Compound<Atom> make(String... argAtoms) {
        return $.p(argAtoms);
    }


//    /**
//     * returns the first subterm, or null if there are 0
//     */
//    default Object first() {
//        if (size() == 0) return null;
//        return term(0);
//    }
//
//    /**
//     * returns the last subterm, or null if there are 0
//     */
//    default Term last() {
//        int s = size();
//        if (s == 0) return null;
//        return term(s - 1);
//    }

    static String toString(Compound product) {
        StringBuilder sb = new StringBuilder().append((char)Symbols.COMPOUND_TERM_OPENER);
        int s = product.size();
        for (int i = 0; i < s; i++) {
            sb.append(product.term(i));
            if (i < s - 1)
                sb.append(", ");
        }
        sb.append((char)Symbols.COMPOUND_TERM_CLOSER);
        return sb.toString();
    }


//    /** create a Product from terms contained in a TermContainer
//     *  (ex: the subterms of a compound) */
//    static Product make(TermContainer c) {
//        return Product.make( c.terms() );
//    }
}
