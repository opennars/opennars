package org.clockwise;

import java.util.TimeZone;

public class CronTrigger implements Trigger {

    private final CronSequenceGenerator sequenceGenerator;

    /**
     * Build a {@link CronTrigger} from the pattern provided in the default time
     * zone.
     * 
     * @param cronExpression
     *            a space-separated list of time fields, following cron
     *            expression conventions
     */
    public CronTrigger(String cronExpression) {
        this.sequenceGenerator = new CronSequenceGenerator(cronExpression);
    }

    /**
     * Build a {@link CronTrigger} from the pattern provided.
     * 
     * @param cronExpression
     *            a space-separated list of time fields, following cron
     *            expression conventions
     * @param timeZone
     *            a time zone in which the trigger times will be generated
     */
    public CronTrigger(String cronExpression, TimeZone timeZone) {
        this.sequenceGenerator = new CronSequenceGenerator(cronExpression, timeZone);
    }

    /**
     * Determine the next execution time according to the given trigger context.
     * <p>
     * Next execution times are calculated based on the
     * {@linkplain TriggerContext#lastCompletionTime completion time} of the
     * previous execution; therefore, overlapping executions won't occur.
     */
    @Override
    public long nextExecutionTime(TriggerContext triggerContext) {
        long date = triggerContext.lastCompletionTime();
        if (date != Long.MIN_VALUE) {
            long scheduled = triggerContext.lastScheduledExecutionTime();
            if ((scheduled != Long.MIN_VALUE) && (date < scheduled)) {
                // Previous task apparently executed too early...
                // Let's simply use the last calculated execution time then,
                // in order to prevent accidental re-fires in the same second.
                date = scheduled;
            }
        } else {
            date = System.currentTimeMillis();
        }
        return this.sequenceGenerator.next(date);
    }

    public String getExpression() {
        return this.sequenceGenerator.getExpression();
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj || (obj instanceof CronTrigger && this.sequenceGenerator
                .equals(((CronTrigger) obj).sequenceGenerator)));
    }

    @Override
    public int hashCode() {
        return this.sequenceGenerator.hashCode();
    }

    @Override
    public String toString() {
        return this.sequenceGenerator.toString();
    }
}
