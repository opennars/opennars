package nars.nal;

import nars.Global;
import nars.Symbols;
import org.apache.commons.math3.util.FastMath;

/**
 * Created by me on 5/19/15.
 */
public class DefaultTruth implements Truth {


    /** determines the internal precision used for TruthValue calculations.
     *  a value of 0.01 gives 100 truth value states between 0 and 1.0.
     *  other values may be used, for example, 0.02 for 50, 0.10 for 10, etc.
     *  Change at your own risk
     */
    //public static boolean TASK_LINK_UNIQUE_BY_INDEX = false;


    /**
     * The frequency factor of the truth value
     */
    private float frequency;
    /**
     * The confidence factor of the truth value
     */
    private float confidence;

    /**
     * Whether the truth value is derived from a definition
     */
    private boolean analytic = false;
    final float epsilon;


    public DefaultTruth() {
        this(0, 0);
    }


    /**
     * Constructor with two ShortFloats
     *
     * @param f The frequency value
     * @param c The confidence value
     */
    public DefaultTruth(final float f, final float c) {
        this(f, c, false);
    }

    public DefaultTruth(final float f, final float c, final boolean b, float epsilon) {
        this.epsilon = epsilon;
        setFrequency(f);
        setConfidence(c);
        setAnalytic(b);
    }

    /**
     * Constructor with two ShortFloats
     *
     * @param f The frequency value
     * @param c The confidence value
     */
    public DefaultTruth(final float f, final float c, final boolean b) {
        this(f, c, b, Global.DEFAULT_TRUTH_EPSILON);
    }

    public DefaultTruth(final float f, final float c, float epsilon) {
        this(f, c, false, epsilon);
    }

    /**
     * Constructor with a TruthValue to clone
     *
     * @param v The truth value to be cloned
     */
    public DefaultTruth(final Truth v) {
        this(v.getFrequency(), v.getConfidence(), v.getAnalytic(), Global.DEFAULT_TRUTH_EPSILON);
    }

    public DefaultTruth(final nars.nal.DefaultTruth v) {
        this(v.getFrequency(), v.getConfidence(), v.getAnalytic(), v.getEpsilon());
    }

    public DefaultTruth(char punctuation) {
        this.epsilon = Global.DEFAULT_TRUTH_EPSILON;
        float c;
        switch (punctuation) {
            case Symbols.JUDGMENT:
                c = Global.DEFAULT_JUDGMENT_CONFIDENCE;
                break;
            case Symbols.GOAL:
                c = Global.DEFAULT_GOAL_CONFIDENCE;
                break;
            default:
                throw new RuntimeException("Invalid punctuation " + punctuation + " for a TruthValue");
        }
        float f = 1;
        setFrequency(f);
        setConfidence(c);
    }

    public float getFrequency() {
        return frequency;
    }

    @Override
    public void setAnalytic() {
        this.analytic = true;
    }

    @Override
    public boolean getAnalytic() {
        return analytic;
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

    public float getEpsilon() {
        return epsilon;
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

    @Override
    public float getConfidenceMax() {
        return Global.MAX_CONFIDENCE;
    }

    @Override
    public Truth setAnalytic(final boolean a) {
        analytic = a;
        return this;
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
