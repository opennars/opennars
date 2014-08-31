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

import nars.util.meter.session.StatsSession;

/**
 * A tracker that tracks time duration with millisecond precision 
 * (but not necessarily millisecond accuracy). The value is stored as milliseconds.
 *
 * <p>
 * The advantage of using this less-precise time duration tracker is that 
 * {@link System#currentTimeMillis()} called upon {@link #commit()} is re-used by the 
 * {@link StatsSession}.
 *
 * @see System#currentTimeMillis()
 *
 * @author The Stajistics Project
 */
public class MilliTimeDurationTracker extends AbstractSpanTracker {

    public MilliTimeDurationTracker(final StatsSession session) {
        super(session);
    }

    @Override
    protected void startImpl(long now) {
        if (now < 0) {
            now = System.currentTimeMillis();
        }

        value = now - startTime;

        session.track(this, now);
    }


}
