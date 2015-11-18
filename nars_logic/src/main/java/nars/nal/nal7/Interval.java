package nars.nal.nal7;

import nars.term.Term;

/**
 * Term which specifies a temporal duration in which an event occurrs
 */
public interface Interval {

    /** number of cycles that this sequence spans from start to end (= sum of intermval values) */
    int duration();

    /** computes the duration if it were perceived via the specified default eventDuration */
    default int duration(int eventDuration) {
        return duration();
    }


    /** returns a count of how many interval terms are in the array */
    static int intervalCount(Term[] a) {
        int c = 0;
        for (Term x : a) {
            if (x instanceof CyclesInterval) {
                //long d = ((CyclesInterval)x).duration();
                c++;
            }
        }
        return c;
    }


    static Term firstNonIntervalIn(Term[] a) {

        for (Term x : a) {
            if (!(x instanceof CyclesInterval)) {
                //long d = ((CyclesInterval)x).duration();
                return x;
            }
        }
        return null;
    }
}
