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

import java.io.Serializable;

/**
 * Used to implement the (internal) question processing
 */
public interface ProcessQuestion extends Process, Serializable {
    /**
     * Recognize an existing belief task as solution to the what question task, which contains a query variable
     * <p>
     * called only in GeneralInferenceControl.insertTaskLink on concept selection
     *
     * @param concept The concept which potentially outdated anticipations should be processed
     * @param ques The belief task
     * @param nal The derivation context
     */
    void processWhatQuestion(final Concept concept, final Task ques, final DerivationContext nal);

    /**
     * Recognize an added belief task as solution to what questions, those that contain query variable
     * <p>
     * called only in GeneralInferenceControl.insertTaskLink on concept selection
     *
     * @param concept The concept which potentially outdated anticipations should be processed
     * @param t The belief task
     * @param nal The derivation context
     */
    void processWhatQuestionAnswer(final Concept concept, final Task t, final DerivationContext nal);
}
