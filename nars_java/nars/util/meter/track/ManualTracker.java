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
package nars.util.meter.track;

import nars.util.meter.Tracker;
import nars.util.meter.session.StatsSession;

/**
 * A tracker type that allows direct manipulation of the numeric <i>value</i> to be published
 * to the associated {@link StatsSession}. Upon creation of a {@link ManualTracker}, the value
 * is set to <tt>0</tt>.
 *
 * @author The Stajistics Project
 */
public interface ManualTracker extends Tracker {


    /**
     * Add the given <tt>value</tt> to the currently stored value.
     *
     * @param value The value to add to the existing value.
     * @return <tt>this</tt>.
     */
    ManualTracker addValue(double value);

    /**
     * Set or replace the currently stored value with the given <tt>value</tt>.
     *
     * @param value The value to set.
     * @return <tt>this</tt>.
     */
    ManualTracker setValue(double value);

    /**
     * Publish the manually set value to the {@link StatsSession}.
     */
    void commit();
}
