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
 * CPU Time, in milliseconds
 *
 * @author The Stajistics Project
 */
public class ThreadCPUTimeTracker extends AbstractThreadInfoSpanTracker {

    private long startCPUTime; // nanos

    public ThreadCPUTimeTracker(final String id) {
        super(id);

        ensureCPUTimeMonitoringEnabled();
    }

    public ThreadCPUTimeTracker(final StatsSession session) {
        super(session);

        ensureCPUTimeMonitoringEnabled();
    }

    @Override
    protected void startImpl(final long now) {
        if (isCPUTimeMonitoringEnabled()) {
            startCPUTime = getThreadMXBean().getCurrentThreadCpuTime();

            super.startImpl(now);
        }
    }

    @Override
    protected void stopImpl(final long now) {
        if (isCPUTimeMonitoringEnabled()) {
            long endCPUTime = getThreadMXBean().getCurrentThreadCpuTime();

            value = (endCPUTime - startCPUTime) / 1000000d; // to millis

            session.update(this, now);
        }

    }

    @Override
    public Meter reset() {
        super.reset();

        startCPUTime = -1;

        return this;
    }

}
