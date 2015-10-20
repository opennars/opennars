package nars.nal.nal7;

import nars.Op;
import nars.nal.nal5.Conjunctive;
import nars.term.Term;

/**
 * Parallel Conjunction (&|)
 */
public class Parallel extends Conjunctive implements Intermval {

    transient private long duration = -1;

    public Parallel(Term... arg) {
        super(arg);
        init(arg);
    }

    @Override
    public final Op op() {
        return Op.PARALLEL;
    }

    @Override
    public final int getTemporalOrder() {
        return Temporal.ORDER_CONCURRENT;
    }

    @Override
    public Term clone() {
        return new Parallel(term);
    }

    @Override
    public Term clone(Term[] replaced) {
        return Conjunctive.make(replaced, Temporal.ORDER_CONCURRENT);
    }

    @Override
    public final int[] intervals() {
        return null; //N/A
    }

    @Override
    public final long duration() {
        return duration;
    }
}
