package org.clockwise;

/**
 * Simple data holder implementation of the {@link TriggerContext} interface.
 * 
 * @author Juergen Hoeller
 * @since 3.0
 */
public class SimpleTriggerContext implements TriggerContext {
    private volatile long lastScheduledExecutionTime;
    private volatile long lastActualExecutionTime;
    private volatile long lastCompletionTime;

    /**
     * Create a SimpleTriggerContext with all time values set to {@code null}.
     */
    public SimpleTriggerContext() {
    }

    /**
     * Create a SimpleTriggerContext with the given time values.
     * 
     * @param lastScheduledExecutionTime
     *            last <i>scheduled</i> execution time
     * @param lastActualExecutionTime
     *            last <i>actual</i> execution time
     * @param lastCompletionTime
     *            last completion time
     */
    public SimpleTriggerContext(long lastScheduledExecutionTime, long lastActualExecutionTime, long lastCompletionTime) {
        this.lastScheduledExecutionTime = lastScheduledExecutionTime;
        this.lastActualExecutionTime = lastActualExecutionTime;
        this.lastCompletionTime = lastCompletionTime;
    }

    /**
     * Update this holder's state with the latest time values.
     * 
     * @param lastScheduledExecutionTime
     *            last <i>scheduled</i> execution time
     * @param lastActualExecutionTime
     *            last <i>actual</i> execution time
     * @param lastCompletionTime
     *            last completion time
     */
    public void update(long lastScheduledExecutionTime, long lastActualExecutionTime, long lastCompletionTime) {
        this.lastScheduledExecutionTime = lastScheduledExecutionTime;
        this.lastActualExecutionTime = lastActualExecutionTime;
        this.lastCompletionTime = lastCompletionTime;
    }

    @Override
    public long lastScheduledExecutionTime() {
        return this.lastScheduledExecutionTime;
    }

    @Override
    public long lastActualExecutionTime() {
        return this.lastActualExecutionTime;
    }

    @Override
    public long lastCompletionTime() {
        return this.lastCompletionTime;
    }
}