package nars.util.meter.sensor;

/**
 *
 * @author me
 */
/**
 * Uses Runtime methods to calculate changes in memory use, measured in KiloBytes (1024 bytes)
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
