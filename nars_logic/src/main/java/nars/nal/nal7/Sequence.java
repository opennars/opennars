package nars.nal.nal7;

import nars.Global;
import nars.Op;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Conjunctive;
import nars.term.Compound;
import nars.term.Term;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.arraycopy;
import static nars.Symbols.COMPOUND_TERM_OPENER;
import static nars.nal.nal7.Tense.appendInterval;

/**
 * Sequential Conjunction (&/)
 */
public class Sequence extends Conjunctive implements Intermval {

    protected final int[] intervals;

    /** duration of terms themselves, as events.
     *  (not intervals between them).
     *  this should be set to the reasoner's perceptual
     *  duration at the time the Sequence is formed. */
    private int eventDuration = -1;

    transient private int duration = -1;

    /**
     * for subterms: (A, B, C) and intervals (i0, i1, i2, i3)
     * the effective sequence is:
     *      (&/,   /i0, A, /i1, B, /i2, C, /i3)
     *
     */
    private Sequence(Term[] subterms, int[] intervals) {
        super();

        if (intervals == null) {
            //TODO leave as null, avoiding allocating this array if all zeros
            intervals = new int[subterms.length+1];
        }
        else {
            if (intervals.length != 1 + subterms.length)
                throw new RuntimeException("invalid intervals length: " + intervals.length + " should equal " + (subterms.length + 1));
        }


//        //operate on a clone in case this will be created from a subrange of another array etc */
//        final int s = subterms.length;
//        if (intervals[s]!=0) {
//            intervals = Arrays.copyOf(intervals, s+1);
//            intervals[s] = 0;
//        }

        this.intervals = intervals;

        init(subterms);

    }

    @Override public final boolean isCommutative() {
        return false;
    }

    @Override
    public final void setDuration(int duration) {
        super.setDuration(duration);
        if (this.eventDuration!=duration) {
            this.eventDuration = duration;
            this.duration = -1; //force recalculate
        }
    }

    @Override
    public final int duration() {
        int duration = this.duration;
        if (duration < 0) {
            int l = 0;
            for (final int x : intervals())
                l += x;

            final int defaultEventDuration = this.eventDuration;

            //add embedded terms with temporal duration
            for (Term t : this) {
                if (t instanceof Interval) {
                    l += ((Interval)t).duration();
                }
                else {
                    l += defaultEventDuration;
                }
            }


            return this.duration = l;
        }
        return duration;
    }

    @Deprecated public static final Term make(final Term[] argList) {
        throw new RuntimeException("Use Sequence.makeSequence");
    }

    @Override
    public final int getTemporalOrder() {
        return Tense.ORDER_FORWARD;
    }


    @Override
    public Sequence clone() {
        return new Sequence(term, intervals);
    }

    @Override
    public Term clone(final Term[] t) {
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


        //HACK this reconstructs a dummy sequence of terms to send through makeSequence,
        // avoiding a cyclical normalization process necessary in order to avoid reduction
        //TODO do this without constructing such an array but just copying the int[] interval array to the result

        List<Term> c = Global.newArrayList(t.length);
        int j = 0;
        for (Term x : t) {
            c.add(x);
            int d = intervals[j++];
            if (d > 0)
                c.add(CyclesInterval.make(d));
        }
        if (intervals[j]>0)
            c.add(CyclesInterval.make(intervals[j])); //final suffix interval

        return makeSequence(c.toArray(new Term[c.size()]));
    }

    @Override
    public final int[] intervals() {
        return intervals;
    }

    @Override
    public final Op op() {
        return Op.SEQUENCE;
    }


    public static Term makeSequence(final Term[] a) {
        return makeSequence(a, true);
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
    public static Term makeSequence(final Term[] a, boolean allowReduction) {

        //count how many intervals so we know how to resize the final arrays
        final int intervalsPresent = Interval.intervalCount(a);

        if (intervalsPresent == 0) {
            if (allowReduction && (a.length == 1)) return a[0]; //TODO combine this with singleton condition at end of this method
            return new Sequence(a, null);
        }


        //if intervals are present:
        Term[] b = new Term[a.length - intervalsPresent];

        int blen = b.length;
        if (blen == 0)
            throw new RuntimeException("empty sequence containing only intervals");

        int[] i = new int[b.length + 1];

        int p = 0;
        for (final Term x : a) {
            if (x instanceof CyclesInterval) {
                long dd = ((CyclesInterval) x).duration();
                if (dd < 0)
                    throw new RuntimeException("cycles must be >= 0");

                i[p] += dd;
            } else {
                b[p++] = x;
            }
        }

        return makeSequence(b, i);
    }

    public static Term makeSequence(Term[] b, int[] i) {
        if (b.length == 1) {
            //detect if this is reducible to the singleton term (no preceding or following interval)
            if ((i[0]==0) && (i[1]==0))
                return b[0];
        }

        return new Sequence(b, i);
    }


    public static Term makeSequence(Term term1, Term term2) {
        final Term[] components;

        if ((term1 instanceof Conjunction) && (term1.getTemporalOrder() == Tense.ORDER_FORWARD)) {

            Compound cterm1 = (Compound) term1;

            List<Term> list = Global.newArrayList(cterm1.length());
            cterm1.addTermsTo(list);

            if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == Tense.ORDER_FORWARD)) {
                // (&/,(&/,P,Q),(&/,R,S)) = (&/,P,Q,R,S)
                ((Compound) term2).addTermsTo(list);
            } else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                list.add(term2);
            }

            components = list.toArray(new Term[list.size()]);

        } else if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == Tense.ORDER_FORWARD)) {
            Compound cterm2 = (Compound) term2;
            components = new Term[term2.length() + 1];
            components[0] = term1;
            arraycopy(cterm2.term, 0, components, 1, cterm2.length());
        } else {
            components = new Term[]{term1, term2};
        }

        return makeSequence(components);
    }

    //TODO maybe override only Compound.appendArgs
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

    @Override
    public int getByteLen() {

        int add = intervals.length * 4;
        add += 4; //eventDuration int
        return super.getByteLen() + add;
    }

    @Override
    protected void appendBytes(int numArgs, ByteBuf b) {
        super.appendBytes(numArgs, b);

        b.addUnsignedInt(eventDuration);

        //add intermval suffix
        for (int i : intervals) //the intermval array
            b.addUnsignedInt(i);
    }



    public Term cloneRemovingSuffixInterval() {
        final int s = length();
        int[] ii = intervals();

        if (ii[s]!=0) {
            //dont disturb the original copy
            ii = Arrays.copyOf(intervals(), s + 1);
            ii[s] = 0;
        }

        Term[] t = term;
        return makeSequence(t, ii);
    }

}
