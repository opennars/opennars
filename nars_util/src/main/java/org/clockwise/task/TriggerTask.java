package org.clockwise.task;

import org.clockwise.Trigger;

/**
* {@link Task} implementation defining a {@code Runnable} to be executed according to a
* given {@link Trigger}.
*/
public class TriggerTask extends Task {

    private final Trigger trigger;

    /**
     * Create a new {@link TriggerTask}.
     * 
     * @param runnable
     *            the underlying task to execute
     * @param trigger
     *            specifies when the task should be executed
     */
    public TriggerTask(Runnable runnable, Trigger trigger) {
        super(runnable);
        this.trigger = trigger;
    }

    public Trigger getTrigger() {
        return trigger;
    }
}
