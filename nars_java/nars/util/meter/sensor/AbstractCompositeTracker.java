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

import java.util.List;
import nars.util.meter.Sensor;

/**
 * A convenience base implementation of {@link CompositeTracker}.
 *
 * @author The Stajistic Project
 */
public abstract class AbstractCompositeTracker<T extends Sensor> {

    public final T[] trackers;

    public AbstractCompositeTracker(final T... trackers) {
        //assertNotEmpty(trackers, "trackers");
        this.trackers = trackers;
    }

    @SuppressWarnings("unchecked")
    public AbstractCompositeTracker(final List<T> trackers) {
        this(trackers.toArray((T[]) new Sensor[trackers.size()]));
    }

//    public Collection<? extends T> composites() {
//        return Collections.unmodifiableCollection(Arrays.asList(trackers));
//    }
    public AbstractCompositeTracker reset() {
        int len = trackers.length;
        for (int i = 0; i < len; i++) {
            trackers[i].reset();
        }
        return this;
    }

    @Override
    public String toString() {
        final int trackerCount = trackers.length;

        StringBuilder buf = new StringBuilder(32 + (64 * trackerCount));

        buf.append(getClass().getSimpleName());
        buf.append('[');

        for (int i = 0; i < trackerCount; i++) {
            if (i > 0) {
                buf.append(',');
            }

            buf.append(trackers[i]);
        }

        buf.append(']');

        return buf.toString();
    }

    //TODO create aggregation methods
    /*
     public DataSet get() {
     return getSession().collectData();
     }
     public DataSet getReset() {
     return getSession().drainData();
     }
     */
}
