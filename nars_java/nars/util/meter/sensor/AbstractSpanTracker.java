package nars.util.meter.sensor;

import java.util.Date;
import nars.util.meter.Sensor;
import nars.util.meter.session.StatsSession;
import nars.util.meter.util.Range;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
public abstract class AbstractSpanTracker extends AbstractTracker implements SpanTracker {

    //private static final Logger logger = Logger.getLogger(AbstractSpanTracker.class.toString());
    protected long startTime = 0L;
    protected boolean tracking = false;
    protected int sampleResolution = 1;
    int cycle = 0;

    public AbstractSpanTracker(final StatsSession session) {
        super(session);
    }

    public AbstractSpanTracker(final String id) {
        super(id);
    }

    public AbstractSpanTracker(final String id, final Range... ranges) {
        super(id, ranges);
    }

    /**
     * sample every Nth cycle; 1 = sample always, 2 = sample every other cycle, etc..
     */
    public void setSampleResolution(int resolutionDivisor) {
        this.sampleResolution = resolutionDivisor;
    }

    public int getSampleResolution() {
        return sampleResolution;
    }

    
    @Override
    public final SpanTracker start() {
        cycle++;

        //try {

        if (tracking) {
            //logger.warning("track() called when already tracking: {} " + this);
            //throw new RuntimeException(this + " can not track() while already tracking");
            
            //allow this to happen
        }

        tracking = true;

        startTime = System.currentTimeMillis();

        if (cycle % sampleResolution == 0)
            startImpl(startTime);
        else {            
            session.track(this, startTime);
        }


        /*} catch (Exception e) {
         Misc.logHandledException(logger, e, "Caught Exception in track()");
         Misc.handleUncaughtException(getKey(), e);
         }*/
        
        return this;
    }

    protected void startImpl(final long now) {
        session.track(this, now);
    }

    private double lastActualValue;
    
    @Override
    public void stop() {
        //try {

        if (!tracking) {
            //logger.warning("commit() called when not tracking: {} " + this);
            throw new RuntimeException(this + " can not commit(); not tracking");
        }

        tracking = false;

        if (cycle % sampleResolution == 0) {
            stopImpl(-1);            
            lastActualValue = value;
        }
        else {
            //sampling prevented implementation from measuring, so re-use last value
            value = lastActualValue;
            session.update(this, -1);
        }

        /*} catch (Exception e) {
         Misc.logHandledException(logger, e, "Caught Exception in commit()");
         Misc.handleUncaughtException(getKey(), e);
         }*/
        
    }

    protected void stopImpl(final long now) {
        session.update(this, now);
    }

    @Override
    public boolean isTracking() {
        return tracking;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public Sensor reset() {
        super.reset();
        startTime = 0L;
        tracking = false;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(256);

        buf.append(getClass().getSimpleName());
        buf.append("[startTime=");
        buf.append(new Date(startTime));
        buf.append(",tracking=");
        buf.append(tracking);
        buf.append(",value=");
        buf.append(value);
        buf.append(",session=");
        try {
            buf.append(session);
        } catch (Exception e) {
            buf.append(e.toString());

            //logger.severe("Caught Exception in toString(): {} " + e.toString());
            //logger.debug("Caught Exception in toString()", e);
        }
        buf.append(']');

        return buf.toString();
    }

//    public abstract static class AbstractSpanTrackerFactory implements TrackerFactory<SpanTracker> {
//
//        @Override
//        public Class<SpanTracker> getTrackerType() {
//            return SpanTracker.class;
//        }
//    }
}
