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
package nars.util.meter.key;

import java.util.Collections;
import java.util.Map;

/**
 * A singleton {@link StatsKey} implementation conforming to the null object pattern.
 *
 * @author The Stajistics Project
 */
public final class NullStatsKey implements StatsKey {

    private static final NullStatsKey instance = new NullStatsKey();

    private NullStatsKey() {}

    /**
     * Get the sole instance of NullStatsKey.
     *
     * @return The singleton NullStatsKey.
     */
    public static NullStatsKey getInstance() {
        return instance;
    }

    @Override
    public String getNamespace() {
        return "";
    }

    /**
     * @return An empty String.
     */
    @Override
    public String getName() {
        return "";
    }

    /**
     * @return <tt>null</tt>.
     */
    @Override
    public Object getAttribute(final String name) {
        return null;
    }

    /**
     * @return An empty {@link Map}.
     */
    @Override
    public Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }

    /**
     * @return <tt>0</tt>.
     */
    @Override
    public int getAttributeCount() {
        return 0;
    }



//    /**
//     * @return {@link NullStatsKeyBuilder#getInstance()}
//     */
//    @Override
//    public StatsKeyBuilder buildCopy() {
//        return NullStatsKeyBuilder.getInstance();
//    }

    /**
     * @return <tt>0</tt>.
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * @return <tt>true</tt> if <tt>obj</tt> is the same instance returned by
     *         {@link #getInstance()}, <tt>false</tt> otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        return this == obj;
    }

    /**
     * @return <tt>-1</tt>.
     */
    @Override
    public int compareTo(final StatsKey o) {
        return -1;
    }

    /**
     * @return <tt>"NullStatsKey"</tt>.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
