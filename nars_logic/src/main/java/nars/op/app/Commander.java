package nars.op.app;

import com.google.common.collect.Iterators;
import nars.Memory;
import nars.NAR;
import nars.budget.Budget;
import nars.budget.ItemAccumulator;
import nars.event.NARReaction;
import nars.task.Task;
import nars.util.event.Observed;

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
public class Commander extends NARReaction implements Consumer<Memory> {

    public final ItemAccumulator<Task> commands;
    public final Iterator<Task> commandIterator;
    private final Observed.DefaultObserved.DefaultObservableRegistration cycleEnd;
    private final NAR nar;

    /** how far away from the occurence time of a temporal belief before it is deleted */
    private final int maxTemporalBeliefAge;
    private final int maxTemporalBeliefDurations = 16 /* should be tuned */;


    float priorityPerCycle = 1,
            priorityRemaining = 0; //change left over from last cycle

    public Commander(NAR nar, boolean active) {
        this(nar, new ItemAccumulator<>(Budget.plus), active);
    }

    public Commander(NAR nar, ItemAccumulator<Task> buffer, boolean active) {
        super(nar);

        this.nar = nar;

        this.cycleEnd = active ?
                nar.memory.eventCycleEnd.on(this) : null;

        this.commands = buffer;
        commandIterator = Iterators.cycle(commands.items.keySet());


        this.maxTemporalBeliefAge = nar.memory.duration() * maxTemporalBeliefDurations;


        nar.memory.eventTaskProcess.on((tp) -> {
            Task t = tp.getTask();
            if (!t.isDeleted())
                input(t);
        });
    }


    @Override
    public void setActive(boolean b) {
        super.setActive(b);
        if (b == false) {
            commands.clear();
        }
    }

    @Override
    public void event(Class event, Object... args) {

    }

    protected void input(Task t) {
        if (/*(t.isGoal() || t.isQuestOrQuestion()) && */ t.isInput()) {
            commands.add(t);
        }
    }


    @Override
    public void accept(Memory memory) {

        //TODO iterate tasks until allotted priority has been reached,
        //  TaskProcess each

        if (commands.size() == 0) return;

        int inputsPerCycle = 1; //Math.min(1, commands.size());

        final long now = nar.time();

        final Iterator<Task> commandIterator = this.commandIterator;
        for (int i = 0; i < inputsPerCycle; i++) {
            if (commandIterator.hasNext()) {
                final Task next = commandIterator.next();
                if (valid(now, next))
                    memory.input(next);
                else
                    commandIterator.remove();
            }
        }

    }

    public final boolean valid(final long now, final Task t) {

        if (t.getBudget().isDeleted())
            return false;

        if (!t.isEternal()) {
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
