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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * TODO: This tracker currently depends on the ordering of
 * ManagementFactory.getGarbageCollectorMXBeans(). Is that safe?
 *
 *
 * @author The Stajistics Project
 */
public class GarbageCollectionTimeTracker extends AbstractSpanTracker {

    private String[] startGCNames = null;
    private long[] startGCTimes = null;

    public GarbageCollectionTimeTracker(final StatsSession session) {
        super(session);
    }

    @Override
    protected void startImpl(final long now) {
        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        startGCNames = new String[gcMXBeans.size()];
        startGCTimes = new long[gcMXBeans.size()];

        int i = 0;
        for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
            startGCNames[i] = gcMXBean.getName();
            startGCTimes[i] = gcMXBean.getCollectionTime();
            i++;
        }

        super.startImpl(now);
    }

    @Override
    protected void stopImpl(final long now) {
        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        if (gcMXBeans.size() == startGCNames.length) {

            boolean commit = true;
            long totalGCTime = 0;

            int i = 0;

            for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
                if (!startGCNames[i].equals(gcMXBean.getName())) {
                    commit = false;
                    break;
                }

                long startGCTime = startGCTimes[i];
                long endGCTime = gcMXBean.getCollectionTime();

                if (startGCTime == -1) {
                    if (endGCTime != -1) {
                        commit = false;
                        break;
                    }
                } else {
                    if (endGCTime == -1) {
                        commit = false;
                        break;
                    }

                    totalGCTime += endGCTime - startGCTime;
                }

                i++;
            }

            if (commit) {
                value = totalGCTime;
                session.update(this, now);
            }
        }
    }

    @Override
    public Tracker reset() {
        super.reset();

        startGCNames = null;
        startGCTimes = null;

        return this;
    }

//    public static class Factory extends AbstractSpanTrackerFactory {
//
//        @Override
//        public SpanTracker createTracker(final StatsKey key,
//                                         final StatsSessionManager sessionManager) {
//            return new GarbageCollectionTimeTracker(sessionManager.getOrCreateSession(key));
//        }
//    }
}
