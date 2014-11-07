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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import nars.util.meter.Sensor;
import nars.util.meter.data.DataSet;
import nars.util.meter.event.EventManager;
import nars.util.meter.event.EventType;
import nars.util.meter.event.TaskService;
import nars.util.meter.event.TaskServiceFactory;
import nars.util.meter.key.StatsKey;
import nars.util.meter.recorder.DataRecorder;
import nars.util.meter.util.Misc;

/**
 * <p>
 * An implementation of {@link StatsSession} that can potentially pad the
 * tracker, and thus the client of the tracker, from blocking on calls to
 * {@link #track(Tracker, long)}, and {@link #update(Tracker, long)}. When
 * either of these calls are made, they are queued for execution by the
 * associated {@link TaskService}, which will typically invoke them in a
 * background thread. This queuing behaviour does impose the overhead of queue
 * entry object creation on the client of a tracker, but this is necessary to
 * fulfil the main purpose of this implementation. {@link #collectData()} may be
 * called on an instance of this class and the resulting {@link DataSet} is
 * guaranteed to contain data fields that are related to one another for a given
 * update. For example, if {@link #collectData()} is called at the same time as
 * a update #2, the resulting {@link DataSet} will contain data related only to
 * update #1 or update #2. It will not contain the data from a partially
 * recorded update. Furthermore, it allows this data integrity without having to
 * block the client of the {@link Sensor}, which could sacrifice performance.
 * If better performance is preferred at the cost of potentially inconsistent
 * data, see {@link org.stajistics.session.ConcurrentSession}.</p>
 *
 * <p>
 * The {@link DataRecorder}s manipulated by this session implementation are
 * updated within locks, so the {@link DataRecorder}s themselves do not need to
 * be thread safe.</p>
 *
 * @see org.stajistics.session.ConcurrentSession
 *
 * @author The Stajistics Project
 */
public class AsynchronousSession extends AbstractStatsSession {

    private static final Logger logger = Logger.getLogger(AsynchronousSession.class.toString());

    private volatile long hits = DataSet.Field.DefaultField.HITS;
    private volatile long firstHitStamp = DataSet.Field.DefaultField.FIRST_HIT_STAMP;
    private volatile long lastHitStamp = DataSet.Field.DefaultField.LAST_HIT_STAMP;
    private volatile long commits = DataSet.Field.DefaultField.COMMITS;

    private volatile Double first = null; // The proper default is taken care of in getFirst()
    private volatile double last = DataSet.Field.DefaultField.LAST;
    private volatile double min = Double.POSITIVE_INFINITY;
    private volatile double max = Double.NEGATIVE_INFINITY;
    private volatile double sum = DataSet.Field.DefaultField.SUM;

    private final Queue<TrackerEntry> updateQueue;
    private final Lock updateQueueProcessingLock = new ReentrantLock();

    private final Lock stateLock = new ReentrantLock();

    public AsynchronousSession(final StatsKey key,
            final EventManager eventManager,
            final DataRecorder... dataRecorders) {
        this(key, eventManager, new ConcurrentLinkedQueue<>(), dataRecorders);
    }

    public AsynchronousSession(final StatsKey key,
            final EventManager eventManager,
            final Queue<TrackerEntry> updateQueue,
            final DataRecorder... dataRecorders) {
        super(key, dataRecorders);

        //assertNotNull(updateQueue, "updateQueue");
        this.updateQueue = updateQueue;
    }

    private void queueUpdateTask(final Sensor tracker, final long now, final boolean update) {
        TrackerEntry entry = new TrackerEntry(tracker, now, update);
        try {
            updateQueue.add(entry);

            ProcessUpdateQueueTask processQueueTask = new ProcessUpdateQueueTask();

            TaskService taskService = TaskServiceFactory.getInstance().getTaskService();
            taskService.execute(getClass(), processQueueTask);
        } catch (Exception e) {
            Misc.logHandledException(logger, e, "Failed to queue task {}", entry);
            Misc.handleUncaughtException(getKey(), e);
        }
    }

    @Override
    public void track(final Sensor tracker, long now) {
        queueUpdateTask(tracker, now, true);
    }

    private void trackImpl(final Sensor tracker, final long now) {
        stateLock.lock();
        try {
            hits++;

            if (firstHitStamp == DataSet.Field.DefaultField.FIRST_HIT_STAMP) {
                firstHitStamp = now;
            }

            lastHitStamp = now;

        } finally {
            stateLock.unlock();
        }

        //logger.trace("Track: {}", this);
    }

    @Override
    public long getHits() {
        return hits;
    }

    @Override
    protected void setHits(final long hits) {
        this.hits = hits;
    }

    @Override
    public long getFirstHitStamp() {
        return firstHitStamp;
    }

    @Override
    protected void setFirstHitStamp(final long firstHitStamp) {
        this.firstHitStamp = firstHitStamp;
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
        return commits;
    }

    @Override
    protected void setCommits(final long commits) {
        this.commits = commits;
    }

    @Override
    public void update(final Sensor tracker, final long now) {
        queueUpdateTask(tracker, now, false);
    }

    private void updateImpl(final Sensor tracker, final long now) {
        final double currentValue = tracker.getValue();

        stateLock.lock();
        try {
            commits++;

            // First
            if (first == null) {
                first = currentValue;
            }

            // Last
            last = currentValue;

            // Min
            if (currentValue < min) {
                min = currentValue;
            }

            // Max
            if (currentValue > max) {
                max = currentValue;
            }

            // Sum
            sum += currentValue;

            for (DataRecorder dataRecorder : dataRecorders) {
                try {
                    dataRecorder.update(this, tracker, now);
                } catch (Exception e) {
                    Misc.logHandledException(logger, e, "Failed to update {}", dataRecorder);
                    Misc.handleUncaughtException(getKey(), e);
                }
            }
        } finally {
            stateLock.unlock();
        }

        //logger.trace("Commit: {}", this);
    }

    @Override
    public double getFirst() {
        Double firstValue = first;

        if (firstValue == null) {
            return DataSet.Field.DefaultField.FIRST;
        }

        return firstValue;
    }

    @Override
    protected void setFirst(final double first) {
        this.first = first;
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
        Double result = min;
        if (result.equals(Double.POSITIVE_INFINITY)) {
            result = DataSet.Field.DefaultField.MIN;
        }
        return result;
    }

    @Override
    protected void setMin(final double min) {
        this.min = min;
    }

    @Override
    public double getMax() {
        Double result = max;
        if (result.equals(Double.NEGATIVE_INFINITY)) {
            result = DataSet.Field.DefaultField.MAX;
        }
        return result;
    }

    @Override
    protected void setMax(final double max) {
        this.max = max;
    }

    @Override
    public double getSum() {
        return sum;
    }

    @Override
    protected void setSum(final double sum) {
        this.sum = sum;
    }


    @Override
    public void restore(final DataSet dataSet) {
        //assertNotNull(dataSet, "dataSet");

        stateLock.lock();
        try {
            clear();
            restoreState(dataSet);

        } finally {
            stateLock.unlock();
        }

        //logger.trace("Restore: {}", this);
    }

    @Override
    public void clear() {
        updateQueueProcessingLock.lock();
        try {
            updateQueue.clear(); // Must be called while holding updateQueueProcessingLock
            clearState();
        } finally {
            updateQueueProcessingLock.unlock();
        }
        fireCleared();
    }

    @Override
    protected void clearState() {
        stateLock.lock();
        try {
            super.clearState();
        } finally {
            stateLock.unlock();
        }
    }

    private void fireCleared() {
        //logger.trace("Clear: {}", this);

    }

    @Override
    public DataSet collectData() {
        stateLock.lock();
        try {
            return super.collectData();
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public DataSet drainData() {
        DataSet data;

        // Do not allow other threads to process entries
        updateQueueProcessingLock.lock();
        try {
            processUpdateQueue();

            data = createDataSet(true);
            collectData(data);

            clearState();
        } finally {
            updateQueueProcessingLock.unlock();
        }

        fireCleared();
        return data;
    }

    /*
     * NOTE: Must be called while holding updateQueueProcessingLock
     */
    protected void processUpdateQueue() {
        // Re-query queue size to avoid allocating a buffer if some
        // other thread has already processed the events
        int count = updateQueue.size();
        if (count > 0) {
            // Extract all entries using poll() to avoid obtaining
            // the queue write lock which would block a tracker
            // client.
            // A call to updateQueue.toArray(...) is likely to lock
            // the entire queue.
            // Drain the queue as soon as possible then process
            // later so that
            // other task threads can "short circuit" more
            // effectively.
            TrackerEntry[] entries = new TrackerEntry[count];
            for (int i = 0; i < count; i++) {
                entries[i] = updateQueue.poll();
                assert entries[i] != null;
            }

            // Do the updates within the updateQueueProcessingLock
            // to ensure they are executed linearly in the exact
            // order in which they were submitted.
            for (TrackerEntry entry : entries) {
                if (entry.track) {
                    trackImpl(entry.tracker, entry.now);
                } else {
                    updateImpl(entry.tracker, entry.now);
                }
            }
        }
    }

    /* INNER CLASSES */
    protected final class ProcessUpdateQueueTask implements Runnable {

        @Override
        public void run() {
            // Only do work if there is something in the updateQueue
            if (!updateQueue.isEmpty()) {
                // Do not allow other threads to process entries
                updateQueueProcessingLock.lock();
                try {
                    processUpdateQueue();
                } finally {
                    updateQueueProcessingLock.unlock();
                }
            }
        }
    }

    protected static final class TrackerEntry {

        private final Sensor tracker;
        private final long now;
        private final boolean track;

        private TrackerEntry(final Sensor tracker, final long now, final boolean track) {
            this.tracker = tracker;
            this.now = now;
            this.track = track;
        }

        @Override
        public String toString() {
            return tracker + " @ " + now + " " + (track ? "track" : "update");
        }
    }

//    /* NESTED CLASSES */
//
//    public static final class Factory implements StatsSessionFactory {
//        @Override
//        public StatsSession createSession(final StatsKey key,
//                                          final DataRecorder[] dataRecorders) {
//            StatsManager statsManager = StatsManagerRegistry.getInstance().getStatsManager(key.getNamespace());
//            return new AsynchronousSession(key,
//                                           statsManager.getEventManager(),
//                                           dataRecorders);
//        }
//    }
}
