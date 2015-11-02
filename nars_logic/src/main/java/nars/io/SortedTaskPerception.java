package nars.io;

import nars.NAR;
import nars.budget.TaskAccumulator;
import nars.task.Task;
import nars.util.data.MutableInteger;

import java.util.function.Consumer;
import java.util.function.Predicate;

/** sorts and de-duplicates incoming tasks into a capacity-limited buffer */
public class SortedTaskPerception extends TaskPerception {

    final TaskAccumulator<?> buffer;

    final MutableInteger inputPerCycle = new MutableInteger();

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
    final public void accept(Task t) {
        if (!t.isDeleted())
            buffer.put(t);
    }

    @Override
    final public void send() {
        //ItemAccumulator<> b = this.buffer;
        if (!buffer.isEmpty()) {

            buffer.print(System.out);
            System.out.println();

            //TODO special case where size <= inputPerCycle, the entire bag can be flushed in one operation

            int n = Math.min(size(), inputPerCycle.intValue());
            for (int i = 0; i < n; i++)
                receiver.accept(buffer.pop());

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
