package nars.util.meter;

import nars.NAR;
import nars.task.Temporal;
import nars.util.event.On;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * Counts the various removal reasons of tasks
 * useful for assessing the efficiency of the reasoner
 */
public class TaskRemovalReasons {

    private final On removals, all;
    final Frequency freq = new Frequency();
    final SummaryStatistics lifespanOfRemovals = new SummaryStatistics();
    private final NAR nar;

    long processed = 0;

    public TaskRemovalReasons(NAR n) {
        this.nar = n;
        all = n.memory.eventTaskProcess.on(t -> processed++);
        removals = n.memory.eventTaskRemoved.on(t -> {
            freq.addValue(t.getLogLast().toString());
            final long age = ((Temporal)t).getLifespan(nar.memory);
            lifespanOfRemovals.addValue(age);
        });
    }

    public SummaryStatistics getLifespanOfActive() {
        SummaryStatistics s = new SummaryStatistics();
        nar.forEachConceptTask(t -> s.addValue(((Temporal)t).getLifespan(nar.memory)));
        return s;
    }

    public String toString() {
        long removed = freq.getSumFreq();
        return freq.toString() + '\n' +
                removed + " (eventually) removed, " + processed + " processed" +
                ",\n\n" +
                "lifespan (of removals)=" + lifespanOfRemovals + '\n' +
                "lifespan (of active)=" + getLifespanOfActive();
    }
}
