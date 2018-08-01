package org.opennars.interfaces.conceptProcessing;

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
