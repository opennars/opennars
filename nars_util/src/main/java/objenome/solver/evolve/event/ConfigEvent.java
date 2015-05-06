/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.solver.evolve.event;

import objenome.solver.evolve.GPContainer;
import objenome.solver.evolve.GPContainer.GPKey;

/**
 * An event which indicates that the configuration has changed. The event has
 * the <code>ConfigKey</code> object that has changed.
 *
 * @see GPContainer
 */
public class ConfigEvent implements Event {

    /**
     * The <code>ConfigKey</code> associated with the event.
     */
    private final GPKey<?> key;
    private final GPContainer config;

    /**
     * Constructs a <code>ConfigEvent</code>.
     *
     * @param key the <code>ConfigKey</code> associated with the event.
     */
    public ConfigEvent(GPContainer config, GPKey<?> key) {
        this.key = key;
        this.config = config;
    }

    /**
     * Returns the <code>ConfigKey</code>.
     *
     * @return the <code>ConfigKey</code>.
     */
    public GPKey<?> getKey() {
        return key;
    }

    /**
     * Determines if this <code>ConfigEvent</code>'s key is one of the specified
     * keys.
     *
     * @param keys the keys to check against.
     *
     * @return <code>true</code> if this <code>ConfigEvent</code>'s key is one
     * of the specified keys; <code>false</code> otherwise.
     */
    public boolean isKindOf(GPKey<?>... keys) {
        for (GPKey<?> k : keys) {
            if (k == key) {
                return true;
            }
        }

        return false;
    }

    public GPContainer getConfig() {
        return config;
    }
}
