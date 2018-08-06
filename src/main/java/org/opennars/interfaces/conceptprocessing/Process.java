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
package org.opennars.interfaces.conceptprocessing;

import org.opennars.control.DerivationContext;
import org.opennars.entity.Concept;
import org.opennars.entity.Task;

/**
 * Used to implement the (internal) task processing
 */
public interface Process {
    /**
     * process the task
     *
     * @param task The judgment task to be accepted
     * @param concept The concept of the judment task
     * @param nal The derivation context
     */
    void processTask(final Concept concept, final DerivationContext nal, final Task task);
}
