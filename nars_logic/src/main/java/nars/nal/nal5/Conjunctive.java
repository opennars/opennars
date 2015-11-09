package nars.nal.nal5;

import nars.Global;
import nars.nal.nal7.Parallel;
import nars.nal.nal7.Sequence;
import nars.nal.nal7.Tense;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by me on 10/20/15.
 */
public abstract class Conjunctive extends Junction<Term> {


    protected Conjunctive() {
        super();
    }

//    public Conjunctive(Term[] arg) {
//        super();
//
////        if (Global.DEBUG) {
////            if (isCommutative()) {
////                if (!Arrays.equals(Terms.toSortedSetArray(this.term), this.term))
////                    throw new RuntimeException("should have been sorted prior to construction");
////            }
////        }
//    }

    /**
     * returns null if not conjunction with same order
     */
    public static Conjunctive isConjunction(Term t, int order) {
        if (t instanceof Conjunctive) {
            Conjunctive c = (Conjunctive) t;
            if (c.getTemporalOrder() == order) {
                return c;
            }
        }
        return null;
    }

    /**
     * recursively flatten a embedded conjunction subterms if they are of a specific order
     */
    public static Term[] flattenAndSort(Term[] args, int order) {
        //determine how many there are with same order

        int expandedSize;
        while ((expandedSize = getFlattenedLength(args, order)) != args.length) {
            args = _flatten(args, order, expandedSize);
        }
        return Terms.toSortedSetArray(args);
    }

    private static Term[] _flatten(Term[] args, int order, int expandedSize) {
        final Term[] ret = new Term[expandedSize];
        int k = 0;
        for (int i = 0; i < args.length; i++) {
            Term a = args[i];
            Conjunctive c = isConjunction(a, order);
            if (c != null) {
                //arraycopy?
                for (Term t : c.term) {
                    ret[k++] = t;
                }
            } else {
                ret[k++] = a;
            }
        }

        return ret;
    }

    protected static int getFlattenedLength(Term[] args, int order) {
        int sz = 0;
        for (int i = 0; i < args.length; i++) {
            Term a = args[i];
            Conjunctive c = isConjunction(a, order);
            if (c != null)
                sz += c.length();
            else
                sz += 1;
        }
        return sz;
    }


    /**
     * @param c a set of Term as term
     * @return the Term generated from the arguments
     */
    public final static Term make(final Collection<Term> c, int temporalOrder) {
        Term[] argument = c.toArray(new Term[c.size()]);
        return make(argument, temporalOrder);
    }

    /**
     * Try to make a new compound from a list of term. Called by StringParser.
     *
     * @param argList the list of arguments
     * @return the Term generated from the arguments
     */
    public static Term make(Term[] argList, int temporalOrder) {
        switch (temporalOrder) {
            case Tense.ORDER_NONE:
                return Conjunction.make(argList);
            case Tense.ORDER_FORWARD:
                return Sequence.makeSequence(argList);
            case Tense.ORDER_CONCURRENT:
                return Parallel.makeParallel(argList);
        }
        throw new RuntimeException("invalid: " + Arrays.toString(argList) + " " + temporalOrder);
    }

    final public static Term make(final Term term1, final Term term2, int temporalOrder) {
        if (temporalOrder == Tense.ORDER_FORWARD) {
            return Sequence.makeSequence(term1, term2);
        } else if (temporalOrder == Tense.ORDER_BACKWARD) {
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
}
