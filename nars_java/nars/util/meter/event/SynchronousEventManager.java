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
package nars.util.meter.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import nars.util.meter.key.StatsKey;
import nars.util.meter.util.ServiceLifeCycle.Support;
import static nars.util.meter.util.Util.assertNotNull;
import nars.util.meter.key.StatsKeyMatcher;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
public class SynchronousEventManager implements EventManager {

    private static final Logger logger = Logger.getLogger(SynchronousEventManager.class.toString());

    private final List<EventHandler> globalEventHandlers = createEventHandlerList();

    private final ConcurrentMap<StatsKey,List<EventHandler>> sessionEventHandlers =
        new ConcurrentHashMap<StatsKey,List<EventHandler>>();

    private final Support lifeCycleSupport = new Support();

    protected List<EventHandler> createEventHandlerList() {
        return new CopyOnWriteArrayList<EventHandler>();
    }

    @Override
    public void initialize() {
        lifeCycleSupport.initialize(null);
    }

    @Override
    public boolean isRunning() {
        return lifeCycleSupport.isRunning();
    }

    @Override
    public void shutdown() {
        lifeCycleSupport.shutdown(null);
    }

    @Override
    public Collection<EventHandler> getGlobalEventHandlers() {
        return Collections.unmodifiableCollection(globalEventHandlers);
    }

    @Override
    public Map<StatsKey,Collection<EventHandler>> getEventHandlers() {
        return getEventHandlers(StatsKeyMatcher.all());
    }

    @Override
    public Map<StatsKey,Collection<EventHandler>> getEventHandlers(final StatsKeyMatcher keyMatcher) {
        if (keyMatcher.equals(StatsKeyMatcher.none())) {
            return Collections.emptyMap();
        }

        Map<StatsKey,Collection<EventHandler>> matches =
            new HashMap<StatsKey,Collection<EventHandler>>(sessionEventHandlers.size());

        for (Map.Entry<StatsKey,List<EventHandler>> entry : sessionEventHandlers.entrySet()) {
            if (keyMatcher.matches(entry.getKey())) {
                matches.put(entry.getKey(),
                            Collections.unmodifiableCollection(new ArrayList<EventHandler>(entry.getValue())));
            }
        }

        return Collections.unmodifiableMap(matches);
    }

    @Override
    public void addGlobalEventHandler(final EventHandler eventHandler) {
        assertNotNull(eventHandler, "eventHandler");
        globalEventHandlers.add(eventHandler);
    }

    @Override
    public void addEventHandler(final StatsKey key,
                                final EventHandler eventHandler) {
        assertNotNull(key, "key");
        assertNotNull(eventHandler, "eventHandler");

        List<EventHandler> eventHandlers = getEventHandlers(key, true);
        eventHandlers.add(eventHandler);
    }

    @Override
    public void removeGlobalEventHandler(EventHandler eventHandler) {
        globalEventHandlers.remove(eventHandler);
    }

    @Override
    public void removeEventHandler(StatsKey key, EventHandler eventHandler) {
        List<EventHandler> eventHandlers = getEventHandlers(key, false);
        if (eventHandlers != null) {
            eventHandlers.remove(eventHandler);
        }
    }

    @Override
    public void clearGlobalEventHandlers() {
        globalEventHandlers.clear();
    }

    @Override
    public void clearEventHandlers() {
        for (Map.Entry<StatsKey,List<EventHandler>> entry : sessionEventHandlers.entrySet()) {
            entry.getValue().clear();
        }

        sessionEventHandlers.clear();
    }

    @Override
    public void clearAllEventHandlers() {
        clearGlobalEventHandlers();
        clearEventHandlers();
    }

    private List<EventHandler> getEventHandlers(final StatsKey key,
                                                final boolean create) {
        List<EventHandler> eventHandlers = null;
        if (key != null) {
            eventHandlers = sessionEventHandlers.get(key);
        }
        if (eventHandlers == null && create) {
            eventHandlers = createEventHandlerList();
            List<EventHandler> old = sessionEventHandlers.putIfAbsent(key, eventHandlers);
            if (old != null) {
                eventHandlers = old;
            }
        }

        return eventHandlers;
    }

    @Override
    public void fireEvent(final EventType eventType,
                          final StatsKey key,
                          final Object target) {
        assertNotNull(eventType, "eventType");
        assertNotNull(target, "target");

        logger.info("Firing event: {}, key: {}" + eventType + ' ' + key);

        List<EventHandler> sessionEventHandlers = getEventHandlers(key, false);
        if (sessionEventHandlers != null) {
            fireEvent(sessionEventHandlers, eventType, key, target);
        }

        fireEvent(globalEventHandlers, eventType, key, target);
    }

    protected void fireEvent(final List<EventHandler> handlers,
                             final EventType eventType,
                             final StatsKey key,
                             final Object target) {
        for (EventHandler handler : handlers) {
            try {
                handler.handleStatsEvent(eventType, key, target);
            } catch (Exception e) {
                logger.severe("Uncaught Exception: " + e);
            }
        }
    }
}
