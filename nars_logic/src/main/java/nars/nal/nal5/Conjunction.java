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
import nars.nal.NALOperator;
import nars.nal.Terms;
import nars.nal.nal7.Interval;
import nars.nal.nal7.TemporalRules;
import nars.nal.task.TaskSeed;
import nars.nal.term.Compound;
import nars.nal.term.Term;

import java.util.*;

import static java.lang.System.arraycopy;

/**
 * Conjunction of statements
 */
public class Conjunction extends Junction {

    transient private int offset;

    public final int temporalOrder;

    /**
     * Constructor with partial values, called by make
     *
     * @param arg The component list of the term
     */
    protected Conjunction(Term[] arg, final int order) {
        super(arg = flatten(arg, order));

        if ((order == TemporalRules.ORDER_BACKWARD) ||
                (order == TemporalRules.ORDER_INVALID)) {
            throw new RuntimeException("Invalid temporal order=" + order + "; args=" + Arrays.toString(arg));
        } else {
            temporalOrder = order;
        }

        init(arg);

    }


    @Override
    public Term clone(Term[] t) {
        return make(t, temporalOrder);
    }

    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Conjunction clone() {
        return new Conjunction(term, temporalOrder);
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
     * flatten a embedded conjunction subterms if they are of a specific order
     */
    public static Term[] flatten(Term[] args, int order) {
        //determine how many there are with same order
        int sz = 0;
        for (int i = 0; i < args.length; i++) {
            Term a = args[i];
            Conjunction c = isConjunction(a, order);
            if (c != null)
                sz += c.length();
            else
                sz += 1;
        }
        if (sz == args.length) {
            //no change
            return args;
        }

        final Term[] ret = new Term[sz];
        int k = 0;
        for (int i = 0; i < args.length; i++) {
            Term a = args[i];
            Conjunction c = isConjunction(a, order);
            if (c != null) {
                for (Term t : c.term) {
                    ret[k++] = t;
                }
            } else {
                ret[k++] = a;
            }
        }
        return ret;
    }


    /**
     * Get the operate of the term.
     *
     * @return the operate of the term
     */
    @Override
    public NALOperator operator() {
        switch (temporalOrder) {
            case TemporalRules.ORDER_FORWARD:
                return NALOperator.SEQUENCE;
            case TemporalRules.ORDER_CONCURRENT:
                return NALOperator.PARALLEL;
            default:
                return NALOperator.CONJUNCTION;
        }
    }

    /**
     * Check if the compound is commutative.
     *
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return temporalOrder != TemporalRules.ORDER_FORWARD;
    }

    /**
     * Try to make a new compound from a list of term. Called by StringParser.
     *
     * @param argList the list of arguments
     * @return the Term generated from the arguments
     */
    final public static Term make(final Term[] argList) {

        return make(argList, TemporalRules.ORDER_NONE);
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
        if (len == 1) {
            return argList[0];
        }


        if (temporalOrder == TemporalRules.ORDER_FORWARD) {


            int remaining = len;
            //long cycleOffset = 0;
            Term l = null;
            int offset = 0;
            while (remaining > 0 && ((l = argList[remaining - 1]) instanceof Interval)) {
                remaining--;
                offset -= ((Interval) l).cycles(new Interval.AtomicDuration(5));
            }

            if (len != remaining) {
                if (remaining == 0) return null;
                if (remaining == 1) {
                    return argList[0];
                }

                argList = Arrays.copyOfRange(argList, 0, remaining);
            }

            Conjunction cj = new Conjunction(argList, temporalOrder);

            if (len != remaining) {
                cj.setOffset(offset);
                System.err.println("interval making an assumption temporarily that " + argList + " shifts " + offset + " when represented as" + cj);
            }
            return cj;

        } else if (temporalOrder == TemporalRules.ORDER_BACKWARD) {
            throw new RuntimeException("Conjunction does not allow reverse order; args=" + Arrays.toString(argList));
            //return new Conjunction(Terms.reverse(argList), TemporalRules.ORDER_FORWARD);
            //return null;
        } else {
            Term[] a = Terms.toSortedSetArray(argList);
            if (a.length == 1) return a[0];
            return new Conjunction(a, temporalOrder);
        }
    }

    final public static Term make(final int temporalOrder, final Term prefix, final Term... suffix) {
        final int suffixLen = suffix.length;
        Term[] t = new Term[suffixLen + 1];
        int i = 0;
        t[i++] = prefix;
        System.arraycopy(suffix, 0, t, 1, suffixLen);
        return make(t, temporalOrder);
    }


    /**
     * @param c a set of Term as term
     * @return the Term generated from the arguments
     */
    final private static Term make(final Collection<Term> c, int temporalOrder) {
        Term[] argument = c.toArray(new Term[c.size()]);
        return make(argument, temporalOrder);
    }


    // overload this method by term type?

    /**
     * Try to make a new compound from two term. Called by the logic rules.
     *
     * @param term1 The first component
     * @param term2 The second component
     * @return A compound generated or a term it reduced to
     */
    final public static Term make(final Term term1, final Term term2) {
        return make(term1, term2, TemporalRules.ORDER_NONE);
    }


    final public static Term make(final Term term1, final Term term2, int temporalOrder) {
        if (temporalOrder == TemporalRules.ORDER_FORWARD) {
            return makeForward(term1, term2);
        } else if (temporalOrder == TemporalRules.ORDER_BACKWARD) {
            //throw new RuntimeException("Conjunction does not allow reverse order; args=" + term1 + ", " + term2);
            return makeForward(term2, term1);
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


    protected static Term makeForward(Term term1, Term term2) {
        final Term[] components;

        if ((term1 instanceof Conjunction) && (term1.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {

            Compound cterm1 = (Compound) term1;

            ArrayList<Term> list = new ArrayList<>(cterm1.length());
            cterm1.addTermsTo(list);

            if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                // (&/,(&/,P,Q),(&/,R,S)) = (&/,P,Q,R,S)
                ((Compound) term2).addTermsTo(list);
            } else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                list.add(term2);
            }

            components = list.toArray(new Term[list.size()]);

        } else if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
            Compound cterm2 = (Compound) term2;
            components = new Term[((Compound) term2).length() + 1];
            components[0] = term1;
            arraycopy(cterm2.term, 0, components, 1, cterm2.length());
        } else {
            components = new Term[]{term1, term2};
        }

        return make(components, TemporalRules.ORDER_FORWARD);
    }

    @Override
    public int getTemporalOrder() {
        return temporalOrder;
    }


    /** records an amount of cycles that this conjunction will shift the occurence time of a non-eternal sentence it will be a term of */
    public int setOffset(int deltaCycles) {
        return offset;
    }

    @Override
    public <T extends Compound> Compound sentencize(TaskSeed task) {
        task.occurr(task.getOccurrenceTime());
        return this;
    }

    public Term first() {
        return term[0];
    }

    public Term last() {
        return term[term.length - 1];
    }

}
