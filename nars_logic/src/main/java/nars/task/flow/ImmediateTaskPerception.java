package nars.task.flow;

import nars.NAR;
import nars.task.Task;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * accumulates a buffer of iasks which can be delivered at a specific rate.
 * <p>
 * consists of 2 buffers which are sampled in some policy each cycle
 * <p>
 * "input" - a dequeue in which input tasks are appended
 * in the order they are received
 * <p>
 * "newTasks" - a priority buffer, emptied in batches,
 * in which derived tasks and other feedback accumulate
 * <p>
 * Sub-interfaces
 * <p>
 * Storage
 * <p>
 * Delivery (procedure for cyclical input policy
 */
public class ImmediateTaskPerception extends TaskPerception {

    /**
     * max # of inputs to perceive per cycle; -1 means unlimited (attempts to drains input to empty each cycle)
     */
    public final AtomicInteger inputsPerCycleMax = new AtomicInteger(1000);

    public final Deque<Task> buffer = new ArrayDeque();


    public ImmediateTaskPerception(NAR nar, Predicate<Task> filter, Consumer<Task> receiver) {
        super(nar.memory, filter, receiver);
    }

    @Override
    public void accept(Task t) {
        if (filter == null || filter.test(t)) {

                if (t.isDeleted()) {
                    throw new RuntimeException("task deleted");
                }

            buffer.add(t);
        }
    }

    @Override
    public void clear() {
        buffer.clear();
    }


    /** sends the next batch of tasks to the receiver */
    @Override
    public void send() {


        int s = buffer.size();
        int n = Math.min(s, inputsPerCycleMax.get()); //counts down successful sends
        int r = n; //actual cycles counted

        for(Task t: buffer) {
            receiver.accept(t);
        }


        //n will be equal to or greater than r
    /*   for (; n > 0 && r > 0; r--) {
            final Task t = buffer.removeFirst();

            if (t.isDeleted()) {
                //the task became deleted while this was in the buffer. no need to repeat Memory.removed
                continue;
            }

            receiver.accept(t);
            n--;
        }
*/
    }
}
