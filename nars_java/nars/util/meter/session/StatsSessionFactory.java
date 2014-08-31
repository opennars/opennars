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

import java.io.Serializable;
import nars.util.meter.key.StatsKey;
import nars.util.meter.session.recorder.DataRecorder;

/**
 * A factory for {@link StatsSession} instances.
 *
 * @author The Stajistics Project
 */
public interface StatsSessionFactory extends Serializable {

    /**
     * Create a {@link StatsSession} instance for the given <tt>key</tt>.
     *
     * @param key The key for which to create a {@link StatsSession}.
     * @param dataRecorders The array of DataRecorders to be passed into the new {@link StatsSession}.
     * @return A {@link StatsSession} instance, never <tt>null</tt>.
     */
    StatsSession createSession(StatsKey key,
                               DataRecorder[] dataRecorders);

}
