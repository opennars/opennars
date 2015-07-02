package nars.nal.truth;

/**
 * A truth value which allows a mutable arbitrary epsilon,
 * with static make methods for creating new truth values that will delegate
 * to the lighter-weight DefaultTruth when the epsilon == DefaultTruth.DEFAULT_TRUTH_EPSILON
 */
public class BasicTruth extends AbstractTruth {

    public final float epsilon;

    public BasicTruth(final float freq, final float conf, final float epsilon) {
        super();
        this.epsilon = epsilon; //epsilon must be set first
        set(freq, conf);
    }

    BasicTruth(final Truth cloneFrom) {
        this(cloneFrom.getFrequency(), cloneFrom.getConfidence(), cloneFrom.getEpsilon() );
    }

    @Override
    public boolean isAnalytic() {
        return false;
    }

    @Override
    public float getEpsilon() {
        return epsilon;
    }

    /** creates a new truth using the maximum epsilon (least precision) of 2 parent truth's */
    public static AbstractTruth make(final float f, final float c, final Truth a, final Truth b) {
        return make(f, c, Math.max( a.getEpsilon(), b.getEpsilon() ));
    }

    /** use this instead of a constructor to automatically have DefaultTruth used when epsilon is default, saving the storage of a float */
    public static AbstractTruth make(final float f, final float c, final float epsilon) {
        if (epsilon == DefaultTruth.DEFAULT_TRUTH_EPSILON)
            return new DefaultTruth(f, c);
        else
            return new BasicTruth(f, c, epsilon);
    }

    public static Truth clone(final Truth truth) {
        return make(truth.getFrequency(), truth.getConfidence(), truth.getEpsilon());
    }
}
