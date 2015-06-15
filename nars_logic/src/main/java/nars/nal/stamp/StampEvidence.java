package nars.nal.stamp;

/**
 * Provides stamp evidenceBase and evidenceSet
 */
public interface StampEvidence extends AbstractStamper {


    /** deduplicated and sorted version of the evidentialBase.
     * this can always be calculated deterministically from the evidentialBAse
     * since it is the deduplicated and sorted form of it. */
    abstract public long[] getEvidentialSet();



    public boolean isCyclic();

}
