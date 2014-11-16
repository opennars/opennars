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
package nars.util.meter.recorder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import nars.util.meter.Meter;
import nars.util.meter.data.DataSet;
import nars.util.meter.session.StatsSession;
import nars.util.meter.util.Range;
import nars.util.meter.util.RangeList;
import nars.util.meter.util.ThreadSafe;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
@ThreadSafe
public class RangeDataRecorder implements DataRecorder {

    private static final Logger logger = Logger.getLogger(RangeDataRecorder.class.toString());

    private final RangeList rangeList;
    private final AtomicLong[] hits;

    public RangeDataRecorder(final RangeList rangeList) {
        //assertNotNull(rangeList, "rangeList");

        int size = rangeList.size();
        if (size == 0) {
            throw new IllegalArgumentException("rangeList is empty");
        }

        this.rangeList = rangeList;

        hits = new AtomicLong[size];
        for (int i = 0; i < size; i++) {
            hits[i] = new AtomicLong(0);
        }
    }

    @Override
    public Set<String> getSupportedFieldNames() {
        Set<String> result = new HashSet<>(rangeList.size());

        for (Range range : rangeList) {
            result.add(range.getName());
        }

        return Collections.unmodifiableSet(result);
    }

    @Override
    public void update(final StatsSession session,
            final Meter tracker,
            final long now) {
        final double value = tracker.getValue();
        final boolean hasOverlap = rangeList.hasOverlap();

        int i = -1;
        do {
            i = rangeList.indexOfRangeContaining(value, i + 1);
            if (i != -1) {
                hits[i].incrementAndGet();
            }
        } while (i != -1 && hasOverlap);
    }

    @Override
    public Object getField(final StatsSession session,
            final String name) {
        List<Range> ranges = rangeList.getRanges();
        final int rangeCount = ranges.size();
        for (int i = 0; i < rangeCount; i++) {
            if (ranges.get(i)
                    .getName()
                    .equals(name)) {
                return hits[i].get();
            }
        }

        // Not found
        return null;
    }

    @Override
    public void collectData(final StatsSession session, final DataSet dataSet) {
        List<Range> ranges = rangeList.getRanges();
        final int rangeCount = ranges.size();
        for (int i = 0; i < rangeCount; i++) {
            dataSet.put(ranges.get(i).getName(), hits[i].get());
        }
    }

    @Override
    public void restore(final DataSet dataSet) {

        long[] values = new long[rangeList.size()];

        Range range;
        Long value;
        int i = 0;

        for (Iterator<Range> itr = rangeList.iterator(); itr.hasNext(); i++) {
            range = itr.next();
            value = dataSet.getField(range.getName(), Long.class);

            if (value == null) {
                // The full range list is not present, so do not limp along with partial data
                logger.warning("Dropping restore() call due to partial field data. Missing: {} "
                        + range.getName());
                return;
            }

            values[i] = value;
        }

        // The full range list is available, so restore it now
        for (i = 0; i < values.length; i++) {
            hits[i].set(values[i]);
        }
    }

    @Override
    public void clear() {
        final int rangeCount = rangeList.size();
        for (int i = 0; i < rangeCount; i++) {
            hits[i].set(0);
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(128);

        buf.append(getClass().getSimpleName());
        buf.append(rangeList.getRanges());

        return buf.toString();
    }
}
