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
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;

import java.util.Collection;
import java.util.List;

/**
 * A Product is a sequence of 1 or more terms.
 */
public interface Product<T extends Term> extends Compound<T>, Iterable<T> {

    Compound empty = $.p(new Term[] {}); //length 0 product

    /**
     * Get the operate of the term.
     * @return the operate of the term
     */
    @Override
    default Op op() {
        return Op.PRODUCT;
    }

    /**
     * Try to make a Product from an ImageExt/ImageInt and a component. Called by the logic rules.
     * @param image The existing Image
     * @param component The component to be added into the component list
     * @param index The index of the place-holder in the new Image -- optional parameter
     * @return A compound generated or a term it reduced to
     */
    static Term make(final Compound<Term> image, final Term component, final int index) {
        Term[] argument = image.termsCopy();
        argument[index] = component;
        return make(argument);
    }

//    static Product make(final Term[] pre, final Term... suf) {
//        final int pLen = pre.length;
//        final int sLen = suf.length;
//        Term[] x = new Term[pLen + suf.length];
//        System.arraycopy(pre, 0, x, 0, pLen);
//        System.arraycopy(suf, 0, x, pLen, sLen);
//        return make(x);
//    }

    static <T extends Term> Compound<T> make(final Collection<T> t) {
        return make(t.toArray((T[]) new Term[t.size()]));
    }
//    static Product makeFromIterable(final Iterable<Term> t) {
//        return make(Iterables.toArray(t, Term.class));
//    }

    static <T extends Term> Compound<T> only(final T the) {
        return $.p(the);
    }

    /** 2 term constructor */
    static <T extends Term> Compound<T> make(final T a, final T b) {
        return $.p(new Term[] { a, b });
    }

    /** creates from a sublist of a list */
    static Compound make(final List<Term> l, int from, int to) {
        Term[] x = new Term[to - from];

        for (int j = 0, i = from; i < to; i++)
            x[j++] = l.get(i);

        return make(x);
    }

    public static <T extends Term> Compound<T> make(final T... arg) {
        int l = arg.length;

        //length 0 product are allowd
        if (l == 0)
            return empty;

        if (l == 1)
            return only(arg[0]);

        return new GenericCompound(Op.PRODUCT, arg);
    }

    static Compound<Atom> make(final String... argAtoms) {
        return $.p(argAtoms);
    }



    /** returns the first subterm, or null if there are 0 */
    default Object first() {
        if (size() == 0) return null;
        return term(0);
    }

    /** returns the last subterm, or null if there are 0 */
    default Term last() {
        int s = size();
        if (s == 0) return null;
        return term(s-1);
    }


//    /** create a Product from terms contained in a TermContainer
//     *  (ex: the subterms of a compound) */
//    static Product make(TermContainer c) {
//        return Product.make( c.terms() );
//    }
}
