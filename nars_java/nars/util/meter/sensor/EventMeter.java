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

import nars.util.meter.Meter;
import nars.util.meter.session.StatsSession;

/**
 * <p>
 * A tracker type dedicated to collecting statistics related to occurrences of
 * events. This tracker is only concerned with recording the fact that an event
 * occurred; it does not care about the details of the event. The details of an
 * event may be stored in the attributes if this trackers associated
 * {@link String} so that event statistics can be separated or aggregated as
 * desired.</p>
 *
 * Example usage:
 * <pre>
 * Stats.getIncidentTracker("myKey")
 *      .incident();
 * // or...
 * Stats.incident("myKey");
 * </pre>
 *
 * @author The Stajistics Project
 */
public interface EventMeter extends Meter {

    /**
     * Report an occurrence of an event. Sets the <tt>value</tt> field to
     * <tt>1</tt> and immediately publishes the incident occurrence to the
     * {@link StatsSession}.
     *
     * @return <tt>this</tt>.
     */
    EventMeter event();
}
