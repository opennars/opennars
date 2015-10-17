package nars.io;

import nars.NAR;
import nars.budget.Budget;
import nars.budget.ItemAccumulator;
import nars.task.Task;
import nars.util.data.MutableInteger;

import java.util.function.Consumer;
import java.util.function.Predicate;

/** sorts and de-duplicates incoming tasks into a capacity-limited buffer */
public class SortedTaskPerception extends TaskPerception {

    final ItemAccumulator<Task> buffer = new ItemAccumulator(Budget.plus);

    final MutableInteger capacity = new MutableInteger();
    final MutableInteger inputPerCycle = new MutableInteger();

    public SortedTaskPerception(NAR nar, Predicate<Task> filter, Consumer<Task> receiver, int capacity, int inputPerCycle) {
        super(nar.memory, filter, receiver);
        this.capacity.set(capacity);
        this.inputPerCycle.set( inputPerCycle );
    }

    @Override
    public void accept(Task t) {
        if (!t.isDeleted())
            buffer.add(t);
    }

    @Override
    public void send() {
        buffer.limit(capacity.intValue());
        buffer.next(inputPerCycle.intValue(), receiver);
    }

    @Override
    public void clear() {
        buffer.clear();
    }
}
