package org.opennars.interfaces.conceptProcessing;

import org.opennars.control.DerivationContext;
import org.opennars.entity.Concept;
import org.opennars.entity.Task;

/**
 * Used to implement the (internal) goal processing
 */
public interface ProcessGoal {
    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it, potentially executing in case of an operation goal
     *
     * @param concept The concept of the goal
     * @param nal The derivation context
     * @param task The goal task to be processed
     */
    void processGoal(final Concept concept, final DerivationContext nal, final Task task);
}
