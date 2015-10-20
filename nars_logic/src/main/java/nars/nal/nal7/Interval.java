package nars.nal.nal7;

import nars.term.Term;
import nars.term.TermMetadata;

/**
 * Term which specifies a temporal duration in which an event occurrs
 */
public interface Interval extends TermMetadata {

    /** number of cycles that this sequence spans from start to end (= sum of intermval values) */
    long duration();

    /** returns a count of how many interval terms are in the array */
    static int intervalCount(Term[] a) {
        int c = 0;
        for (Term x : a) {
            if (x instanceof CyclesInterval) c++;
        }
        return c;
    }
}
