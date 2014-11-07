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
package nars.util.meter.session;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import nars.util.meter.Sensor;
import nars.util.meter.data.DataSet;
import nars.util.meter.event.EventManager;
import nars.util.meter.event.EventType;
import nars.util.meter.key.StatsKey;
import nars.util.meter.recorder.DataRecorder;
import nars.util.meter.recorder.DataRecorders;


/**
 * <p>
 * An implementation of {@link StatsSession} that reads and writes data fields
 * atomically without locking. This allows scalable updates that minimize the
 * runtime overhead of statistics collection. However, the cost of using this
 * implementation is that the {@link DataSet} returned from
 * {@link #collectData()} may contain values that are not related to one
 * another. For example, the DataSet may contain all the data from update #1,
 * but only half of the data from update #2 (because update #2 is executing
 * simultaneously to the {@link #collectData()} call). For a
 * {@link StatsSession} implementation that guarantees data integrity, see
 * {@link org.stajistics.session.AsynchronousSession}.</p>
 *
 * <p>
 * Due to the concurrent nature of this session implementation, the associated
 * {@link DataRecorder}s must be thread safe. {@link DataRecorder}s that are
 * passed into the constructor are passed through the
 * {@link DataRecorders#lockingIfNeeded(DataRecorder[])} method in order to
 * ensure thread safe usage. Note that if any {@link DataRecorder}s are wrapped
 * in a locking decorator, it could negatively impact performance of the client
 * application. For optimal performance, use {@link DataRecorder}
 * implementations that are thread safe through the use of atomic
 * primitives.</p>
 *
 * @see org.stajistics.session.AsynchronousSession
 *
 * @author The Stajistics Project
 */
public class ConcurrentSession extends AbstractStatsSession {

    //public static final Factory FACTORY = new Factory();
    private static final Logger logger = Logger.getLogger(ConcurrentSession.class.toString());

    protected final AtomicLong hits = new AtomicLong(DataSet.Field.DefaultField.HITS);
    protected final AtomicLong firstHitStamp = new AtomicLong(DataSet.Field.DefaultField.FIRST_HIT_STAMP);
    protected volatile long lastHitStamp = DataSet.Field.DefaultField.LAST_HIT_STAMP;
    protected final AtomicLong commits = new AtomicLong(DataSet.Field.DefaultField.COMMITS);

    // The proper default is taken care of in getFirst()
    protected final AtomicDouble first = new AtomicDouble(Double.NEGATIVE_INFINITY);

    protected volatile double last = DataSet.Field.DefaultField.LAST;
    protected final AtomicDouble min = new AtomicDouble(Double.POSITIVE_INFINITY);
    protected final AtomicDouble max = new AtomicDouble(Double.NEGATIVE_INFINITY);
    protected final AtomicDouble sum = new AtomicDouble(DataSet.Field.DefaultField.SUM);

    public ConcurrentSession(final StatsKey key,
            final EventManager eventManager,
            final DataRecorder... dataRecorders) {
        super(key,
                DataRecorders.lockingIfNeeded(dataRecorders));
    }

    @Override
    public void track(final Sensor tracker,
            long now) {
        if (now < 0) {
            now = System.currentTimeMillis();
        }

        hits.incrementAndGet();

        if (firstHitStamp.get() == DataSet.Field.DefaultField.FIRST_HIT_STAMP) {
            firstHitStamp.compareAndSet(DataSet.Field.DefaultField.FIRST_HIT_STAMP, now);
        }
        lastHitStamp = now;

        
    }

    @Override
    public long getHits() {
        return hits.get();
    }

    @Override
    protected void setHits(final long hits) {
        this.hits.set(hits);
    }

    @Override
    public long getFirstHitStamp() {
        return firstHitStamp.get();
    }

    @Override
    protected void setFirstHitStamp(long firstHitStamp) {
        this.firstHitStamp.set(firstHitStamp);
    }

    @Override
    public long getLastHitStamp() {
        return lastHitStamp;
    }

    @Override
    protected void setLastHitStamp(final long lastHitStamp) {
        this.lastHitStamp = lastHitStamp;
    }

    @Override
    public long getCommits() {
        return commits.get();
    }

    @Override
    protected void setCommits(final long commits) {
        this.commits.set(commits);
    }

    @Override
    public void update(final Sensor tracker, final long now) {

        final double currentValue = tracker.getValue();
        double tmp;

        commits.incrementAndGet();

        // First
        //if (first.get() == null) {
        first.compareAndSet(Double.NEGATIVE_INFINITY, last);            
        //}

        // Last
        last = currentValue;

        // Min
        for (;;) {
            tmp = min.get();
            if (currentValue < tmp) {
                if (min.compareAndSet(tmp, currentValue)) {
                    break;
                }
            } else {
                break;
            }
        }

        // Max
        for (;;) {
            tmp = max.get();
            if (currentValue > tmp) {
                if (max.compareAndSet(tmp, currentValue)) {
                    break;
                }
            } else {
                break;
            }
        }

        // Sum
        sum.addAndGet(currentValue);

        for (final DataRecorder dataRecorder : dataRecorders) {
            //try {
            dataRecorder.update(this, tracker, now);
            /*} catch (Exception e) {
             Misc.logHandledException(logger, e, "Failed to update {}", dataRecorder);
             Misc.handleUncaughtException(getKey(), e);
             }*/
        }

        //logger.info("Commit: {}" + " " + this);
        
    }

    @Override
    public double getFirst() {
        Double firstValue = first.get();

        if (firstValue == null) {
            return DataSet.Field.DefaultField.FIRST;
        }

        return firstValue;
    }

    @Override
    protected void setFirst(final double first) {
        this.first.set(first);
    }

    @Override
    public double getLast() {
        return last;
    }

    @Override
    protected void setLast(final double last) {
        this.last = last;
    }

    @Override
    public double getMin() {
        Double result = min.get();
        if (result.equals(Double.POSITIVE_INFINITY)) {
            result = DataSet.Field.DefaultField.MIN;
        }
        return result;
    }

    @Override
    protected void setMin(final double min) {
        this.min.set(min);
    }

    @Override
    public double getMax() {
        Double result = max.get();
        if (result.equals(Double.NEGATIVE_INFINITY)) {
            result = DataSet.Field.DefaultField.MAX;
        }
        return result;
    }

    @Override
    protected void setMax(double max) {
        this.max.set(max);
    }

    @Override
    public double getSum() {
        return sum.get();
    }

    @Override
    protected void setSum(final double sum) {
        this.sum.set(sum);
    }

    @Override
    public void restore(final DataSet dataSet) {
        //assertNotNull(dataSet, "dataSet");

        clearState();
        restoreState(dataSet);

        //logger.trace("Restore: {}", this);
        
    }

    @Override
    public void clear() {
        clearState();

        //logger.trace("Clear: {}", this);
        
    }

    @Override
    public DataSet drainData() {
        DataSet data = createDataSet(true);
        collectData(data);
        clear();
        return data;
    }

//    /* NESTED CLASSES */
//
//    public static final class Factory implements StatsSessionFactory {
//        @Override
//        public StatsSession createSession(final StatsKey key,
//                                          final DataRecorder[] dataRecorders) {
//            StatsManager statsManager = StatsManagerRegistry.getInstance().getStatsManager(key.getNamespace());
//            return new ConcurrentSession(key,
//                                         statsManager.getEventManager(),
//                                         dataRecorders);
//        }
//    }
}
