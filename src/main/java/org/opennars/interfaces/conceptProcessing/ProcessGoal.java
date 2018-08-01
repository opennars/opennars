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
package org.opennars.interfaces.conceptProcessing;

import java.io.Serializable;

/**
 * Used to implement the (internal) goal processing
 */
public interface ProcessGoal extends Process, Serializable {
    /*
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it, potentially executing in case of an operation goal
     *
     * @param concept The concept of the goal
     * @param nal The derivation context
     * @param task The goal task to be processed
     */
    //void processTask(final Concept concept, final DerivationContext nal, final Task task);
}
