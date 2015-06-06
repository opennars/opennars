package nars.nal.stamp;

import nars.nal.Sentence;
import nars.nal.term.Compound;

import java.util.function.Consumer;

/**
 * Indicates that this can be used to "stamp" a sentence
 */
public interface AbstractStamper {

    /**
     * responsible for setting some or all of the following Stamp setters:
     *      setDuration(int duration);
     *      setCreationTime(long creationTime);
     *      setOccurrenceTime(long occurrenceTime);
     *      setEvidentialBase(long[] evidentialBase);
     */
    void applyToStamp(Stamp target);


}
