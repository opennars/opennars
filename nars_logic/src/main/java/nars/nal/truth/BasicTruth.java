package nars.nal.truth;

/**
 * Created by me on 7/1/15.
 */
public class BasicTruth extends AbstractTruth {

    public final float epsilon;

    public BasicTruth(float freq, float conf, float epsilon) {
        super(freq, conf);
        this.epsilon = epsilon;
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

    /** create a new BasicTruth using the maximum epsilon of two parents */
    public static AbstractTruth make(float f, float c, Truth a, Truth b) {
        return make(f, c, Math.max( a.getEpsilon(), b.getEpsilon() ));
    }

    public static AbstractTruth make(float f, float c, float epsilon) {
        if (epsilon == DefaultTruth.DEFAULT_TRUTH_EPSILON)
            return new DefaultTruth(f, c);
        else
            return new BasicTruth(f, c, epsilon);
    }

    public static Truth clone(Truth truth) {
        return make(truth.getFrequency(), truth.getConfidence(), truth.getEpsilon());
    }
}
