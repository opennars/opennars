/*
 * Conjunction.java
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
package nars.nal.nal5;

import nars.Op;
import nars.nal.nal7.Tense;
import nars.term.Term;

/**
 * Conjunction (&&)
 */
public class Conjunction extends Conjunctive {


    /**
     * Constructor with partial values, called by make
     *
     * @param arg The component list of the term
     */
    private Conjunction(Term[] arg) {/*
        , final int order
        super(
            //flatten only if no temporal order (&&)
            order == Temporal.ORDER_NONE ?
                    flatten(arg, order) : arg
        );*/
        super(arg);

//        if ((order == Temporal.ORDER_BACKWARD) ||
//                (order == Temporal.ORDER_INVALID)) {
//            throw new RuntimeException("Invalid temporal order=" + order + "; args=" + Arrays.toString(this.term));
//        }

//        if (((order == Temporal.ORDER_FORWARD) && (!(this instanceof Sequence)))) {
//            throw new RuntimeException("should be creating a Sequence instance not Conjunction");
//        }


//        switch (order) {
//            case Temporal.ORDER_FORWARD:
//                this.op = Op.SEQUENCE;
//                break;
//            case Temporal.ORDER_CONCURRENT:
//                this.op = Op.PARALLEL;
//                break;
//            default:
//                this.op = Op.CONJUNCTION;
//                break;
//        }
    }

    @Override
    public final int getTemporalOrder() {
        return Tense.ORDER_NONE;
    }

    @Override
    public final Term clone(Term[] t) {
        return make(t, getTemporalOrder());
    }

    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Conjunction clone() {
        return new Conjunction(terms());
    }



    /**
     * Get the operate of the term.
     *
     * @return the operate of the term
     */
    @Override
    public final Op op() {
        return Op.CONJUNCTION;
    }

    /**
     * Check if the compound is commutative.
     *
     * @return true for commutative
     */
    @Override
    public final boolean isCommutative() {
        return true;
    }



//    /**
//     * Try to make a new compound from a list of term
//     *
//     * @param temporalOrder The temporal order among term
//     * @param argList       the list of arguments
//     * @return the Term generated from the arguments, or null if not possible
//     */
//    final public static Term make(Term[] argList, final int temporalOrder) {
//
//        final int len = argList.length;
//
//        if (Global.DEBUG) {
//            Terms.verifyNonNull(argList);
//        }
//
//        if (len == 0) {
//            return null;
//        }                         // special case: single component
//
//        if (temporalOrder == Temporal.ORDER_FORWARD) {
//            //allow sequences of len 1
//            return Sequence.makeSequence(argList);
//        }
//
//
//        //parallel and none: one arg collapses to itself
//        if (len == 1) {
//            return argList[0];
//        }
//
//        switch (temporalOrder)
//        if (temporalOrder == Temporal.ORDER_BACKWARD) {
//            throw new RuntimeException("Conjunction does not allow reverse order; args=" + Arrays.toString(argList));
//            //return new Conjunction(Terms.reverse(argList), TemporalRules.ORDER_FORWARD);
//            //return null;
//        }
//
//        else {
//            Term[] a = Terms.toSortedSetArray(argList);
//            if (a.length == 1) return a[0];
//            return new Conjunction(a, temporalOrder);
//        }
//    }

//    final public static Term make(final int temporalOrder, final Term prefix, final Term... suffix) {
//        final int suffixLen = suffix.length;
//        Term[] t = new Term[suffixLen + 1];
//        t[0] = prefix;
//        System.arraycopy(suffix, 0, t, 1, suffixLen);
//        return make(t, temporalOrder);
//    }


    // overload this method by term type?

//    /**
//     * Try to make a new compound from two term. Called by the logic rules.
//     *
//     * @param term1 The first component
//     * @param term2 The second component
//     * @return A compound generated or a term it reduced to
//     */
//    final public static Term make(final Term term1, final Term term2) {
//        return make(term1, term2, Temporal.ORDER_NONE);
//    }


    @Override
    public Term _car() {
        return term(0);
    }

    static Term make(Term[] argList) {
        argList = flattenAndSort(argList, Tense.ORDER_NONE);

        //collapse to a singular term if none and parallel
        if (argList.length < 2) return argList[0];

        return new Conjunction(argList);
    }


//    public Term last() {
//        return term[term.length - 1];
//    }


}
