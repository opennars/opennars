package nars.task.stamp;

/**
 * Indicates that this can be used to "stamp" a sentence
 */
@Deprecated public interface AbstractStamper {

    /**
     * responsible for setting some or all of the following Stamp setters:
     *      setDuration(int duration);
     *      setCreationTime(long creationTime);
     *      setOccurrenceTime(long occurrenceTime);
     *      setEvidentialBase(long[] evidentialBase);
     */
    void applyToStamp(Stamp target);



}
