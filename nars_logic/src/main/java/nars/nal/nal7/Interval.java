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


}
