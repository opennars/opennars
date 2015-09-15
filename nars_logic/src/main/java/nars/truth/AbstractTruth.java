package nars.truth;

import nars.Global;
import nars.util.data.Util;

import static nars.Symbols.GOAL;
import static nars.Symbols.JUDGMENT;


public abstract class AbstractTruth<T> implements MetaTruth<T> {


    /**
     * The confidence factor of the truth value
     */
    protected float confidence;


    public AbstractTruth() {
        //freq and conf will begin at 0
    }

    @Override
    public float getConfidence() {
        return confidence;
    }





//    @Override
//    public float getConfidenceMax() {
//        return Global.MAX_CONFIDENCE;
//    }

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

            final float e = DefaultTruth.DEFAULT_TRUTH_EPSILON;//getEpsilon();

            if (!equalsValue(t))
                return false;
            return Util.isEqual(getConfidence(), t.getConfidence(), e);
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
