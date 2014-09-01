/* Copyright 2009 - 2010 The Stajistics Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nars.util.meter.sensor;

import nars.util.meter.Sensor;
import nars.util.meter.data.DataSet;
import nars.util.meter.event.EventManager;
import nars.util.meter.key.DefaultStatsKey;
import nars.util.meter.key.StatsKey;
import nars.util.meter.recorder.DistributionDataRecorder;
import nars.util.meter.recorder.RangeDataRecorder;
import nars.util.meter.session.ConcurrentSession;
import nars.util.meter.session.StatsSession;
import nars.util.meter.util.FastPutsArrayMap;
import nars.util.meter.util.Range;
import nars.util.meter.util.RangeList;

/**
 * A convenience base implementation of {@link Sensor}.
 *
 * @author The Stajistics Project
 */
public abstract class AbstractTracker implements Sensor {

    //private static final Logger logger = Logger.getLogger(AbstractTracker.class.toString());
    protected final StatsSession session;

    protected double value = 0;

    int lastHits = 0, currentHits = 0;

    public AbstractTracker(final StatsSession session) {
        //assertNotNull(session, "session");
        this.session = session;
    }

    public AbstractTracker(String id) {
        this(new ConcurrentSession(
                new DefaultStatsKey("", id, new FastPutsArrayMap<String, Object>()),
                null,
                new DistributionDataRecorder()));
    }

    public AbstractTracker(String id, Range... ranges) {
        this(new ConcurrentSession(new DefaultStatsKey("", id, new FastPutsArrayMap<>()), null, new DistributionDataRecorder(), new RangeDataRecorder(new RangeList(ranges))));
    }

    public void setEventManager(EventManager e) {
        getSession().setEventManager(e);
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public Sensor reset() {
        value = 0;
        lastHits = currentHits = 0;
        return this;
    }

    @Override
    public StatsKey getKey() {
        return session.getKey();
    }

    @Override
    public StatsSession getSession() {
        return session;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(256);

        buf.append(getClass().getSimpleName());
        buf.append("[value=");
        buf.append(value);
        buf.append(",session=");
        try {
            buf.append(session);
        } catch (Exception e) {
            buf.append(e.toString());

            //Misc.logHandledException(logger, e, "Caught Exception in toString()");
            //Misc.handleUncaughtException(getKey(), e);
        }
        buf.append(']');

        return buf.toString();
    }

    public DataSet get() {
        return getSession().collectData();
    }

    public DataSet getReset() {
        return getSession().drainData();
    }

    public double getDeltaHits() {
        int deltaHits = currentHits - lastHits;
        lastHits = currentHits;
        currentHits = 0;
        return deltaHits;
    }    
}
