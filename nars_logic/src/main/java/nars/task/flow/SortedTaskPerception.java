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

    /**
     *
     * @param nar
     * @param filter
     * @param receiver
     * @param capacity
     * @param inputPerCycle -1 for everything
     */
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
        if (!t.isDeleted()) {
            Task overflow = buffer.put(t);
            if (overflow!=null)
                onOverflow(overflow);
        }
    }

    protected void onOverflow(Task t) {

    }

    @Override
    public void forEach(Consumer<? super Task> each) {
        buffer.forEach(each);
    }

    @Override
    public final void nextFrame() {
        //ItemAccumulator<> b = this.buffer;
        TaskAccumulator buffer = this.buffer;
        int available = size();
        if (available > 0) {

            int inputsPerCyc = inputPerCycle.intValue();
            if (inputsPerCyc == -1) {
                //send everything
                inputsPerCyc = available;
            }

            buffer.pop(receiver,
                Math.min(available, inputsPerCyc)
            );
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
