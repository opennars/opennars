/*
 * Negation.java
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
package nars.nal.nal1;

import nars.Op;
import nars.term.Compound;
import nars.term.CompoundN;
import nars.term.Term;
import nars.term.transform.FindSubst;

import java.io.IOException;

/**
 * A negation of a statement.
 */
public final class Negation<T extends Term> extends CompoundN<T> {

    /** avoid using this externally, because double-negatives can be unwrapped to the 
     * original term using Negation.make */
    protected Negation(final T t) {
        super(t);
    }

    @Override public final boolean isCommutative() {
        return false;
    }

    public final T the() { return term(0); }

    public final Term clone() {
        return Negation.make(the());
    }

    @Override
    public Term clone(final Term[] replaced) {

        if (replaced.length != 1)
            throw new RuntimeException("negation requires 1 arg");

        Term t = replaced[0];
        if (t == the())
            return this; //no change

        return Negation.make(replaced);
    }

    /**
     * Try to make a Negation of one component. Called by the logic rules.
     *
     * @param t The component
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term t) {
        if (t instanceof Negation) {
            // (--,(--,P)) = P
            return ((Negation) t).the();
        }
        return new Negation(t);
    }



    /**
     * Try to make a new Negation. Called by StringParser.
     *
     * @return the Term generated from the arguments
     * @param argument The list of term
     */
    public static Term make(final Term[] argument) {
        if (argument.length != 1)
            throw new RuntimeException("negation requires 1 arg");

        return make(argument[0]);
    }


    /**
     * Get the operate of the term.
     *
     * @return the operate of the term
     */
    @Override
    public final Op op() {
        return Op.NEGATION;
    }


//    public static boolean areMutuallyInverse(Term a, Term b) {
//        //doesnt seem necessary to check both, one seems sufficient.
//        //incurs cost of creating a Negation and its id
//        return (b.equals(Negation.make(a)) /* || tc.equals(Negation.make(ptc))*/ );
//    }


    @Override
    public final byte[] bytes() {
        return Compound.newCompound1Key(Op.NEGATION, the());
    }

    @Override
    public final void append(Appendable p, boolean pretty) throws IOException {
        Compound.writeCompound1(op(), the(), p, pretty);
    }


    @Override public boolean matchSubterms(Compound Y, FindSubst subst) {
        return subst.match(the(), Y.term(0));
        //return matchSubterm(0, Y, subst);
    }

    /*
    static boolean areMutuallyInverseNOTWORKINGYET(Term a, Term b) {
        boolean aNeg = a instanceof Negation;
        boolean bNeg = b instanceof Negation;

        if (aNeg && !bNeg)
            return areMutuallyInverse((Negation)a, b);
        else if (!aNeg && bNeg)
            return areMutuallyInverse((Negation)b, a);
        else
            return false;
    }


    static boolean areMutuallyInverse(Negation a, Term b) {
        return (a.negated().equals(b));
    }
    */

}
