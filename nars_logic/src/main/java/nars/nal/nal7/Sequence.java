package nars.nal.nal7;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.$;
import nars.Global;
import nars.Op;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Conjunctive;
import nars.term.Term;
import nars.term.Terms;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
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
public class Sequence<T extends Term> extends GenericCompound<T> implements Intermval {

    protected final int[] intervals;

    /** duration of terms themselves, as events.
     *  (not intervals between them).
     *  this should be set to the reasoner's perceptual
     *  duration at the time the Sequence is formed. */
    private int eventDuration = -1;

    private transient int duration = -1;

    /**
     * for subterms: (A, B, C) and intervals (i0, i1, i2, i3)
     * the effective sequence is:
     *      (&/,   /i0, A, /i1, B, /i2, C, /i3)
     *
     */
    private Sequence(T[] subterms, int[] intervals) {
        super(Op.SEQUENCE, subterms, 0);

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

    }

//    @Override
//    public int compareTo(Object that) {
//        int i = super.compareTo(that);
//        /*if (i == 0) {
//            if (!equals2((Sequence)that)) {
//                System.err.println("equality compared but not actually equal");
//            }
//        }*/
//        return i;
//    }
//
//    @Override
//    public boolean equals(Object that) {
//        boolean e = super.equals(that);
////        if (e) {
////            /**
////             * allowed for:
////             *      Conceptualize, Concept Activation, Concept lookup
////             *
////             * should disallow for:
////             *      task tables
////             *
////             * to be decided:
////             *      substitution maps
////             *      term index
////             *
////             */
////            if (!equals2((Sequence)that)) {
////                System.err.println("equality compared but not actually equal");
////            }
////
////            return true;
////        }
//        return e;
//    }

    /** compares 2nd-order "metadata" components: intervals, duration
     */
    public final boolean equals2(Sequence that) {
        if (this == that) return true;
        return Arrays.equals(intervals, that.intervals)
                &&
                //compare durations in the same eventDuration
                duration(eventDuration) == that.duration(eventDuration);

    }


    @Override
    public final void setDuration(int duration) {
        super.setDuration(duration);
        //if (this.eventDuration!=duration) {
        eventDuration = duration;
            this.duration = -1; //force recalculate
        //}
    }

    @Override
    public final int duration() {
        int duration = this.duration;
        if (duration < 0) {
            return this.duration = duration(eventDuration);
        }
        return duration;
    }

    @Override
    public final int duration(int eventDuration) {
        if (duration >= 0 && this.eventDuration==eventDuration)
            return duration; //return the cached value because it will be the same as recalculating

        int l = 0;
        for (int x : intervals())
            l += x;

        //if eventDuration is not set, then
        int defaultEventDuration = Math.max(0, eventDuration);

        //add embedded terms with temporal duration
        for (Term t : this) {
            if (t instanceof Intermval) {
                l += ((Intermval)t).duration(eventDuration);
            }
            else if (t instanceof Interval) {
                l += ((Interval)t).duration();
            }
            else {
                l += defaultEventDuration;
            }
        }
        return l;
    }


    @Deprecated public static final Term make(Term[] argList) {
        throw new RuntimeException("Use Sequence.makeSequence");
    }

    @Override
    public Term clone(Term[] t) {

//        if (Variable.hasPatternVariable(this)) {
//            // this is a pattern, in which case all intervals will be zero.
//            // so do a direct instantiation ignoring the all-zero intervals here
//            // because due to varargs, the sizes may not match anyway
//
//        }

        return size() != t.length ? makeSequence(t) : cloneIntervals(t);
    }

    /** only works if the # of terms are the same as this */
    protected Term cloneIntervals(Term[] t) {


        //HACK this reconstructs a dummy sequence of terms to send through makeSequence,
        // avoiding a cyclical normalization process necessary in order to avoid reduction
        //TODO do this without constructing such an array but just copying the int[] interval array to the result

        int tLen = t.length;

        List<Term> c = Global.newArrayList(tLen);

        int p = 0; //pointer to term in this
        for (Term x : t) {
            c.add(x);


//            boolean aligned = equalLength;
//            //scan ahead until either the term matches again, or end of the term is reached
//            while (!aligned && p < tLen) {
//                aligned = x.equals(term(p++));
//            }
//
//            int d; //duration between
//            if (equalLength || x.equals(term(p))) {
//                //this term corresponds to the next one in this sequence, so use the interval from this
            int d = intervals[p++];
//            }
//            else {
//                d = 0; //default to zero; could be a method parameter
//            }

            if (d > 0)
                c.add($.cycles(d));
        }

        //TODO check if this is necessary and/or correct
        if (intervals[p] > 0)
            c.add($.cycles(intervals[p])); //final suffix interval


        return makeSequence(c.toArray(new Term[c.size()]));
    }

    @Override
    public final int[] intervals() {
        return intervals;
    }


    public static Term makeSequence(Term[] a) {
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
    public static Term makeSequence(Term[] a, boolean allowReduction) {

        //count how many intervals so we know how to resize the final arrays
        int intervalsPresent = Interval.intervalCount(a);

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
        for (Term x : a) {
            /*if (x == Ellipsis.Expand)
                continue;*/
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
        Term[] components;

        if ((term1 instanceof Conjunction) && (term1.getTemporalOrder() == Tense.ORDER_FORWARD)) {

            Compound cterm1 = (Compound) term1;

            List<Term> list = Global.newArrayList(cterm1.size());
            cterm1.addAllTo(list);

            if (Conjunctive.isConjunction(term2, Tense.ORDER_FORWARD)) {
                // (&/,(&/,P,Q),(&/,R,S)) = (&/,P,Q,R,S)
                ((Compound) term2).addAllTo(list);
            } else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                list.add(term2);
            }

            components = list.toArray(new Term[list.size()]);

        } else if (Conjunctive.isConjunction(term2, Tense.ORDER_FORWARD)) {
            Compound cterm2 = (Compound) term2;
            components = new Term[term2.size() + 1];
            components[0] = term1;
            arraycopy(cterm2.terms(), 0, components, 1, cterm2.size());
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

        Compound.appendSeparator(p, pretty);

        int nterms = size();

        int[] ii = intervals();

        for (int i = 0; i < nterms+1; i++) {


            if (ii.length <= i) {
                System.err.println("Sequence has incorrect number of Intermvals " + Arrays.toString(intervals()) + " does not match terms ");// + Arrays.toString(term));
                break;
            }

            long c = ii[i];

            if (c != 0) {

                if (i == nterms)
                    Compound.appendSeparator(p, pretty);

                //insert Interval virtual term
                appendInterval(p, c);

                if (i == nterms)
                    break;
                else
                    Compound.appendSeparator(p, pretty);

            }

            if (i < nterms) {
                term(i).append(p, pretty);
            }

            if (i < nterms-1) {
                Compound.appendSeparator(p, pretty);
            }

        }


        Compound.appendCloser(p);


    }

    @Override
    public int bytesLength() {

        int add = intervals.length * 4;
        add += 4; //eventDuration int
        return super.bytesLength() + add;
    }

    @Override
    public void appendSubtermBytes(ByteBuf b) {
        super.appendSubtermBytes(b);

        b.addUnsignedInt(eventDuration);

        //add intermval suffix
        for (int i : intervals) //the intermval array
            b.addUnsignedInt(i);
    }



    public Term cloneRemovingSuffixInterval(long[] offsetAdjustment) {
        int s = size();
        int[] ii = intervals();

        if (ii[s]!=0) {
            //dont disturb the original copy
            ii = Arrays.copyOf(intervals(), s + 1);
            offsetAdjustment[0] = ii[s];
            ii[s] = 0;
        }

        Term[] t = terms();
        return makeSequence(t, ii);
    }


    public final Term[] toArrayWithIntervals() {
        return toArrayWithIntervals( (x,y) -> true );
    }

    /** constructs a subterm array with the relevant intervals included */
    public final Term[] toArrayWithIntervals(IntObjectPredicate filter) {
        List<Term> l = Global.newArrayList();
        Term[] s = terms();
        int[] i = intervals();
        int p = 0;
        for (Term x : s) {
            if (filter.accept(p++, x)) {
                l.add(x);
                int d = i[p];
                if (d!=0)
                    l.add(CyclesInterval.make(d));
            }
        }
        return Terms.toArray(l);
    }

}
