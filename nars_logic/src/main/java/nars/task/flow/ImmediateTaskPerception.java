package nars.task.flow;

import nars.NAR;
import nars.bag.impl.CurveBag;
import nars.bag.impl.LevelBag;
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


    boolean SingleStep = false;
    NAR nar=null;
    public ImmediateTaskPerception(boolean singleStep, NAR nar, Predicate<Task> filter, Consumer<Task> receiver) {
        super(nar.memory, filter, receiver);
        this.SingleStep = singleStep;
        this.nar=nar;
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
        if(!SingleStep) {
            CurveBag bag = new CurveBag(buffer.size(),nar.memory.random); //new LevelBag(10,buffer.size());//new CurveBag(buffer.size(),nar.memory.random);
            bag.mergeMax();
            try {
                for (Task t : buffer) {
                    if (t != null && t.getBudget() != null && !t.isInput())
                        bag.put(t);
                }
            }catch(Exception ex) {} //no crash on concurrent exception ^^
            Task t = (Task) bag.pop();

            //todo create a bag of the buffer, sample one element with probability determmined by budget priority, and then clear the buffer
            if (t!=null && !(t.isJudgment() && t.getTruth().getExpectation() < 0.5)) {
                receiver.accept(t);
            }

            buffer.stream().filter(tt -> tt.isInput()).forEach(receiver::accept);
            buffer.clear();
        } else {
            for(Task t: buffer) {
                if (!(t.isJudgment() && t.getTruth().getExpectation() < 0.5)) {
                    receiver.accept(t);
                }
            }
            buffer.clear();
        }
    }
}
