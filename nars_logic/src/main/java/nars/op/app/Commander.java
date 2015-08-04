package nars.op.app;

import com.google.common.collect.Iterators;
import nars.Events;
import nars.NAR;
import nars.budget.ItemAccumulator;
import nars.budget.ItemComparator;
import nars.event.NARReaction;
import nars.task.Task;

import java.util.Iterator;

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
 */
public class Commander extends NARReaction {

    public final ItemAccumulator<Task> commands;
    public final Iterator<Task> commandIterator;
    float priorityPerCycle = 1,
            priorityRemaining = 0; //change left over from last cycle

    public Commander(NAR nar) {
        this(nar, new ItemAccumulator(new ItemComparator.Plus()));
    }

    public Commander(NAR nar, ItemAccumulator<Task> buffer) {
        super(nar, Events.CycleEnd.class, Events.IN.class);
        this.commands = buffer;
        commandIterator = Iterators.cycle(commands.items);
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
        if (event == Events.CycleEnd.class) {
            cycle();
        }
        else if (event == Events.IN.class) {
            Task t = (Task)args[0];
            input(t);
        }
    }

    protected void input(Task t) {
        if ((t.isGoal() || t.isQuestOrQuestion()) && t.isInput()) {
            commands.add(t);
        }
    }

    protected void cycle() {

        //TODO iterate tasks until allotted priority has been reached,
        //  TaskProcess each

    }

    //TODO getBufferPrioritySum
    //TODO setPriorityPerCycle
    //TODO max tasks limit
    //TODO rebudgeting Function<Budget,Budget> for manipulating values
    //add with TTL?
}
