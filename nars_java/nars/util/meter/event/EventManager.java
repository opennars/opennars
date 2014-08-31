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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import nars.util.meter.key.StatsKey;
import nars.util.meter.util.ServiceLifeCycle;
import nars.util.meter.key.StatsKeyMatcher;


/**
 *
 *
 *
 * @author The Stajistics Project
 */
public interface EventManager extends Serializable,ServiceLifeCycle {

    Collection<EventHandler> getGlobalEventHandlers();

    Map<StatsKey,Collection<EventHandler>> getEventHandlers();

    Map<StatsKey,Collection<EventHandler>> getEventHandlers(StatsKeyMatcher keyMatcher);

    void addGlobalEventHandler(EventHandler eventHandler);

    void addEventHandler(StatsKey key,
                         EventHandler eventHandler);

    void removeGlobalEventHandler(EventHandler eventHandler);

    void removeEventHandler(StatsKey key,
                            EventHandler eventHandler);

    void clearAllEventHandlers();

    void clearGlobalEventHandlers();

    void clearEventHandlers();

    void fireEvent(EventType eventType,
                   StatsKey key,
                   Object target);

}
