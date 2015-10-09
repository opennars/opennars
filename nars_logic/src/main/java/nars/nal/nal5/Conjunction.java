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

import nars.Global;
import nars.Op;
import nars.nal.nal7.Sequence;
import nars.nal.nal7.Temporal;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Conjunction of statements
 */
public class Conjunction extends Junction<Term> {


    protected Op op;

    public Conjunction() {
        super();
    }

    /**
     * Constructor with partial values, called by make
     *
     * @param arg The component list of the term
     */
    protected Conjunction(Term[] arg, final int order) {
        super(flatten(arg, order));

        if ((order == Temporal.ORDER_BACKWARD) ||
                (order == Temporal.ORDER_INVALID)) {
            throw new RuntimeException("Invalid temporal order=" + order + "; args=" + Arrays.toString(this.term));
        }

        if (((order == Temporal.ORDER_FORWARD) && (!(this instanceof Sequence)))) {
            throw new RuntimeException("should be creating a Sequence instance not Conjunction");
        }


        switch (order) {
            case Temporal.ORDER_FORWARD:
                this.op = Op.SEQUENCE;
                break;
            case Temporal.ORDER_CONCURRENT:
                this.op = Op.PARALLEL;
                break;
            default:
                this.op = Op.CONJUNCTION;
                break;
        }

        if (Global.DEBUG) {
            if (isCommutative()) {
                if (Terms.toSortedSetArray(this.term).length!=this.term.length)
                    throw new RuntimeException("duplicates in commutative: " + this);
            }
        }

        init(this.term);

    }

    @Override
    public final int getTemporalOrder() {
        switch(op) {
            case SEQUENCE: return Temporal.ORDER_FORWARD;
            case PARALLEL: return Temporal.ORDER_CONCURRENT;
            case CONJUNCTION: return Temporal.ORDER_NONE;
            default:
                throw new RuntimeException("invalid op for Conjunction: " + this);
        }
    }

    @Override
    public Term clone(Term[] t) {
        return make(t, getTemporalOrder());
    }

    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Conjunction clone() {
        return new Conjunction(term, getTemporalOrder());
    }


    /**
     * returns null if not conjunction with same order
     */
    public static Conjunction isConjunction(Term t, int order) {
        if (t instanceof Conjunction) {
            Conjunction c = (Conjunction) t;
            if (c.getTemporalOrder() == order) {
                return c;
            }
        }
        return null;
    }

    /**
     * recursively flatten a embedded conjunction subterms if they are of a specific order
     */
    public static Term[] flatten(Term[] args, int order) {
        //determine how many there are with same order

        int expandedSize;
        while ((expandedSize = getFlattenedLength(args, order)) != args.length) {
            args = _flatten(args, order, expandedSize);
        }
        return args;
    }

    private static Term[] _flatten(Term[] args, int order, int expandedSize) {
        final Term[] ret = new Term[expandedSize];
        int k = 0;
        for (int i = 0; i < args.length; i++) {
            Term a = args[i];
            Conjunction c = isConjunction(a, order);
            if (c != null) {
                //arraycopy?
                for (Term t : c.term) {
                    ret[k++] = t;
                }
            } else {
                ret[k++] = a;
            }
        }

        return Terms.toSortedSetArray(ret);
    }

    protected static int getFlattenedLength(Term[] args, int order) {
        int sz = 0;
        for (int i = 0; i < args.length; i++) {
            Term a = args[i];
            Conjunction c = isConjunction(a, order);
            if (c != null)
                sz += c.length();
            else
                sz += 1;
        }
        return sz;
    }


    /**
     * Get the operate of the term.
     *
     * @return the operate of the term
     */
    @Override
    public Op op() {
        return op;
    }

    /**
     * Check if the compound is commutative.
     *
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return op!=Op.SEQUENCE;
    }

    /**
     * Try to make a new compound from a list of term. Called by StringParser.
     *
     * @param argList the list of arguments
     * @return the Term generated from the arguments
     */
    public static Term make(final Term[] argList) {

        return make(argList, Temporal.ORDER_NONE);
    }

    /**
     * Try to make a new compound from a list of term
     *
     * @param temporalOrder The temporal order among term
     * @param argList       the list of arguments
     * @return the Term generated from the arguments, or null if not possible
     */
    final public static Term make(Term[] argList, final int temporalOrder) {

        final int len = argList.length;

        if (Global.DEBUG) {
            Terms.verifyNonNull(argList);
        }

        if (len == 0) {
            return null;
        }                         // special case: single component

        if (temporalOrder == Temporal.ORDER_FORWARD) {
            //allow sequences of len 1
            return Sequence.makeSequence(argList);
        }

        if (len == 1) {
            return argList[0];
        }

        if (temporalOrder == Temporal.ORDER_BACKWARD) {
            throw new RuntimeException("Conjunction does not allow reverse order; args=" + Arrays.toString(argList));
            //return new Conjunction(Terms.reverse(argList), TemporalRules.ORDER_FORWARD);
            //return null;
        } else {
            Term[] a = Terms.toSortedSetArray(argList);
            if (a.length == 1) return a[0];
            return new Conjunction(a, temporalOrder);
        }
    }

//    final public static Term make(final int temporalOrder, final Term prefix, final Term... suffix) {
//        final int suffixLen = suffix.length;
//        Term[] t = new Term[suffixLen + 1];
//        t[0] = prefix;
//        System.arraycopy(suffix, 0, t, 1, suffixLen);
//        return make(t, temporalOrder);
//    }


    /**
     * @param c a set of Term as term
     * @return the Term generated from the arguments
     */
    public final static Term make(final Collection<Term> c, int temporalOrder) {
        Term[] argument = c.toArray(new Term[c.size()]);
        return make(argument, temporalOrder);
    }


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


    final public static Term make(final Term term1, final Term term2, int temporalOrder) {
        if (temporalOrder == Temporal.ORDER_FORWARD) {
            return Sequence.makeSequence(term1, term2);
        } else if (temporalOrder == Temporal.ORDER_BACKWARD) {
            //throw new RuntimeException("Conjunction does not allow reverse order; args=" + term1 + ", " + term2);
            return Sequence.makeSequence(term2, term1);
            //return null;
        } else {
            if (term1 instanceof Conjunction) {
                Compound ct1 = ((Compound) term1);
                final List<Term> set = Global.newArrayList(ct1.length() + 1);
                Collections.addAll(set, ct1.term);
                if (term2 instanceof Conjunction) {
                    // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                    Collections.addAll(set, ((Compound) term2).term);
                } else {
                    // (&,(&,P,Q),R) = (&,P,Q,R)
                    set.add(term2);
                }
                return make(set, temporalOrder);
            } else if (term2 instanceof Conjunction) {
                Compound ct2 = ((Compound) term2);
                final List<Term> set = Global.newArrayList(ct2.length() + 1);
                Collections.addAll(set, ct2.term);
                set.add(term1);                              // (&,R,(&,P,Q)) = (&,P,Q,R)
                return make(set, temporalOrder);
            } else {
                return make(new Term[]{term1, term2}, temporalOrder);
            }

        }
    }





    @Override
    public Term _car() {
        return term[0];
    }

//    public Term last() {
//        return term[term.length - 1];
//    }


}
