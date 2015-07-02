package nars.truth;

import nars.Global;
import org.apache.commons.math3.util.FastMath;

import static nars.Symbols.GOAL;
import static nars.Symbols.JUDGMENT;

/**
 * Created by me on 7/1/15.
 */
public abstract class AbstractTruth implements Truth {

    /**
     * The frequency factor of the truth value
     */
    private float frequency;

    /**
     * The confidence factor of the truth value
     */
    private float confidence;



    public AbstractTruth(final float freq, final float conf) {
        super();
        setFrequency(freq);
        setConfidence(conf);
    }

    public AbstractTruth() {
        //freq and conf will begin at 0
    }

    public float getFrequency() {
        return frequency;
    }

    @Override
    public int hashCode() {
        return Truth.hash(this);
    }

    public Truth setFrequency(float f) {
        if (f > 1.0f) f = 1.0f;
        if (f < 0f) f = 0f;
        //if ((f > 1.0f) || (f < 0f)) throw new RuntimeException("Invalid frequency: " + f); //f = 0f;

        final float e = getEpsilon();
        this.frequency = FastMath.round(f / e) * e;
        return this;
    }

    @Override
    public float getConfidence() {
        return confidence;
    }

    public Truth setConfidence(float c) {
        //if ((c > 1.0f) || (c < 0f)) throw new RuntimeException("Invalid confidence: " + c);
        final float maxConf = getConfidenceMax();
        if (c > maxConf) c = maxConf;
        if (c < 0) c = 0;

        final float e = getEpsilon();
        this.confidence = Math.round(c / e) * e;
        return this;
    }

    public BasicTruth clone() {
        if (isAnalytic())
            return new AnalyticTruth(this);
        else
            return new BasicTruth(this);
    }


    @Override
    public float getConfidenceMax() {
        return Global.MAX_CONFIDENCE;
    }

    /**
     * Compare two truth values
     *
     * @param that The other TruthValue
     * @return Whether the two are equivalent
     */

    @Override
    public boolean equals(final Object that) {
        if (that == this) return true;
        if (that instanceof Truth) {
            final Truth t = ((Truth) that);

            final float e = getEpsilon();

            if (!Truth.isEqual(getFrequency(), t.getFrequency(), e))
                return false;
            if (!Truth.isEqual(getConfidence(), t.getConfidence(), e))
                return false;
            return true;
        }
        return false;
    }


    public static float getDefaultConfidence(char punctuation) {

        switch (punctuation) {
            case JUDGMENT:
                return Global.DEFAULT_JUDGMENT_CONFIDENCE;

            case GOAL:
                return Global.DEFAULT_GOAL_CONFIDENCE;

            default:
                throw new RuntimeException("Invalid punctuation " + punctuation + " for a TruthValue");
        }
    }

    /**
     * The String representation of a TruthValue, as used internally by the system
     *
     * @return The String
     */
    @Override
    public String toString() {
        //return DELIMITER + frequency.toString() + SEPARATOR + confidence.toString() + DELIMITER;

        //1 + 6 + 1 + 6 + 1
        return name().toString();
    }

}
