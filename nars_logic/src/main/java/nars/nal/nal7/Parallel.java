package nars.nal.nal7;

import nars.Op;
import nars.term.Term;
import nars.term.TermSet;
import nars.term.compound.GenericCompound;

import java.io.IOException;

import static nars.Symbols.ARGUMENT_SEPARATOR;

/**
 * Parallel Conjunction (&|)
 */
public class Parallel extends GenericCompound implements Intermval {

    //total duration (cached), the maximum duration of all included temporal terms
    transient int totalDuration = -1;

    //supplied by the memory, used as the default subterm event duration if they do not implement their own Interval.duration()
    private int eventDuration = 0;


    public Parallel(Term[] arg) {
        super(Op.PARALLEL, -1, TermSet.the(arg));
    }


//    @Override
//    public Term clone(Term[] replaced) {
//        return Parallel.makeParallel(replaced);
//    }

//    @Override
//    public final int bytesLength() {
//        return super.bytesLength()
//                + 4 /* for storing eventDuration */
//                ;
//    }
//
//    @Override
//    public final void appendSubtermBytes(ByteBuf b) {
//        super.appendSubtermBytes(b);
//
//        //add intermval suffix
//        b.addUnsignedInt(eventDuration);
//    }


    @Override public void appendArgs(Appendable p, boolean pretty, boolean appendedOperator) throws IOException {
        p.append(ARGUMENT_SEPARATOR);
        if (pretty) p.append(' ');

        super.appendArgs(p, pretty, false);
    }

    @Override
    public void setDuration(int duration) {
        super.setDuration(duration);
        if (duration!= eventDuration) {
            eventDuration = duration;
            totalDuration = -1; //force recalc
        }
    }

    @Override
    public final int duration() {
        int totalDuration = this.totalDuration;
        if (totalDuration == -1) {
            return this.totalDuration = calculateTotalDuration(eventDuration);
        }
        return totalDuration;
    }

    @Override
    public final int duration(int eventDuration) {
        if (totalDuration < 0 || eventDuration!=this.eventDuration) {
            return calculateTotalDuration(eventDuration);
        }
        return totalDuration;
    }


    int calculateTotalDuration(int eventDuration) {
        int totalDuration = eventDuration;

        //add embedded terms with temporal duration
        int s = size();
        for (int i = 0; i < s; i++) {
            Term t = term(i);
            if (t instanceof Interval) {
                totalDuration = Math.max(totalDuration,
                        ((Interval)t).duration(eventDuration));
            }
        }

        if (totalDuration <= 0)
            throw new RuntimeException("cycles must be > 0: " + this);

        return totalDuration;
    }


    @Override
    public int[] intervals() {
        return null;
    }
}
