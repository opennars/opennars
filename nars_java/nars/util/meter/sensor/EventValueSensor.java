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
import nars.util.meter.session.StatsSession;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
public class EventValueSensor extends AbstractSensor implements ManualTracker {

    //private static final Logger logger = Logger.getLogger(EventValueSensor.class.toString());

    double lastValue = 0, currentDelta = 0;
    long lastFirstcommit = -1;
    
    public EventValueSensor(final StatsSession statsSession, boolean active) {
        super(statsSession);
        setActive(active);
    }

    /** active defaults to false */
    public EventValueSensor(final StatsSession statsSession) {
        this(statsSession, false);
    }
    
    public EventValueSensor(final String id, boolean active) {
        super(id);
        setActive(active);
    }
    
    public EventValueSensor(final String id) {
        super(id);
    }

    @Override
    public ManualTracker addValue(final double value) {
        if (!active) return this;
        
        setValue(this.value + value);
        return this;
    }

    @Override
    public ManualTracker setValue(final double newValue) {
        if (!active) return this;
        
        currentDelta = newValue - lastValue;
        lastValue = this.value;
        this.value = newValue;        
        return this;
    }

    public void commit(double value) {
        if (!active) return;

        super.commit();
        setValue(value);
        commit();
    }

    @Override
    public void commit() {       
        if (!active) return;
        
        final long now = System.currentTimeMillis();
        session.track(this, now);
        session.update(this, now);

        currentHits++;
    }
    
    
    /** difference in value from current to previous iteration */
    public double getDelta() {
        return currentDelta;
    }

    @Override
    public Sensor reset() {
        lastValue = currentDelta = 0;
        return super.reset();
    }

//    public double getMean() {
//        return get("hits","mean").mean();
//    }

}
