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

import nars.util.meter.session.StatsSession;
import nars.util.meter.util.Range;

/**
 * A tracker that tracks time duration with nanosecond precision (but not
 * necessarily nanosecond accuracy). The value is stored as a fraction of
 * milliseconds.
 *
 * @see System#nanoTime()
 *
 * @author The Stajistics Project
 */
public class NanoTimeDurationTracker extends TimeDurationTracker {

    private long nanoTime;

    public NanoTimeDurationTracker(final StatsSession session) {
        super(session);
    }

    public NanoTimeDurationTracker(final String id, Range... ranges) {
        super(id, ranges);
    }

    public NanoTimeDurationTracker(final String id) {
        super(id);
    }

    @Override
    protected void startImpl(final long now) {
        nanoTime = System.nanoTime();

        super.startImpl(now);
    }

    @Override
    protected void stopImpl(final long now) {
        value = (System.nanoTime() - nanoTime) / 1000000d;

        currentHits++;
        
        session.update(this, now);
    }

}
