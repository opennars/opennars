package nars.nal.nal7;

import nars.Op;
import nars.nal.nal5.Conjunctive;
import nars.term.Term;
import nars.term.Terms;
import nars.util.utf8.ByteBuf;

import java.io.IOException;

import static nars.Symbols.ARGUMENT_SEPARATOR;

/**
 * Parallel Conjunction (&|)
 */
public class Parallel extends Conjunctive implements Interval {

    //local duration, a virtual sub-term at the top level of the parallel term
    private final long additionalDuration;

    //total duration (cached), the maximum duration of all included temporal terms
    transient long totalDuration = -1;
    private int eventDuration = -1;


//    private Parallel(Term... arg) {
//        this(arg, 0);
//    }

    private Parallel(Term[] arg, long additionalDuration) {
        super(arg = Terms.toSortedSetArray(arg));
        this.additionalDuration = additionalDuration;
        init(arg);
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    @Override
    public final Op op() {
        return Op.PARALLEL;
    }

    @Override
    public final int getTemporalOrder() {
        return Temporal.ORDER_CONCURRENT;
    }

    @Deprecated public static final Term make(final Term[] argList) {
        throw new RuntimeException("Use Parallel.makeParallel");
    }

    @Override
    public Term clone() {
        return new Parallel(term, additionalDuration);
    }

    @Override
    public Term clone(Term[] replaced) {
        return Parallel.makeParallel(replaced);
    }

    @Override
    public final int getByteLen() {
        return super.getByteLen()
                + 4 /* for storing 'duration' */
                + 4 /* for storing eventDuration */
                ;
    }

    @Override
    protected final void appendBytes(int numArgs, ByteBuf b) {
        super.appendBytes(numArgs, b);

        //add intermval suffix
        b.addUnsignedInt(additionalDuration);
        b.addUnsignedInt(eventDuration);
    }


    @Override public void appendArgs(Appendable p, boolean pretty, boolean appendedOperator) throws IOException {
        p.append(ARGUMENT_SEPARATOR);
        if (pretty) p.append(' ');

        super.appendArgs(p, pretty, false);

        if (additionalDuration > eventDuration) {
            p.append(ARGUMENT_SEPARATOR);
            if (pretty) p.append(' ');
            Temporal.appendInterval(p, additionalDuration);
        }
    }

    @Override
    public <T extends Term> T  setDuration(int duration) {
        super.setDuration(duration);
        this.eventDuration = duration;
        this.totalDuration = -1; //force recalc

        //if there is one element and additionalDuration does not influence the overall duration,
        //return that singleton subterm (unwrapped)
        if ((length() == 1) && (duration() > additionalDuration))
            return (T)term(0);

        return (T)this;
    }

    @Override
    public final long duration() {
        long totalDuration = this.totalDuration;

        if (totalDuration == -1) {
            return this.totalDuration = calculateTotalDuration();
        }
        return totalDuration;
    }

    public long calculateTotalDuration() {
        long totalDuration;
        totalDuration = Math.max(eventDuration, additionalDuration);

        //add embedded terms with temporal duration
        for (Term t : this) {
            if (t instanceof Interval) {
                totalDuration = Math.max(
                        totalDuration,
                        ((Interval)t).duration()
                );
            }
        }

        if (totalDuration < 0)
            throw new RuntimeException("cycles must be >= 0");
        return totalDuration;
    }

    public static Term makeParallel(final Term[] a) {

        //count how many intervals so we know how to resize the final arrays
        final int intervalsPresent = Interval.intervalCount(a);

        if (intervalsPresent == 0) {
            return new Parallel(a, 0);
        }
        else {

            //if intervals are present:
            Term[] b = new Term[a.length - intervalsPresent];

            if (b.length == 0)
                throw new RuntimeException("Parallel with no non-Interval terms");

            long duration = 0;
            int p = 0;
            long minTermDuration = 0;
            for (final Term x : a) {
                if (x instanceof CyclesInterval) {
                    duration = Math.max(duration, ((CyclesInterval) x).duration());
                } else {
                    b[p++] = x;
                    if (x instanceof Interval) {
                        minTermDuration = Math.max(minTermDuration, ((Interval)x).duration() );
                    }
                }
            }
            if (duration <= minTermDuration)
                duration = 0; //will have no effect


            //return a singleton if no additional timing information present
            if ((duration == 0) && (b.length == 1)) {
                return b[0];
            }

            return new Parallel(b, duration);
        }
    }

}
