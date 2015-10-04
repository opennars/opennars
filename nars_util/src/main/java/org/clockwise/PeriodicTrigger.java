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
    private long period;
    private final TimeUnit timeUnit;
    //private volatile long initialDelay = 0;

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



    public final long getPeriod() {
        return period;
    }

    //TODO move this policy to a subclass
//    /**
//     * Specify whether the periodic interval should be measured between the
//     * scheduled start times rather than between actual completion times. The
//     * latter, "fixed delay" behavior, is the default.
//     */
//    public void setFixedRate(boolean fixedRate) {
//        this.fixedRate = fixedRate;
//    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public final void setFPS(final float fps) {
        setPeriod((long)(1000f/fps));
    }

    /**
     * Returns the time after which a task should run again.
     */
    @Override
    public final long nextExecutionTime(TriggerContext triggerContext) {
        /*if (triggerContext.lastScheduledExecutionTime() == Long.MIN_VALUE) {
            //initial
            return System.currentTimeMillis() + this.initialDelay;
        } else*/
            return triggerContext.lastScheduledExecutionTime() + this.period;

        //TODO move this policy to a subclass
        /*} else {
            return (triggerContext.lastCompletionTime() + this.period);
        }*/
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PeriodicTrigger)) {
            return false;
        }
        PeriodicTrigger other = (PeriodicTrigger) obj;
        return (this.period == other.period);
        //this.fixedRate == other.fixedRate && this.initialDelay == other.initialDelay &&
    }

    @Override
    public final int hashCode() {
        return (int) (37 * this.period);
        // + (int) (41 * this.initialDelay
        //(this.fixedRate ? 17 : 29)
    }
}
