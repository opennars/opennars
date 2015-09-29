package org.clockwise.task;

class IntervalTask extends Task {

    private final long interval;
    private final long initialDelay;

    /**
     * Create a new {@code IntervalTask}.
     * 
     * @param runnable
     *            the underlying task to execute
     * @param interval
     *            how often in milliseconds the task should be executed
     * @param initialDelay
     *            initial delay before first execution of the task
     */
    private IntervalTask(Runnable runnable, long interval, long initialDelay) {
        super(runnable);
        this.interval = interval;
        this.initialDelay = initialDelay;
    }

    /**
     * Create a new {@code IntervalTask} with no initial delay.
     * 
     * @param runnable
     *            the underlying task to execute
     * @param interval
     *            how often in milliseconds the task should be executed
     */
    public IntervalTask(Runnable runnable, long interval) {
        this(runnable, interval, 0);
    }

    public long getInterval() {
        return this.interval;
    }

    public long getInitialDelay() {
        return this.initialDelay;
    }

}
