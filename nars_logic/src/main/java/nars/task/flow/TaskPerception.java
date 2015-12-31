package nars.task.flow;

import nars.Memory;
import nars.task.Task;

import java.util.function.Consumer;

/**
 * Receives tasks which are input to a memory
 */
public abstract class TaskPerception implements Consumer<Task> {

    /**
     * where to send output
     */
    //private final Active active = new Active();
    private final Memory memory;

    protected TaskPerception(Memory m) {

        this.memory = m;

        //active.add(
            m.eventInput.on(this);

            m.eventReset.on((M) -> clear() );
        //);

    }

    protected TaskPerception(Memory m, Consumer<Task> eachFrameSupplyTo) {
        this(m);
        //active.add(
            m.eventFrameStart.on((M) -> nextFrame(eachFrameSupplyTo));
        //);
    }

    public abstract void forEach(Consumer<? super Task> each);

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

        @Override
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
    public abstract void nextFrame(Consumer<Task> receiver);

    public abstract void clear();
}
