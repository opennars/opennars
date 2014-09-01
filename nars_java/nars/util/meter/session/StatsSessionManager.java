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
import java.util.Collection;
import java.util.Set;
import nars.util.meter.key.StatsKey;
import nars.util.meter.key.StatsKeyMatcher;
import nars.util.meter.util.ServiceLifeCycle;

/**
 * Manages the lifecycle of StatsSession instances.
 *
 * @author The Stajistics Project
 */
public interface StatsSessionManager extends Serializable, ServiceLifeCycle {

    /**
     * Get the total number of {@link StatsSession} instance being managed.
     *
     * @return The number of {@link StatsSession} instances.
     */
    int getSessionCount();

    /**
     * Get the Set of {@link String}s that are associated with the
     * {@link StatsSession}s being managed.
     *
     * @return A Set of {@link String}s, or an empty Set if there are none.
     */
    Set<String> getKeys();

    /**
     * Get the Set of matching {@link String}s that are associated with the
     * {@link StatsSession}s being managed. The passed <tt>keyMatcher</tt> is
     * used to select the desired keys.
     *
     * @param keyMatcher The key matcher with which to filter results.
     * @return A Set of {@link String}s, or an empty Set if there are none.
     */
    Set<String> getKeys(StatsKeyMatcher keyMatcher);

    /**
     * Get all {@link StatsSession}s being managed.
     *
     * @return A Collection of {@link StatsSession}s, or an empty Collection if
     * there are none.
     */
    Collection<StatsSession> getSessions();

    /**
     * Get any {@link StatsSession}s being managed where it's key is matched by
     * the given matcher.
     *
     * @param keyMatcher The key matcher with which to select session keys.
     * @return A Collection of {@link StatsSession}s, or an empty Collection if
     * there are none.
     */
    Collection<StatsSession> getSessions(StatsKeyMatcher keyMatcher);

    /**
     * Get the {@link StatsSession} being managed for the given <tt>key</tt>.
     *
     * @param key The key for which to return the associated
     * {@link StatsSession}.
     * @return The {@link StatsSession} associated with the given <tt>key</tt>
     * or <tt>null</tt>
     * if not found.
     */
    StatsSession getSession(StatsKey key);

    /**
     * Get the {@link StatsSession} being managed for the given <tt>key</tt>, or
     * if the session does not exist, create it and return it.
     *
     * @param key The key for which to return the associated
     * {@link StatsSession}.
     * @return The {@link StatsSession} associated with the given <tt>key</tt>,
     * never <tt>null</tt>.
     */
    StatsSession getOrCreateSession(StatsKey key);

    /**
     * Remove the {@link StatsSession} being managed that is associated with the
     * given <tt>key</tt>.
     *
     * @param key The key for which to remove the {@link StatsSession}.
     * @return The {@link StatsSession} instance that was removed, or
     * <tt>null</tt> if not found.
     */
    StatsSession remove(StatsKey key);

    /**
     * Remove the given <tt>session</tt> instance from this manager.
     *
     * @param session The {@link StatsSession} instance to remove.
     * @return <tt>true</tt> if <tt>session</tt> was found and removed,
     * <tt>false</tt> if not found.
     */
    boolean remove(StatsSession session);

    /**
     * Remove all {@link StatsSession} instances from this manager.
     */
    void clear();

    /**
     * Call {@link StatsSession#clear()} on each {@link StatsSession} instance
     * known to this manager.
     */
    void clearAllSessions();

}
