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
package nars.util.meter.depr.sensor;

import nars.util.meter.depr.session.StatsSession;
import nars.util.other.Range;

/**
 * A common base class for time duration trackers. The class chooses a
 * {@link #FACTORY} that provides the most precise measurements on the current
 * JVM. If <code>sun.misc.Perf</code> is available, the
 * {@link PerfTimeDurationTracker#FACTORY} is chosen, otherwise the
 * {@link NanoTimeDurationTracker#FACTORY} is chosen.
 *
 * @author The Stajistics Project
 */
public abstract class TimeDurationTracker extends AbstractSpanTracker {

    public TimeDurationTracker(final StatsSession session) {
        super(session);
    }

    public TimeDurationTracker(final String id) {
        super(id);
    }

    public TimeDurationTracker(final String id, Range... ranges) {
        super(id, ranges);
    }

}
