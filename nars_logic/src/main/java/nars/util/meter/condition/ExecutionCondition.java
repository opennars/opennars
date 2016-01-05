package nars.util.meter.condition;

import nars.NAR;
import nars.nal.nal7.Tense;
import nars.term.atom.Atom;

/**
 * measures the occurrence of an execution within certain
 * time and expectation ranges
 */
public class ExecutionCondition implements NARCondition {

    private final Atom opTerm;
    private final long start, end;
    private final float minExpect, maxExpect;
    private boolean success = false;
    private long successTime = Tense.TIMELESS;

    public ExecutionCondition(NAR n, long start, long end, Atom opTerm, float minExpect, float maxExpect) {

        this.start = start;
        this.end = end;
        this.opTerm = opTerm;
        this.minExpect = minExpect;
        this.maxExpect = maxExpect;

        n.onExec(opTerm, t -> {

            if (!success) {
                long now = n.time();
                if ((now >= start) && (now <= end)) {
                    float expect = t.task.getExpectation();
                    if ((expect >= minExpect) && (expect <= maxExpect)) {
                        success = true;
                        successTime = now;
                    }
                }
            }
        });

    }

    @Override
    public long getSuccessTime() {
        return successTime;
    }

    @Override
    public boolean isTrue() {
        return success;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + opTerm + ", time in " +
                start + ".." + end + ", expect in " + minExpect + ".." + maxExpect +
                //minExpect, maxExpect
                ']';
    }

    @Override
    public long getFinalCycle() {
        return end;
    }


}
