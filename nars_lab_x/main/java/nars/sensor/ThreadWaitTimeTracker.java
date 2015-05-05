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

import nars.util.meter.depr.Tracker;
import nars.util.meter.depr.session.StatsSession;

import java.lang.management.ThreadInfo;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
public class ThreadWaitTimeTracker extends AbstractThreadInfoSpanTracker {

    private long startWaitTime;

    public ThreadWaitTimeTracker(final StatsSession session) {
        super(session);

        ensureContentionMonitoringEnabled();
    }

    @Override
    protected void startImpl(final long now) {
        ThreadInfo threadInfo = getCurrentThreadInfo();
        if (threadInfo != null) {
            startWaitTime = threadInfo.getWaitedTime();
            if (startWaitTime > -1) {
                super.startImpl(now);
            }

        } else {
            startWaitTime = -1;
        }
    }

    @Override
    protected void stopImpl(final long now) {
        ThreadInfo threadInfo = getCurrentThreadInfo();
        if (threadInfo != null && startWaitTime > -1) {
            long endWaitTime = threadInfo.getWaitedTime();
            if (endWaitTime > -1) {
                value = endWaitTime - startWaitTime;
                session.update(this, now);
            }
        }
    }

    @Override
    public Tracker reset() {
        super.reset();

        startWaitTime = -1;

        return this;
    }

}
