package nars.truth;

import nars.Global;
import org.apache.commons.math3.util.FastMath;

import static nars.Symbols.GOAL;
import static nars.Symbols.JUDGMENT;


public abstract class AbstractTruth<T> implements MetaTruth<T> {


    /**
     * The confidence factor of the truth value
     */
    private float confidence;



    public AbstractTruth(final float conf) {
        super();
        setConfidence(conf);
    }

    public AbstractTruth() {
        //freq and conf will begin at 0
    }

    @Override
    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float c) {
        //if ((c > 1.0f) || (c < 0f)) throw new RuntimeException("Invalid confidence: " + c);
        final float maxConf = getConfidenceMax();

        final float e = getEpsilon();
        c = Math.round(c / e) * e;

        if (c > maxConf) c = maxConf;
        if (c < 0) c = 0;

        this.confidence = c;

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
        if (that instanceof AbstractTruth) {
            final Truth t = ((Truth) that);

            final float e = getEpsilon();

            if (!equalsValue(t))
                return false;
            if (!Truth.isEqual(getConfidence(), t.getConfidence(), e))
                return false;
            return true;
        }
        return false;
    }

    protected abstract boolean equalsValue(Truth t);


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
        return toCharSequence().toString();
    }

}
