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
package nars.util.meter;

import com.sun.org.glassfish.external.statistics.Stats;
import java.io.Serializable;
import nars.core.Memory;
import nars.util.meter.data.DataContainer;
import nars.util.meter.data.DataSet;
import nars.util.meter.key.StatsKey;
import nars.util.meter.sensor.EventMeter;
import nars.util.meter.sensor.ManualTracker;
import nars.util.meter.sensor.SpanTracker;
import nars.util.meter.session.StatsSession;

/**
 * <p>
 * A tracker is the main interface that is manipulated directly by client code
 * for the purposes of performing statistical measurements in a client-specific
 * manner. This interface defines the common methods to all tracker
 * implementations, however, in order to be useful for collecting statistics, a
 * more specific sub-interface must be manipulated, such as
 * {@link EventMeter}, {@link ManualTracker}, or {@link SpanTracker}.</p>
 *
 * <p>
 * The data that a tracker stores is only one element in a series of data
 * elements. For example, if the time taken to run a method is being collected
 * by the Stajistics system, the tracker that measures the elapsed time will
 * only store the time for the single current method invocation; it does not
 * store the times of previous invocations, nor averages, nor any other data.
 * The single data element that is collected by a tracker is published to the
 * associated {@link StatsSession} in which the full data set is
 * represented.</p>
 *
 * <p>
 * StatsTrackers are intentionally designed, by default, to be manipulated by
 * only one thread at a time and as such, are thread-unsafe. A Tracker instance
 * should not be stored or reused unless it is known that it will be accessed in
 * a thread-safe manner. Rather, it is recommended that an instance be retrieved
 * when needed using one of the convenience methods defined in
 * {@link Stats}.</p>
 *
 * @see StatsManager#getTrackerLocator()
 * @see TrackerLocator#getTracker(String)
 *
 * @author The Stajistics Project
 */
public interface Meter extends Serializable {

    /**
     * Obtain the numeric value that was collected as a result of operating this
     * tracker.
     *
     * @return
     */
    double getValue();

    /**
     * Clear the state of the tracker. This does not revert any changes that may
     * have occurred as a result of publishing data to the associated
     * {@link StatsSession}.
     *
     * @return <tt>this</tt>.
     */
    Meter reset();

    /**
     * Get the key that represents the target for which this tracker is
     * collecting statistics. This is equivalent to calling
     * <tt>getSession().getKey()</tt>.
     *
     * @return A {@link String} instance, never <tt>null</tt>.
     */
    StatsKey getKey();

    /**
     * Obtain the {@link StatsSession} to which this tracker publishes
     * statistics data.
     *
     * @return A {@link StatsSession} instance, never <tt>null</tt>.
     */
    StatsSession getSession();

    DataSet get();

    default public String name() {
        return getKey().getName();
    }

    /** called when created and after reset() */
    default public void init() { }
    
    default public void commit(DataContainer d, Memory m)  {
        
    }
    
    public void setCyclesSinceLastUpdate(long cyclesSinceLastUpdate);
    
    abstract public void setActive(boolean b);
}
