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
 * Used to implement the (internal) judgment processing
 */
public interface ProcessJudgment extends Process, Serializable {
    /*
     * To accept a new judgment as belief, and check for revisions and solutions.
     * Revisions will be processed as judgment tasks by themselves.
     * Due to their higher confidence, summarizing more evidence,
     * the will become the top entries in the belief table.
     * Additionally, judgements can themselves be the solution to existing questions
     * and goals, which is also processed here.
     * <p>
     * called only by ConceptProcessing.processTask
     *
     * @param task The judgment task to be accepted
     * @param concept The concept of the judment task
     * @param nal The derivation context
     */
    //void processTask(final Concept concept, final DerivationContext nal, final Task task);
}
