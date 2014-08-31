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

/**
 * 
 * 
 *
 * @author The Stajistics Project
 */
public enum EventType {

    STATS_MANAGER_INITIALIZED,
    STATS_MANAGER_SHUTTING_DOWN,

    CONFIG_MANAGER_INITIALIZED,
    CONFIG_MANAGER_SHUTTING_DOWN,

    SESSION_MANAGER_INITIALIZED,
    SESSION_MANAGER_SHUTTING_DOWN,

    CONFIG_CREATED,
    CONFIG_CHANGED,
    CONFIG_DESTROYED,

    SESSION_CREATED,
    SESSION_CLEARED,
    SESSION_RESTORED,
    SESSION_DESTROYED,

    TRACKER_TRACKING,
    TRACKER_COMMITTED;

    public boolean isManagerEvent() {
        return isStatsManagerEvent() || isSessionManagerEvent() || isConfigEvent();
    }

    public boolean isStatsManagerEvent() {
        switch (this) {
            case STATS_MANAGER_INITIALIZED:
            case STATS_MANAGER_SHUTTING_DOWN:
                return true;
            default:
                return false;
        }
    }

    public boolean isConfigManagerEvent() {
        switch (this) {
            case CONFIG_MANAGER_INITIALIZED:
            case CONFIG_MANAGER_SHUTTING_DOWN:
                return true;
            default:
                return false;
        }
    }

    public boolean isSessionManagerEvent() {
        switch (this) {
            case SESSION_MANAGER_INITIALIZED:
            case SESSION_MANAGER_SHUTTING_DOWN:
                return true;
            default:
                return false;
        }
    }

    public boolean isConfigEvent() {
        switch (this) {
            case CONFIG_CREATED:
            case CONFIG_CHANGED:
            case CONFIG_DESTROYED:
                return true;
            default:
                return false;
        }
    }

    public boolean isSessionEvent() {
        switch (this) {
            case SESSION_CREATED:
            case SESSION_CLEARED:
            case SESSION_RESTORED:
            case SESSION_DESTROYED:
                return true;
            default:
                return false;
        }
    }

    public boolean isTrackerEvent() {
        switch (this) {
            case TRACKER_TRACKING:
            case TRACKER_COMMITTED:
                return true;
            default:
                return false;
        }
    }
}
