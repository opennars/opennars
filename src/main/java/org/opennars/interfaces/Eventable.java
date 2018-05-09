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
package org.opennars.interfaces;

import org.opennars.io.events.EventEmitter;

/**
 * Implementation can observe events
 */
public interface Eventable {
    void on(final Class c, final EventEmitter.EventObserver o);
    void off(final Class c, final EventEmitter.EventObserver o);
    void event(final EventEmitter.EventObserver e, final boolean enabled, final Class... events);
    void emit(final Class c, final Object... o);
}
