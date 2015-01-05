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

/**
 * A tracker type dedicated to collecting statistics related to some span. The
 * span being measured will be different depending on the context and the
 * SpanTracker implementation, but is usually related to time.
 *
 * @author The Stajistics Project
 */
public interface SpanTracker extends Meter {

    /**
     * The factory that will produce the default type of {@link SpanTracker}
     * instances.
     */
    //public static final TrackerFactory<SpanTracker> FACTORY = TimeDurationTracker.FACTORY;
    /**
     * <p>
     * Begin tracking statistics related to some span. The measurement of the
     * span should eventually be completed by a call to {@link #stop()}.</p>
     *
     * <p>
     * After this call {@link #isTracking()} will return <tt>true</tt>.</p>
     *
     * <p>
     * This method does nothing except log a warning if called when this tracker
     * is already tracking.</p>
     *
     * @return <tt>this</tt>.
     */
    SpanTracker start();

    /**
     * <p>
     * Finish tracking statistics related to some span that was initiated by a
     * call to {@link #start()}.</p>
     *
     * <p>
     * After this call {@link #isTracking()} will return <tt>false</tt>.</p>
     *
     * <p>
     * This method does nothing except log a warning if called when this tracker
     * is not already tracking.</p>
     *
     * @return <tt>this</tt>.
     */
    void stop();

    /**
     * Determine if this tracker is actively collecting statistics from a prior
     * call to {@link #start()}. If <tt>true</tt> is returned, this tracker is
     * awaiting a call to {@link #stop()} to finish collecting statistics.
     *
     * @return <tt>true</tt> if statistics are being collected, <tt>false</tt>
     * otherwise.
     */
    boolean isTracking();

    /**
     * Determine the most recent time {@link #start()} was called on this
     * tracker.
     *
     * @return <tt>this</tt>.
     */
    long getStartTime();
}
