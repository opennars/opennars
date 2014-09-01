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
package nars.util.meter.session;

import java.io.Serializable;
import java.util.List;
import nars.util.meter.Sensor;
import nars.util.meter.data.DataSet;
import nars.util.meter.event.EventManager;
import nars.util.meter.event.EventType;
import nars.util.meter.key.StatsKey;
import nars.util.meter.recorder.DataRecorder;

/**
 * Stores statistics data collected for a single {@link String}.
 *
 * @author The Stajistics Project
 */
public interface StatsSession extends Serializable {

    /**
     * Obtain the {@link String} that represents the target for which this
     * session is storing data.
     *
     * @return A {@link String} instance, never <tt>null</tt>.
     */
    StatsKey getKey();

    /**
     * Get a List of {@link DataRecorder}s that are associated with this
     * session.
     *
     * @return A List of {@link DataRecorder}s, or an empty List if none exist.
     */
    List<DataRecorder> getDataRecorders();

    /**
     * Get the number of hits, or the number of times a {@link Sensor}
     * associated with this session's {@link String} has called
     * {@link #track(Tracker, long)}.
     *
     * @return The positive number of hits to this session.
     */
    long getHits();

    /**
     * Get the time stamp of the first hit to this session.
     *
     * @return The time stamp in milliseconds of the first hit, or <tt>-1</tt>
     * if not yet hit.
     */
    long getFirstHitStamp();

    /**
     * Get the time stamp of the most recent hit to this session.
     *
     * @return The time stamp in milliseconds of the last hit, or <tt>-1</tt> if
     * not yet hit.
     */
    long getLastHitStamp();

    /**
     * Get the number of commits, or the number of time a {@link Sensor}
     * associated with this session's {@link String} has called
     * {@link #update(Tracker, long)}.
     *
     * @return The positive number of commits to ths session.
     */
    long getCommits();

    /**
     * Get the first value recorded for this session.
     *
     * @return The first value.
     *
     * @see Tracker#getValue()
     */
    double getFirst();

    /**
     * Get the most recent value recorded for this session.
     *
     * @return The most recent value.
     *
     * @see Tracker#getValue()
     */
    double getLast();

    /**
     * Get the smallest value recorded for this session.
     *
     * @return The minimum value seen for this session.
     *
     * @see Tracker#getValue()
     */
    double getMin();

    /**
     * Get the largest value recorded for this session.
     *
     * @return The maximum value seen for this session.
     *
     * @see Tracker#getValue()
     */
    double getMax();

    /**
     * Get the total of all values recorded for this session.
     *
     * @return The sum of all values seen for this session.
     *
     * @see Tracker#getValue()
     */
    double getSum();

    /**
     * Obtain the value of a single field from this session.
     *
     * @param name The name of the field for which to return the value.
     * @return The value of the requested field, or <tt>null</tt> if not found.
     */
    Object getField(String name);

    /**
     * Obtain a {@link DataSet} that is populated with all data collected for
     * this session. The {@link DataSet} is populated with default data stored
     * by this session, such as hits and commits, as well as data stored by the
     * {@link DataRecorder}s associated with this session.
     *
     * @return A {@link DataSet} full of data, never <tt>null</tt>.
     */
    DataSet collectData();

    /**
     * <p>
     * Obtain a {@link DataSet} that is populated with all data collected for
     * this session and then clear the session. The {@link DataSet} is populated
     * with default data stored by this session, such as hits and commits, as
     * well as data stored by the {@link DataRecorder}s associated with this
     * session.</p>
     *
     * <p>
     * Fires a {@link EventType#SESSION_CLEARED} event.</p>
     *
     * @return A {@link DataSet} full of data, never <tt>null</tt>.
     */
    DataSet drainData();

    /**
     * <p>
     * Re-populate internal data fields and {@link DataRecorder}s using the
     * given
     * <tt>dataSet</tt>. This will clear any previously recorded data.</p>
     *
     * <p>
     * Fires a {@link EventType#SESSION_RESTORED} event.</p>
     *
     * @param dataSet The data set with which to populate this session.
     */
    void restore(DataSet dataSet);

    /**
     * <p>
     * Do not call directly. Normally called by a {@link Sensor}
     * implementation. Increments the hits for this session by one.</p>
     *
     * <p>
     * Fires a {@link EventType#TRACKER_TRACKING} event for the passed
     * <tt>tracker</tt>.</p>
     *
     * @param tracker The {@link Sensor} that, after this call, will be
     * tracking data for this session.
     * @param now The time stamp of the current time if known, otherwise
     * <tt>-1</tt>.
     */
    void track(Sensor tracker, long now);

    /**
     * <p>
     * Do not call directly. Normally called by a {@link Sensor}
     * implementation. Increments the commits for this session by one. The value
     * reported by the given
     * <tt>tracker</tt>'s {@link Tracker#getValue()} method is processed and
     * offered to the {@link DataRecorder}s associated with this session.</p>
     *
     * <p>
     * Fires a {@link EventType#TRACKER_COMMITTED} event for the passed
     * <tt>tracker</tt>.</p>
     *
     * @param tracker The {@link Sensor} that collected the data for this
     * update.
     * @param now The time stamp of the current time if known, otherwise
     * <tt>-1</tt>.
     *
     * @see Tracker#getValue()
     * @see DataRecorder
     * @see #getDataRecorders()
     */
    void update(Sensor tracker, long now);

    /**
     * <p>
     * Clear all data recorded for this session. Resets all fields to initial
     * values and calls {@link DataRecorder#clear()} on all {@link DataRecorder}
     * associated with this session.</p>
     *
     * <p>
     * Fires a {@link EventType#SESSION_CLEARED} event.</p>
     */
    void clear();

    void setEventManager(EventManager e);

}
