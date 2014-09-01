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
package nars.util.meter.recorder;

import java.io.Serializable;
import java.util.Set;
import nars.util.meter.Sensor;
import nars.util.meter.data.DataSet;
import nars.util.meter.session.StatsSession;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
public interface DataRecorder extends Serializable {

    /**
     * Obtain a Set of field names on which this DataRecorder operates.
     *
     * @return A Set of supported field names, never empty, and never
     * <tt>null</tt>.
     */
    Set<String> getSupportedFieldNames();

    /**
     * Get the value of a single field.
     *
     * @param session The session that owns this DataRecorder instance.
     * @param name The name of the field.
     * @return The field value, or <tt>null</tt> if not found.
     */
    Object getField(StatsSession session, String name);

    /**
     * Examine the tracker collected value, perform calculations, and store the
     * new data.
     *
     * @param session The session that owns this DataRecorder instance.
     * @param tracker The tracker that is triggering the update.
     * @param now The current time.
     */
    void update(StatsSession session, Sensor tracker, long now);

    /**
     * Populate internal data structures with the data provided in the given
     * <tt>dataSet</tt>.
     *
     * @param dataSet The DataSet from which to extract data.
     */
    void restore(DataSet dataSet);

    /**
     * Prepare recorded data and add data fields to the given <tt>dataSet</tt>.
     * The data fields added defined by the Set returned by
     * {@link #getSupportedFieldNames()}.
     *
     * @param session The session that owns this DataRecorder instance.
     * @param dataSet The DataSet to populate with data fields.
     */
    void collectData(StatsSession session, DataSet dataSet);

    /**
     * Clear all stored data. After this call the externally visible state will
     * equal that of a newly created instance.
     */
    void clear();

}
