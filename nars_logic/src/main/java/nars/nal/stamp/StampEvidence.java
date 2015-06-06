package nars.nal.stamp;

/**
 * Provides stamp evidenceBase and evidenceSet
 */
public interface StampEvidence extends AbstractStamper {

    /** the evidentialBase array of serials */
    abstract public long[] getEvidentialBase();

    /** deduplicated and sorted version of the evidentialBase.
     * this can always be calculated deterministically from the evidentialBAse
     * since it is the deduplicated and sorted form of it. */
    abstract public long[] getEvidentialSet();



    default public boolean isCyclic() {
        return Stamp.isCyclic(this);
    }

}
