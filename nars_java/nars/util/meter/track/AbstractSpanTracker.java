package nars.util.meter.track;

import java.util.Date;
import java.util.logging.Logger;
import nars.util.meter.Tracker;
import nars.util.meter.session.StatsSession;
import nars.util.meter.util.Misc;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
public abstract class AbstractSpanTracker extends AbstractTracker implements SpanTracker {

    private static final Logger logger = Logger.getLogger(AbstractSpanTracker.class.toString());

    protected long startTime = 0L;
    protected boolean tracking = false;

    public AbstractSpanTracker(final StatsSession session) {
        super(session);
    }

    @Override
    public final SpanTracker track() {
        try {
            if (tracking) {
                logger.warning("track() called when already tracking: {} " + this);

                return this;
            }

            tracking = true;

            startTime = System.currentTimeMillis();

            startImpl(startTime);

        } catch (Exception e) {
            Misc.logHandledException(logger, e, "Caught Exception in track()");
            Misc.handleUncaughtException(getKey(), e);
        }

        return this;
    }

    protected void startImpl(final long now) {
        session.track(this, now);
    }

    @Override
    public final void commit() {
        try {
            if (!tracking) {
                logger.warning("commit() called when not tracking: {} " + this);

                return;
            }

            tracking = false;

            stopImpl(-1);
        } catch (Exception e) {
            Misc.logHandledException(logger, e, "Caught Exception in commit()");
            Misc.handleUncaughtException(getKey(), e);
        }
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
    public Tracker reset() {
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

            logger.severe("Caught Exception in toString(): {} " + e.toString());
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
