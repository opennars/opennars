package nars.util.meter.sensor;

/**
 * Relatively slow, use a setResolutionDivisor to sample every Nth cycle
 * @author me
 * Uses Runtime methods to calculate changes in memory use, measured in KiloBytes (1024 bytes)
 * TODO also use https://github.com/dropwizard/metrics/blob/master/metrics-jvm/src/main/java/com/codahale/metrics/jvm/MemoryUsageGaugeSet.java
 */
public class MemoryUseTracker extends AbstractSpanTracker {

    long lastUsedMemory;

    public MemoryUseTracker(String id) {
        super(id);
    }

    public long getMemoryUsed() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }

    @Override
    protected void startImpl(final long now) {
        lastUsedMemory = getMemoryUsed();

        super.startImpl(now);
    }

    @Override
    protected void stopImpl(final long now) {
        value = ((getMemoryUsed() - lastUsedMemory)/1024.0);

        session.update(this, now);
    }

}
