package nars.task.flow;

import nars.NAR;
import nars.budget.TaskAccumulator;
import nars.task.Task;
import nars.util.data.MutableInteger;

import java.util.function.Consumer;
import java.util.function.Predicate;

/** sorts and de-duplicates incoming tasks into a capacity-limited buffer */
public class SortedTaskPerception extends TaskPerception {

    final TaskAccumulator buffer;

    public final MutableInteger inputPerCycle = new MutableInteger();

    public SortedTaskPerception(NAR nar,
                                Predicate<Task> filter,
                                Consumer<Task> receiver,
                                int capacity, int inputPerCycle) {
        super(nar.memory, filter, receiver);

        this.inputPerCycle.set( inputPerCycle );

        //TODO use MutableInteger for capacity for all Bags
        buffer = new TaskAccumulator(capacity);
    }

    @Override
    public final void accept(Task t) {
        if (!t.isDeleted())
            buffer.put(t);
    }

    @Override
    public final void send() {
        //ItemAccumulator<> b = this.buffer;
        TaskAccumulator buffer = this.buffer;
        int available = size();
        if (available > 0) {
            buffer.pop(receiver, Math.min(available, inputPerCycle.intValue()));
        }
    }

    public final int size() {
        return buffer.size();
    }

    @Override
    public void clear() {
        buffer.clear();
    }
}
