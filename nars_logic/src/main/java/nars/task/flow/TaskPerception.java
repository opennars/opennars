package nars.task.flow;

import nars.Memory;
import nars.task.Task;
import nars.util.event.Active;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Receives tasks which are input to a memory
 */
public abstract class TaskPerception implements Consumer<Task> {


    /**
     * determines if content can enter
     */
    protected final Predicate<Task> filter;

    /**
     * where to send output
     */
    protected final Consumer<Task> receiver;

    private final Active active = new Active();
    private final Memory memory;

    public TaskPerception(Memory m,
                          Predicate<Task> filter,
                          Consumer<Task> receiver) {

        this.filter = filter;
        this.receiver = receiver;
        this.memory = m;

        active.add(
            m.eventInput.on(this),
            m.eventFrameStart.on((M) -> nextFrame()),
            m.eventReset.on((M) -> clear() )
        );

    }

    abstract public void forEach(Consumer<? super Task> each);

    public static class TaskBufferStats implements Consumer<Task> {
        public long time;
        public long minCreationTime;
        public long maxCreationTime;
        public int count;
        //TODO minBudget, maxBudget etc

        public TaskBufferStats(Memory m) {
            reset(m);
        }

        public void reset(Memory m) {
            minCreationTime = Integer.MAX_VALUE;
            maxCreationTime = Integer.MIN_VALUE;
            count = 0;
            time = m.time();
        }

        public void accept(Task t) {
            long c = t.getCreationTime();
            if (c < minCreationTime) minCreationTime = c;
            if (c > maxCreationTime) maxCreationTime = c;
            count++;
        }

        @Override
        public String toString() {
            return "TaskBufferStats{" +
                    "time=" + time +
                    ", minCreationTime=" + minCreationTime +
                    ", maxCreationTime=" + maxCreationTime +
                    ", count=" + count +
                    '}';
        }
    }

    public TaskBufferStats getStatistics() {
        //minCreationTime, maxCreationTime
        TaskBufferStats s = new TaskBufferStats(memory);
        forEach(s);
        return s;
    }

    @Override
    public abstract void accept(Task t);

    /** here the buffer should apply any updates and send some/all inputs to the Memory */
    public abstract void nextFrame();

    public abstract void clear();
}
