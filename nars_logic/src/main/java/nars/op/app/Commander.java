package nars.op.app;

import com.google.common.collect.Iterators;
import nars.Events;
import nars.Memory;
import nars.NAR;
import nars.budget.Budget;
import nars.budget.ItemAccumulator;
import nars.event.NARReaction;
import nars.process.TaskProcess;
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


    float priorityPerCycle = 1,
            priorityRemaining = 0; //change left over from last cycle

    public Commander(NAR nar) {
        this(nar, new ItemAccumulator(Budget.plus));
    }

    public Commander(NAR nar, ItemAccumulator<Task> buffer) {
        super(nar);
        this.nar = nar;
        this.cycleEnd = nar.memory.eventCycleEnd.on(this);
        this.commands = buffer;
        commandIterator = Iterators.cycle(commands.items);

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


        int inputsPerCycle = 1;

        for (int i = 0; i < inputsPerCycle; i++) {
            if (commandIterator.hasNext()) {
                Task next = commandIterator.next();
                memory.input(next);
            }
        }

    }

    //TODO getBufferPrioritySum
    //TODO setPriorityPerCycle
    //TODO max tasks limit
    //TODO rebudgeting Function<Budget,Budget> for manipulating values
    //add with TTL?
}
