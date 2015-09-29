package org.clockwise;

/**
 * Context object encapsulating last execution times and last completion time of
 * a given task.
 */
interface TriggerContext {

    /**
     * Return the last <i>scheduled</i> execution time of the task, or
     * {@code null} if not scheduled before.
     */
    long lastScheduledExecutionTime();

    /**
     * Return the last <i>actual</i> execution time of the task, or {@code null}
     * if not scheduled before.
     */
    long lastActualExecutionTime();

    /**
     * Return the last completion time of the task, or {@code null} if not
     * scheduled before.
     */
    long lastCompletionTime();
}
