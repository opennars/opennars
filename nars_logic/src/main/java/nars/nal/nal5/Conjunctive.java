package nars.nal.nal5;

import nars.Global;
import nars.nal.nal7.Parallel;
import nars.nal.nal7.Sequence;
import nars.nal.nal7.Tense;
import nars.term.Term;
import nars.term.Terms;
import nars.term.compound.Compound;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by me on 10/20/15.
 */
public abstract class Conjunctive<T extends Term> extends Junction<T> {


    @SafeVarargs
    protected Conjunctive(T... arg) {
        super(arg);
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
        Term[] ret = new Term[expandedSize];
        int k = 0;
        for (int i = 0; i < args.length; i++) {
            Term a = args[i];
            Conjunctive c = isConjunction(a, order);
            if (c != null) {
                //arraycopy?
                for (Term t : c.terms()) {
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
            sz += c != null ? c.size() : 1;
        }
        return sz;
    }


    /**
     * @param c a set of Term as term
     * @return the Term generated from the arguments
     */
    public static final Term make(Collection<Term> c, int temporalOrder) {
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
        throw new RuntimeException("invalid: " + Arrays.toString(argList) + ' ' + temporalOrder);
    }

    public static final Term make(Term term1, Term term2, int temporalOrder) {
        if (temporalOrder == Tense.ORDER_FORWARD) {
            return Sequence.makeSequence(term1, term2);
        } else if (temporalOrder == Tense.ORDER_BACKWARD) {
            //throw new RuntimeException("Conjunction does not allow reverse order; args=" + term1 + ", " + term2);
            return Sequence.makeSequence(term2, term1);
            //return null;
        } else {
            if (term1 instanceof Conjunction) {
                Compound ct1 = ((Compound) term1);
                List<Term> set = Global.newArrayList(ct1.size() + 1);
                ct1.addAllTo(set);
                if (term2 instanceof Conjunction) {
                    // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                    ((Compound)term2).addAllTo(set);
                } else {
                    // (&,(&,P,Q),R) = (&,P,Q,R)
                    set.add(term2);
                }
                return make(set, temporalOrder);
            } else if (term2 instanceof Conjunction) {
                Compound ct2 = ((Compound) term2);
                List<Term> set = Global.newArrayList(ct2.size() + 1);
                ct2.addAllTo(set);
                set.add(term1);                              // (&,R,(&,P,Q)) = (&,P,Q,R)
                return make(set, temporalOrder);
            } else {
                return make(new Term[]{term1, term2}, temporalOrder);
            }

        }
    }

}
