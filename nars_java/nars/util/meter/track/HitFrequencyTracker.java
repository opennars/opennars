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
 * 
 * 
 *
 * @author The Stajistics Project
 */
public class HitFrequencyTracker extends AbstractSpanTracker {

    private long lastHitStamp = -1;

    public HitFrequencyTracker(final StatsSession session) {
        super(session);
    }

    @Override
    protected void startImpl(final long now) {
        lastHitStamp = session.getLastHitStamp();

        super.startImpl(now);
    }

    @Override
    protected void stopImpl(final long now) {
        if (lastHitStamp > 0) {
            value = startTime - lastHitStamp;

            session.update(this, now);
        }
    }

    @Override
    public Tracker reset() {
        super.reset();
        lastHitStamp = -1;

        return this;
    }

}
