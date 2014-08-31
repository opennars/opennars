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

import java.util.List;


/**
 *
 *
 *
 * @author The Stajistics Project
 */
public class CompositeSpanTracker extends AbstractCompositeTracker<SpanTracker>
    implements SpanTracker {

    public CompositeSpanTracker(final SpanTracker... trackers) {
        super(trackers);
    }

    public CompositeSpanTracker(final List<SpanTracker> trackers) {
        super(trackers);
    }

    /**
     * Start tracking on all {@link SpanTracker}s.
     *
     * @return <tt>this</tt>.
     */
    @Override
    public SpanTracker track() {
        int len = trackers.length;
        for (int i = 0; i < len; i++) {
            trackers[i].track();
        }
        return this;
    }

    /**
     * Commit all {@link SpanTracker}s.
     *
     * @return <tt>this</tt>.
     */
    @Override
    public void commit() {
        int len = trackers.length;
        for (int i = 0; i < len; i++) {
            trackers[i].commit();
        }
    }

    /**
     * Determine if any of the {@link SpanTracker}s are tracking.
     */
    @Override
    public boolean isTracking() {
        int len = trackers.length;
        for (int i = 0; i < len; i++) {
            if (trackers[i].isTracking()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine the earliest start time of the {@link SpanTracker}s.
     */
    @Override
    public long getStartTime() {
        long startTime = Long.MAX_VALUE;
        int len = trackers.length;
        for (int i = 0; i < len; i++) {
            long t = trackers[i].getStartTime();
            if (t < startTime) {
                startTime = t;
            }
        }
        return startTime;
    }
}
