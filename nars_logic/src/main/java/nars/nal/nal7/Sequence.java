package nars.nal.nal7;

import nars.Op;
import nars.Symbols;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Conjunctive;
import nars.term.Compound;
import nars.term.Term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.arraycopy;
import static nars.Symbols.COMPOUND_TERM_OPENER;

/**
 * Sequential Conjunction (&/)
 */
public class Sequence extends Conjunctive implements Intermval {

    protected final int[] intervals;

    transient private long duration = -1;

    Sequence(Term[] subterms, int[] intervals) {
        super(subterms);

        if (intervals.length != 1 + subterms.length)
            throw new RuntimeException("invalid intervals length: " + intervals.length + " should equal " + (subterms.length + 1));


        final int s = subterms.length;

        //operate on a clone in case this will be created from a subrange of another array etc */
        if (intervals[s]!=0) {
            intervals = Arrays.copyOf(intervals, s+1);
            intervals[s] = 0;
        }

        this.intervals = intervals;


        init(subterms);

    }

    @Override
    public final long duration() {
        long duration = this.duration;
        if (duration == -1) {
            long l = 0;
            for (final int x : intervals())
                l += x;

            //add embedded terms with temporal duration
            for (Term t : this) {
                if (t instanceof Intermval) {
                    l += ((Intermval)t).duration();
                }
            }

            return this.duration = l;
        }
        return duration;
    }

    public static final Term make(final Term[] argList) {
        throw new RuntimeException("Use Sequence.makeSequence");
    }

    @Override
    public final int getTemporalOrder() {
        return Temporal.ORDER_FORWARD;
    }


    @Override
    public Sequence clone() {
        return new Sequence(term, intervals);
    }

    @Override
    public Sequence clone(final Term[] t) {
//        return clone(t, intervals);
//    }
//
//    public Sequence clone(Term[] t, long[] ii) {
        //for now, require that cloning require same # of terms because intervals will be copied as-is
//        int tlen = t.length;
//
//        if (ii.length != tlen +1) {
//            /*throw new RuntimeException("invalid parameters for Sequence clone: " +
//                    Arrays.toString(t) + " (len=" + t.length + ") and intervals " +
//                    Arrays.toString(ii) + " (len=" + ii.length + ")");*/
//            return null;
//        }

        return makeSequence(t);
    }

    @Override
    public final int[] intervals() {
        return intervals;
    }

    @Override
    public final Op op() {
        return Op.SEQUENCE;
    }


    /**
     * the input Terms here is "unnormalized" meaning it may contain
     * Interval whch will need removed as subterms and inserted into the
     * intervals array. the final subterms array for the new Sequence
     * will not contain any Intervals but the data will be separated
     * into the separate intervals array that ensures the terms avoid
     * involving Intervals in equality tests
     *
     * @param a
     * @return
     */
    public static Sequence makeSequence(final Term[] a) {

        //count how many intervals so we know how to resize the final arrays
        final int intervalsPresent = AbstractInterval.intervalCount(a);

        if (intervalsPresent == 0)
            return new Sequence(a, new int[a.length+1] /* empty */);


        //if intervals are present:
        Term[] b = new Term[a.length - intervalsPresent];
        int[] i = new int[b.length + 1];

        int p = 0;
        for (final Term x : a) {
            if (x instanceof AbstractInterval) {
                i[p] += ((AbstractInterval) x).cycles(null);
            } else {
                b[p++] = x;
            }
        }

        return new Sequence(b, i);
    }


//    public static Sequence makeSequence(final Collection<Term> a) {
//        //TODO make more efficient version of this that doesnt involve array copy
//        return makeSequence(a.toArray(new Term[a.size()]));
//    }


    public static Term makeSequence(Term term1, Term term2) {
        final Term[] components;

        if ((term1 instanceof Conjunction) && (term1.getTemporalOrder() == Temporal.ORDER_FORWARD)) {

            Compound cterm1 = (Compound) term1;

            ArrayList<Term> list = new ArrayList<>(cterm1.length());
            cterm1.addTermsTo(list);

            if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == Temporal.ORDER_FORWARD)) {
                // (&/,(&/,P,Q),(&/,R,S)) = (&/,P,Q,R,S)
                ((Compound) term2).addTermsTo(list);
            } else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                list.add(term2);
            }

            components = list.toArray(new Term[list.size()]);

        } else if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == Temporal.ORDER_FORWARD)) {
            Compound cterm2 = (Compound) term2;
            components = new Term[term2.length() + 1];
            components[0] = term1;
            arraycopy(cterm2.term, 0, components, 1, cterm2.length());
        } else {
            components = new Term[]{term1, term2};
        }

        return makeSequence(components);
    }

    @Override
    public void append(Appendable p, boolean pretty) throws IOException {

        boolean opener = appendTermOpener();
        if (opener)
            p.append(COMPOUND_TERM_OPENER);


        appendOperator(p);

        appendSeparator(p, pretty);

        int nterms = term.length;

        int[] ii = intervals();

        for (int i = 0; i < nterms+1; i++) {


            if (ii.length <= i) {
                System.err.println("Sequence has incorrect number of Intermvals " + Arrays.toString(intervals()) + " does not match terms " + Arrays.toString(term));
                break;
            }

            final long c = ii[i];

            if (c != 0) {

                if (i == nterms)
                    appendSeparator(p, pretty);

                //insert Interval virtual term
                appendInterval(p, c);

                if (i == nterms)
                    break;
                else
                    appendSeparator(p, pretty);

            }

            if (i < nterms) {
                term[i].append(p, pretty);
            }

            if (i < nterms-1) {
                appendSeparator(p, pretty);
            }

        }


        appendCloser(p);


    }

    protected static void appendInterval(Appendable p, long iii) throws IOException {
        p.append(Symbols.INTERVAL_PREFIX);
        p.append(Long.toString(iii));
    }



//    public Sequence cloneRemovingSuffixInterval() {
//        final int s = size();
//        long[] ni = Arrays.copyOf(intervals(), s+1);
//        ni[s] = 0;
//        return clone(term, ni);
//    }

}
