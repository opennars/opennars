package nars.util.meter.session;

///* Copyright 2009 - 2010 The Stajistics Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package nars.core.monitor.meter.session;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Set;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import nars.core.monitor.meter.event.EventManager;
//import nars.core.monitor.meter.event.EventType;
//import nars.core.monitor.meter.key.StatsKey;
//import nars.core.monitor.meter.key.StatsProperties;
//import nars.core.monitor.meter.session.recorder.DataRecorder;
//import static nars.core.monitor.meter.util.Util.//assertNotNull;
//import nars.io.Output;
//
//
///**
// * The default implementation of {@link StatsSessionManager}. All operations are done in a
// * thread safe manner using concurrent utils.
// *
// * @author The Stajistics Project
// */
//public class DefaultSessionManager implements StatsSessionManager {
//
//    public static final String PROP_INITIAL_CAPACITY =
//        StatsSessionManager.class.getName() + ".sessionMap.initialCapacity";
//    public static final String PROP_LOAD_FACTOR =
//        StatsSessionManager.class.getName() + ".sessionMap.loadFactor";
//    public static final String PROP_CONCURRENCY_LEVEL =
//        StatsSessionManager.class.getName() + ".sessionMap.concurrencyLevel";
//
//    private static final Output Output = OutputFactory.getOutput(DefaultSessionManager.class);
//
//    protected ConcurrentMap<StatsKey,StatsSession> sessionMap = createSessionMap();
//
//    protected final StatsConfigManager configManager;
//    protected final EventManager eventManager;
//
//    private final Support lifeCycleSupport = new Support();
//
//    public DefaultSessionManager(final StatsConfigManager configManager,
//                                 final EventManager eventManager) {
//        //assertNotNull(configManager, "configManager");
//        //assertNotNull(eventManager, "eventManager");
//
//        this.configManager = configManager;
//        this.eventManager = eventManager;
//    }
//
//    @Override
//    public void initialize() {
//        lifeCycleSupport.initialize(new Callable<Void>() {
//            @Override
//            public Void call() throws Exception {
//                eventManager.fireEvent(EventType.SESSION_MANAGER_INITIALIZED, null, DefaultSessionManager.this);
//                return null;
//            }
//        });
//    }
//
//    @Override
//    public boolean isRunning() {
//        return lifeCycleSupport.isRunning();
//    }
//
//    @Override
//    public void shutdown() {
//        lifeCycleSupport.shutdown(new Callable<Void>() {;
//            @Override
//            public Void call() throws Exception {
//                eventManager.fireEvent(EventType.SESSION_MANAGER_SHUTTING_DOWN, null, DefaultSessionManager.this);
//                clear();
//                return null;
//            }
//        });
//    }
//
//    /**
//     * A factory method for creating the ConcurrentMap that will store {@link StatsSession}s.
//     *
//     * @return A {@link ConcurrentMap}, never <tt>null</tt>.
//     */
//    protected ConcurrentMap<StatsKey,StatsSession> createSessionMap() {
//
//        int initialCapacity = StatsProperties.getIntegerProperty(PROP_INITIAL_CAPACITY, 256);
//        float loadFactor = StatsProperties.getFloatProperty(PROP_LOAD_FACTOR, 0.6f);
//        int concurrencyLevel = StatsProperties.getIntegerProperty(PROP_CONCURRENCY_LEVEL, 64);
//
//        return new ConcurrentHashMap<StatsKey,StatsSession>(initialCapacity, loadFactor, concurrencyLevel);
//    }
//
//    @Override
//    public int getSessionCount() {
//        return sessionMap.size();
//    }
//
//    @Override
//    public Set<String> getKeys() {
//        return Collections.unmodifiableSet(sessionMap.keySet());
//    }
//
//    @Override
//    public Set<String> getKeys(final StringMatcher keyMatcher) {
//        return keyMatcher.filterCopy(sessionMap.keySet());
//    }
//
//    @Override
//    public Collection<StatsSession> getSessions() {
//        return Collections.unmodifiableCollection(sessionMap.values());
//    }
//
//    @Override
//    public Collection<StatsSession> getSessions(final StringMatcher keyMatcher) {
//        return keyMatcher.filterToCollection(sessionMap);
//    }
//
//    @Override
//    public StatsSession getSession(final StatsKey key) {
//        return sessionMap.get(key);
//    }
//
//    @Override
//    public StatsSession getOrCreateSession(final StatsKey key) {
//        //assertNotNull(key, "key");
//
//        StatsSession session = sessionMap.get(key);
//
//        if (session == null) {
//            session = createSession(key);
//
//            StatsSession existingSession = sessionMap.putIfAbsent(key, session);
//            if (existingSession != null) {
//                session = existingSession;
//
//            } else {
//                eventManager.fireEvent(EventType.SESSION_CREATED, key, session);
//            }
//        }
//
//        return session;
//    }
//
//    @Override
//    public boolean remove(final StatsSession statsSession) {
//        return remove(statsSession.getKey()) != null;
//    }
//
//    @Override
//    public StatsSession remove(final StatsKey key) {
//        StatsSession session = sessionMap.remove(key);
//
//        if (session != null) {
//            eventManager.fireEvent(EventType.SESSION_DESTROYED, key, session);
//        }
//
//        return session;
//    }
//
//    /**
//     * A factory method for creating a {@link StatsSession} instance.
//     *
//     * @param key The key for which to create a {@link StatsSession}.
//     * @return A {@link StatsSession} instance, never <tt>null</tt>.
//     */
//    protected StatsSession createSession(final StatsKey key) {
//        StatsConfig config = configManager.getOrCreateConfig(key);
//
//        DataRecorder[] dataRecorders = config.getDataRecorderFactory()
//                                             .createDataRecorders();
//
//        StatsSession session = config.getSessionFactory()
//                                     .createSession(key,
//                                                    dataRecorders);
//
//        Output.debug("Created StatsSession for key: {}", key);
//
//        return session;
//    }
//
//    @Override
//    public void clear() {
//        Set<String> keySet = sessionMap.keySet();
//        for (StatsKey key : keySet) {
//            remove(key);
//        }
//    }
//
//    @Override
//    public void clearAllSessions() {
//        for (StatsSession session : sessionMap.values()) {
//            session.clear();
//        }
//    }
//
//}
