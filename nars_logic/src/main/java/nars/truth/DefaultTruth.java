package nars.truth;

/**
 * Created by me on 5/19/15.
 */
public class DefaultTruth extends AbstractScalarTruth {

    public final static float DEFAULT_TRUTH_EPSILON = 0.01f;

    //public final float epsilon;

    public DefaultTruth(final float f, final float c) {
        super();
        set(f,c);

    }

//    public DefaultTruth(Truth v) {
//        super(v);
//    }

    public DefaultTruth(final char punctuation) {
        super();
        set(1f, getDefaultConfidence(punctuation));
    }

    /** 0, 0 default */
    public DefaultTruth() {
        super();
    }

    public DefaultTruth(AbstractScalarTruth toClone) {
        this(toClone.getFrequency(), toClone.getConfidence());
    }

    public DefaultTruth(Truth truth) {
        this(truth.getFrequency(), truth.getConfidence());
    }

    @Override
    public boolean isAnalytic() {
        return false;
    }

/*    public float getEpsilon() {
        return DEFAULT_TRUTH_EPSILON;
    }*/

//    /** truth with 0.01 resolution */
//    public static class DefaultTruth01 extends DefaultTruth {
//
//        public DefaultTruth01(float f, float c) {
//            super(f, c);
//        }
//    }
//
//
//
//    /** truth with 0.1 resolution */
//    public static class DefaultTruth1 extends AbstractDefaultTruth {
//
//        @Override
//        public float getEpsilon() {
//            return 0.1f;
//        }
//    }
//
//
//    /** truth with 0.001 resolution */
//    public static class DefaultTruth001 extends AbstractDefaultTruth {
//
//        @Override
//        public float getEpsilon() {
//            return 0.001f;
//        }
//    }
//
//
//    /** truth with 0.05 resolution */
//    public static class DefaultTruth05 extends AbstractDefaultTruth {
//
//        @Override
//        public float getEpsilon() {
//            return 0.05f;
//        }
//    }

}
