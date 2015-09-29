package org.clockwise;

import java.util.concurrent.TimeUnit;

/**
 * A trigger for periodic task execution. The period may be applied as either
 * fixed-rate or fixed-delay, and an initial delay value may also be configured.
 * The default initial delay is 0, and the default behavior is fixed-delay (i.e.
 * the interval between successive executions is measured from each
 * <emphasis>completion</emphasis> time). To measure the interval between the
 * scheduled <emphasis>start</emphasis> time of each execution instead, set the
 * 'fixedRate' property to {@code true}.
 * 
 * <p>
 * Note that the TaskScheduler interface already defines methods for scheduling
 * tasks at fixed-rate or with fixed-delay. Both also support an optional value
 * for the initial delay. Those methods should be used directly whenever
 * possible. The value of this Trigger implementation is that it can be used
 * within components that rely on the Trigger abstraction. For example, it may
 * be convenient to allow periodic triggers, cron-based triggers, and even
 * custom Trigger implementations to be used interchangeably.
 * 
 * @author Mark Fisher
 * @since 3.0
 */
public class PeriodicTrigger implements Trigger {
    private final long period;
    private final TimeUnit timeUnit;
    private volatile long initialDelay = 0;
    private volatile boolean fixedRate = false;

    /**
     * Create a trigger with the given period in milliseconds.
     */
    public PeriodicTrigger(long period) {
        this(period, null);
    }

    /**
     * Create a trigger with the given period and time unit. The time unit will
     * apply not only to the period but also to any 'initialDelay' value, if
     * configured on this Trigger later via {@link #setInitialDelay(long)}.
     */
    private PeriodicTrigger(long period, TimeUnit timeUnit) {
        //Assert.isTrue(period >= 0, "period must not be negative");
        this.timeUnit = (timeUnit != null ? timeUnit : TimeUnit.MILLISECONDS);
        this.period = this.timeUnit.toMillis(period);
    }

    /**
     * Specify the delay for the initial execution. It will be evaluated in
     * terms of this trigger's {@link TimeUnit}. If no time unit was explicitly
     * provided upon instantiation, the default is milliseconds.
     */
    public void setInitialDelay(long initialDelay) {
        this.initialDelay = this.timeUnit.toMillis(initialDelay);
    }

    /**
     * Specify whether the periodic interval should be measured between the
     * scheduled start times rather than between actual completion times. The
     * latter, "fixed delay" behavior, is the default.
     */
    public void setFixedRate(boolean fixedRate) {
        this.fixedRate = fixedRate;
    }

    /**
     * Returns the time after which a task should run again.
     */
    @Override
    public long nextExecutionTime(TriggerContext triggerContext) {
        if (triggerContext.lastScheduledExecutionTime() == Long.MIN_VALUE) {
            return /*new Date*/(System.currentTimeMillis() + this.initialDelay);
        } else if (this.fixedRate) {
            return /*new Date*/(triggerContext.lastScheduledExecutionTime() + this.period);
        }
        return /*new Date*/(triggerContext.lastCompletionTime() + this.period);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PeriodicTrigger)) {
            return false;
        }
        PeriodicTrigger other = (PeriodicTrigger) obj;
        return (this.fixedRate == other.fixedRate && this.initialDelay == other.initialDelay && this.period == other.period);
    }

    @Override
    public int hashCode() {
        return (this.fixedRate ? 17 : 29) + (int) (37 * this.period) + (int) (41 * this.initialDelay);
    }
}
