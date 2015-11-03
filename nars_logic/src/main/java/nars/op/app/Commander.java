package nars.op.app;

import com.google.common.collect.Iterators;
import nars.NAR;
import nars.budget.TaskAccumulator;
import nars.nal.nal7.Temporal;
import nars.task.Task;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Captures input goals and questions into a buffer
 * (ie. input by user) and re-processes them in a
 * controllable pattern, frequency,
 * and priority -- in addition to ordinary system activity.
 *
 * This guides inference according to the explicit
 * inputs that were input, focusing it towards those
 * outcomes.
 *
 * Analogous to a continuous echo/delay effect,
 * or a sustain effecct.
 */
public class Commander implements Consumer<NAR> {

    public final TaskAccumulator commands;
    public final Iterator<Task<?>> commandIterator;
//    private final On cycleEnd;
//    private final NAR nar;

    /** how far away from the occurence time of a temporal belief before it is deleted */
    private final int maxTemporalBeliefAge;
    private final int maxTemporalBeliefDurations = 16 /* should be tuned */;

    int inputsPerFrame = 1;
    int cycleDivisor = 6;

//    float priorityPerCycle = 1,
//            priorityRemaining = 0; //change left over from last cycle

    public Commander(NAR nar, int capacity) {
        this(nar, new TaskAccumulator(capacity));
    }

    public Commander(NAR nar, TaskAccumulator buffer) {
        super();

        //this.nar = nar;

        //TODO reset event
        //this.cycleEnd = active ?
                nar.memory.eventFrameStart.on(this);
                //: null;

        this.commands = buffer;
        commandIterator = Iterators.cycle(commands);


        this.maxTemporalBeliefAge = nar.memory.duration() * maxTemporalBeliefDurations;


        nar.memory.eventTaskProcess.on((tp) -> {
            Task t = tp.getTask();
            if (t.isInput() && !commands.contains(t))
                input(t);
        });
    }


//    @Override
//    public void setActive(boolean b) {
//        super.setActive(b);
//        if (!b) {
//            commands.clear();
//        }
//    }


    protected void input(Task t) {
        if (/*(t.isGoal() || t.isQuestOrQuestion()) && */ t.isInput()) {
            commands.put(t);
        }
    }


    @Override
    public void accept(NAR nar) {

        //TODO iterate tasks until allotted priority has been reached,
        //  TaskProcess each

        int cs = commands.size();
        if (cs == 0) return;


        final long now = nar.time();
        if (now%cycleDivisor!= 0) return;

        Iterator<Task<?>> commandIterator = this.commandIterator;
        for (int i = 0; i < inputsPerFrame; i++) {
            if (commandIterator.hasNext()) {
                final Task next = commandIterator.next();
                if (valid(now, next))
                    nar.input(next);
                else
                    commandIterator.remove();
            }
        }

    }

    public final boolean valid(final long now, final Task t) {

        if (t.getBudget().isDeleted())
            return false;

        if (!Temporal.isEternal(t.getOccurrenceTime())) {
            long age = Math.abs( now - t.getOccurrenceTime() );
            if (age > maxTemporalBeliefAge)
                return false;
        }

        return true;
    }

    //TODO getBufferPrioritySum
    //TODO setPriorityPerCycle
    //TODO max tasks limit
    //TODO rebudgeting Function<Budget,Budget> for manipulating values
    //add with TTL?
}
