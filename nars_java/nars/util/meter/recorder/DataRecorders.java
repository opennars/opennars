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

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nars.util.meter.Meter;
import nars.util.meter.data.DataSet;
import nars.util.meter.session.StatsSession;
import nars.util.meter.util.ThreadSafe;

/**
 * Provides utility methods for manipulating {@link DataRecorder}s.
 *
 * @author The Stajistics Project
 */
public final class DataRecorders {

    private DataRecorders() {
    }

    /**
     * Determine if the <tt>dataRecorder</tt> is annotated with
     * {@link ThreadSafe}.
     *
     * @param dataRecorder The {@link DataRecorder} to test.
     * @return <tt>true</tt> if <tt>dataRecorder</tt> is thread safe,
     * <tt>false</tt> otherwise.
     */
    public static boolean isThreadSafe(final DataRecorder dataRecorder) {
        if (dataRecorder.getClass()
                .getAnnotation(ThreadSafe.class) != null) {
            return true;
        }

        return false;
    }

    /**
     * Decorate <tt>dataRecorder</tt> with a wrapper that implements locking on
     * all method calls. This will create a new {@link Lock} instance to protect
     * method calls. Equivalent to calling <tt>locking(DataRecorder, null)</tt>.
     *
     * @param dataRecorder The {@link DataRecorder} to wrap.
     * @return A new {@link DataRecorder} instance that wraps
     * <tt>dataRecorder</tt>.
     *
     * @see #locking(DataRecorder, Lock)
     */
    public static DataRecorder locking(final DataRecorder dataRecorder) {
        return locking(dataRecorder, null);
    }

    /**
     * Decorate <tt>dataRecorder</tt> with a wrapper that implements locking on
     * all method calls using the given <tt>lock</tt>.
     *
     * @param dataRecorder The {@link DataRecorder} to wrap.
     * @param lock The {@link Lock} to use to lock all <tt>dataRecorder</tt>
     * method calls. May be <tt>null</tt> to create a new {@link Lock}.
     * @return A new {@link DataRecorder} instance that wraps
     * <tt>dataRecorder</tt>.
     *
     * @see #locking(DataRecorder)
     */
    public static DataRecorder locking(final DataRecorder dataRecorder,
            final Lock lock) {
        return new LockingDataRecorderDecorator(dataRecorder, lock);
    }

    /**
     * A convenience method for wrapping all elements of <tt>dataRecorders</tt>
     * in the same manner as {@link #locking(DataRecorder)} does for a single
     * {@link DataRecorder}. This will create a new {@link Lock} instance for
     * each <tt>dataRecorders</tt> element. Equivalent to calling
     * <tt>locking(DataRecorder[], null)</tt>.
     *
     * @param dataRecorders An array of {@link DataRecorder}s to be wrapped.
     * This array will not be modified.
     * @return A new array of {@link DataRecorder}s that wrap the passed
     * <tt>dataRecorders</tt>.
     *
     * @see #locking(DataRecorder[], Lock)
     */
    public static DataRecorder[] locking(final DataRecorder[] dataRecorders) {
        return locking(dataRecorders, null);
    }

    /**
     * A convenience method for wrapping all elements of <tt>dataRecorders</tt>
     * in the same manner as {@link #locking(DataRecorder, Lock)} does for a
     * single {@link DataRecorder}.
     *
     * @param dataRecorders An array of {@link DataRecorder}s to be wrapped.
     * This array will not be modified.
     * @param lock The shared {@link Lock} to use to lock all method calls of
     * each <tt>dataRecorders</tt> element. May be <tt>null</tt> to create a new
     * {@link Lock} instance for each <tt>dataRecorders</tt> element.
     * @return A new array of {@link DataRecorder}s that wrap the passed
     * <tt>dataRecorders</tt>.
     *
     * @see #locking(DataRecorder[])
     */
    public static DataRecorder[] locking(final DataRecorder[] dataRecorders,
            final Lock lock) {
        if (dataRecorders == null) {
            return null;
        }

        DataRecorder[] result = new DataRecorder[dataRecorders.length];

        for (int i = 0; i < dataRecorders.length; i++) {
            result[i] = locking(dataRecorders[i], lock);
        }

        return result;
    }

    /**
     * Decorates the given <tt>dataRecorder</tt> using the
     * {@link #locking(DataRecorder, Lock)} method but only if
     * {@link #isThreadSafe(DataRecorder)} returns <tt>false</tt> for the
     * <tt>dataRecorder</tt>. Equivalent to calling
     * <tt>lockingIfNeeded(DataRecorder, null)</tt>.
     *
     * @param dataRecorder The {@link DataRecorder} to wrap if it is not thread
     * safe.
     * @return A new {@link DataRecorder} instance that wraps
     * <tt>dataRecorder</tt> if it is not thread safe. If <tt>dataRecorder</tt>
     * is thread safe, it is returned untouched.
     *
     * @see #lockingIfNeeded(DataRecorder, Lock)
     */
    public static DataRecorder lockingIfNeeded(final DataRecorder dataRecorder) {
        return lockingIfNeeded(dataRecorder, null);
    }

    /**
     * Decorates the given <tt>dataRecorder</tt> using the
     * {@link #locking(DataRecorder, Lock)} method but only if
     * {@link #isThreadSafe(DataRecorder)} returns <tt>false</tt> for the
     * <tt>dataRecorder</tt>.
     *
     * @param dataRecorder The {@link DataRecorder} to wrap if it is not thread
     * safe.
     * @param lock The {@link Lock} to use to lock all <tt>dataRecorder</tt>
     * method calls if it is not thread safe. May be <tt>null</tt> to create a
     * new {@link Lock}.
     * @return A new {@link DataRecorder} instance that wraps
     * <tt>dataRecorder</tt> if it is not thread safe. If <tt>dataRecorder</tt>
     * is thread safe, it is returned untouched.
     *
     * @see #lockingIfNeeded(DataRecorder)
     */
    public static DataRecorder lockingIfNeeded(final DataRecorder dataRecorder,
            final Lock lock) {
        if (isThreadSafe(dataRecorder)) {
            return dataRecorder;
        }

        return locking(dataRecorder, lock);
    }

    /**
     * A convenience method for wrapping elements of <tt>dataRecorders</tt> in
     * the same manner as {@link #locking(DataRecorder)} does for a single
     * {@link DataRecorder}, however, an element will only be wrapped if
     * {@link #isThreadSafe(DataRecorder)} returns
     * <tt>false</tt> for it. This will create a new {@link Lock} for each
     * element that needs to be decorated. Equivalent to calling
     * <tt>lockingIfNeeded(DataRecorder[], null)</tt>.
     *
     * @param dataRecorders An array of {@link DataRecorder}s to be wrapped.
     * This array will not be modified.
     * @return A new array of {@link DataRecorder}s that wrap the passed
     * <tt>dataRecorders</tt>.
     *
     * @see #lockingIfNeeded(DataRecorder[], Lock)
     */
    public static DataRecorder[] lockingIfNeeded(final DataRecorder[] dataRecorders) {
        return lockingIfNeeded(dataRecorders, null);
    }

    /**
     * A convenience method for wrapping elements of <tt>dataRecorders</tt> in
     * the same manner as {@link #locking(DataRecorder)} does for a single
     * {@link DataRecorder}, however, an element will only be wrapped if
     * {@link #isThreadSafe(DataRecorder)} returns
     * <tt>false</tt> for it.
     *
     * @param dataRecorders An array of {@link DataRecorder}s to be wrapped.
     * This array will not be modified.
     * @param lock The shared {@link Lock} to use to lock all method calls of
     * each <tt>dataRecorders</tt> element. May be <tt>null</tt> to create a new
     * {@link Lock} instance for each <tt>dataRecorders</tt> element.
     * @return A new array of {@link DataRecorder}s that wrap the passed
     * <tt>dataRecorders</tt>.
     *
     * @see #lockingIfNeeded(DataRecorder[])
     */
    public static DataRecorder[] lockingIfNeeded(final DataRecorder[] dataRecorders,
            final Lock lock) {
        if (dataRecorders == null) {
            return null;
        }

        DataRecorder[] result = new DataRecorder[dataRecorders.length];

        for (int i = 0; i < dataRecorders.length; i++) {
            result[i] = lockingIfNeeded(dataRecorders[i], lock);
        }

        return result;
    }

    /* NESTED CLASSES */
    @ThreadSafe
    private static final class LockingDataRecorderDecorator
            implements DataRecorder {

        private final DataRecorder delegate;
        private final Lock lock;

        private LockingDataRecorderDecorator(final DataRecorder delegate,
                final Lock lock) {
            //assertNotNull(delegate, "delegate");
            this.delegate = delegate;

            if (lock == null) {
                this.lock = new ReentrantLock();
            } else {
                this.lock = lock;
            }
        }

        public DataRecorder delegate() {
            return delegate;
        }

        @Override
        public void clear() {
            lock.lock();
            try {
                delegate.clear();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Object getField(final StatsSession session,
                final String name) {
            lock.lock();
            try {
                return delegate.getField(session, name);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void collectData(final StatsSession session,
                final DataSet dataSet) {
            lock.lock();
            try {
                delegate.collectData(session, dataSet);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Set<String> getSupportedFieldNames() {
            lock.lock();
            try {
                return delegate.getSupportedFieldNames();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void restore(final DataSet dataSet) {
            lock.lock();
            try {
                delegate.restore(dataSet);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void update(final StatsSession session,
                final Meter tracker,
                final long now) {
            lock.lock();
            try {
                delegate.update(session, tracker, now);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(128);

            buf.append(getClass().getSimpleName());
            buf.append('[');
            buf.append(delegate.toString());
            buf.append(']');

            return buf.toString();
        }
    }
}
