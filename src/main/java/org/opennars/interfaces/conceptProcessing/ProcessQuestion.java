package org.opennars.interfaces.conceptProcessing;

import org.opennars.control.DerivationContext;
import org.opennars.entity.Concept;
import org.opennars.entity.Task;

/**
 * Used to override the (internal) question processing
 */
public interface ProcessQuestion extends Process {
    /*
     * To answer a question by existing beliefs
     *
     * @param task The task to be processed
     */
    //void processTask(final Concept concept, final DerivationContext nal, final Task task);

    /**
     * Recognize an existing belief task as solution to the what question task, which contains a query variable
     * <p>
     * called only in GeneralInferenceControl.insertTaskLink on concept selection
     *
     * @param concept The concept which potentially outdated anticipations should be processed
     * @param ques The belief task
     * @param nal The derivation context
     */
    void ProcessWhatQuestion(final Concept concept, final Task ques, final DerivationContext nal);

    /**
     * Recognize an added belief task as solution to what questions, those that contain query variable
     * <p>
     * called only in GeneralInferenceControl.insertTaskLink on concept selection
     *
     * @param concept The concept which potentially outdated anticipations should be processed
     * @param t The belief task
     * @param nal The derivation context
     */
    void ProcessWhatQuestionAnswer(final Concept concept, final Task t, final DerivationContext nal);
}
