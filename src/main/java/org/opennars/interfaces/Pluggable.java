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

import org.opennars.main.Nar;
import org.opennars.plugin.Plugin;

import java.util.List;

/**
 * Implementation can have plugins
 */
public interface Pluggable {
    /**
     * adds/registers a plugin
     * @param plugin plugin to be registered
     */
    void addPlugin(final Plugin plugin);

    /**
     * removes a plugin
     * @param pluginState plugin to be removed
     */
    void removePlugin(final Nar.PluginState pluginState);

    /**
     * returns all plugins which were added
     * @return plugins
     */
    List getPlugins();
}
