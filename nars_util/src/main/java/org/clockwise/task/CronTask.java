package org.clockwise.task;

import org.clockwise.CronTrigger;

public class CronTask extends TriggerTask {

    private final String expression;

    /**
     * Create a new {@code CronTask}.
     * 
     * @param runnable
     *            the underlying task to execute
     * @param expression
     *            cron expression defining when the task should be executed
     */
    public CronTask(Runnable runnable, String expression) {
        this(runnable, new CronTrigger(expression));
    }

    /**
     * Create a new {@code CronTask}.
     * 
     * @param runnable
     *            the underlying task to execute
     * @param cronTrigger
     *            the cron trigger defining when the task should be executed
     */
    public CronTask(Runnable runnable, CronTrigger cronTrigger) {
        super(runnable, cronTrigger);
        this.expression = cronTrigger.getExpression();
    }

    public String getExpression() {
        return this.expression;
    }
}
