package nars.truth;

/**
 * Created by me on 7/1/15.
 */
abstract public class AbstractDefaultTruth extends AbstractScalarTruth {

    /**
     * determines the internal precision used for TruthValue calculations.
     * a value of 0.01 gives 100 truth value states between 0 and 1.0.
     * other values may be used, for example, 0.02 for 50, 0.10 for 10, etc.
     * Change at your own risk
     */
    //public static boolean TASK_LINK_UNIQUE_BY_INDEX = false;
    public AbstractDefaultTruth() {
        this(0, 0);
    }


    /**
     * Constructor with two ShortFloats
     *
     * @param f The frequency value
     * @param c The confidence value
     */
    public AbstractDefaultTruth(final float f, final float c) {
        super(f, c);
    }


    /**
     * Constructor with a TruthValue to clone
     *
     * @param v The truth value to be cloned
     */
    public AbstractDefaultTruth(final Truth v) {
        this(v.getFrequency(), v.getConfidence());
    }



    public AbstractDefaultTruth(final char punctuation) {
        this(1f, getDefaultConfidence(punctuation));
    }


    @Override
    public boolean isAnalytic() {
        return false;
    }

}
