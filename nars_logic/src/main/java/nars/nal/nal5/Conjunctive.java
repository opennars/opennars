package nars.nal.nal5;

import nars.Global;
import nars.Op;
import nars.nal.nal7.Order;
import nars.nal.nal7.Parallel;
import nars.nal.nal7.Sequence;
import nars.term.Term;
import nars.term.compound.Compound;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static nars.nal.nal7.Order.Backward;
import static nars.nal.nal7.Order.Forward;

/**
 * Created by me on 10/20/15.
 */
public interface Conjunctive<T extends Term>  {


    int conjunctiveBits = Op.or(Op.CONJUNCTION, Op.SEQUENCE, Op.PARALLEL);

    /** null if not conjunction with same order */
    public static boolean isConjunction(Term t, Order order) {
        if (t.isAny(conjunctiveBits)) {
            return t.getTemporalOrder() == order;// ? (Compound) t : null;
        }
        return false;
    }

    /**
     * recursively flatten a embedded conjunction subterms if they are of a specific order
     */
    public static Term[] flatten(Term[] args, Order order) {
        //determine how many there are with same order

        int expandedSize;
        while ((expandedSize = getFlattenedLength(args, order)) != args.length) {
            args = _flatten(args, order, expandedSize);
        }
        return args;
    }

    static Term[] _flatten(Term[] args, Order order, int expandedSize) {
        Term[] ret = new Term[expandedSize];
        int k = 0;
        for (Term a : args) {
            if (isConjunction(a, order)) {
                //arraycopy?
                for (Term t : ((Compound)a).terms()) {
                    ret[k++] = t;
                }
            } else {
                ret[k++] = a;
            }
        }

        return ret;
    }

    static int getFlattenedLength(Term[] args, Order order) {
        int sz = 0;
        for (Term a : args) {
            if (isConjunction(a, order))
                sz += a.size();
            else
                sz++;
        }
        return sz;
    }


    /**
     * @param c a set of Term as term
     * @return the Term generated from the arguments
     */
    public static Term make(Collection<Term> c, Order temporalOrder) {
        Term[] argument = c.toArray(new Term[c.size()]);
        return make(argument, temporalOrder);
    }

    /**
     * Try to make a new compound from a list of term. Called by StringParser.
     *
     * @param argList the list of arguments
     * @return the Term generated from the arguments
     */
    public static Term make(Term[] argList, Order temporalOrder) {
        switch (temporalOrder) {
            case None:
                return Conjunction.conjunction(argList);
            case Forward:
                return Sequence.makeSequence(argList);
            case Concurrent:
                return Parallel.makeParallel(argList);
            default:
                throw new RuntimeException("invalid: " + Arrays.toString(argList) + ' ' + temporalOrder);
        }
    }

    static Term make(Term term1, Term term2, Order temporalOrder) {
        if (temporalOrder == Forward) {
            return Sequence.makeSequence(term1, term2);
        } else if (temporalOrder == Backward) {
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
