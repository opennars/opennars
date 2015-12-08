package nars.truth;

import nars.util.data.Util;


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
            return equalsConfidence(t) && equalsFrequency(t);
        }
        return false;
    }

    public final boolean equalsConfidence(final Truth t) {
        final float e = DefaultTruth.DEFAULT_TRUTH_EPSILON;//getEpsilon();
        return Util.equal(getConfidence(), t.getConfidence(), e);
    }

    public abstract boolean equalsFrequency(Truth t);



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
