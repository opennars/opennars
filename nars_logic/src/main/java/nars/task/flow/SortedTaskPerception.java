package nars.task.flow;

import nars.NAR;
import nars.budget.TaskAccumulator;
import nars.task.Task;
import nars.util.data.MutableInteger;

import java.util.function.Consumer;
import java.util.function.Predicate;

/** sorts and de-duplicates incoming tasks into a capacity-limited buffer */
public class SortedTaskPerception extends TaskPerception {

    final TaskAccumulator<?> buffer;

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
    final public void accept(Task t) {
        if (!t.isDeleted())
            buffer.put(t);
    }

    @Override
    final public void send() {
        //ItemAccumulator<> b = this.buffer;
        final TaskAccumulator<?> buffer = this.buffer;
        if (!buffer.isEmpty()) {

            //TODO special case where size <= inputPerCycle, the entire bag can be flushed in one operation

            int n = Math.min(size(), inputPerCycle.intValue());
            final Consumer<Task> receiver = this.receiver;

            for (int i = 0; i < n; i++) {
                Task<?> b = buffer.pop();
                if (b!=null)
                    receiver.accept(b);
                //else why?
            }

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
