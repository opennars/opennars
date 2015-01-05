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

import java.util.HashMap;
import nars.core.Memory;
import nars.util.meter.Meter;
import nars.util.meter.data.DataContainer;
import nars.util.meter.data.DataSet;
import nars.util.meter.key.DefaultStatsKey;
import nars.util.meter.key.StatsKey;
import nars.util.meter.recorder.DistributionDataRecorder;
import nars.util.meter.recorder.RangeDataRecorder;
import nars.util.meter.session.ConcurrentSession;
import nars.util.meter.session.StatsSession;
import nars.util.meter.util.Range;
import nars.util.meter.util.RangeList;

/**
 * A convenience base implementation of {@link Meter}.
 *
 * @author The Stajistics Project
 * @author heavily modified for NARS
 * 
 * TODO find better names for this class hierarchy, it has inconsistent namig system
 */
public abstract class AbstractSensor implements Meter {

    //private static final Logger logger = Logger.getLogger(AbstractTracker.class.toString());
    protected final StatsSession session;

    protected double value = 0;
    private long cyclesSinceLastUpdate = -1;

    int lastHits = 0, currentHits = 0;
    //private long lastFirstCommit;
    protected boolean active = false;

    public AbstractSensor(final StatsSession session) {        
        this.session = session;        
        init();
    }

    public AbstractSensor(String id) {
        this(new ConcurrentSession(
                new DefaultStatsKey("", id, new HashMap() /*new FastPutsArrayMap<>()*/),
                null,
                new DistributionDataRecorder()));
    }

    public AbstractSensor(String id, Range... ranges) {
        this(new ConcurrentSession(new DefaultStatsKey("", id, new HashMap() /*new FastPutsArrayMap<>()*/), null, new DistributionDataRecorder(), new RangeDataRecorder(new RangeList(ranges))));
    }

    
    @Override
    public double getValue() {
        return value;
    }
    

    @Override
    public Meter reset() {
        value = 0;
        lastHits = currentHits = 0;
        init();
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

    int gets = 0;
    int sampleWindow = 0;
    
    /** automatically drain after n cycles; set to 0 for no auto-drain */
    public AbstractSensor setSampleWindow(int s) {
        this.sampleWindow = s;
        return this;
    }

    public int getSampleWindow() {
        return sampleWindow;
    }    

    protected void commit() {
        gets++;        
    }
    
    

    
//    public <D> D get(D defaultValue, String... keys) {
//        if (sampleWindow > 0) {
//            if (gets % sampleWindow == 0)
//                return getSession().drainData(defaultValue, keys);
//        }
//        
//        return getSession().collectData(defaultValue, keys);
//    }
//    
    
    /** called to store the final value in a dataset; override in subclasses */
    @Override
    public void commit(DataContainer d, Memory m) {
        
    }
    
    public double d(String field) {
        return ((Number)getSession().getField(field)).doubleValue();
    }
    public double dReset(String field) {
        double d = ((Number)getSession().getField(field)).doubleValue();
        getSession().clear();
        return d;
    }
    
    @Override
    public DataSet get() {
        if (sampleWindow > 0) {
            if (gets % sampleWindow == 0)
                return getReset();
        }
        
        return getSession().collectData();
    }

    public DataSet getReset() {
        return getSession().drainData();
    }

    @Override
    public void setCyclesSinceLastUpdate(final long cyclesSinceLastUpdate) {
        //if (cyclesSinceLastUpdate == -1)
        this.cyclesSinceLastUpdate = cyclesSinceLastUpdate;
    }
    
    public double getHits() {        
        int hits = currentHits;
        currentHits = 0;
        return hits;
    }
    
    public double getHitRate() {
        int hits = currentHits;
        currentHits = 0;
        if (cyclesSinceLastUpdate == -1)
            return hits;
        else {
            double h = ((double)hits) / ((double)cyclesSinceLastUpdate);
            cyclesSinceLastUpdate = -1;
            return h;
        }
    }
    
    public double getDeltaHits() {
        int deltaHits = currentHits - lastHits;
        lastHits = currentHits;
        currentHits = 0;
        return deltaHits;
    }    
    
    
    public String getName() {
        return getKey().getName();
    }    
    
    public void setActive(boolean a) {
        this.active = a;
    }

    public boolean isActive() {
        return active;
    }
    
}
