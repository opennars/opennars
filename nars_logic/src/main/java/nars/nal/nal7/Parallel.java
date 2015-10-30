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

    //total duration (cached), the maximum duration of all included temporal terms
    transient long totalDuration = -1;

    //supplied by the memory, used as the default subterm event duration if they do not implement their own Interval.duration()
    private int eventDuration;


    private Parallel(Term[] arg) {
        super(arg = Terms.toSortedSetArray(arg));
        init(arg);
    }

    @Override
    public final boolean isCommutative() {
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
        return new Parallel(term);
    }

    @Override
    public Term clone(Term[] replaced) {
        return Parallel.makeParallel(replaced);
    }

    @Override
    public final int getByteLen() {
        return super.getByteLen()
                + 4 /* for storing eventDuration */
                ;
    }

    @Override
    protected final void appendBytes(int numArgs, ByteBuf b) {
        super.appendBytes(numArgs, b);

        //add intermval suffix
        b.addUnsignedInt(eventDuration);
    }


    @Override public void appendArgs(Appendable p, boolean pretty, boolean appendedOperator) throws IOException {
        p.append(ARGUMENT_SEPARATOR);
        if (pretty) p.append(' ');

        super.appendArgs(p, pretty, false);
    }

    @Override
    public <T extends Term> T  setDuration(int duration) {
        super.setDuration(duration);
        this.eventDuration = duration;
        this.totalDuration = -1; //force recalc
        return (T)this;
    }

    @Override
    public final long duration() {
        final long totalDuration = this.totalDuration;
        if (totalDuration == -1) {
            return this.totalDuration = calculateTotalDuration();
        }
        return totalDuration;
    }

    long calculateTotalDuration() {
        long totalDuration = eventDuration;

        //add embedded terms with temporal duration
        for (Term t : this) {
            if (t instanceof Interval) {
                totalDuration = Math.max(totalDuration,
                        ((Interval)t).duration());
            }
        }

        if (totalDuration <= 0)
            throw new RuntimeException("cycles must be > 0");

        return totalDuration;
    }

    public static Term makeParallel(final Term[] a) {

        //count how many intervals so we know how to resize the final arrays
        final int intervalsPresent = Interval.intervalCount(a);
        final int subterms = a.length - intervalsPresent;

        if (subterms == 0)
            return null;

        if (subterms == 1)
            return Interval.firstNonIntervalIn(a); //unwrap the only non-interval subterm

        if (intervalsPresent == 0)
            return new Parallel(a); //no intervals need to be removed

        //otherwise, intervals are present:

        Term[] b = new Term[subterms];

        int p = 0;
        for (final Term x : a) {
            if (!(x instanceof CyclesInterval))
                b[p++] = x;
        }

        return new Parallel(b);

    }

}
