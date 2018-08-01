package org.opennars.interfaces.conceptProcessing;

/**
 * Used to implement the (internal) judgment processing
 */
public interface ProcessJudgment extends Process {
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
