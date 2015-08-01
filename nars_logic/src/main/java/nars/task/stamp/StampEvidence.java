package nars.task.stamp;

/**
 * Provides stamp evidenceBase and evidenceSet
 */
public interface StampEvidence {


    /** deduplicated and sorted version of the evidentialBase.
     * this can always be calculated deterministically from the evidentialBAse
     * since it is the deduplicated and sorted form of it. */
    abstract public long[] getEvidentialSet();



    public boolean isCyclic();

    default public boolean isInput() {
        return false;
    }

    /**
     * responsible for setting some or all of the following Stamp setters:
     *      setDuration(int duration);
     *      setCreationTime(long creationTime);
     *      setOccurrenceTime(long occurrenceTime);
     *      setEvidentialBase(long[] evidentialBase);
     */
    void applyToStamp(Stamp target);
}
