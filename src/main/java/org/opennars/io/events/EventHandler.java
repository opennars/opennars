/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.io.events;

import org.opennars.main.Nar;

/**
 *
 */
public abstract class EventHandler implements EventEmitter.EventObserver {
    protected final EventEmitter source;
    protected boolean active = false;
    private final Class[] events;

    public EventHandler(final Nar n, final boolean active, final Class... events) {
        this(n.memory.event, active, events);
    }
    
    public EventHandler(final EventEmitter source, final boolean active, final Class... events) {
        this.source = source;
        this.events = events;
        setActive(active);
    }

    public void setActive(final boolean b) {
        if (this.active == b) return;
        
        this.active = b;
        source.set(this, b, events);
    }

    public boolean isActive() {
        return active;
    }
}
